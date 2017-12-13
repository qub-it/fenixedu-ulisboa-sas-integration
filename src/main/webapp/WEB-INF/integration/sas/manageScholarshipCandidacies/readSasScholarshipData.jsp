<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
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

<!-- Choose ONLY ONE:  bennuToolkit OR bennuAngularToolkit -->
<%--${portal.angularToolkit()} --%>
${portal.toolkit()}

<link
	href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css"
	rel="stylesheet" />
<script
	src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link
	href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css"
	rel="stylesheet" />
<script
	src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>
<script type="text/javascript"
	src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js"></script>



<%-- TITLE --%>
<div class="page-header">
	<h1>
		<spring:message
			code="label.manageScholarshipCandidacies.readSasScholarshipData" />
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
	<%-- |&nbsp;&nbsp; <span
		class="glyphicon glyphicon-pencil" aria-hidden="true"></span>&nbsp;<a
		class=""
		href="${pageContext.request.contextPath}/academicTreasury/managefinantialentityadministrativeoffice/finantialentity/update/${finantialEntity.externalId}"><spring:message
			code="label.event.update" /></a> &nbsp; --%>
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

<div class="panel panel-primary">
	<div class="panel-heading">
		<h3 class="panel-title">
			<spring:message code="label.SasScholarshipCandidacy.mainInfo" />
		</h3>
	</div>
	<div class="panel-body">
		<form method="post" class="form-horizontal">
			<table class="table">
				<tbody>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipCandidacy.studentNumber" /></th>
						<td><c:out value='${sasScholarshipData.sasScholarshipCandidacy.studentNumber}' />
						</td>
					</tr>
					
					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipCandidacy.degreeName" /></th>
						<td><c:out value='[${sasScholarshipData.sasScholarshipCandidacy.degreeCode}] ${sasScholarshipData.sasScholarshipCandidacy.degreeName}' />
						</td>
					</tr>
					
					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipCandidacy.candidacyName" /></th>
						<td><c:out value='${sasScholarshipData.sasScholarshipCandidacy.candidacyName}' />
						</td>
					</tr>
					
					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipCandidacy.candidacyNumber" /></th>
						<td><c:out value='${sasScholarshipData.sasScholarshipCandidacy.candidacyNumber}' />
						</td>
					</tr>
					
					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipCandidacy.fiscalNumber" /></th>
						<td><c:out value='${sasScholarshipData.sasScholarshipCandidacy.fiscalNumber}' />
						</td>
					</tr>
					
					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipCandidacy.docIdNumber" /></th>
						<td><c:out value='${sasScholarshipData.sasScholarshipCandidacy.docIdNumber}' />
						</td>
					</tr>
					
					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipCandidacy.docIdType" /></th>
						<td><c:out value='${sasScholarshipData.sasScholarshipCandidacy.docIdType.localizedName}' />
						</td>
					</tr>
					
					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipCandidacy.docIdNumber" /></th>
						<td><c:out value='${sasScholarshipData.sasScholarshipCandidacy.docIdNumber}' />
						</td>
					</tr>
					
					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipCandidacy.candidacyState" /></th>
						<td><c:out value='${sasScholarshipData.sasScholarshipCandidacy.candidacyState.localizedName}' />
						</td>
					</tr>
					
				</tbody>
			</table>

		</form>
	</div>
</div>
				
					
<div class="panel panel-primary">
	<div class="panel-heading">
		<h3 class="panel-title">
			<spring:message code="label.details" />
		</h3>
	</div>
	<div class="panel-body">
		<form method="post" class="form-horizontal">
			<table class="table">
				<tbody>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.registrationYear" /></th>
						<td><c:out value='${sasScholarshipData.registrationYear}' />
						</td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.curricularYear" /></th>
						<td><c:out value='${label.SasScholarshipData.curricularYear}' /></td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.lastAcademicActDateLastYear" /></th>
						<td><c:out
								value='${sasScholarshipData.lastAcademicActDateLastYear}' /></td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.enrolmentDate" /></th>
						<td><c:out value='${sasScholarshipData.enrolmentDate}' /></td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.firstMonthGratuity" /></th>
						<td><c:out value='${sasScholarshipData.firstMonthGratuity}' /></td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.numberOfDegreeCurricularYears" /></th>
						<td><c:out
								value='${sasScholarshipData.numberOfDegreeCurricularYears}' /></td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.numberOfEnrolledECTS" /></th>
						<td><c:out value='${sasScholarshipData.numberOfEnrolledECTS}' /></td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.numberOfApprovedEctsLastYear" /></th>
						<td><c:out
								value='${sasScholarshipData.numberOfApprovedEctsLastYear}' /></td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.numberOfEnrolledEctsLastYear" /></th>
						<td><c:out
								value='${sasScholarshipData.numberOfEnrolledEctsLastYear}' /></td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.numberOfEnrolmentsYears" /></th>
						<td><c:out
								value='${sasScholarshipData.numberOfEnrolmentsYears}' /></td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.numberOfEnrolledEctsLastYear" /></th>
						<td><c:out
								value='${sasScholarshipData.numberOfEnrolledEctsLastYear}' /></td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.numberOfEnrolmentsYears" /></th>
						<td><c:out
								value='${sasScholarshipData.numberOfEnrolmentsYears}' /></td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.cycleNumberOfEnrolmentYears" /></th>
						<td><c:out
								value='${sasScholarshipData.cycleNumberOfEnrolmentYears}' /></td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.numberOfMonthsGratuity" /></th>
						<td><c:out
								value='${sasScholarshipData.numberOfMonthsGratuity}' /></td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.numberOfDegreeChanges" /></th>
						<td><c:out
								value='${sasScholarshipData.numberOfDegreeChanges}' /></td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.observations" /></th>
						<td>
							<c:out value="${fn:replace(sasScholarshipData.observations, '.', '.<br/>')}" escapeXml="false"/>
						</td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.hasMadeDegreeChangeOnCurrentYear" /></th>
						<td><c:out
								value='${sasScholarshipData.hasMadeDegreeChangeOnCurrentYear}' /></td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.regime" /></th>
						<td><c:out value='${sasScholarshipData.regime}' /></td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.cetQualificationOwner" /></th>
						<td>
							<c:choose>
							   <c:when test = "${sasScholarshipData.cetQualificationOwner}">
							      <spring:message code="label.true" />
							   </c:when>
							   <c:otherwise>
							     <spring:message code="label.false" />
							   </c:otherwise>
							</c:choose>
						</td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.ctspQualificationOwner" /></th>
						<td>
							<c:choose>
							   <c:when test = "${sasScholarshipData.ctspQualificationOwner}">
							      <spring:message code="label.true" />
							   </c:when>
							   <c:otherwise>
							     <spring:message code="label.false" />
							   </c:otherwise>
							</c:choose>
						</td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.phdQualificationOwner" /></th>
						<td>
							<c:choose>
							   <c:when test = "${sasScholarshipData.phdQualificationOwner}">
							      <spring:message code="label.true" />
							   </c:when>
							   <c:otherwise>
							     <spring:message code="label.false" />
							   </c:otherwise>
							</c:choose>
						</td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.degreeQualificationOwner" /></th>
						<td>
							<c:choose>
							   <c:when test = "${sasScholarshipData.degreeQualificationOwner}">
							      <spring:message code="label.true" />
							   </c:when>
							   <c:otherwise>
							     <spring:message code="label.false" />
							   </c:otherwise>
							</c:choose>
						</td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.masterQualificationOwner" /></th>
						<td>
							<c:choose>
							   <c:when test = "${sasScholarshipData.masterQualificationOwner}">
							      <spring:message code="label.true" />
							   </c:when>
							   <c:otherwise>
							     <spring:message code="label.false" />
							   </c:otherwise>
							</c:choose>
						</td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.lastEnrolmentYear" /></th>
						<td><c:out value='${sasScholarshipData.lastEnrolmentYear}' /></td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.gratuityAmount" /></th>
						<td><c:out value='${sasScholarshipData.gratuityAmount}' /></td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.registered" /></th>
						<td>
							<c:choose>
							   <c:when test = "${sasScholarshipData.registered}">
							      <spring:message code="label.true" />
							   </c:when>
							   <c:otherwise>
							     <spring:message code="label.false" />
							   </c:otherwise>
							</c:choose>
						</td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.numberOfApprovedEcts" /></th>
						<td><c:out value='${sasScholarshipData.numberOfApprovedEcts}' /></td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.stateDate" /></th>
						<td><c:out value='${sasScholarshipData.stateDate}' /></td>
					</tr>

					<tr>
						<th scope="row" class="col-xs-3"><spring:message
								code="label.SasScholarshipData.state" /></th>
						<td><c:out value='${sasScholarshipData.state.localizedName}' /></td>
					</tr>

				</tbody>
			</table>

		</form>
	</div>
</div>

<script>
	$(document).ready(function() {

	});
</script>
