package org.fenixedu.ulisboa.integration.sas.service.process;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.SchoolLevelType;
import org.fenixedu.academic.domain.StudentCurricularPlan;
import org.fenixedu.academic.domain.candidacy.IngressionType;
import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.academic.domain.degreeStructure.CycleType;
import org.fenixedu.academic.domain.person.IDDocumentType;
import org.fenixedu.academic.domain.person.RoleType;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.student.RegistrationRegimeType;
import org.fenixedu.academic.domain.student.Student;
import org.fenixedu.academic.domain.student.registrationStates.RegistrationState;
import org.fenixedu.academic.domain.student.registrationStates.RegistrationStateType;
import org.fenixedu.academic.domain.treasury.IAcademicTreasuryEvent;
import org.fenixedu.academic.domain.treasury.TreasuryBridgeAPIFactory;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.ulisboa.integration.sas.domain.ScholarshipReportRequest;
import org.fenixedu.ulisboa.integration.sas.domain.SchoolLevelTypeMapping;
import org.fenixedu.ulisboa.integration.sas.domain.SocialServicesConfiguration;
import org.fenixedu.ulisboa.integration.sas.dto.AbstractScholarshipStudentBean;
import org.fenixedu.ulisboa.integration.sas.service.registration.report.RegistrationHistoryReport;
import org.fenixedu.ulisboa.integration.sas.service.registration.report.RegistrationHistoryReportService;
import org.joda.time.DateTime;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class AbstractFillScholarshipService {

    private final Multimap<AbstractScholarshipStudentBean, String> messages = ArrayListMultimap.create();

    protected static final Map<String, IDDocumentType> ID_DOCUMENT_TYPE_MAPPING = Maps.newHashMap();

    public static final String REGIME_FULL_TIME = "Tempo integral";

    public static final String REGIME_FULL_TIME_WORKING_STUDENT = "Trabalhador estudante tempo integral";

    public static final String REGIME_PARTIAL_TIME = "Tempo parcial";

    public static final String REGIME_PARTIAL_TIME_WORKING_STUDENT = "Trabalhador estudante tempo parcial";

    public static final String REGIME_PROFESSIONAL_INTERNSHIP = "Estágio Profissional";

    public static final String ERROR_OBSERVATION = "ERRO";

    public static final String WARNING_OBSERVATION = "AVISO";

    static {

        // id document types mapping
        ID_DOCUMENT_TYPE_MAPPING.put("BI / N.º ID CIVIL", IDDocumentType.IDENTITY_CARD);
        ID_DOCUMENT_TYPE_MAPPING.put("Autorização de residência", IDDocumentType.RESIDENCE_AUTHORIZATION);

    }

    private static DegreeType findDegreeTypeByPredicate(Predicate<? super DegreeType> predicate) {
        return Bennu.getInstance().getDegreeTypeSet().stream().filter(predicate).findAny().orElse(null);
    }

    public Registration getRegistrationByAbstractScholarshipStudentBean(AbstractScholarshipStudentBean bean,
            ScholarshipReportRequest request) {

        final Student student = findStudent(bean, request);
        return findRegistration(student, bean, request);

    }

    public void fillAllInfo(Collection<AbstractScholarshipStudentBean> scholarshipStudentBeans,
            ScholarshipReportRequest request) {

        messages.clear();

        for (final AbstractScholarshipStudentBean bean : scholarshipStudentBeans) {

            Registration registration = getRegistrationByAbstractScholarshipStudentBean(bean, request);

            fillAllInfo(request, bean, registration);

        }
    }

    public void fillAllInfo(ScholarshipReportRequest request, final AbstractScholarshipStudentBean bean,
            Registration registration) {

        try {

            validateStudentNumber(bean, registration);
            checkPreconditions(bean, registration, request);
            final RegistrationHistoryReport currentYearRegistrationReport =
                    new RegistrationHistoryReportService().generateReport(registration, request.getExecutionYear());

            fillCommonInfo(bean, currentYearRegistrationReport, request);
            fillSpecificInfo(bean, currentYearRegistrationReport, request);

        } catch (FillScholarshipException e) {
            // ignore FillScholarshipException
        } finally {
            bean.setObservations(formatObservations(bean));
        }
    }

    private void validateStudentNumber(final AbstractScholarshipStudentBean bean, final Registration registration) {
        if (bean.getStudentNumber() != null && registration.getNumber().intValue() != bean.getStudentNumber().intValue()) {
            addWarning(bean, "O número de aluno indicado no ficheiro de entrada não corresponde ao número de aluno no sistema.");
        }
    }

    private void checkPreconditions(AbstractScholarshipStudentBean bean, Registration registration,
            ScholarshipReportRequest request) {

        if (registration.getEnrolments(request.getExecutionYear()).isEmpty()) {
            addWarning(bean,
                    "A matrícula não tem inscrições para o ano lectivo " + request.getExecutionYear().getQualifiedName() + ".");
        }

        final RegistrationState lastRegistrationState = registration.getLastRegistrationState(request.getExecutionYear());
        if (lastRegistrationState != null && !lastRegistrationState.isActive()) {
            addWarning(bean, "A matrícula não está activa em " + request.getExecutionYear().getQualifiedName() + ".");
        }

        if (isInMobility(registration, request.getExecutionYear())) {
            addWarning(bean, "A matrícula está em mobilidade.");
        }

        if (isEnroledInStandaloneOnly(registration, request.getExecutionYear())) {

            final IngressionType ingression = getRootRegistration(registration).getStudentCandidacy().getIngressionType();
            addWarning(bean, "A matrícula apenas tem inscrições em isoladas (ingresso: "
                    + (ingression != null ? ingression.getDescription() : "n/a") + ").");
        }

        if (request.getFirstYearOfCycle() && !isFirstTimeInCycle(registration, request.getExecutionYear())) {
            addWarning(bean, "O aluno não é primeira vez.");
        }
    }

    private boolean isFirstTimeInCycle(Registration registration, ExecutionYear executionYear) {

        Predicate<Registration> hasSameDegreeType = r -> r.getDegreeType() == registration.getDegreeType();
        Predicate<Registration> differentRegistration = r -> r != registration;
        if (registration.getStudent().getRegistrationsSet().stream().filter(differentRegistration.and(hasSameDegreeType))
                .findAny().isPresent()) {
            return false;
        }

        return registration.getFirstEnrolmentExecutionYear() == executionYear;

    }

    private boolean isEnroledInStandaloneOnly(Registration registration, ExecutionYear executionYear) {

        final Collection<Enrolment> enrolments = registration.getEnrolments(executionYear);

        if (enrolments.isEmpty()) {
            return false;
        }

        for (final Enrolment enrolment : enrolments) {
            if (!enrolment.getCurriculumGroup().isStandalone()) {
                return false;
            }
        }

        return true;
    }

    protected boolean isInMobility(Registration registration, ExecutionYear executionYear) {

        for (final RegistrationState registrationState : registration.getRegistrationStates(executionYear)) {
            if (registrationState.getStateType() == RegistrationStateType.MOBILITY) {
                return true;
            }
        }

        //TODO wth?
//        for (final MobilityRegistrationInformation mobility : registration.getMobilityRegistrationInformationsSet()) {
//            final ExecutionSemester begin = mobility.getBegin();
//            final ExecutionSemester end = mobility.getEnd();
//
//            if (!executionYear.isBefore(begin.getExecutionYear())
//                    && (end == null || !executionYear.isAfter(end.getExecutionYear()))) {
//                return true;
//            }
//        }

        return false;
    }

    private void fillCommonInfo(AbstractScholarshipStudentBean bean, RegistrationHistoryReport currentYearRegistrationReport,
            ScholarshipReportRequest request) {

        final Registration registration = currentYearRegistrationReport.getRegistration();

        // replace the student number (provided by input file) by the system value
        bean.setStudentNumber(registration.getNumber());

        bean.setNumberOfMonthsExecutionYear(SocialServicesConfiguration.getInstance().getNumberOfMonthsOfAcademicYear());
        bean.setGratuityAmount(calculateGratuityAmount(registration, request));
        bean.setFirstMonthExecutionYear(calculateFirstMonthOfExecutionYear(request));

        bean.setCetQualificationOwner(isCETQualificationOwner(registration));
        bean.setCtspQualificationOwner(isCTSPQualificationOwner(registration));
        bean.setDegreeQualificationOwner(isDegreeQualificationOwner(registration));
        bean.setMasterQualificationOwner(isMasterQualificationOwner(registration));
        bean.setPhdQualificationOwner(isPhdQualificationOwner(registration));

        // add warning if person is enrolled in a degree that already exists as a completed qualification
        checkIfRegistrationDegreeIsCompleted(bean, registration);

        bean.setNumberOfDegreeCurricularYears(calculateNumberOfDegreeCurricularYears(bean, registration, request));

        bean.setRegime(calculateRegime(bean, currentYearRegistrationReport));
        bean.setRegistrationDate(currentYearRegistrationReport.getEnrolmentDate());
        bean.setRegistered(isRegistered(registration, request));
        bean.setNumberOfEnrolledECTS(currentYearRegistrationReport.getTotalEnroledCredits());

    }

    private void checkIfRegistrationDegreeIsCompleted(AbstractScholarshipStudentBean bean, Registration registration) {
        SchoolLevelTypeMapping schoolLevelTypeMapping = registration.getDegreeType().getSchoolLevelTypeMapping();
        SchoolLevelType schoolLevelType = schoolLevelTypeMapping == null ? null : schoolLevelTypeMapping.getSchoolLevel();
        if (bean.getCetQualificationOwner() && SchoolLevelTypeMapping.isCET(schoolLevelType)) {
            addWarning(bean, "O grau da qualificação concluida (CET) é igual ao grau que o aluno frequenta.");
        }

        if (bean.getCtspQualificationOwner() && SchoolLevelTypeMapping.isCTSP(schoolLevelType)) {
            // check if current registration degree is the same of completed qualification
            addWarning(bean, "O grau da qualificação concluida (CTSP) é igual ao grau que o aluno frequenta.");
        }

        if (bean.getDegreeQualificationOwner() && SchoolLevelTypeMapping.isDegree(schoolLevelType)) {
            // check if current registration degree is the same of completed qualification
            addWarning(bean, "O grau da qualificação concluida (licenciatura) é igual ao grau que o aluno frequenta.");
        }

        if (bean.getMasterQualificationOwner() && SchoolLevelTypeMapping.isMasterDegree(schoolLevelType)) {
            // check if current registration degree is the same of completed qualification
            addWarning(bean, "O grau da qualificação concluida (mestrado) é igual ao grau que o aluno frequenta.");
        }

        if (bean.getPhdQualificationOwner() && SchoolLevelTypeMapping.isPhd(schoolLevelType)) {
            // check if current registration degree is the same of completed qualification
            addWarning(bean, "O grau da qualificação concluida (douturamento) é igual ao grau que o aluno frequenta.");
        }
    }

    private Boolean isRegistered(Registration registration, ScholarshipReportRequest request) {
        final RegistrationState stateInDate = registration.getStateInDate(new DateTime());

        return stateInDate != null && stateInDate.isActive() && !registration.getEnrolments(request.getExecutionYear()).isEmpty();
    }

    private Integer calculateNumberOfDegreeCurricularYears(AbstractScholarshipStudentBean bean, Registration registration,
            ScholarshipReportRequest request) {
        final StudentCurricularPlan studentCurricularPlan =
                findStudentCurricularPlan(bean, registration, request.getExecutionYear());
        final float weight = studentCurricularPlan.getDegreeCurricularPlan().getDegreeStructure().getAcademicPeriod().getWeight();
        return (int) Math.ceil(weight);
    }

    private String calculateRegime(AbstractScholarshipStudentBean bean, RegistrationHistoryReport registrationHistoryReport) {

        final boolean partialRegime = registrationHistoryReport.getRegimeType() == RegistrationRegimeType.PARTIAL_TIME;
        final boolean workingStudent = registrationHistoryReport.isWorkingStudent();

        //TODO: not supported
        final boolean professionalInternship = false;

        if (professionalInternship) {
            return REGIME_PROFESSIONAL_INTERNSHIP;
        }

        if (partialRegime) {

            if (workingStudent) {
                return REGIME_PARTIAL_TIME_WORKING_STUDENT;
            }

            return REGIME_PARTIAL_TIME;
        }

        if (workingStudent) {
            return REGIME_FULL_TIME_WORKING_STUDENT;
        }

        return REGIME_FULL_TIME;

    }

    private Boolean isPhdQualificationOwner(Registration registration) {
        return getPersonSchoolLevelTypes(registration.getPerson()).stream().filter(SchoolLevelTypeMapping::isPhd).findAny()
                .isPresent();
    }

    private Boolean isMasterQualificationOwner(Registration registration) {
        return getPersonSchoolLevelTypes(registration.getPerson()).stream().filter(SchoolLevelTypeMapping::isMasterDegree)
                .findAny().isPresent();
    }

    private Boolean isDegreeQualificationOwner(Registration registration) {
        return getPersonSchoolLevelTypes(registration.getPerson()).stream().filter(SchoolLevelTypeMapping::isDegree).findAny()
                .isPresent();
    }

    private Boolean isCETQualificationOwner(Registration registration) {
        return getPersonSchoolLevelTypes(registration.getPerson()).stream().filter(SchoolLevelTypeMapping::isCET).findAny()
                .isPresent();
    }

    private Boolean isCTSPQualificationOwner(Registration registration) {
        return getPersonSchoolLevelTypes(registration.getPerson()).stream().filter(SchoolLevelTypeMapping::isCTSP).findAny()
                .isPresent();
    }

    private Collection<SchoolLevelType> getPersonSchoolLevelTypes(final Person person) {
        final Set<SchoolLevelType> result = Sets.newHashSet();
        result.addAll(getCompletedQualificationsSchoolLevelTypes(person));
        result.addAll(getCompletedRegistrationSchoolLevelTypes(person.getStudent()));

        return result;

    }

    private Collection<SchoolLevelType> getCompletedRegistrationSchoolLevelTypes(final Student student) {
        final Set<SchoolLevelType> result = Sets.newHashSet();
        for (final Registration registration : student.getRegistrationsSet()) {

            //TODO: find cleaner solution
            if (registration.getDegreeType().isIntegratedMasterDegree()
                    && registration.hasConcludedCycle(CycleType.FIRST_CYCLE)) {
                result.add(SchoolLevelType.DEGREE);
            }

            if (registration.isConcluded() || registration.hasConcluded()) {
                final SchoolLevelTypeMapping schoolLevelTypeMapping = registration.getDegreeType().getSchoolLevelTypeMapping();
                if (schoolLevelTypeMapping != null) {
                    result.add(schoolLevelTypeMapping.getSchoolLevel());
                }
            }

        }

        return result;
    }

    private Collection<SchoolLevelType> getCompletedQualificationsSchoolLevelTypes(final Person person) {
        return person.getStudent().getPersonalIngressionsDataSet().stream()
                .flatMap(x -> x.getPrecedentDegreesInformationsSet().stream()).map(x -> x.getSchoolLevel())
                .collect(Collectors.toSet());
    }

    protected StudentCurricularPlan findStudentCurricularPlan(AbstractScholarshipStudentBean bean, Registration registration,
            ExecutionYear executionYear) {
        final StudentCurricularPlan studentCurricularPlan = registration.getStudentCurricularPlan(executionYear);

        if (studentCurricularPlan == null) {
            addError(bean, "Não foi possível encontrar o plano curricular para o ano lectivo fornecido.");
        }

        return studentCurricularPlan;
    }

    private String calculateFirstMonthOfExecutionYear(ScholarshipReportRequest request) {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, request.getExecutionYear().getBeginLocalDate().getMonthOfYear() - 1);

        return cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.forLanguageTag("pt-PT"));
    }

    private BigDecimal calculateGratuityAmount(Registration registration, ScholarshipReportRequest request) {

        IAcademicTreasuryEvent tuitionForRegistrationTreasuryEvent = TreasuryBridgeAPIFactory.implementation()
                .getTuitionForRegistrationTreasuryEvent(registration, request.getExecutionYear());

        if (tuitionForRegistrationTreasuryEvent == null) {
            return BigDecimal.ZERO;
        }

        return tuitionForRegistrationTreasuryEvent.getAmountToPay()
                .subtract(tuitionForRegistrationTreasuryEvent.getInterestsAmountToPay());
    }

    public String formatObservations(final AbstractScholarshipStudentBean bean) {
        if (!messages.containsKey(bean)) {
            return "";
        }

        return messages.get(bean).stream().collect(Collectors.joining("\n"));
    }

    private Registration findRegistration(Student student, AbstractScholarshipStudentBean bean,
            ScholarshipReportRequest request) {

        final Collection<Degree> degrees = findDegree(bean);

        // TODO: erasmus dismissal should also be considered
        final Predicate<Registration> hasActiveEnrolments =
                r -> r.getEnrolments(request.getExecutionYear()).stream().anyMatch(e -> !e.isAnnulled());

        final Set<Registration> registrations = Sets.newHashSet();
        for (final Degree degree : degrees) {
            registrations
                    .addAll(student.getRegistrationsFor(degree).stream().filter(hasActiveEnrolments).collect(Collectors.toSet()));
        }

        if (registrations.size() == 1) {
            return registrations.iterator().next();

        } else if (registrations.size() > 1) {
            addError(bean, "Múltiplas matrículas para o mesmo curso activas no mesmo ano.");
            throw new FillScholarshipException();
        } else {

            final Collection<DegreeType> possibleDegreeTypes =
                    degrees.stream().map(d -> d.getDegreeType()).collect(Collectors.toSet());
            final Predicate<Registration> degreeTypePredicate = r -> possibleDegreeTypes.contains(r.getDegreeType());
            final Collection<Registration> registrationsWithActiveEnrolments = student.getRegistrationsSet().stream()
                    .filter(hasActiveEnrolments.and(degreeTypePredicate)).collect(Collectors.toSet());

            if (registrationsWithActiveEnrolments.size() == 1) {
                final Registration registration = registrationsWithActiveEnrolments.iterator().next();
                addWarning(bean,
                        "O curso indicado no ficheiro não coincide com o curso onde o aluno tem inscrições no ano lectivo do inquérito. Seleccionado o curso "
                                + registration.getDegree().getCode());
                return registration;
            } else if (registrationsWithActiveEnrolments.size() > 1) {
                addError(bean,
                        "Não foi encontrada a matrícula para o curso indicado no ficheiro e não foi possível determinar a matrícula porque existe mais do que uma com inscrições no ano lectivo do inquérito.");
            } else {
                addError(bean,
                        "Não foi possível encontrar a matrícula para o curso indicado no ficheiro ou o aluno não se encontra inscrito no corrente ano letivo.");
            }

            throw new FillScholarshipException();
        }

    }

    private Collection<Degree> findDegree(AbstractScholarshipStudentBean bean) {
        final Collection<Degree> degrees = Bennu.getInstance().getDegreesSet().stream()
                .filter(d -> Objects.equals(d.getMinistryCode(), bean.getDegreeCode())).collect(Collectors.toSet());

        if (degrees.isEmpty()) {
            addError(bean, "Não foi possível encontrar o curso.");
            throw new FillScholarshipException();
        }

        return degrees;
    }

    private Student findStudent(AbstractScholarshipStudentBean bean, ScholarshipReportRequest request) {

        final Person person = findPerson(bean, request);
        if (person == null) {
            addError(bean, "Não foi possível encontrar a pessoa.");
            throw new FillScholarshipException();
        }

        if (person.getStudent() == null) {
            addError(bean, "A pessoa encontrada não é um aluno.");
            throw new FillScholarshipException();
        }

        if (bean.getStudentNumber() == null) {
            addWarning(bean,
                    "Não foi possível verificar o número de aluno com o do sistema (o campo encontrava-se vazio no Excel, mas foi preenchido).");
        }

        return person.getStudent();

    }

    protected Person findPerson(AbstractScholarshipStudentBean bean, ScholarshipReportRequest request) {

        final Collection<Person> withDocumentId = Person.readByDocumentIdNumber(bean.getDocumentNumber());

        if (withDocumentId.size() == 1) {
            return ensureDocumentIdType(withDocumentId.iterator().next(), bean);

        } else if (withDocumentId.size() > 1) {
            addWarning(bean, "Encontradas múltiplas pessoas com o mesmo documento de identificação.");
            return findPersonByName(withDocumentId, bean);
        } else {
            // try partial id document number and with the student number and name

            if (bean.getDocumentNumber().length() != 0) {

                // try document id without check digit
                final String documentIdWithoutCheckDigit =
                        bean.getDocumentNumber().substring(0, bean.getDocumentNumber().length() - 1);

                final Collection<Person> withPartialDocumentId = Person.readByDocumentIdNumber(documentIdWithoutCheckDigit);

                if (withPartialDocumentId.size() == 1) {
                    if (bean.getDocumentBINumber() == null || !bean.getDocumentBINumber().equals(documentIdWithoutCheckDigit)) {
                        addWarning(bean,
                                "O número de BI indicado na última coluna não corresponde ao número de documento (sem o dígito de controlo) de identificação.");
                    }

                    return ensureDocumentIdType(withPartialDocumentId.iterator().next(), bean);
                }

                if (withPartialDocumentId.size() > 1) {
                    if (bean.getDocumentBINumber() == null || !bean.getDocumentBINumber().equals(documentIdWithoutCheckDigit)) {
                        addWarning(bean,
                                "O número de BI indicado na última coluna não corresponde ao número de documento (sem o dígito de controlo) de identificação.");
                    }

                    addWarning(bean,
                            "Encontradas múltiplas pessoas com o mesmo número de documento (sem o dígito de controlo) de identificação.");
                    return findPersonByName(withPartialDocumentId, bean);
                }

                // try document id without check digit and without citizen card serial
                final String documentIdWithoutCitizenCardSerial =
                        bean.getDocumentNumber().substring(0, bean.getDocumentNumber().length() - 4);

                final Collection<Person> withPartialDocumentIdWithoutCCSerial =
                        Person.readByDocumentIdNumber(documentIdWithoutCitizenCardSerial);

                if (withPartialDocumentIdWithoutCCSerial.size() == 1) {
                    if (bean.getDocumentBINumber() == null
                            || !bean.getDocumentBINumber().equals(documentIdWithoutCitizenCardSerial)) {
                        addWarning(bean,
                                "O número de BI indicado na última coluna não corresponde ao número de documento (sem o dígito de controlo e sem o número de série) de identificação.");
                    }
                    return ensureDocumentIdType(withPartialDocumentIdWithoutCCSerial.iterator().next(), bean);
                }

                if (withPartialDocumentIdWithoutCCSerial.size() > 1) {
                    if (bean.getDocumentBINumber() == null
                            || !bean.getDocumentBINumber().equals(documentIdWithoutCitizenCardSerial)) {
                        addWarning(bean,
                                "O número de BI indicado na última coluna não corresponde ao número de documento (sem o dígito de controlo e sem o número de série) de identificação.");
                    }
                    addWarning(bean,
                            "Encontradas múltiplas pessoas com o mesmo número de documento (sem o dígito de controlo e sem o número de série) de identificação.");
                    return findPersonByName(withPartialDocumentIdWithoutCCSerial, bean);
                }

                // try with student name and student number
                final Collection<Person> studentsWithSameName =
                        Person.readPersonsByNameAndRoleType(bean.getStudentName(), RoleType.STUDENT);
                for (Person person : studentsWithSameName) {
                    Registration findRegistration = findRegistration(person.getStudent(), bean, request);
                    if (findRegistration.getNumber().equals(bean.getStudentNumber())) {
                        addWarning(bean,
                                "Não foi possível encontrar o aluno usando o documento de identificação, no entanto o nome e número de aluno correspondem aos do sistema.");
                        return person;
                    }
                }
            }

            return null;
        }

    }

    private Person findPersonByName(Collection<Person> toCheck, AbstractScholarshipStudentBean bean) {

        for (final Person person : toCheck) {
            if (person.getName().equalsIgnoreCase(bean.getStudentName())) {
                return ensureDocumentIdType(person, bean);
            }
        }

        return null;

    }

    private Person ensureDocumentIdType(final Person person, final AbstractScholarshipStudentBean bean) {

        if (person.getIdDocumentType() != ID_DOCUMENT_TYPE_MAPPING.get(bean.getDocumentTypeName())
                && !person.getIdDocumentType().name().equalsIgnoreCase(bean.getDocumentTypeName())) {
            addError(bean, "O tipo de documento não corresponde com o tipo definido no sistema.");
            throw new FillScholarshipException();
        }

        return person;

    }

    protected void fillSpecificInfo(AbstractScholarshipStudentBean bean, RegistrationHistoryReport currentYearRegistrationReport,
            ScholarshipReportRequest request) {
        //nothing to be done
    }

    protected void addError(AbstractScholarshipStudentBean bean, String message) {
        messages.put(bean, ERROR_OBSERVATION + ": " + message);
    }

    protected void addWarning(AbstractScholarshipStudentBean bean, String message) {
        messages.put(bean, WARNING_OBSERVATION + ": " + message);
    }

    // TODO Methods should be inserted into Registration@academic
    protected Registration getRootRegistration(Registration registration) {
        final SortedSet<Registration> registrations = Sets.newTreeSet(Registration.COMPARATOR_BY_START_DATE);
        registrations.add(registration);
        registrations.addAll(getPrecedentDegreeRegistrations(registration));

        return registrations.first();
    }

    public static Collection<Registration> getPrecedentDegreeRegistrations(Registration registration) {

        final Set<Degree> precedentDegreesUntilRoot = getPrecedentDegreesUntilRoot(registration.getDegree());
        final Set<Registration> result = Sets.newHashSet();
        for (final Registration iteratedRegistration : registration.getStudent().getRegistrationsSet()) {

            if (registration == iteratedRegistration) {
                continue;
            }

            if (iteratedRegistration.isCanceled() || iteratedRegistration.isConcluded() || iteratedRegistration.hasConcluded()) {
                continue;
            }

            if (precedentDegreesUntilRoot.contains(iteratedRegistration.getDegree())) {
                result.add(iteratedRegistration);
            }

        }

        return result;
    }

    static public Set<ExecutionYear> getExecutionYears(final Registration registration,
            final Function<Registration, Stream<ExecutionYear>> mapper, final Predicate<ExecutionYear> predicate) {

        final Collection<Registration> toInspect = getPrecedentDegreeRegistrations(registration);
        toInspect.add(registration);

        return toInspect.stream().flatMap(mapper).filter(predicate).collect(Collectors.toSet());
    }

    // TODO Method should be inserted in Degree@academic
    protected static Set<Degree> getPrecedentDegreesUntilRoot(Degree degree) {
        //TODO IMPLEMENT
        return Collections.EMPTY_SET;
    }

}