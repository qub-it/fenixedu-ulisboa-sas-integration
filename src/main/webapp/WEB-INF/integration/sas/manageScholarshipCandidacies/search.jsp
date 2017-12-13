<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>
<spring:url var="datatablesUrl"
	value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js" />
<spring:url var="datatablesBootstrapJsUrl"
	value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl"
	value="/CSS/dataTables/dataTables.bootstrap.min.css" />
<link rel="stylesheet" href="${datatablesCssUrl}" />
<spring:url var="datatablesI18NUrl"
	value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json" />

<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css" />

<link
	href="//cdn.datatables.net/responsive/1.0.4/css/dataTables.responsive.css"
	rel="stylesheet" />
<script
	src="//cdn.datatables.net/responsive/1.0.4/js/dataTables.responsive.js"></script>
<link
	href="//cdn.datatables.net/tabletools/2.2.3/css/dataTables.tableTools.css"
	rel="stylesheet" />
<script
	src="//cdn.datatables.net/tabletools/2.2.3/js/dataTables.tableTools.min.js"></script>
<link
	href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0-rc.1/css/select2.min.css"
	rel="stylesheet" />
<script
	src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0-rc.1/js/select2.min.js"></script>
<script
	src="${pageContext.request.contextPath}/static/integration/sas/js/bootbox.min.js"></script>
<script
	src="${pageContext.request.contextPath}/static/integration/sas/js/omnis.js"></script>

${portal.toolkit()}

<%-- TITLE --%>
<div class="page-header">
	<h1>
		<spring:message
			code="label.manageScholarshipCandidacies.searchScholarshipCandidacies" />
		<small></small>
	</h1>
</div>

<%-- <bean:define id="deleteConfirm">
	return confirm('<bean:message key="message.confirm.delete.candidacy"/>')
</bean:define> --%>

<!-- /.modal -->
<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
	<span class="glyphicon glyphicon-import" aria-hidden="true"></span>&nbsp;<a
		class=""
		href="${pageContext.request.contextPath}/integration/sas/manageScholarshipCandidacies/syncAll"><spring:message
			code="label.event.syncAll" /></a> |&nbsp;&nbsp;
	<span
		class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<a
		class=""
		href="${pageContext.request.contextPath}/integration/sas/manageScholarshipCandidacies/processAll"><spring:message
			code="label.event.processAll" /></a> |&nbsp;&nbsp;
	<span
		class="glyphicon glyphicon-export" aria-hidden="true"></span>&nbsp;<a
		class=""
		href="${pageContext.request.contextPath}/integration/sas/manageScholarshipCandidacies/sendAll"><spring:message
			code="label.event.sendAll" /></a> |&nbsp;&nbsp;
	<span
		class="glyphicon glyphicon-zoom-in" aria-hidden="true"></span>&nbsp;<a
		class=""
		href="${pageContext.request.contextPath}/integration/sas/manageScholarshipCandidacies/logs"><spring:message
			code="label.event.logs" /></a> |&nbsp;&nbsp;
	<span
		class="glyphicon glyphicon-remove" aria-hidden="true"></span>&nbsp;<a
		class=""
		href="${pageContext.request.contextPath}/integration/sas/manageScholarshipCandidacies/removeAll"<%-- onclick='<%= pageContext.findAttribute("deleteConfirm").toString() %>' --%>><spring:message
			code="label.event.removeAll" /></a>


</div>

<c:if test="${not empty infoMessages}">
	<div class="alert alert-info" role="alert">

		<c:forEach items="${infoMessages}" var="message">
			<p>
				<span class="glyphicon glyphicon glyphicon-ok-sign"
					aria-hidden="true">&nbsp;</span> ${message}
			</p>
		</c:forEach>

	</div>
</c:if>

<script type="text/javascript">
	function submitOptions(tableID, formID, attributeName) {
	array = $("#" + tableID).DataTable().rows(".selected")[0];	
	$("#" + formID).empty();
	if (array.length>0) {
		$.each(array,function(index, value) {
			externalId = $("#" + tableID).DataTable().row(value).data()["DT_RowId"];
			$("#" + formID).append("<input type='hidden' name='" + attributeName+ "' value='" + externalId + "'/>");
		});
		$("#" + formID).submit();
	}
	else
	{
		messageAlert('<spring:message code = "label.warning"/>','<spring:message code = "label.select.mustselect"/>');
	}
		
	}
</script>



<c:choose>
	<c:when test="${not empty scholarshipCandidacies}">
		<table id="searchScholarshipCandidaciesTable"
			class="table responsive table-bordered table-hover">
			<thead>
				<tr>
					<%--!!!  Field names here --%>
					<th><spring:message
							code="label.SasScholarshipCandidacy.submissionDate" /></th>
					<th><spring:message
							code="label.SasScholarshipCandidacy.studentNumber" /></th>
					<th><spring:message
							code="label.SasScholarshipCandidacy.candidacyName" /></th>
					<th><spring:message
							code="label.SasScholarshipCandidacy.fiscalNumber" /></th>
					<th><spring:message
							code="label.SasScholarshipCandidacy.docIdNumber" /></th>
					<th><spring:message
							code="label.SasScholarshipCandidacy.docIdType" /></th>
					<th><spring:message
							code="label.SasScholarshipCandidacy.degreeName" /></th>
					<th><spring:message
							code="label.SasScholarshipCandidacy.importDate" /></th>
					<th><spring:message
							code="label.SasScholarshipCandidacy.exportDate" /></th>
					<th><spring:message code="label.SasScholarshipData.state" /></th>
					<th></th>
				</tr>
			</thead>
			<tbody>

			</tbody>
		</table>

		<%-- <form id="syncEntries"
			action="${pageContext.request.contextPath}/integration/sas/manageScholarshipCandidacies/sync"
			style="display: none;" method="POST"></form>

		<button id="syncEntryButton" type="button"
			onClick="javascript:submitOptions('searchScholarshipCandidaciesTable', 'syncEntries', 'sasScholarshipCandidacyEntries')">
			<span class="glyphicon glyphicon-import"
				aria-hidden="true"></span>&nbsp;
			<spring:message code='label.event.sync' />
		</button>

		<form id="processEntries"
			action="${pageContext.request.contextPath}/integration/sas/manageScholarshipCandidacies/process"
			style="display: none;" method="POST"></form>

		<button id="processEntryButton" type="button"
			onClick="javascript:submitOptions('searchScholarshipCandidaciesTable', 'processEntries', 'sasScholarshipCandidacyEntries')">
			<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;
			<spring:message code='label.event.process' />
		</button>

		<form id="sendEntries"
			action="${pageContext.request.contextPath}/integration/sas/manageScholarshipCandidacies/send"
			style="display: none;" method="POST"></form>

		<button id="sendEntryButton" type="button"
			onClick="javascript:submitOptions('searchScholarshipCandidaciesTable', 'sendEntries', 'sasScholarshipCandidacyEntries')">
			<span class="glyphicon glyphicon-export"
				aria-hidden="true"></span>&nbsp;
			<spring:message code='label.event.send' />
		</button>

		<form id="removeEntries"
			action="${pageContext.request.contextPath}/integration/sas/manageScholarshipCandidacies/remove"
			style="display: none;" method="POST"></form>

		<button id="removeEntryButton" type="button"
			onClick="javascript:submitOptions('searchScholarshipCandidaciesTable', 'removeEntries', 'sasScholarshipCandidacyEntries')">
			<span class="glyphicon glyphicon-remove" aria-hidden="true"></span>&nbsp;
			<spring:message code='label.event.remove' />
		</button> --%>

	</c:when>
	<c:otherwise>
		<div class="alert alert-warning" role="alert">

			<spring:message code="label.noResultsFound" />

		</div>

	</c:otherwise>
</c:choose>

<script>
	var scholarshipCandidaciesDataSet = [
			<c:forEach items="${scholarshipCandidacies}" var="searchResult">
			 
				<%-- Field access / formatting  here CHANGE_ME --%>
				{
				"DT_RowId" : '<c:out value='${searchResult.externalId}'/>',
				"submissionDate" : "<joda:format value='${searchResult.submissionDate}' pattern='yyyy-MM-dd' />",
				"studentNumber" : '${searchResult.studentNumber}',
				"candidacyName" : '${searchResult.candidacyName}',
				"fiscalNumber" : '${searchResult.fiscalNumber}',
				"docIdNumber" : '${searchResult.docIdNumber}',
				"docIdType" : '${searchResult.docIdType.localizedName}',
				"degreeName" : '[${searchResult.degreeCode}] ${searchResult.degreeName}',
				"importDate" : "<joda:format value='${searchResult.importDate}' pattern='yyyy-MM-dd' />",
				"exportDate" : "<joda:format value='${searchResult.exportDate}' pattern='yyyy-MM-dd' />",
				"state" : '${searchResult.sasScholarshipData.state.localizedName}',
				"actions" :
					" <a  class=\"btn btn-default btn-xs\" href=\"${pageContext.request.contextPath}/integration/sas/manageScholarshipCandidacies/search/viewSasScholarshipCandidacy/${searchResult.externalId}\"><spring:message code='label.details.SasScholarshipCandidacy'/></a>" +
					                "" +
					                <c:if test="${searchResult.sasScholarshipData != null}">
					                	" <a class=\"btn btn-default btn-xs\" href=\"${pageContext.request.contextPath}/integration/sas/manageScholarshipCandidacies/search/viewSasScholarshipData/${searchResult.sasScholarshipData.externalId}\">" + 
					                		"<spring:message code='label.details.SasScholarshipData'/>" +
					                	"</a>"
					                + </c:if>
					                "" 
				},
			</c:forEach>
	];
	
	$(document).ready(function() {

		var table = $('#searchScholarshipCandidaciesTable').DataTable({
			language : {
				url : "${datatablesI18NUrl}",			
			},
			"columns": [
				{ data: 'submissionDate' },
				{ data: 'studentNumber' },
				{ data: 'candidacyName' },
				{ data: 'fiscalNumber' },
				{ data: 'docIdNumber' },
				{ data: 'docIdType' },
				{ data: 'degreeName' },
				{ data: 'importDate' },
				{ data: 'exportDate' },
				{ data: 'state' },
				{ data: 'actions',className:"all", width: "12%" }
				
				],
			"data" : scholarshipCandidaciesDataSet,
			"order": [[ 0, "desc" ]],

			"dom": '<"col-sm-6"l><"col-sm-6"f>rtip',

        	"tableTools": { "sSwfPath": "//cdn.datatables.net/tabletools/2.2.3/swf/copy_csv_xls_pdf.swf" }
			
		});
		
		table.columns.adjust().draw();
		
		  $('#searchScholarshipCandidaciesTable tbody').on( 'click', 'tr', function () {
		        $(this).toggleClass('selected');
		    } );
		  
	}); 
</script>