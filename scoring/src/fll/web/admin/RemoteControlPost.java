/*
 * Copyright (c) 2010 INSciTE.  All rights reserved
 * INSciTE is on the web at: http://www.hightechkids.org
 * This code is released under GPL; see LICENSE.txt for details.
 */

package fll.web.admin;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.icepush.PushContext;

import fll.web.BaseFLLServlet;
import fll.web.DisplayInfo;
import fll.web.SessionAttributes;

@WebServlet("/admin/RemoteControlPost")
public class RemoteControlPost extends BaseFLLServlet {

  private static final Logger LOGGER = Logger.getLogger(RemoteControlPost.class.getName());

  @Override
  protected void processRequest(final HttpServletRequest request,
                                final HttpServletResponse response,
                                final ServletContext application,
                                final HttpSession session)
      throws IOException, ServletException {
    final Collection<DisplayInfo> displays = DisplayInfo.getDisplayInformation(application);

    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("numBrackets "
          + request.getParameter("numBrackets"));
      for (int i = 0; i < Integer.parseInt(request.getParameter("numBrackets")); ++i) {
        LOGGER.trace("playoffDivision "
            + i + ": " + request.getParameter("playoffDivision_"
                + i));
        LOGGER.trace("playoffRoundNumber "
            + i + ": " + request.getParameter("playoffRoundNumber_"
                + i));
      }
      LOGGER.trace("finalistDivision "
          + request.getParameter("finalistDivision"));

      for (final DisplayInfo display : displays) {
        final String displayName = display.getName();

        LOGGER.trace("display "
            + displayName);
        LOGGER.trace("\tremotePage "
            + request.getParameter(display.getRemotePageFormParamName()));
        LOGGER.trace("\tremoteURL "
            + request.getParameter(display.getRemoteUrlFormParamName()));

        for (int i = 0; i < Integer.parseInt(request.getParameter(display.getHead2HeadNumBracketsFormParamName())); ++i) {
          LOGGER.trace("\tplayoffDivision "
              + i + ": " + request.getParameter(displayName
                  + "_playoffDivision_" + i));
          LOGGER.trace("\tplayoffRoundNumber "
              + i + ": " + request.getParameter(displayName
                  + "_playoffRoundNumber_" + i));
        }
        LOGGER.trace("\tdelete? "
            + request.getParameter(display.getDeleteFormParamName()));

        LOGGER.trace("\tfinalistDivision "
            + request.getParameter(display.getFinalistScheduleAwardGroupFormParamName()));

      } // foreach display
    } // if trace enabled

    final String slideIntervalStr = request.getParameter("slideInterval");
    if (null != slideIntervalStr) {
      application.setAttribute("slideShowInterval", Integer.valueOf(slideIntervalStr));
    }

    final List<DisplayInfo> toDelete = new LinkedList<>();
    for (final DisplayInfo display : displays) {
      if (null != request.getParameter(display.getDeleteFormParamName())) {
        toDelete.add(display);
      } else {
        if (DisplayInfo.DEFAULT_DISPLAY_NAME.equals(request.getParameter(display.getRemotePageFormParamName()))) {
          display.setFollowDefault();
        } else {
          display.setRemotePage(request.getParameter(display.getRemotePageFormParamName()));
        }

        display.setSpecialUrl(request.getParameter(display.getSpecialUrlFormParamName()));

        display.setFinalistScheduleAwardGroup(request.getParameter(display.getFinalistScheduleAwardGroupFormParamName()));

        final List<DisplayInfo.H2HBracketDisplay> brackets = new LinkedList<>();
        final int numBrackets = Integer.parseInt(request.getParameter(display.getHead2HeadNumBracketsFormParamName()));
        for (int bracketIdx = 0; bracketIdx < numBrackets; ++bracketIdx) {
          final String bracket = request.getParameter(display.getHead2HeadBracketFormParamName(bracketIdx));

          final String firstRoundStr = request.getParameter(display.getHead2HeadFirstRoundFormParamName(bracketIdx));
          final int firstRound;
          if (null == firstRoundStr) {
            // there are no head to head rounds yet, just use 1
            firstRound = 1;
          } else {
            firstRound = Integer.parseInt(firstRoundStr);
          }

          final DisplayInfo.H2HBracketDisplay bracketInfo = new DisplayInfo.H2HBracketDisplay(display, bracketIdx,
                                                                                              bracket, firstRound);
          brackets.add(bracketInfo);
        }
        display.setBrackets(brackets);
      }
    }

    for (final DisplayInfo display : toDelete) {
      DisplayInfo.deleteDisplay(application, display);
    }

    PushContext pc = PushContext.getInstance(application);
    pc.push("playoffs");
    pc.push("display");
    session.setAttribute(SessionAttributes.MESSAGE, "<i id='success'>Successfully set remote control parameters</i>");

    response.sendRedirect(response.encodeRedirectURL("remoteControl.jsp"));

  }

}
