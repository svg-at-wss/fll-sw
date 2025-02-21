<%@ include file="/WEB-INF/jspf/init.jspf"%>

<fll-sw:required-roles roles="ADMIN" allowSetup="false" />

<%
fll.web.schedule.ChooseScheduleHeaders.populateContext(application, pageContext);
%>

<html>
<head>
<title>Choose Headers (Upload Schedule)</title>
<link rel="stylesheet" type="text/css"
    href="<c:url value='/style/fll-sw.css'/>" />
</head>

<body>
    <h1>Choose Headers (Upload Schedule)</h1>

    <p>
        If the number of performance rounds or practice rounds does not
        match what is expected, <a
            href="<c:url value='/admin/edit_tournament_parameters.jsp'/>">edit
            the tournament parameters</a> and then upload the schedule
        again.
    </p>

    <div class='status-message'>${message}</div>
    <%-- clear out the message, so that we don't see it again --%>
    <c:remove var="message" />

    <form name="choose_headers" id="choose_headers" method='POST'
        action="<c:url value='/schedule/ChooseScheduleHeaders'/>">

        <p>Highlighted columns are required, all others are
            optional.</p>

        <table border='1'>

            <tr>
                <th>Needed information</th>
                <th>Data file column name</th>
            </tr>

            <tr bgcolor='yellow'>
                <td>Team Number</td>
                <td>
                    <select name='teamNumber'>
                        <c:forEach items="${spreadsheetHeaderNames}"
                            var="fileHeader">
                            <option value="${fileHeader}">${fileHeader}</option>
                        </c:forEach>
                    </select>
                </td>
            </tr>

            <tr>
                <td>Team Name</td>
                <td>
                    <select name='teamName'>
                        <option value='' selected>None</option>

                        <c:forEach items="${spreadsheetHeaderNames}"
                            var="fileHeader">
                            <option value="${fileHeader}">${fileHeader}</option>
                        </c:forEach>
                    </select>
                </td>
            </tr>

            <tr>
                <td>Organization</td>
                <td>
                    <select name='organization'>
                        <option value='' selected>None</option>

                        <c:forEach items="${spreadsheetHeaderNames}"
                            var="fileHeader">
                            <option value="${fileHeader}">${fileHeader}</option>
                        </c:forEach>
                    </select>
                </td>
            </tr>

            <tr>
                <td>Award Group</td>
                <td>
                    <select name='awardGroup'>
                        <option value='' selected>None</option>

                        <c:forEach items="${spreadsheetHeaderNames}"
                            var="fileHeader">
                            <option value="${fileHeader}">${fileHeader}</option>
                        </c:forEach>
                    </select>
                </td>
            </tr>

            <tr>
                <td>Judging Group</td>
                <td>
                    <select name='judgingGroup'>
                        <option value='' selected>None</option>

                        <c:forEach items="${spreadsheetHeaderNames}"
                            var="fileHeader">
                            <option value="${fileHeader}">${fileHeader}</option>
                        </c:forEach>
                    </select>
                </td>
            </tr>

            <c:forEach begin="1" end="${numPracticeRounds}"
                varStatus="practiceLoopStatus">
                <tr>
                    <td bgcolor='yellow'>Practice
                        ${practiceLoopStatus.index}</td>
                    <td>
                        <select
                            name='practice${practiceLoopStatus.index}'>
                            <c:forEach items="${spreadsheetHeaderNames}"
                                var="fileHeader">
                                <option value="${fileHeader}">${fileHeader}</option>
                            </c:forEach>
                        </select>
                    </td>
                </tr>
                <td bgcolor='yellow'>Practice
                    ${practiceLoopStatus.index} table</td>
                <td>
                    <select
                        name='practiceTable${practiceLoopStatus.index}'>
                        <c:forEach items="${spreadsheetHeaderNames}"
                            var="fileHeader">
                            <option value="${fileHeader}">${fileHeader}</option>
                        </c:forEach>
                    </select>
                </td>
                </tr>

            </c:forEach>

            <c:forEach begin="1" end="${numSeedingRounds}"
                varStatus="perfLoopStatus">
                <tr>
                    <td bgcolor='yellow'>Performance
                        ${perfLoopStatus.index}</td>
                    <td>
                        <select name='perf${perfLoopStatus.index}'>
                            <c:forEach items="${spreadsheetHeaderNames}"
                                var="fileHeader">
                                <option value="${fileHeader}">${fileHeader}</option>
                            </c:forEach>
                        </select>
                    </td>
                </tr>
                <td bgcolor='yellow'>Performance
                    ${perfLoopStatus.index} table</td>
                <td>
                    <select name='perfTable${perfLoopStatus.index}'>
                        <c:forEach items="${spreadsheetHeaderNames}"
                            var="fileHeader">
                            <option value="${fileHeader}">${fileHeader}</option>
                        </c:forEach>
                    </select>
                </td>
                </tr>

            </c:forEach>

        </table>


        <p>Match the column names from the schedule data file with
            the subjective categories that they contain the schedule
            for.</p>

        <table border='1'>
            <tr>
                <th>Subjective Category</th>
                <th>Data file column name</th>
            </tr>
            <c:forEach
                items="${challengeDescription.subjectiveCategories}"
                var="subcat">
                <tr>

                    <td bgcolor='yellow'>${subcat.title}</td>

                    <td>
                        <select name='${subcat.name}:header'>

                            <c:forEach items="${spreadsheetHeaderNames}"
                                var="subjHeader">
                                <option value='${subjHeader}'>${subjHeader}</option>
                            </c:forEach>
                            <!-- foreach data file header -->

                        </select>
                    </td>

                </tr>
                <!-- row for category -->

            </c:forEach>
            <!--  foreach category -->

        </table>

        <input type="submit" id='submit_data' value="Submit" />
    </form>

</body>
</html>