package org.fenixedu.ulisboa.integration.sas.service.process;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.SchoolLevelType;
import org.fenixedu.academic.domain.StudentCurricularPlan;
import org.fenixedu.academic.domain.degree.DegreeType;
import org.fenixedu.academic.domain.degreeStructure.CycleType;
import org.fenixedu.academic.domain.person.IDDocumentType;
import org.fenixedu.academic.domain.person.RoleType;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.academic.domain.student.RegistrationRegimeType;
import org.fenixedu.academic.domain.student.Student;
import org.fenixedu.academic.domain.student.registrationStates.RegistrationState;
import org.fenixedu.academic.domain.studentCurriculum.CurriculumLine;
import org.fenixedu.academic.domain.treasury.IAcademicTreasuryEvent;
import org.fenixedu.academic.domain.treasury.TreasuryBridgeAPIFactory;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.ulisboa.integration.sas.domain.SchoolLevelTypeMapping;
import org.fenixedu.ulisboa.integration.sas.domain.SocialServicesConfiguration;
import org.fenixedu.ulisboa.integration.sas.dto.AbstractScholarshipStudentBean;
import org.fenixedu.ulisboa.specifications.domain.services.RegistrationServices;
import org.fenixedu.ulisboa.specifications.domain.services.statute.StatuteServices;
import org.fenixedu.ulisboa.specifications.domain.studentCurriculum.CreditsReasonType;
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
        ID_DOCUMENT_TYPE_MAPPING.put("Outros", IDDocumentType.OTHER);

    }

    public void fillAllInfo(Collection<AbstractScholarshipStudentBean> scholarshipStudentBeans, ExecutionYear requestYear,
            boolean firstYearOfCycle) {

        messages.clear();

        for (final AbstractScholarshipStudentBean bean : scholarshipStudentBeans) {
            try {
                fillAllInfo(bean, getRegistrationByAbstractScholarshipStudentBean(bean, requestYear), requestYear,
                        firstYearOfCycle);
            } catch (FillScholarshipException e) {
                //ignore FillScholarshipException
                addError(bean, e.getMessage());
                bean.setObservations(formatObservations(bean));
            }
        }
    }

    public Registration getRegistrationByAbstractScholarshipStudentBean(AbstractScholarshipStudentBean bean,
            ExecutionYear requestYear) {
        return findRegistration(findStudent(bean, requestYear), bean, requestYear);
    }

    private Registration findRegistration(Student student, AbstractScholarshipStudentBean bean, ExecutionYear requestYear) {

        final Collection<Degree> degrees = findDegree(bean);

        // TODO: erasmus dismissal should also be considered
        final Predicate<Registration> hasActiveEnrolments =
                r -> r.getEnrolments(requestYear).stream().anyMatch(e -> !e.isAnnulled());

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

    private Student findStudent(AbstractScholarshipStudentBean bean, ExecutionYear requestYear) {

        final Person person = findPerson(bean, requestYear);
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

    protected Person findPerson(AbstractScholarshipStudentBean bean, ExecutionYear requestYear) {

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
                    Registration findRegistration = findRegistration(person.getStudent(), bean, requestYear);
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

    public void fillAllInfo(final AbstractScholarshipStudentBean bean, Registration registration, ExecutionYear requestYear,
            boolean firstYearOfCycle) {

        try {

            validateStudentNumber(bean, registration);
            checkPreconditions(bean, registration, requestYear, firstYearOfCycle);

            fillCommonInfo(bean, registration, requestYear);
            fillSpecificInfo(bean, registration, requestYear);

        } catch (FillScholarshipException e) {
            // ignore FillScholarshipException
            addError(bean, e.getMessage());
        } finally {
            bean.setObservations(formatObservations(bean));
        }
    }

    private void validateStudentNumber(final AbstractScholarshipStudentBean bean, final Registration registration) {
        if (bean.getStudentNumber() != null && registration.getNumber().intValue() != bean.getStudentNumber().intValue()) {
            addWarning(bean, "O número de aluno indicado no ficheiro de entrada não corresponde ao número de aluno no sistema.");
        }
    }

    private void checkPreconditions(AbstractScholarshipStudentBean bean, Registration registration, ExecutionYear requestYear,
            boolean firstYearOfCycle) {

        if (!hasNormalEnrolments(registration, requestYear)) {
            addError(bean, "A matrícula não tem inscrições para o ano lectivo " + requestYear.getQualifiedName() + ".");

            throw new FillScholarshipException();
        }

        final RegistrationState lastRegistrationState = registration.getLastRegistrationState(requestYear);
        if (lastRegistrationState != null && !lastRegistrationState.isActive()) {
            addWarning(bean, "A matrícula não está activa em " + requestYear.getQualifiedName() + ".");
        }

        if (firstYearOfCycle && !isFirstTimeInCycle(registration, requestYear)) {
            addWarning(bean, "O aluno não é primeira vez.");
        }
    }

    private boolean hasNormalEnrolments(Registration registration, ExecutionYear executionYear) {
        return registration.getEnrolments(executionYear).stream()
                .anyMatch(e -> !e.getCurriculumGroup().isNoCourseGroupCurriculumGroup());
    }

    static public boolean isFirstTimeInCycle(Registration registration, ExecutionYear requestYear) {
        final List<Registration> cycleRegistrations = getCycleRegistrations(registration);
        final List<ExecutionYear> cycleEnrolmentYears = getCycleEnrolmentYears(registration, requestYear);

        return cycleRegistrations.size() > 1 ? false : cycleEnrolmentYears.size() == 1
                && cycleEnrolmentYears.iterator().next() == requestYear;
    }

    private void fillCommonInfo(AbstractScholarshipStudentBean bean, Registration registration, ExecutionYear requestYear) {

        bean.setGratuityAmount(getTuitionAmount(registration, requestYear));
        bean.setNumberOfMonthsExecutionYear(SocialServicesConfiguration.getInstance().getNumberOfMonthsOfAcademicYear());
        bean.setFirstMonthExecutionYear(getFirstMonthOfExecutionYear(requestYear));

        // replace the student number (provided by input file) by the system value
        bean.setStudentNumber(registration.getNumber());

        bean.setRegime(getRegime(bean, registration, requestYear));

        bean.setEnroled(isEnroled(registration, requestYear));
        bean.setEnrolmentDate(RegistrationServices.getEnrolmentDate(registration, requestYear));

        bean.setNumberOfEnrolledECTS(getEnroledCredits(registration, requestYear));

        bean.setCetQualificationOwner(isCETQualificationOwner(registration));
        bean.setCtspQualificationOwner(isCTSPQualificationOwner(registration));
        bean.setDegreeQualificationOwner(isDegreeQualificationOwner(registration));
        bean.setMasterQualificationOwner(isMasterQualificationOwner(registration));
        bean.setPhdQualificationOwner(isPhdQualificationOwner(registration));

        // add warning if person is enrolled in a degree that already exists as a completed qualification
        checkIfRegistrationDegreeIsCompleted(bean, registration);

        bean.setCycleIngressionYear(getCycleIngressionYear(bean, registration));
        bean.setCycleNumberOfEnrolmentsYears(getCycleEnrolmentYears(registration, requestYear).size());

        bean.setNumberOfDegreeCurricularYears(getNumberOfDegreeCurricularYears(registration, requestYear));
    }

    private Boolean isEnroled(Registration registration, ExecutionYear requestYear) {
        final RegistrationState stateInDate = registration.getStateInDate(new DateTime());
        return stateInDate != null && stateInDate.isActive() && !getEnroledCurriculumLines(registration, requestYear).isEmpty();
    }

    private Integer getNumberOfDegreeCurricularYears(Registration registration, ExecutionYear requestYear) {
        return getStudentCurricularPlan(registration, requestYear).getDegreeCurricularPlan().getDurationInYears();
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

    private BigDecimal getTuitionAmount(Registration registration, ExecutionYear requestYear) {

        final IAcademicTreasuryEvent treasuryEvent =
                TreasuryBridgeAPIFactory.implementation().getTuitionForRegistrationTreasuryEvent(registration, requestYear);
        if (treasuryEvent == null) {
            return BigDecimal.ZERO;
        }

        return treasuryEvent.getAmountToPay().subtract(treasuryEvent.getInterestsAmountToPay());
    }

    private Integer getFirstMonthOfExecutionYear(ExecutionYear requestYear) {
        return requestYear.getBeginLocalDate().getMonthOfYear();
    }

    private String getRegime(AbstractScholarshipStudentBean bean, Registration registration, ExecutionYear requestYear) {

        final boolean partialRegime = registration.getRegimeType(requestYear) == RegistrationRegimeType.PARTIAL_TIME;

        final boolean workingStudent =
                StatuteServices.findStatuteTypes(registration, requestYear).stream().anyMatch(s -> s.isWorkingStudentStatute());

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

    static public Integer getCycleIngressionYear(AbstractScholarshipStudentBean bean, Registration registration) {

        final Registration cycleFirstRegistration = getCycleRegistrations(registration).iterator().next();
        final Integer cycleIngressionYear = cycleFirstRegistration.getStartExecutionYear().getBeginDateYearMonthDay().getYear();

        if (bean.getCycleIngressionYear() != null && !bean.getCycleIngressionYear().equals(cycleIngressionYear)) {
            String message = "o ano de ingresso no ciclo de estudos declarado no ficheiro (" + bean.getCycleIngressionYear()
                    + ") não corresponde ao ano de início do sistema (" + cycleIngressionYear + ").";
            //TODO:
           // addWarning(bean, message);
        }

        return cycleIngressionYear;
    }

    static protected List<ExecutionYear> getCycleEnrolmentYears(Registration registration, ExecutionYear requestYear) {
        return getCycleRegistrations(registration).stream().flatMap(r -> RegistrationServices.getEnrolmentYears(r).stream())
                .distinct().filter(ey -> ey.isBeforeOrEquals(requestYear)).sorted(ExecutionYear.COMPARATOR_BY_BEGIN_DATE)
                .collect(Collectors.toList());
    }

    static protected List<Registration> getCycleRegistrations(final Registration registration) {
        return registration.getStudent().getRegistrationsByDegreeTypes(registration.getDegreeType()).stream()
                .filter(r -> r.getStartExecutionYear().isBeforeOrEquals(registration.getStartExecutionYear()))
                .flatMap(r -> Stream.concat(Stream.of(r), Stream.of(RegistrationServices.getRootRegistration(r)))).distinct()
                .sorted(Registration.COMPARATOR_BY_START_DATE).collect(Collectors.toList());
    }

    public String formatObservations(final AbstractScholarshipStudentBean bean) {
        if (!messages.containsKey(bean)) {
            return "";
        }

        return messages.get(bean).stream().collect(Collectors.joining("\n"));
    }

    protected void addError(AbstractScholarshipStudentBean bean, String message) {
        messages.put(bean, ERROR_OBSERVATION + ": " + message);
    }

    protected void addWarning(AbstractScholarshipStudentBean bean, String message) {
        messages.put(bean, WARNING_OBSERVATION + ": " + message);
    }

    private StudentCurricularPlan getStudentCurricularPlan(Registration registration, ExecutionYear executionYear) {
        final StudentCurricularPlan studentCurricularPlan =
                RegistrationServices.getStudentCurricularPlan(registration, executionYear);
        if (studentCurricularPlan == null) {
            throw new FillScholarshipException("Plano curricular não encontrado no ano lectivo");
        }

        return studentCurricularPlan;
    }

    protected BigDecimal getEnroledCredits(Registration registration, ExecutionYear executionYear) {
        return sumCredits(getEnroledCurriculumLines(registration, executionYear).stream());
    }

    protected BigDecimal getApprovedCredits(Registration registration, ExecutionYear executionYear) {
        return sumCredits(getEnroledCurriculumLines(registration, executionYear).stream().filter(l -> l.isApproved()));
    }

    private Set<CurriculumLine> getEnroledCurriculumLines(Registration registration, ExecutionYear executionYear) {
        return RegistrationServices.getNormalEnroledCurriculumLines(registration, executionYear, true,
                getDismissalTypesToConsider());
    }

    private Set<CreditsReasonType> getDismissalTypesToConsider() {
        return Bennu.getInstance().getSocialServicesConfiguration().getCreditsReasonTypesSet();
    }

    private BigDecimal sumCredits(final Stream<CurriculumLine> linesStream) {
        return linesStream.map(line -> line.getEctsCreditsForCurriculum()).reduce((x, y) -> x.add(y)).orElse(BigDecimal.ZERO);
    }

    private Boolean isCETQualificationOwner(Registration registration) {
        return getPersonSchoolLevelTypes(registration.getPerson()).stream().filter(SchoolLevelTypeMapping::isCET).findAny()
                .isPresent();
    }

    private Boolean isCTSPQualificationOwner(Registration registration) {
        return getPersonSchoolLevelTypes(registration.getPerson()).stream().filter(SchoolLevelTypeMapping::isCTSP).findAny()
                .isPresent();
    }

    private Boolean isDegreeQualificationOwner(Registration registration) {
        return getPersonSchoolLevelTypes(registration.getPerson()).stream().filter(SchoolLevelTypeMapping::isDegree).findAny()
                .isPresent();
    }

    private Boolean isMasterQualificationOwner(Registration registration) {
        return getPersonSchoolLevelTypes(registration.getPerson()).stream().filter(SchoolLevelTypeMapping::isMasterDegree)
                .findAny().isPresent();
    }

    private Boolean isPhdQualificationOwner(Registration registration) {
        return getPersonSchoolLevelTypes(registration.getPerson()).stream().filter(SchoolLevelTypeMapping::isPhd).findAny()
                .isPresent();
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

    protected void fillSpecificInfo(AbstractScholarshipStudentBean bean, Registration registration, ExecutionYear requestYear) {
        //nothing to be done
    }

}