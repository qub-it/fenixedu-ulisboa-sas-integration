<%@page import="org.fenixedu.ulisboa.integration.sas.ui.spring.controller.manageScholarshipCandidacies.ScholarshipCandidaciesController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>


<script type="text/javascript" src="https://cdn.datatables.net/1.10.12/js/jquery.dataTables.min.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/select/1.2.0/js/dataTables.select.min.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/buttons/1.2.2/js/dataTables.buttons.min.js"></script>
<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jszip/2.5.0/jszip.min.js"></script>
<script type="text/javascript" src="https://cdn.rawgit.com/bpampuch/pdfmake/0.1.18/build/pdfmake.min.js"></script>
<script type="text/javascript" src="https://cdn.rawgit.com/bpampuch/pdfmake/0.1.18/build/vfs_fonts.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/buttons/1.2.2/js/buttons.html5.min.js"></script>
<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/buttons/1.2.2/css/buttons.dataTables.min.css" /> 


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

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css" />

${portal.toolkit()}

<link href="//cdn.datatables.net/responsive/1.0.6/css/dataTables.responsive.css" rel="stylesheet" />
<script src="//cdn.datatables.net/responsive/1.0.6/js/dataTables.responsive.js"></script>
<link href="//cdn.datatables.net/tabletools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet" />
<script	src="//cdn.datatables.net/tabletools/2.2.4/js/dataTables.tableTools.min.js"></script>
<link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0-rc.2/css/select2.min.css" rel="stylesheet" />
<script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0-rc.2/js/select2.min.js"></script>
<script	src="${pageContext.request.contextPath}/static/integration/sas/js/bootbox.min.js"></script>
<script	src="${pageContext.request.contextPath}/static/integration/sas/js/omnis.js"></script>

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
		href="${pageContext.request.contextPath}<%=ScholarshipCandidaciesController.SYNC_ALL_ENTRIES_URL%>/${executionYear.externalId}"><spring:message
			code="label.event.syncAll" /></a> &nbsp;|&nbsp;
	<span
		class="glyphicon glyphicon-cog" aria-hidden="true"></span>&nbsp;<a
		class=""
		href="${pageContext.request.contextPath}<%=ScholarshipCandidaciesController.PROCESS_ALL_ENTRIES_URL%>/${executionYear.externalId}"><spring:message
			code="label.event.processAll" /></a> &nbsp;|&nbsp;
	<span
		class="glyphicon glyphicon-export" aria-hidden="true"></span>&nbsp;<a
		class=""
		href="${pageContext.request.contextPath}<%=ScholarshipCandidaciesController.SEND_ALL_ENTRIES_URL%>/${executionYear.externalId}"><spring:message
			code="label.event.sendAll" /></a> &nbsp;|&nbsp;
	<span
		class="glyphicon glyphicon-zoom-in" aria-hidden="true"></span>&nbsp;<a
		class=""
		href="${pageContext.request.contextPath}<%=ScholarshipCandidaciesController.VIEW_LOGS_ENTRIES_URL%>/${executionYear.externalId}"><spring:message
			code="label.event.logs" /></a> &nbsp;|&nbsp;
	<span
		class="glyphicon glyphicon-remove" aria-hidden="true"></span>&nbsp;<a
		class=""
		href="${pageContext.request.contextPath}<%=ScholarshipCandidaciesController.DELETE_ALL_ENTRIES_URL%>/${executionYear.externalId}"<%-- onclick='<%= pageContext.findAttribute("deleteConfirm").toString() %>' --%>><spring:message
			code="label.event.deleteAll" /></a>


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
<c:if test="${not empty warningMessages}">
	<div class="alert alert-warning" role="alert">

		<c:forEach items="${warningMessages}" var="message">
			<p>
				<span class="glyphicon glyphicon-exclamation-sign"
					aria-hidden="true">&nbsp;</span> ${message}
			</p>
		</c:forEach>

	</div>
</c:if>
<c:if test="${not empty errorMessages}">
	<div class="alert alert-danger" role="alert">

		<c:forEach items="${errorMessages}" var="message">
			<p>
				<span class="glyphicon glyphicon-exclamation-sign"
					aria-hidden="true">&nbsp;</span> ${message}
			</p>
		</c:forEach>

	</div>
</c:if>

<div class="panel panel-default">
    <form method="post" class="form-horizontal" action="${pageContext.request.contextPath}<%=ScholarshipCandidaciesController.CHANGE_EXECUTION_YEAR_URL%>">
        <div class="panel-body">    
            <div class="form-group row">
                <div class="col-sm-1 control-label">
                    <spring:message code="label.executionYear" />
                </div>

                <div class="col-sm-1">
                    <select id="executionYearSelect"
                        class="js-example-basic-single"
                        name="executionYearId" style="width:100%" onchange="this.form.submit();">
                        <%-- empty option remove it if you don't want to have it or give it a label CHANGE_ME --%>
                        <c:forEach var="each" items="${executionYears}">
                                <option value="${each.externalId}" ${each.externalId == executionYear.externalId ? 'selected' : ''}>${each.qualifiedName}</option>
                        </c:forEach>
                    </select>
                    <script type="text/javascript">
                        $("#executionYearSelect").select2({
                            width: 'element'
                          });
                        
                    </script>
                </div>
            </div>
        </div>
   	</form>
</div>


<c:choose>
	<c:when test="${not empty scholarshipCandidacies}">
		<table id="searchScholarshipCandidaciesTable"
			class="table responsive table-bordered table-hover">
			<thead>
				<tr>
					<%--!!!  Field names here --%>
					<th><spring:message code="label.SasScholarshipData.state" /></th>
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
					<th><spring:message
							code="label.SasScholarshipCandidacy.firstYear" /></th>
					<th></th>
				</tr>
			</thead>
			<tbody>
			
			<c:forEach items="${scholarshipCandidacies}" var="searchResult">
				<tr>
					<td>	
						<c:choose>
							
							<c:when test="${searchResult.state.name == 'PENDING'}">
								<strong><span>${searchResult.state.localizedName}</span></strong>
							</c:when>
							
							<c:when test="${searchResult.state.name == 'PROCESSED'}">
 								<strong><span class="text-info">${searchResult.state.localizedName}</span></strong>
							</c:when>
							<c:when test="${searchResult.state.name == 'PROCESSED_WARNINGS'}">
 								<strong><span class="text-warning">${searchResult.state.localizedName}</span></strong>
							</c:when>
							
							<c:when test="${searchResult.state.name == 'PROCESSED_ERRORS'}">
 								<strong><span class="text-danger">${searchResult.state.localizedName}</span></strong>
							</c:when>
							
							<c:when test="${searchResult.state.name == 'SENT'}">
 								<strong><span class="text-success">${searchResult.state.localizedName}</span></strong>
							</c:when>
							
							<c:when test="${searchResult.state.name == 'MODIFIED'}">
								<strong><span class="text-warning">${searchResult.state.localizedName}</span></strong>	
							</c:when>
							
							<c:when test="${searchResult.state.name == 'ANNULLED'}">
								<strong><span class="text-mutted">${searchResult.state.localizedName}</span></strong>	
							</c:when>

						</c:choose>
						</strong>
						  
					</td>
					<td><joda:format value='${searchResult.submissionDate}' pattern='yyyy-MM-dd' /></td>
					<td>${searchResult.studentNumber}</td>
					<td>${searchResult.candidacyName}</td>
					<td>${searchResult.fiscalNumber}</td>
					<td>${searchResult.docIdNumber}</td>
					<td>${searchResult.docIdType.localizedName}</td>
					<td>[${searchResult.degreeCode}] ${searchResult.degreeName}</td>
					
					<td><joda:format value='${searchResult.importDate}' pattern='yyyy-MM-dd' /></td>
					<td><joda:format value='${searchResult.exportDate}' pattern='yyyy-MM-dd' /></td>
					<td>
						<c:if test="${searchResult.firstYear}"><spring:message code="label.true" /></c:if>
						<c:if test="${not searchResult.firstYear}"><spring:message code="label.false" /></c:if>
					</td>
					
					<td style="width: 5%">
						<a class="btn btn-default btn-xs" href="${pageContext.request.contextPath}<%=ScholarshipCandidaciesController.READ_SAS_SCHOLARSHIP_CANDIDACY_URL%>/${searchResult.externalId}">
							<spring:message code='label.details'/>
						</a>
					</td>
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

		 var table = $('#searchScholarshipCandidaciesTable').DataTable({
			language : {
				url : "${datatablesI18NUrl}",			
			},
			"columns": [
				{ data: 'state' },
				{ data: 'submissionDate' },
				{ data: 'studentNumber' },
				{ data: 'candidacyName' },
				{ data: 'fiscalNumber' },
				{ data: 'docIdNumber' },
				{ data: 'docIdType' },
				{ data: 'degreeName' },
				{ data: 'importDate' },
				{ data: 'exportDate' },
				{ data: 'firstTime' },
				{ data: 'actions',className:"all", width: "12%" }
				
				],
			"order": [[ 1, "desc" ]],

			"dom": '<"col-sm-6"l><"col-sm-6"f>rtip',
			buttons: [
		        'copyHtml5',
		        'excelHtml5',
		        'csvHtml5',
		        'pdfHtml5'
			],
        	tableTools: {
            	sSwfPath: "${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/swf/copy_csv_xls_pdf.swf"
        	},
		});
		
		table.columns.adjust().draw();
		  
	}); 
</script>