<%@ include file="/WEB-INF/jspf/init.jspf"%>

<%@ page import="fll.db.Queries"%>
<%@ page import="java.sql.Connection"%>
<%@ page import="fll.web.ApplicationAttributes"%>
<%@ page import="javax.sql.DataSource"%>

<fll-sw:required-roles roles="REF,JUDGE" allowSetup="false" />

<%
final DataSource datasource = ApplicationAttributes.getDataSource(application);
final Connection connection = datasource.getConnection();
pageContext.setAttribute("tournament", Queries.getCurrentTournament(connection));
%>

<html>
<head>
<link rel="stylesheet" type="text/css"
    href="<c:url value='/style/fll-sw.css'/>" />
<title>Team
    <c:out value="${param.TeamNumber}" /> Performance Scores
</title>

<style>
table#perf-data {
    border-collapse: collapse;
}

table#perf-data, table#perf-data th, table#perf-data td {
    border: 1px solid black;
}

table#perf-data th, table#perf-data td {
    padding-right: 5px;
    padding-left: 5px;
}
</style>
</head>

<body>
    <h1>
        Team
        <c:out value="${param.TeamNumber}" />
        Performance Scores
    </h1>
    <sql:query var="result" dataSource="${datasource}">
      SELECT RunNumber, ComputedTotal, NoShow, TIMESTAMP
        FROM Performance
        WHERE TeamNumber = <c:out value="${param.TeamNumber}" />
          AND Performance.Tournament = <c:out value="${tournament}" />
        ORDER By RunNumber
    </sql:query>
    <table id="perf-data">
        <tr>
            <th>Run Number</th>
            <th>Score</th>
            <th>Last Edited</th>
        </tr>
        <c:forEach items="${result.rows}" var="row">
            <tr>
                <td>${row.RunNumber}</td>
                <td>
                    <c:choose>
                        <c:when test="${row.NoShow == True}">
                        No Show
                    </c:when>
                        <c:otherwise>
                    ${row.ComputedTotal}
                    </c:otherwise>
                    </c:choose>
                </td>
                <td class="right">
                    <fmt:formatDate value="${row.TIMESTAMP}"
                        pattern="h:mm" />
                </td>
            </tr>
        </c:forEach>
    </table>

</body>
</html>
