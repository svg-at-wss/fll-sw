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
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fll.Utilities;
import fll.db.AdvancingTeam;
import fll.db.Queries;
import fll.util.FLLRuntimeException;
import fll.web.ApplicationAttributes;
import fll.web.AuthenticationContext;
import fll.web.SessionAttributes;

/**
 * Collection of advancing team numbers in the current tournament.
 * Type is {@link Collection} of {@link AdvancingTeam}.
 */
@WebServlet("/api/AdvancingTeams")
public class AdvancingTeamsServlet extends HttpServlet {

  private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger();

  @Override
  protected final void doGet(final HttpServletRequest request,
                             final HttpServletResponse response)
      throws IOException, ServletException {
    final ServletContext application = getServletContext();
    final HttpSession session = request.getSession();
    final AuthenticationContext auth = SessionAttributes.getAuthentication(session);

    if (!auth.isHeadJudge()) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    final DataSource datasource = ApplicationAttributes.getDataSource(application);
    try (Connection connection = datasource.getConnection()) {

      final ObjectMapper jsonMapper = Utilities.createJsonMapper();

      response.reset();
      response.setContentType("application/json");
      final PrintWriter writer = response.getWriter();

      final int tournamentId = Queries.getCurrentTournament(connection);

      final Collection<AdvancingTeam> advancing = AdvancingTeam.loadAdvancingTeams(connection, tournamentId);

      jsonMapper.writeValue(writer, advancing);
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected final void doPost(final HttpServletRequest request,
                              final HttpServletResponse response)
      throws IOException, ServletException {
    final HttpSession session = request.getSession();
    final AuthenticationContext auth = SessionAttributes.getAuthentication(session);

    if (!auth.isHeadJudge()) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    final ObjectMapper jsonMapper = Utilities.createJsonMapper();
    response.reset();
    response.setContentType("application/json");

    final ServletContext application = getServletContext();

    final StringWriter debugWriter = new StringWriter();
    request.getReader().transferTo(debugWriter);

    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Read data: "
          + debugWriter.toString());
    }

    final Reader reader = new StringReader(debugWriter.toString());
    final PrintWriter writer = response.getWriter();

    final DataSource datasource = ApplicationAttributes.getDataSource(application);
    try (Connection connection = datasource.getConnection()) {

      final int currentTournament = Queries.getCurrentTournament(connection);

      final Collection<AdvancingTeam> advancing = jsonMapper.readValue(reader,
                                                                       AdvancingTeam.AdvancingTeamCollectionTypeInformation.INSTANCE);

      AdvancingTeam.storeAdvancingTeams(connection, currentTournament, advancing);

      final PostResult result = new PostResult(true, Optional.empty());
      jsonMapper.writeValue(writer, result);

    } catch (final SQLException e) {
      final PostResult result = new PostResult(false, Optional.ofNullable(e.getMessage()));
      jsonMapper.writeValue(writer, result);

      throw new FLLRuntimeException(e);
    } catch (final JsonProcessingException e) {
      final PostResult result = new PostResult(false, Optional.ofNullable(e.getMessage()));
      jsonMapper.writeValue(writer, result);

      throw new FLLRuntimeException(e);
    }

  }
}
