<%@ include file="/WEB-INF/jspf/init.jspf" %>

<%@ page import="fll.Utilities" %>
<%@ page import="fll.Queries" %>
  
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>

<%@ page import="java.sql.Connection" %>
  
<%
final Connection connection = (Connection)application.getAttribute("connection");
      
final Map tournamentTeams = Queries.getTournamentTeams(connection);
final String currentTournament = Queries.getCurrentTournament(connection);
final List divisions = Queries.getDivisions(connection);
final int numSeedingRounds = Queries.getNumSeedingRounds(connection);
  
if(null != request.getParameter("division")) {
  application.setAttribute("playoffDivision", request.getParameter("division"));
}
if(null != request.getParameter("runNumber")) {
  application.setAttribute("playoffRunNumber", Utilities.NUMBER_FORMAT_INSTANCE.parse(request.getParameter("runNumber")));
}
  
if(null == application.getAttribute("playoffDivision") && !divisions.isEmpty()) {
  application.setAttribute("playoffDivision", divisions.get(0));
}
if(null == application.getAttribute("playoffRunNumber")) {
  application.setAttribute("playoffRunNumber", new Integer(numSeedingRounds + 1));
}

final String playoffDivision = (String)application.getAttribute("playoffDivision");
final int playoffRunNumber = ((Number)application.getAttribute("playoffRunNumber")).intValue();
%>
  
<html>
  <head>
    <link rel="stylesheet" type="text/css" href="<c:url value='/style/style.jsp'/>" />
    <title><x:out select="$challengeDocument/fll/@title"/> (Playoff's)</title>
  </head>

  <body>
    <h1><x:out select="$challengeDocument/fll/@title"/> (Playoff menu)</h1>
      <ol>
        <li>First you should check to make sure all of the teams have the
        correct number of rounds.  You can use <a href="check.jsp">this
        page</a> to check that.  If any teams show up on this page you'll want
        to try and fix that first.</li>

        <li>
          <B>WARNING: Do not select brackets until all seeding runs for that division have been recorded!</b><br>
          <form action='adminbrackets.jsp' method='get'>
            Go to the admin/printable bracket page for division <select name='division'>
<%
{
final Iterator divisionIter = divisions.iterator();
while(divisionIter.hasNext()) {
  final String div = (String)divisionIter.next();
%>
<option value='<%=div%>'><%=div%></option>
<%
}
}
%>
            </select>
            <input type='submit' value='Go to Playoffs'>
          </form>               
        </li>

        <li>
          <B>WARNING: Do not select brackets until all seeding runs have been recorded!</b><br>
          <a href="remoteMain.jsp">Go to remotely controlled brackets</a>
          These brackets can be controlled from the admin page.
        </li>
      </ol>
    </p>



<%@ include file="/WEB-INF/jspf/footer.jspf" %>
  </body>
</html>
