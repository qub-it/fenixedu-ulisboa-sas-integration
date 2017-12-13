<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>
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
			code="label.manageScholarshipCandidacies.sasScholarshipDataChangeLogs" />
		<small></small>
	</h1>
</div>

<!-- /.modal -->
<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
	<span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>&nbsp;<a
		class=""
		href="${pageContext.request.contextPath}/academic-administration/sas-scholarship-management/integration-sas-manageScholarshipCandidacies/"><spring:message
			code="label.event.back" /></a>

</div>


<c:choose>
	<c:when test="${not empty sasScholarshipDataChangeLogs}">
		<table id="searchSasScholarshipDataChangeLogsTable"
			class="table responsive table-bordered table-hover">
			<thead>
				<tr>
					<%--!!!  Field names here --%>
					<th><spring:message
							code="label.SasScholarshipDataChangeLog.date" /></th>
					<th><spring:message
							code="label.SasScholarshipDataChangeLog.studentNumber" /></th>
					<th><spring:message
							code="label.SasScholarshipDataChangeLog.studentName" /></th>
					<th><spring:message
							code="label.SasScholarshipDataChangeLog.description" /></th>
				</tr>
			</thead>
			<tbody>

			<c:forEach items="${sasScholarshipDataChangeLogs}" var="logResult">
				<tr>
					<td><joda:format value='${logResult.date}' pattern='yyyy-MM-dd HH:mm' /></td>
					<td>${logResult.studentNumber}</td>
					<td>${logResult.studentName}</td>
					<td>${logResult.description}</td>
				</tr>
			</c:forEach>

			</tbody>
		</table>
				
	</c:when>
	<c:otherwise>
		<div class="alert alert-warning" role="alert">

			<spring:message code="label.noResultsFound" />

		</div>

	</c:otherwise>
</c:choose>

<script>
	
	
	$(document).ready(function() {

		var table = $('#searchSasScholarshipDataChangeLogsTable').DataTable({
			language : {
				url : "${datatablesI18NUrl}",			
			},
			"columns": [
				{ data: 'date' },
				{ data: 'studentNumber' },
				{ data: 'studentName' },
				{ data: 'description' }
				
				],
			"order": [[ 0, "desc" ]],

			"dom": '<"col-sm-6"l><"col-sm-6"f>rtip',

        	"tableTools": { "sSwfPath": "//cdn.datatables.net/tabletools/2.2.3/swf/copy_csv_xls_pdf.swf" }
			
		});
		
		table.columns.adjust().draw();
		
		
		  
	}); 
</script>