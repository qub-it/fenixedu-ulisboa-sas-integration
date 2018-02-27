package org.fenixedu.ulisboa.integration.sas.service.registration.report;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academic.domain.EnrolmentEvaluation;
import org.fenixedu.academic.domain.ExecutionInterval;
import org.fenixedu.academic.domain.ExecutionSemester;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.StudentCurricularPlan;
import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.academic.domain.degreeStructure.DegreeModule;
import org.fenixedu.academic.domain.exceptions.DomainException;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.student.RegistrationRegimeType;
import org.fenixedu.academic.domain.student.curriculum.Curriculum;
import org.fenixedu.academic.domain.student.curriculum.ICurriculumEntry;
import org.fenixedu.academic.domain.studentCurriculum.CurriculumLine;
import org.fenixedu.academic.domain.studentCurriculum.CycleCurriculumGroup;
import org.fenixedu.academic.domain.studentCurriculum.Dismissal;
import org.fenixedu.academic.dto.student.RegistrationConclusionBean;
import org.fenixedu.ulisboa.integration.sas.service.process.AbstractFillScholarshipService;
import org.fenixedu.ulisboa.specifications.domain.studentCurriculum.CreditsReasonType;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

public class SasRegistrationHistoryReportService {

    private static class NormalEnrolmentsPredicate implements Predicate<Enrolment> {

        @Override
        public boolean test(Enrolment enrolment) {

            if (enrolment.isAnnulled()) {
                return false;
            }

            //TODO: replace with RegistrationHistoryReportService from specifications
            return (enrolment.getCurriculumGroup().isInternalCreditsSourceGroup()
                    || !enrolment.getCurriculumGroup().isNoCourseGroupCurriculumGroup())
                    && (enrolment.getParentCycleCurriculumGroup() == null
                            || !enrolment.getParentCycleCurriculumGroup().isExternal());

        }

    }

    public SasRegistrationHistoryReport generateReport(final Registration registration,
            final ExecutionInterval executionInterval) {
        return processRegistration(registration,
                ExecutionInterval.assertExecutionIntervalType(ExecutionYear.class, executionInterval));
    }

    protected Set<Registration> getRegistrationsToProcess(ExecutionYear executionYear, DegreeType degreeType) {

        final Set<Registration> result = new HashSet<>();
        for (final ExecutionSemester executionSemester : executionYear.getExecutionPeriodsSet()) {
            for (final Enrolment enrolment : executionSemester.getEnrolmentsSet()) {
                if (enrolment.getRegistration().getDegreeType() == degreeType) {
                    result.add(enrolment.getRegistration());
                }
            }
        }

        return result;
    }

    private SasRegistrationHistoryReport processRegistration(Registration registration, ExecutionYear executionYear) {
        final SasRegistrationHistoryReport result = new SasRegistrationHistoryReport(registration, executionYear);

        final StudentCurricularPlan studentCurricularPlan = getStudentCurricularPlan(registration, executionYear);
        if (studentCurricularPlan == null) {
            throw new DomainException("error.RegistrationHistoryReportService.unable.to.find.student.curricular.plan.for.year",
                    registration.getStudent().getNumber().toString(), executionYear.getQualifiedName());
        }
        result.setStudentCurricularPlan(studentCurricularPlan);

        result.setTotalEnroledCredits(calculateEnroledCredits(studentCurricularPlan, executionYear));
        result.setTotalApprovedCredits(calculateApprovedCredits(studentCurricularPlan, executionYear));

        result.setRegimeType(registration.getRegimeType(executionYear) == null ? RegistrationRegimeType.FULL_TIME : registration
                .getRegimeType(executionYear));

        final Curriculum curriculum = studentCurricularPlan.getCurriculum(new DateTime(), executionYear);
        result.setApprovedCredits(curriculum.getSumEctsCredits());

        result.setEquivalenceCredits(calculateEquivalenceCredits(studentCurricularPlan, executionYear));
        result.setCurricularYear(curriculum.getCurricularYear());
        LocalDate enrolmentDate = getEnrolmentDate(registration, executionYear);
        result.setEnrolmentDate(enrolmentDate);

        Set<ExecutionYear> collect = getEnrolmentYearsIncludingPrecedentRegistrations(registration);
        int size = collect.size();
        result.setEnrolmentYearsCount(size);
        result.setEnrolmentYearsInFullRegimeCount(calculateEnrolmentYearsInFullRegimeCount(registration, executionYear));
        result.setLastAcademicActDate(calculateLastAcademicActDate(registration, executionYear));

        return result;
    }

    private StudentCurricularPlan getStudentCurricularPlan(Registration registration, ExecutionYear executionYear) {
        if (registration.getStudentCurricularPlansSet().size() == 1) {
            return registration.getLastStudentCurricularPlan();
        }

        return registration.getStudentCurricularPlan(executionYear);
    }

    private BigDecimal calculateEnroledCredits(StudentCurricularPlan studentCurricularPlan, ExecutionYear executionYear) {
        return sumCredits(getEnroledCurriculumLines(studentCurricularPlan, executionYear).stream());
    }

    private BigDecimal calculateApprovedCredits(StudentCurricularPlan studentCurricularPlan, ExecutionYear executionYear) {
        return sumCredits(getEnroledCurriculumLines(studentCurricularPlan, executionYear).stream().filter(l -> l.isApproved()));
    }

    //Enrolments and dismissals
    private Set<CurriculumLine> getEnroledCurriculumLines(StudentCurricularPlan studentCurricularPlan,
            ExecutionYear executionYear) {

        final Predicate<CurriculumLine> isForYear = line -> line.getExecutionYear() == executionYear;

        final Predicate<CurriculumLine> isValidType = line -> {
            if (line.isDismissal()) {
                final Dismissal dismissal = (Dismissal) line;
                return dismissal.getCredits().getReason() != null
                        && getErasmusDismissalTypes().contains(dismissal.getCredits().getReason());
            }

            return !((Enrolment) line).isAnnulled();
        };

        final Predicate<CurriculumLine> isNormal = line -> {
            //TODO: refactor to common logic
            return (line.getCurriculumGroup().isInternalCreditsSourceGroup()
                    || !line.getCurriculumGroup().isNoCourseGroupCurriculumGroup())
                    && (line.getParentCycleCurriculumGroup() == null || !line.getParentCycleCurriculumGroup().isExternal());
        };

        final Set<CurriculumLine> candidateLines = studentCurricularPlan.getAllCurriculumLines().stream()
                .filter(isForYear.and(isValidType).and(isNormal)).collect(Collectors.toSet());

        final Set<DegreeModule> dismissalDegreeModules =
                candidateLines.stream().filter(line -> line.isDismissal() && line.getDegreeModule() != null)
                        .map(line -> line.getDegreeModule()).collect(Collectors.toSet());

        return candidateLines.stream().filter(
                line -> line.isDismissal() || (line.isEnrolment() && !dismissalDegreeModules.contains(line.getDegreeModule())))
                .collect(Collectors.toSet());

    }

    private Set<CreditsReasonType> getErasmusDismissalTypes() {
        //TODO: read from config
        return Collections.emptySet();
    }

    private BigDecimal sumCredits(final Stream<CurriculumLine> linesStream) {
        return linesStream.map(line -> line.getEctsCreditsForCurriculum()).reduce((x, y) -> x.add(y)).orElse(BigDecimal.ZERO);
    }

    private BigDecimal calculateEquivalenceCredits(final StudentCurricularPlan studentCurricularPlan,
            final ExecutionYear executionYear) {
        BigDecimal result = BigDecimal.ZERO;

        final Curriculum curriculum = studentCurricularPlan.getCurriculum(new DateTime(), null);
        for (final ICurriculumEntry entry : curriculum.getCurricularYearEntries()) {
            if (entry.getExecutionYear() == executionYear && entry instanceof Dismissal
                    && ((Dismissal) entry).getCredits().isEquivalence()) {
                result = result.add(entry.getEctsCreditsForCurriculum());
            }
        }

        return result;
    }

    static private Set<ExecutionYear> getEnrolmentYearsIncludingPrecedentRegistrations(final Registration input) {
        return AbstractFillScholarshipService.getExecutionYears(input, r -> r.getEnrolmentsExecutionYears().stream(), ey -> true);
    }

    private LocalDate getEnrolmentDate(Registration registration, ExecutionYear executionYear) {
        return registration.getRegistrationDataByExecutionYearSet().stream()
                .filter(rdbey -> rdbey.getExecutionYear() == executionYear).map(rdbey -> rdbey.getEnrolmentDate())
                .filter(x -> x != null).findAny().orElse(null);
    }

    //TODO: this should be moved to registration (including normal students predicate)
    //TODO: call registration history report / check ui
    private LocalDate calculateLastAcademicActDate(final Registration registration, final ExecutionYear executionYear) {

        if (registration.hasConcluded()) {

            final RegistrationConclusionBean conclusionBean;
            if (registration.getDegreeType().hasAnyCycleTypes()) {
                final CycleCurriculumGroup cycleCurriculumGroup =
                        registration.getLastStudentCurricularPlan().getLastConcludedCycleCurriculumGroup();
                conclusionBean = new RegistrationConclusionBean(registration, cycleCurriculumGroup);
            } else {
                conclusionBean = new RegistrationConclusionBean(registration);
            }

            if (conclusionBean.getConclusionYear() == executionYear) {
                return conclusionBean.getConclusionDate().toLocalDate();
            }
        }

        final StudentCurricularPlan studentCurricularPlan = getStudentCurricularPlan(registration, executionYear);
        final Collection<Enrolment> normalEnrolments =
                getFilteredEnrolments(studentCurricularPlan, executionYear, new NormalEnrolmentsPredicate());

        final Collection<Enrolment> normalEnrolmentsEvaluated = normalEnrolments.stream()
                .filter(enrolment -> enrolment.getFinalEnrolmentEvaluation() != null).collect(Collectors.toSet());

        if (!normalEnrolmentsEvaluated.isEmpty()) {
            Comparator<? super Enrolment> compareByFinalEnrolmentEvalution = (x, y) -> EnrolmentEvaluation.COMPARATOR_BY_EXAM_DATE
                    .compare(x.getFinalEnrolmentEvaluation(), y.getFinalEnrolmentEvaluation());
            return Collections.max(normalEnrolmentsEvaluated, compareByFinalEnrolmentEvalution).getFinalEnrolmentEvaluation()
                    .getExamDateYearMonthDay().toLocalDate();
        }

        return registration.getRegistrationDataByExecutionYearSet().stream().filter(r -> r.getExecutionYear() == executionYear)
                .map(rdbey -> rdbey.getEnrolmentDate()).filter(x -> x != null).findAny().orElse(null);
    }

    private Integer calculateEnrolmentYearsInFullRegimeCount(final Registration registration, final ExecutionYear executionYear) {

        final Collection<ExecutionYear> enrolmentYears = getEnrolmentYearsIncludingPrecedentRegistrations(registration);
        return enrolmentYears.stream().filter(enrolmentYear -> !registration.isPartialRegime(enrolmentYear))
                .collect(Collectors.toSet()).size();

    }

    private Collection<Enrolment> getFilteredEnrolments(StudentCurricularPlan studentCurricularPlan, ExecutionYear executionYear,
            Predicate<Enrolment> predicate) {
        final List<Enrolment> enrolments = studentCurricularPlan.getEnrolmentsByExecutionYear(executionYear);
        return enrolments.stream().filter(predicate).collect(Collectors.toSet());
    }

}