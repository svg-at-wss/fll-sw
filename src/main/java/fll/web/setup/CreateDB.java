/*
 * Copyright (c) 2000-2002 INSciTE.  All rights reserved
 * INSciTE is on the web at: http://www.hightechkids.org
 * This code is released under GPL; see LICENSE.txt for details.
 */
package fll.web.setup;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.zip.ZipInputStream;

import javax.sql.DataSource;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileUploadException;

import static org.checkerframework.checker.nullness.util.NullnessUtil.castNonNull;

import fll.Utilities;
import fll.db.Authentication;
import fll.db.GenerateDB;
import fll.db.ImportDB;
import fll.util.FLLInternalException;
import fll.web.ApplicationAttributes;
import fll.web.AuthenticationContext;
import fll.web.BaseFLLServlet;
import fll.web.MissingRequiredParameterException;
import fll.web.SessionAttributes;
import fll.web.UploadProcessor;
import fll.web.UserRole;
import fll.xml.ChallengeDescription;
import fll.xml.ChallengeParser;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Create a new database either from an xml descriptor or from a database dump.
 *
 * @author jpschewe
 */
@WebServlet("/setup/CreateDB")
public class CreateDB extends BaseFLLServlet {

  private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger();

  /**
   * Session key for the previous accounts. Collection of {@link UserAccount}.
   */
  public static final String PREVIOUS_ACCOUNTS = "previousAccounts";

  @Override
  protected void processRequest(final HttpServletRequest request,
                                final HttpServletResponse response,
                                final ServletContext application,
                                final HttpSession session)
      throws IOException, ServletException {
    final AuthenticationContext auth = SessionAttributes.getAuthentication(session);

    if (!auth.requireRoles(request, response, session, Set.of(UserRole.ADMIN), true)) {
      return;
    }

    String redirect;
    final StringBuilder message = new StringBuilder();
    final DataSource datasource = ApplicationAttributes.getDataSource(application);
    try (Connection connection = datasource.getConnection()) {

      final Collection<UserAccount> accounts = new LinkedList<>();
      if (Utilities.testDatabaseInitialized(connection)) {
        for (final String username : Authentication.getUsers(connection)) {
          // password cannot be null for users that are known to be in the database
          final String hashedPassword = castNonNull(Authentication.getHashedPassword(connection, username));
          final Set<UserRole> roles = Authentication.getRoles(connection, username);

          final UserAccount account = new UserAccount(username, hashedPassword, roles);
          accounts.add(account);
        }
      }

      // must be first to ensure the form parameters are set
      UploadProcessor.processUpload(request);

      boolean success = false;
      if (null != request.getAttribute("chooseDescription")) {
        final String description = (String) request.getAttribute("description");
        if (null == description) {
          throw new MissingRequiredParameterException("description");
        }

        try {
          final URL descriptionURL = new URL(description);
          final ChallengeDescription challengeDescription = ChallengeParser.parse(new InputStreamReader(descriptionURL.openStream(),
                                                                                                        Utilities.DEFAULT_CHARSET));

          GenerateDB.generateDB(challengeDescription, connection);

          application.removeAttribute(ApplicationAttributes.CHALLENGE_DESCRIPTION);

          success = true;
        } catch (final MalformedURLException e) {
          throw new FLLInternalException("Could not parse URL from choosen description: "
              + description, e);
        }
      } else if (null != request.getAttribute("reinitializeDatabase")) {
        // create a new empty database from an XML descriptor
        final FileItem xmlFileItem = (FileItem) request.getAttribute("xmldocument");

        if (null == xmlFileItem
            || xmlFileItem.getSize() < 1) {
          message.append("<p class='error'>XML description document not specified</p>");
        } else {
          final ChallengeDescription challengeDescription = ChallengeParser.parse(new InputStreamReader(xmlFileItem.getInputStream(),
                                                                                                        Utilities.DEFAULT_CHARSET));

          GenerateDB.generateDB(challengeDescription, connection);

          application.removeAttribute(ApplicationAttributes.CHALLENGE_DESCRIPTION);

          success = true;
        }
      } else if (null != request.getAttribute("createdb")) {
        // import a database from a dump
        final FileItem dumpFileItem = (FileItem) request.getAttribute("dbdump");

        if (null == dumpFileItem
            || dumpFileItem.getSize() < 1) {
          message.append("<p class='error'>Database dump not specified</p>");
        } else {

          final ImportDB.ImportResult importResult = ImportDB.loadFromDumpIntoNewDB(new ZipInputStream(dumpFileItem.getInputStream()),
                                                                                    connection);
          if (importResult.hasBugs()) {
            message.append("<p id='bugs_found' class='warning'>Bug reports found in the import.</p>");
          }
          if (Files.exists(importResult.getImportDirectory())) {
            message.append(String.format("<p id='import_logs_dir'>See %s for bug reports and logs.</p>",
                                         importResult.getImportDirectory()));
          }

          // remove application variables that depend on the database
          application.removeAttribute(ApplicationAttributes.CHALLENGE_DESCRIPTION);

          final Collection<String> newDbUsers = Authentication.getUsers(connection);
          final Iterator<UserAccount> accountIter = accounts.iterator();
          while (accountIter.hasNext()) {
            final UserAccount account = accountIter.next();
            if (newDbUsers.contains(account.getUsername())) {
              // don't give the option to overwrite users
              accountIter.remove();
            }
          }

          success = true;
        }

      } else {
        message.append("<p class='error'>Unknown form state, expected form fields not seen: "
            + request
            + "</p>");
      }

      if (success) {
        message.append("<p id='success'><i>Successfully initialized database</i></p>");

        // setup special authentication for setup
        session.setAttribute(SessionAttributes.AUTHENTICATION, AuthenticationContext.inSetup());

        if (accounts.isEmpty()) {
          if (Authentication.getAdminUsers(connection).isEmpty()) {
            redirect = "/admin/createUsername.jsp?ADMIN";
          } else {
            redirect = "/admin/ask-create-admin.jsp";
          }
        } else {
          session.setAttribute(PREVIOUS_ACCOUNTS, accounts);
          redirect = "/setup/import-users.jsp";
        }
      } else {
        redirect = "/setup";
      }

    } catch (final FileUploadException fue) {
      message.append("<p class='error'>Error handling the file upload: "
          + fue.getMessage()
          + "</p>");
      LOGGER.error(fue, fue);
      redirect = "/setup";
    } catch (final IOException ioe) {
      message.append("<p class='error'>Error reading uploaded database: "
          + ioe.getMessage()
          + "</p>");
      LOGGER.error(ioe, ioe);
      redirect = "/setup";
    } catch (final SQLException sqle) {
      message.append("<p class='error'>Error loading data into the database: "
          + sqle.getMessage()
          + "</p>");
      LOGGER.error(sqle, sqle);
      redirect = "/setup";
    }

    SessionAttributes.appendToMessage(session, message.toString());
    response.sendRedirect(response.encodeRedirectURL(request.getContextPath()
        + redirect));

  }

  /**
   * Store user account information for inserting into a new database.
   */
  public static final class UserAccount implements Serializable {

    private final String username;

    /**
     * @return username
     */
    public String getUsername() {
      return username;
    }

    private final String hashedPassword;

    /**
     * @return hashed password
     */
    public String getHashedPassword() {
      return hashedPassword;
    }

    private final HashSet<UserRole> roles;

    /**
     * @return unmodifiable set of roles
     */
    public Set<UserRole> getRoles() {
      return Collections.unmodifiableSet(roles);
    }

    /**
     * @param username {@link #getUsername()}
     * @param hashedPassword {@link #getHashedPassword()}
     * @param roles {@link #getRoles()}
     */
    /* package */ UserAccount(final String username,
                              final String hashedPassword,
                              final Set<UserRole> roles) {
      this.username = username;
      this.hashedPassword = hashedPassword;
      this.roles = new HashSet<>(roles);
    }
  }

}
