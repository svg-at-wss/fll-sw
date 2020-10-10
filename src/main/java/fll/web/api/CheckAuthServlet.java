/*
 * Copyright (c) 2014 High Tech Kids.  All rights reserved
 * HighTechKids is on the web at: http://www.hightechkids.org
 * This code is released under GPL; see LICENSE.txt for details.
 */
package fll.web.api;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import fll.Utilities;
import fll.web.WebUtils;

/**
 * Check if the user is logged in.
 * GET: AuthResult
 */
@WebServlet("/api/CheckAuth")
public class CheckAuthServlet extends HttpServlet {

  @Override
  protected final void doGet(final HttpServletRequest request,
                             final HttpServletResponse response)
      throws IOException, ServletException {
    final ServletContext application = getServletContext();

    final boolean authenticated = WebUtils.checkAuthenticated(request, application);
    final AuthResult result = new AuthResult(authenticated);

    final ObjectMapper jsonMapper = Utilities.createJsonMapper();

    response.reset();
    response.setContentType("application/json");
    final PrintWriter writer = response.getWriter();

    jsonMapper.writeValue(writer, result);

  }

  /**
   * Result of checking authentication.
   */
  public static class AuthResult {
    /**
     * @param authenticated {@link #getAuthenticated()}
     */
    public AuthResult(final boolean authenticated) {
      mAuthenticated = authenticated;
    }

    private final boolean mAuthenticated;

    /**
     * @return if the user is authenticated
     */
    public boolean getAuthenticated() {
      return mAuthenticated;
    }
  }

}
