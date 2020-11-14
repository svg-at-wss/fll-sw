/*
 * Copyright (c) 2000-2002 INSciTE.  All rights reserved
 * INSciTE is on the web at: http://www.hightechkids.org
 * This code is released under GPL; see LICENSE.txt for details.
 */
package fll.web.setup;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import fll.Utilities;
import fll.db.Authentication;
import fll.db.GenerateDB;
import fll.db.ImportDB;
import fll.util.FLLInternalException;
import fll.web.ApplicationAttributes;
import fll.web.BaseFLLServlet;
import fll.web.UploadProcessor;
import fll.xml.ChallengeDescription;
import fll.xml.ChallengeParser;

/**
 * Create a new database either from an xml descriptor or from a database dump.
 *
 * @author jpschewe
 */
@WebServlet("/setup/CreateDB")
public class CreateDB extends BaseFLLServlet {

  private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger();

  @Override
  protected void processRequest(final HttpServletRequest request,
                                final HttpServletResponse response,
                                final ServletContext application,
                                final HttpSession session)
      throws IOException, ServletException {
    String redirect;
    final StringBuilder message = new StringBuilder();
    final DataSource datasource = ApplicationAttributes.getDataSource(application);
    try (Connection connection = datasource.getConnection()) {

      // must be first to ensure the form parameters are set
      UploadProcessor.processUpload(request);

      if (null != request.getAttribute("chooseDescription")) {
        final String description = (String) request.getAttribute("description");
        try {
          final URL descriptionURL = new URL(description);
          final ChallengeDescription challengeDescription = ChallengeParser.parse(new InputStreamReader(descriptionURL.openStream(),
                                                                                                        Utilities.DEFAULT_CHARSET));

          GenerateDB.generateDB(challengeDescription, connection);

          application.removeAttribute(ApplicationAttributes.CHALLENGE_DESCRIPTION);

          message.append("<p id='success'><i>Successfully initialized database</i></p>");
          if (Authentication.getAdminUsers(connection).isEmpty()) {
            redirect = "/admin/createUsername.jsp?ADMIN";
          } else {
            redirect = "/admin/ask-create-admin.jsp";
          }

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
          redirect = "/setup";
        } else {
          final ChallengeDescription challengeDescription = ChallengeParser.parse(new InputStreamReader(xmlFileItem.getInputStream(),
                                                                                                        Utilities.DEFAULT_CHARSET));

          GenerateDB.generateDB(challengeDescription, connection);

          application.removeAttribute(ApplicationAttributes.CHALLENGE_DESCRIPTION);

          message.append("<p id='success'><i>Successfully initialized database</i></p>");
          if (Authentication.getAdminUsers(connection).isEmpty()) {
            redirect = "/admin/createUsername.jsp?ADMIN";
          } else {
            redirect = "/admin/ask-create-admin.jsp";
          }
        }
      } else if (null != request.getAttribute("createdb")) {
        // import a database from a dump
        final FileItem dumpFileItem = (FileItem) request.getAttribute("dbdump");

        if (null == dumpFileItem
            || dumpFileItem.getSize() < 1) {
          message.append("<p class='error'>Database dump not specified</p>");
          redirect = "/setup";
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

          message.append("<p id='success'><i>Successfully initialized database from dump</i></p>");
          if (Authentication.getAdminUsers(connection).isEmpty()) {
            redirect = "/admin/createUsername.jsp?ADMIN";
          } else {
            redirect = "/admin/ask-create-admin.jsp";
          }
        }

      } else {
        message.append("<p class='error'>Unknown form state, expected form fields not seen: "
            + request
            + "</p>");
        redirect = "/setup";
      }

    } catch (final FileUploadException fue) {
      message.append("<p class='error'>Error handling the file upload: "
          + fue.getMessage()
          + "</p>");
      LOG.error(fue, fue);
      redirect = "/setup";
    } catch (final IOException ioe) {
      message.append("<p class='error'>Error reading uploaded database: "
          + ioe.getMessage()
          + "</p>");
      LOG.error(ioe, ioe);
      redirect = "/setup";
    } catch (final SQLException sqle) {
      message.append("<p class='error'>Error loading data into the database: "
          + sqle.getMessage()
          + "</p>");
      LOG.error(sqle, sqle);
      redirect = "/setup";
    }

    session.setAttribute("message", message.toString());
    response.sendRedirect(response.encodeRedirectURL(request.getContextPath()
        + redirect));

  }

}
