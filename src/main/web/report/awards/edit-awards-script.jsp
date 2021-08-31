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

  awardsScriptModule.configureMacroEntry(macro_${macro.text}, ${macroSpecified[macro]}, ${macroValue[macro]});
  </c:forEach>
  

  <c:forEach items="${sections}" var="section"> 
  awardsScriptModule.configureTextEntry(macros, "${section.identifier}", ${sectionSpecified[section]});
  </c:forEach>
  
  <c:forEach items="${subjectiveCategories}" var="category">
  awardsScriptModule.configureTextEntry(macros, "category_${category.name}", ${subjectiveCategorySpecified[category]});
  </c:forEach>
  
  <c:forEach items="${nonNumericCategories}" var="category">
  awardsScriptModule.configureTextEntry(macros, "category_${category.title}", ${nonNumericCategorySpecified[category]});
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

        <h1>Macro Values</h1>
        <div>Some of the macros need values populated. These
            values can be populated at any layer of the script.</div>

        <h2>${NUM_TRAINED_OFFICIALS.title}</h2>
        <div>
            <label>
                Specify value:
                <input type="checkbox"
                    name="${NUM_TRAINED_OFFICIALS.text}_specified"
                    id="${NUM_TRAINED_OFFICIALS.text}_specified" />
            </label>
            <input type="text"
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
            <input type="text" name="${NUM_TEAMS_ADVANCING.text}_value"
                id="${NUM_TEAMS_ADVANCING.text}_value" />
        </div>


        <hr />
        <h1>Awards script sections</h1>

        <div>Tournament Location - Tournament Date</div>
        <hr />

        <c:set var="sectionName">${FRONT_MATTER.identifier}</c:set>
        <c:set var="sectionTextValue">${sectionText[FRONT_MATTER]}</c:set>
        <%@ include file="edit-awards-script_textarea-macros.jspf"%>
        <hr />


        <c:set var="sectionName">${SPONSORS_INTRO.identifier}</c:set>
        <c:set var="sectionTextValue">${sectionText[SPONSORS_INTRO]}</c:set>
        <%@ include file="edit-awards-script_textarea-macros.jspf"%>
        <hr />


        <div>FIXME: disable editing unless the checkbox is checked</div>
        <label>
            Specify value:
            <input type="checkbox" name="sponsors_names_specified" />
        </label>
        <div>FIXME: user entered sponsor names</div>
        <hr />

        <c:set var="sectionName">${SPONSORS_RECOGNITION.identifier}</c:set>
        <c:set var="sectionTextValue">${sectionText[SPONSORS_RECOGNITION]}</c:set>
        <%@ include file="edit-awards-script_textarea-macros.jspf"%>
        <hr />


        <c:set var="sectionName">${VOLUNTEERS.identifier}</c:set>
        <c:set var="sectionTextValue">${sectionText[VOLUNTEERS]}</c:set>
        <%@ include file="edit-awards-script_textarea-macros.jspf"%>
        <hr />


        <div>FIXME: specify award order, iterates through all
            awards and then allows them to be moved up and down.
            Checkbox to see if it is being specified</div>
        <label>
            Specify value:
            <input type="checkbox" name="award_order_specified" />
        </label>
        <table>
            <tr>
                <th>Award</th>
            </tr>

        </table>

        <div>
            Enter the text for each award. The information about why
            each team received an award is entered in the <a
                href="<c:url value='/report/edit-award-winners.jsp'/>"
                target="_blank">awards for the tournament</a>.
        </div>

        <h1>Awards</h1>

        <h2>Championship</h2>
        <div>
            Presenter:
            <input type="text" />
        </div>
        <c:set var="sectionName">${CATEGORY_CHAMPIONSHIP.identifier}</c:set>
        <c:set var="sectionTextValue">${sectionText[CATEGORY_CHAMPIONSHIP]}</c:set>
        <%@ include file="edit-awards-script_textarea-macros.jspf"%>
        <hr />

        <h2>Robot Performance Awards</h2>
        <div>
            Presenter:
            <input type="text" />
        </div>
        <c:set var="sectionName">${CATEGORY_PERFORMANCE.identifier}</c:set>
        <c:set var="sectionTextValue">${sectionText[CATEGORY_PERFORMANCE]}</c:set>
        <%@ include file="edit-awards-script_textarea-macros.jspf"%>
        <hr />

        <h2>Subjective awards</h2>
        <c:forEach items="${subjectiveCategories}" var="category">
            <h3>Category ${category.title}</h3>
            <div>
                Presenter:
                <input type="text" />
            </div>
            <c:set var="sectionName">category_${category.name}</c:set>
            <c:set var="sectionTextValue">${subjectiveCategoryText[category.name]}</c:set>
            <%@ include file="edit-awards-script_textarea-macros.jspf"%>
            <hr />
        </c:forEach>

        <!-- FIXME: make the default text for non-numeric categories be the description of the category -->
        <h2>Non-numeric awards</h2>
        <div>
            The categories that have awards are <a
                href="<c:url value='/report/awards/edit-categories-awarded.jsp'/>"
                target="_blank">specified per tournament level</a>.
        </div>
        <c:forEach items="${nonNumericCategories}" var="category">
            <h3>Category ${category.title}</h3>
            <div>
                Presenter:
                <input type="text" />
            </div>
            <c:set var="sectionName">category_${category.title}</c:set>
            <c:set var="sectionTextValue">${nonNumericCategoryText[category.title]}</c:set>
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

        <input type="submit" value="Submit" />
    </form>
    <!-- before end body -->
</body>
<!-- before html -->
</html>
<!--  end -->