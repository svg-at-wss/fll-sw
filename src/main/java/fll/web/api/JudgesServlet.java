/*
 * Copyright (c) 2013 High Tech Kids.  All rights reserved
 * HighTechKids is on the web at: http://www.hightechkids.org
 * This code is released under GPL; see LICENSE.txt for details.
 */

package fll.web.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import javax.sql.DataSource;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import fll.JudgeInformation;
import fll.Utilities;
import fll.db.Queries;
import fll.web.ApplicationAttributes;
import fll.web.AuthenticationContext;
import fll.web.SessionAttributes;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * API access to judges.
 * GET: {judges}
 * POST: expects the data from GET and returns UploadResult
 */
@WebServlet("/api/Judges/*")
public class JudgesServlet extends HttpServlet {

  private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger();

  @Override
  protected final void doGet(final HttpServletRequest request,
                             final HttpServletResponse response)
      throws IOException, ServletException {
    final HttpSession session = request.getSession();
    final AuthenticationContext auth = SessionAttributes.getAuthentication(session);

    if (!auth.isJudge()) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    final ServletContext application = getServletContext();

    final DataSource datasource = ApplicationAttributes.getDataSource(application);
    try (Connection connection = datasource.getConnection()) {

      final ObjectMapper jsonMapper = Utilities.createJsonMapper();

      response.reset();
      response.setContentType("application/json");
      final PrintWriter writer = response.getWriter();

      final int currentTournament = Queries.getCurrentTournament(connection);
      final Collection<JudgeInformation> judges = JudgeInformation.getJudges(connection, currentTournament);

      jsonMapper.writeValue(writer, judges);
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }

  }

  @SuppressFBWarnings(value = { "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING" }, justification = "Dynamic table based upon categories")
  @Override
  protected final void doPost(final HttpServletRequest request,
                              final HttpServletResponse response)
      throws IOException, ServletException {
    final HttpSession session = request.getSession();
    final AuthenticationContext auth = SessionAttributes.getAuthentication(session);

    if (!auth.isJudge()) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    final ObjectMapper jsonMapper = Utilities.createJsonMapper();
    response.reset();
    response.setContentType("application/json");
    final PrintWriter writer = response.getWriter();

    final ServletContext application = getServletContext();

    final DataSource datasource = ApplicationAttributes.getDataSource(application);
    try (Connection connection = datasource.getConnection()) {

      final int currentTournament = Queries.getCurrentTournament(connection);

      final StringWriter debugWriter = new StringWriter();
      request.getReader().transferTo(debugWriter);

      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Read data: "
            + debugWriter.toString());
      }

      final Reader reader = new StringReader(debugWriter.toString());

      final Collection<JudgeInformation> judges = jsonMapper.readValue(reader, JudgesTypeInformation.INSTANCE);

      final int numNewJudges = processJudges(connection, currentTournament, judges);

      final UploadResult result = new UploadResult(true, "Successfully uploaded judges", numNewJudges);
      response.reset();
      jsonMapper.writeValue(writer, result);

    } catch (final SQLException e) {
      LOGGER.error("Error uploading judges", e);

      final UploadResult result = new UploadResult(false, e.getMessage(), -1);
      jsonMapper.writeValue(writer, result);
    }
  }

  /**
   * Process uploaded judges. Add judges to the database that haven't been seen
   * yet.
   * 
   * @param connection database connection
   * @param currentTournament id for the tournament the judges are for
   * @param judges the judges
   * @return the number of new judges seen
   * @throws SQLException on a database error
   */
  public static int processJudges(final Connection connection,
                                  final int currentTournament,
                                  final Collection<JudgeInformation> judges)
      throws SQLException {
    int numNewJudges = 0;

    final Collection<JudgeInformation> currentJudges = JudgeInformation.getJudges(connection, currentTournament);
    LOGGER.trace("Current judges: {}", currentJudges);

    try (
        PreparedStatement insertJudge = connection.prepareStatement("INSERT INTO Judges (id, category, Tournament, station) VALUES (?, ?, ?, ?)")) {
      insertJudge.setInt(3, currentTournament);

      for (final JudgeInformation judge : judges) {
        if (null != judge) {
          if (!currentJudges.contains(judge)) {
            LOGGER.trace("Adding judge: {}", judge.getId());

            insertJudge.setString(1, judge.getId());
            insertJudge.setString(2, judge.getCategory());
            insertJudge.setString(4, judge.getGroup());
            insertJudge.executeUpdate();
            ++numNewJudges;
          } // add judge
        } // non-null judge
      } // foreach judge sent
    } // prepared statement

    return numNewJudges;
  }

  /**
   * The result of an upload.
   */
  public static final class UploadResult {
    /**
     * @param success {@link #getSuccess()}
     * @param message {@link #getMessage()}
     * @param numNewJudges {@link #getNumNewJudges()}
     */
    public UploadResult(final boolean success,
                        final @Nullable String message,
                        final int numNewJudges) {
      mSuccess = success;
      mMessage = message;
      mNumNewJudges = numNewJudges;
    }

    private final boolean mSuccess;

    /**
     * @return if the upload was successful
     */
    public boolean getSuccess() {
      return mSuccess;
    }

    private final @Nullable String mMessage;

    /**
     * @return message for the user
     */
    public @Nullable String getMessage() {
      return mMessage;
    }

    private final int mNumNewJudges;

    /**
     * @return number of new judges
     */
    public int getNumNewJudges() {
      return mNumNewJudges;
    }

  }

  private static final class JudgesTypeInformation extends TypeReference<Collection<JudgeInformation>> {
    public static final JudgesTypeInformation INSTANCE = new JudgesTypeInformation();
  }

}
