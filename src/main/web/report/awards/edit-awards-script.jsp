<%@ include file="/WEB-INF/jspf/init.jspf"%>

<fll-sw:required-roles roles="ADMIN" allowSetup="false" />

<%
fll.web.report.awards.EditAwardsScript.populateContext(request, application, pageContext);
%>

<!DOCTYPE HTML>
<html>
<head>
<title>Edit Awards Script</title>
<link rel="stylesheet" type="text/css"
    href="<c:url value='/style/fll-sw.css'/>" />
<link rel="stylesheet" type="text/css" href="edit-awards-script.css" />

<script type="text/javascript"
    src="<c:url value='/js/fll-functions.js'/>"></script>

<script type="text/javascript" src="edit-awards-script.js"></script>

<script type="text/javascript">
awardsScriptModule.init = ( ) => {

  const macros = [];  
  <c:forEach items="${macros}" var="macro">
  const macro_${macro.text} = awardsScriptModule.createMacro("${macro.text}", "${macro.title}");
  macros.push(macro_${macro.text});

  awardsScriptModule.configureParameterEntry("${macro.text}", ${macroSpecified[macro]}, "${macroValue[macro]}");
  </c:forEach>

  awardsScriptModule.configureParameterEntry("num_performance_awards", ${numPerformanceAwardsSpecified}, ${numPerformanceAwardsValue});


  <c:forEach items="${sections}" var="section"> 
  awardsScriptModule.configureTextEntry(macros, "${section.identifier}", ${sectionSpecified[section]});
  </c:forEach>
  
  <c:forEach items="${subjectiveCategories}" var="category">
  awardsScriptModule.configureTextEntry(macros, "category_${category.name}", ${subjectiveCategorySpecified[category]});
  
  awardsScriptModule.configurePresenterEntry("category_${category.name}", ${subjectiveCategoryPresenterSpecified[category]});
  </c:forEach>
  
  <c:forEach items="${nonNumericCategories}" var="category">
  awardsScriptModule.configureTextEntry(macros, "category_${category.title}", ${nonNumericCategorySpecified[category]});
  
  awardsScriptModule.configurePresenterEntry("category_${category.title}", ${nonNumericCategoryPresenterSpecified[category]});  
  </c:forEach>
  
  awardsScriptModule.setSponsorsSpecified(${sponsorsSpecified});
  <c:forEach items="${sponsors}" var="sponsor">
  awardsScriptModule.addSponsor("${sponsor}");
  </c:forEach>
  
  awardsScriptModule.setAwardOrderSpecified(${awardOrderSpecified});
  <c:forEach items="${awardOrder}" var="category">
  awardsScriptModule.addToAwardOrder("${category.title}");
  </c:forEach>


};
</script>
<body>

    <form action="EditAwardsScript" method="POST">

        <input type="hidden" name="level" value="${tournamentLevel.id}" />
        <input type="hidden" name="tournament"
            value="${tournament.tournamentID}" />

        <p>Editing the awards script for the ${descriptionText}.</p>

        <p>The text sections have macros available that can be
            inserted into the text. These will be replaced with the
            appropriate value when the report is generated. Put the
            cursor where you want to insert the macro and then click the
            appropriate macro button.</p>

        <hr />

        <h1>Parameter Values</h1>
        <div>Some parameters need to be specifed as they do not
            exist elsewhere in the database. These values can be
            populated at any layer of the script.</div>

        <h2>Number of Performance Awards per Award Group</h2>
        <div>
            <label>
                Specify value:
                <input type="checkbox"
                    name="num_performance_awards_specified"
                    id="num_performance_awards_specified" />
            </label>
            <input type="number" name="num_performance_awards_value"
                id="num_performance_awards_value" />
        </div>


        <h2>${NUM_TRAINED_OFFICIALS.title}</h2>
        <div>
            <label>
                Specify value:
                <input type="checkbox"
                    name="${NUM_TRAINED_OFFICIALS.text}_specified"
                    id="${NUM_TRAINED_OFFICIALS.text}_specified" />
            </label>
            <input type="number"
                name="${NUM_TRAINED_OFFICIALS.text}_value"
                id="${NUM_TRAINED_OFFICIALS.text}_value" />
        </div>

        <h2>${HOST_SCHOOL.title}</h2>
        <div>
            <label>
                Specify value:
                <input type="checkbox"
                    name="${HOST_SCHOOL.text}_specified"
                    id="${HOST_SCHOOL.text}_specified" />
            </label>
            <input type="text" name="${HOST_SCHOOL.text}_value"
                id="${HOST_SCHOOL.text}_value" />
        </div>

        <h2>${TOURNAMENT_DIRECTORS.title}</h2>
        <div>
            <label>
                Specify value:
                <input type="checkbox"
                    name="${TOURNAMENT_DIRECTORS.text}_specified"
                    id="${TOURNAMENT_DIRECTORS.text}_specified" />
            </label>
            <input type="text" name="${TOURNAMENT_DIRECTORS.text}_value"
                id="${TOURNAMENT_DIRECTORS.text}_value" />
        </div>

        <h2>${NUM_TEAMS_ADVANCING.title}</h2>
        <div>
            <label>
                Specify value:
                <input type="checkbox"
                    name="${NUM_TEAMS_ADVANCING.text}_specified"
                    id="${NUM_TEAMS_ADVANCING.text}_specified" />
            </label>
            <input type="number"
                name="${NUM_TEAMS_ADVANCING.text}_value"
                id="${NUM_TEAMS_ADVANCING.text}_value" />
        </div>


        <hr />
        <h1>Awards script sections</h1>

        <div>Tournament Location - Tournament Date</div>
        <hr />

        <h2>Introduction</h2>

        <c:set var="sectionName">${FRONT_MATTER.identifier}</c:set>
        <c:set var="sectionTextValue">${sectionText[FRONT_MATTER]}</c:set>
        <%@ include file="edit-awards-script_textarea-macros.jspf"%>
        <hr />


        <h2>Sponsors</h2>

        <c:set var="sectionName">${SPONSORS_INTRO.identifier}</c:set>
        <c:set var="sectionTextValue">${sectionText[SPONSORS_INTRO]}</c:set>
        <%@ include file="edit-awards-script_textarea-macros.jspf"%>
        <hr />

        <label>
            Specify value:
            <input id="sponsors_specified" name="sponsors_specified"
                type="checkbox" />
        </label>
        <button type="button" id="add_sponsor">Add Sponsor</button>

        <div id="sponsors"></div>
        <hr />

        <c:set var="sectionName">${SPONSORS_RECOGNITION.identifier}</c:set>
        <c:set var="sectionTextValue">${sectionText[SPONSORS_RECOGNITION]}</c:set>
        <%@ include file="edit-awards-script_textarea-macros.jspf"%>
        <hr />


        <c:set var="sectionName">${VOLUNTEERS.identifier}</c:set>
        <c:set var="sectionTextValue">${sectionText[VOLUNTEERS]}</c:set>
        <%@ include file="edit-awards-script_textarea-macros.jspf"%>
        <hr />


        <h2>Award presentation order</h2>

        <div>
            The order of the award groups for each category is the same
            as the <a
                href="<c:url value='/report/edit-advancing-teams.jsp'/>">award
                group order for advancing teams.</a>
        </div>

        <label>
            Specify value:
            <input type="checkbox" name="awardOrder_specified"
                id="awardOrder_specified" />
        </label>
        <div id="award_order"></div>

        <h2>Awards</h2>

        <div>
            Enter the text for each award. The information about why
            each team received an award is entered in the <a
                href="<c:url value='/report/edit-award-winners.jsp'/>"
                target="_blank">awards for the tournament</a>.
        </div>

        <h3>${championshipAwardTitle}</h3>
        <div>
            Specify Presenter
            <input type="checkbox"
                name="${CATEGORY_CHAMPIONSHIP_PRESENTER.identifier}_specified"
                id="${CATEGORY_CHAMPIONSHIP_PRESENTER.identifier}_specified" />
            <input type="text"
                name="${CATEGORY_CHAMPIONSHIP_PRESENTER.identifier}_text"
                id="${CATEGORY_CHAMPIONSHIP_PRESENTER.identifier}_text"
                value="${sectionText[CATEGORY_CHAMPIONSHIP_PRESENTER]}" />
        </div>
        <c:set var="sectionName">${CATEGORY_CHAMPIONSHIP.identifier}</c:set>
        <c:set var="sectionTextValue">${sectionText[CATEGORY_CHAMPIONSHIP]}</c:set>
        <%@ include file="edit-awards-script_textarea-macros.jspf"%>
        <hr />

        <h3>${head2headAwardTitle}</h3>
        <div>
            Specify Presenter
            <input type="checkbox"
                name="${CATEGORY_HEAD2HEAD_PRESENTER.identifier}_specified"
                id="${CATEGORY_HEAD2HEAD_PRESENTER.identifier}_specified" />
            <input type="text"
                name="${CATEGORY_HEAD2HEAD_PRESENTER.identifier}_text"
                id="${CATEGORY_HEAD2HEAD_PRESENTER.identifier}_text"
                value="${sectionText[CATEGORY_HEAD2HEAD_PRESENTER]}" />
        </div>
        <c:set var="sectionName">${CATEGORY_HEAD2HEAD.identifier}</c:set>
        <c:set var="sectionTextValue">${sectionText[CATEGORY_HEAD2HEAD]}</c:set>
        <%@ include file="edit-awards-script_textarea-macros.jspf"%>
        <hr />

        <h3>${performanceAwardTitle}</h3>
        <div>
            Specify Presenter
            <input type="checkbox"
                name="${CATEGORY_PERFORMANCE_PRESENTER.identifier}_specified"
                id="${CATEGORY_PERFORMANCE_PRESENTER.identifier}_specified" />
            <input type="text"
                name="${CATEGORY_PERFORMANCE_PRESENTER.identifier}_text"
                id="${CATEGORY_PERFORMANCE_PRESENTER.identifier}_text"
                value="${sectionText[CATEGORY_PERFORMANCE_PRESENTER]}" />
        </div>
        <c:set var="sectionName">${CATEGORY_PERFORMANCE.identifier}</c:set>
        <c:set var="sectionTextValue">${sectionText[CATEGORY_PERFORMANCE]}</c:set>
        <%@ include file="edit-awards-script_textarea-macros.jspf"%>
        <hr />

        <h3>Subjective awards</h3>
        <c:forEach items="${subjectiveCategories}" var="category">
            <h4>Category ${category.title}</h4>
            <div>
                Specify Presenter
                <input type="checkbox"
                    name="category_${category.name}_presenter_specified"
                    id="category_${category.name}_presenter_specified" />
                <input type="text"
                    name="category_${category.name}_presenter_text"
                    id="category_${category.name}_presenter_text"
                    value="${subjectiveCategoryPresenter[category]}" />
            </div>
            <c:set var="sectionName">category_${category.name}</c:set>
            <c:set var="sectionTextValue">${subjectiveCategoryText[category]}</c:set>
            <%@ include file="edit-awards-script_textarea-macros.jspf"%>
            <hr />
        </c:forEach>

        <h3>Non-numeric awards</h3>
        <div>
            The categories that have awards are <a
                href="<c:url value='/report/awards/edit-categories-awarded.jsp'/>"
                target="_blank">specified per tournament level</a>.
        </div>
        <c:forEach items="${nonNumericCategories}" var="category">
            <h4>Category ${category.title}</h4>
            <div>
                Specify Presenter
                <input type="checkbox"
                    name="category_${category.title}_presenter_specified"
                    id="category_${category.title}_presenter_specified" />
                <input type="text"
                    name="category_${category.title}_presenter_text"
                    id="category_${category.title}_presenter_text"
                    value="${nonNumericCategoryPresenter[category]}" />
            </div>
            <c:set var="sectionName">category_${category.title}</c:set>
            <c:set var="sectionTextValue">${nonNumericCategoryText[category]}</c:set>
            <%@ include file="edit-awards-script_textarea-macros.jspf"%>
            <hr />
        </c:forEach>


        <c:set var="sectionName">${END_AWARDS.identifier}</c:set>
        <c:set var="sectionTextValue">${sectionText[END_AWARDS]}</c:set>
        <%@ include file="edit-awards-script_textarea-macros.jspf"%>
        <hr />

        <div>
            <b>The advancing teams will be listed here when the
                report is generated.</b>
        </div>
        <hr />


        <c:set var="sectionName">${FOOTER.identifier}</c:set>
        <c:set var="sectionTextValue">${sectionText[FOOTER]}</c:set>
        <%@ include file="edit-awards-script_textarea-macros.jspf"%>
        <hr />

        <input type="submit" id="submit_data" value="Submit" />
    </form>
    <!-- before end body -->
</body>
<!-- before html -->
</html>
<!--  end -->