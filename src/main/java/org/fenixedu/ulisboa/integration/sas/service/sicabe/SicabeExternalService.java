package org.fenixedu.ulisboa.integration.sas.service.sicabe;

import java.util.List;
import java.util.Set;

import javax.xml.ws.BindingProvider;

import org.apache.commons.lang.math.NumberUtils;
import org.datacontract.schemas._2004._07.sicabe_contracts.AlterarDadosAcademicosContratualizacaoRequest;
import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.exceptions.DomainException;
import org.fenixedu.academic.domain.person.IDDocumentType;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.ulisboa.integration.sas.domain.CandidacyState;
import org.fenixedu.ulisboa.integration.sas.domain.SasScholarshipCandidacy;
import org.fenixedu.ulisboa.integration.sas.domain.SasScholarshipCandidacyState;
import org.fenixedu.ulisboa.integration.sas.domain.SasScholarshipData;
import org.fenixedu.ulisboa.integration.sas.domain.SasScholarshipDataChangeLog;
import org.fenixedu.ulisboa.integration.sas.domain.ScholarshipReportRequest;
import org.fenixedu.ulisboa.integration.sas.dto.AbstractScholarshipStudentBean;
import org.fenixedu.ulisboa.integration.sas.dto.ScholarshipStudentFirstYearBean;
import org.fenixedu.ulisboa.integration.sas.dto.ScholarshipStudentOtherYearBean;
import org.fenixedu.ulisboa.integration.sas.service.process.AbstractFillScholarshipService;
import org.fenixedu.ulisboa.integration.sas.service.process.FillScholarshipException;
import org.fenixedu.ulisboa.integration.sas.service.process.FillScholarshipFirstYearService;
import org.fenixedu.ulisboa.integration.sas.service.process.FillScholarshipServiceOtherYearService;
import org.fenixedu.ulisboa.specifications.domain.services.RegistrationServices;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import com.qubit.solution.fenixedu.bennu.webservices.services.client.BennuWebServiceClient;

import pt.dges.schemas.data.sicabe.v1.AlterarCursoInsituicaoRequest;
import pt.dges.schemas.data.sicabe.v1.AlterarDadosAcademicosPrimeiraVezRequest;
import pt.dges.schemas.data.sicabe.v1.AlterarDadosAcademicosRestantesCasosRequest;
import pt.dges.schemas.data.sicabe.v1.CandidaturaSubmetida;
import pt.dges.schemas.data.sicabe.v1.IdentificadorCandidatura;
import pt.dges.schemas.data.sicabe.v1.ObjectFactory;
import pt.dges.schemas.data.sicabe.v1.ObterCandidaturasSubmetidasRequest;
import pt.dges.schemas.data.sicabe.v1.ObterCandidaturasSubmetidasResponse;
import pt.dges.schemas.data.sicabe.v1.ObterEstadoCandidaturaRequest;
import pt.dges.schemas.data.sicabe.v1.RegistarMatriculaAlunoRequest;
import pt.dges.schemas.data.sicabe.v1.ResultadoEstadoCandidatura;
import pt.dges.schemas.data.sicabe.v1.TipoDocumentoIdentificacao;
import pt.dges.schemas.services.sicabe.v1.DadosAcademicos;
import pt.dges.schemas.services.sicabe.v1.DadosAcademicosObterCandidaturasSubmetidasSicabeBusinessMessageFaultFaultMessage;
import pt.dges.schemas.services.sicabe.v1.DadosAcademicosObterCandidaturasSubmetidasSicabeErrorMessageFaultFaultMessage;
import pt.dges.schemas.services.sicabe.v1.DadosAcademicosObterCandidaturasSubmetidasSicabeValidationMessageFaultFaultMessage;
import pt.dges.schemas.services.sicabe.v1.DadosAcademicos_Service;
import pt.ist.fenixframework.Atomic;

public class SicabeExternalService extends BennuWebServiceClient<DadosAcademicos> {

    public SicabeExternalService() {
    }

    @Atomic
    public static void init() {
        new SicabeExternalService();
    }

    @Override
    protected BindingProvider getService() {
        return (BindingProvider) new DadosAcademicos_Service().getCustomBindingDadosAcademicos();
    }

    @Atomic
    public void fillAllSasScholarshipCandidacies(ExecutionYear executionYear, int instituitionCode) {
        final ObterCandidaturasSubmetidasRequest parameters = new ObterCandidaturasSubmetidasRequest();
        parameters.setAnoLectivo(executionYear.getBeginCivilYear());

        try {
            final ObterCandidaturasSubmetidasResponse obterCandidaturasSubmetidas =
                    getClient().obterCandidaturasSubmetidas(parameters);
            obterCandidaturasSubmetidas.getCandidaturas().getCandidaturaSubmetida().stream()
                    .filter(c -> c.getCodigoInstituicaoEnsino() == instituitionCode).forEach(c -> {

                        updateOrCreateSasScholarshipCandidacy(c, executionYear);

                    });
        } catch (DadosAcademicosObterCandidaturasSubmetidasSicabeBusinessMessageFaultFaultMessage e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DadosAcademicosObterCandidaturasSubmetidasSicabeErrorMessageFaultFaultMessage e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DadosAcademicosObterCandidaturasSubmetidasSicabeValidationMessageFaultFaultMessage e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Atomic
    public void fillSasScholarshipCandidacies(List<SasScholarshipCandidacy> list2Update, ExecutionYear executionYear,
            int instituitionCode) {
        final ObterCandidaturasSubmetidasRequest parameters = new ObterCandidaturasSubmetidasRequest();
        parameters.setAnoLectivo(executionYear.getBeginCivilYear());

        Set<Integer> candidacyNumbersSet = Sets.newHashSet();
        list2Update.stream().forEach(s -> candidacyNumbersSet.add(s.getCandidacyNumber()));

        try {
            final ObterCandidaturasSubmetidasResponse obterCandidaturasSubmetidas =
                    getClient().obterCandidaturasSubmetidas(parameters);

            obterCandidaturasSubmetidas.getCandidaturas().getCandidaturaSubmetida().stream()
                    .filter(c -> c.getCodigoInstituicaoEnsino() == instituitionCode
                            && candidacyNumbersSet.contains(c.getNumeroCandidatura()))
                    .forEach(c -> {

                        updateOrCreateSasScholarshipCandidacy(c, executionYear);

                    });
        } catch (DadosAcademicosObterCandidaturasSubmetidasSicabeBusinessMessageFaultFaultMessage e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DadosAcademicosObterCandidaturasSubmetidasSicabeErrorMessageFaultFaultMessage e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DadosAcademicosObterCandidaturasSubmetidasSicabeValidationMessageFaultFaultMessage e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void updateOrCreateSasScholarshipCandidacy(CandidaturaSubmetida input, ExecutionYear executionYear) {

        System.out.println(input.getCodigoCurso() + "\t" + input.getCodigoInstituicaoEnsino() + "\t" + input.getNif() + "\t"
                + input.getNumeroDocumentoIdentificacao() + "\t" + input.getTipoDocumentoIdentificacao() + "\t"
                + input.getNumeroCandidatura() + "\t" + input.getEstadoCandidatura().getResultadoEstadoCandidatura().name());

        SasScholarshipCandidacy candidacy = SasScholarshipCandidacy.findAll().stream()
                .filter(c -> c.getExecutionYear() == executionYear && (c.getFiscalNumber().equalsIgnoreCase(input.getNif())
                        || (c.getDocIdType() == convertCandidacyDocumentType(input.getTipoDocumentoIdentificacao())
                                && c.getDocIdNumber().equalsIgnoreCase(input.getNumeroDocumentoIdentificacao()))))
                .findFirst().orElse(null);

        if (candidacy == null) {

            candidacy = new SasScholarshipCandidacy();
            fillCandidacyInfos(input, executionYear, true, candidacy);
            candidacy.setState(SasScholarshipCandidacyState.PENDING);

        } else if (!equalsDataBetweenCandidacyAndInput(candidacy, input)) {

            fillCandidacyInfos(input, executionYear, false, candidacy);
            candidacy.setState(SasScholarshipCandidacyState.MODIFIED);

        }

    }

    private boolean equalsDataBetweenCandidacyAndInput(SasScholarshipCandidacy candidacy, CandidaturaSubmetida input) {
        if (Objects.equal(candidacy.getDegreeCode(), input.getCodigoCurso())
                && Objects.equal(candidacy.getInstitutionCode(), input.getCodigoInstituicaoEnsino())
                && Objects.equal(candidacy.getInstitutionName(), input.getInstituicaoEnsino())
                && Objects.equal(candidacy.getDegreeName(), input.getCurso())
                && Objects.equal(candidacy.getFiscalNumber(), input.getNif()) &&

                Objects.equal((candidacy.getSubmissionDate() != null ? candidacy.getSubmissionDate().toString() : null),
                        (input.getDataSubmissao() != null ? new DateTime(input.getDataSubmissao().toGregorianCalendar().getTime())
                                .toString() : null))
                &&

                Objects.equal(candidacy.getTechnicianEmail(), input.getEmailTecnico().getValue()) &&

                Objects.equal((candidacy.getAssignmentDate() != null ? candidacy.getAssignmentDate().toString() : null),
                        (input.getEstadoCandidatura().getDataAtribuicao() != null ? new DateTime(
                                input.getEstadoCandidatura().getDataAtribuicao().toGregorianCalendar().getTime())
                                        .toString() : null))
                &&

                Objects.equal(candidacy.getDescription(), input.getEstadoCandidatura().getDescricao().getValue())
                && Objects.equal(candidacy.getGratuityAmount(), input.getEstadoCandidatura().getValorBolsa()) &&

                Objects.equal(candidacy.getCandidacyName(), input.getNomeCandidato().getValue())
                && Objects.equal(candidacy.getTechnicianName(), input.getNomeTecnico().getValue())
                && Objects.equal(candidacy.getStudentNumber(), input.getNumeroAluno().getValue())
                && Objects.equal(candidacy.getCandidacyNumber(), input.getNumeroCandidatura())
                && Objects.equal(candidacy.getDocIdNumber(), input.getNumeroDocumentoIdentificacao())
                && candidacy.getDocIdType() == convertCandidacyDocumentType(input.getTipoDocumentoIdentificacao()) &&

                Objects.equal(candidacy.getCetQualificationOwner(), input.getTitularidade().getValue().isTitularCET())
                && Objects.equal(candidacy.getCstpQualificationOwner(), input.getTitularidade().getValue().isTitularCSTP())
                && Objects.equal(candidacy.getPhdQualificationOwner(), input.getTitularidade().getValue().isTitularDoutoramento())
                && Objects.equal(candidacy.getDegreeQualificationOwner(),
                        input.getTitularidade().getValue().isTitularLicenciatura())
                && Objects.equal(candidacy.getMasterQualificationOwner(), input.getTitularidade().getValue().isTitularMestrado())

        ) {
            return true;
        }

        return false;

    }

    private void fillCandidacyInfos(CandidaturaSubmetida input, ExecutionYear executionYear, boolean newCandidacy,
            SasScholarshipCandidacy candidacy) {
        candidacy.setDegreeCode(input.getCodigoCurso());
        candidacy.setInstitutionCode(input.getCodigoInstituicaoEnsino());
        candidacy.setDegreeName(input.getCurso());
        candidacy.setSubmissionDate(
                input.getDataSubmissao() != null ? new DateTime(input.getDataSubmissao().toGregorianCalendar().getTime()) : null);
        candidacy.setTechnicianEmail(input.getEmailTecnico().getValue());

        //candidacy state
        candidacy.setAssignmentDate(input.getEstadoCandidatura().getDataAtribuicao() != null ? new DateTime(
                input.getEstadoCandidatura().getDataAtribuicao().toGregorianCalendar().getTime()) : null);
        candidacy.setDescription(input.getEstadoCandidatura().getDescricao().getValue());
        candidacy.setCandidacyState(convertCandidacyState(input.getEstadoCandidatura().getResultadoEstadoCandidatura()));
        candidacy.setGratuityAmount(input.getEstadoCandidatura().getValorBolsa());

        candidacy.setInstitutionName(input.getInstituicaoEnsino());
        candidacy.setFiscalNumber(input.getNif());
        candidacy.setCandidacyName(input.getNomeCandidato().getValue());
        candidacy.setTechnicianName(input.getNomeTecnico().getValue());
        candidacy.setStudentNumber(input.getNumeroAluno().getValue());
        candidacy.setCandidacyNumber(input.getNumeroCandidatura());
        candidacy.setDocIdNumber(input.getNumeroDocumentoIdentificacao());
        candidacy.setDocIdType(convertCandidacyDocumentType(input.getTipoDocumentoIdentificacao()));

        // Ownership
        candidacy.setCetQualificationOwner(input.getTitularidade().getValue().isTitularCET());
        candidacy.setCstpQualificationOwner(input.getTitularidade().getValue().isTitularCSTP());
        candidacy.setPhdQualificationOwner(input.getTitularidade().getValue().isTitularDoutoramento());

        candidacy.setDegreeQualificationOwner(input.getTitularidade().getValue().isTitularLicenciatura());
        candidacy.setMasterQualificationOwner(input.getTitularidade().getValue().isTitularMestrado());

        if (executionYear == null) {
            throw new DomainException("error.SicabeExternalService.ExecutionYear.not.found");
        }

        if (newCandidacy) {
            executionYear.addSasScholarshipCandidacies(candidacy);
        }

        // write log file
        SasScholarshipDataChangeLog log = new SasScholarshipDataChangeLog();
        log.setDate(new DateTime());
        log.setDescription(
                newCandidacy ? "Nova candidatura recolhida do SICABE." : "Atualização de candidatura a partir do SICABE.");
        log.setStudentName(candidacy.getCandidacyName());
        log.setStudentNumber(candidacy.getStudentNumber());
        candidacy.addSasScholarshipDataChangeLogs(log);

        fillCandidacyInfos(candidacy);

        candidacy.setImportDate(new DateTime());

    }

    private void fillCandidacyInfos(SasScholarshipCandidacy c) {

        final AbstractFillScholarshipService service = new FillScholarshipFirstYearService();

        ScholarshipReportRequest request = new ScholarshipReportRequest(c.getExecutionYear(), false, false, "/", "".getBytes());

        AbstractScholarshipStudentBean tempBean = new ScholarshipStudentFirstYearBean();
        tempBean.setStudentNumber(c.getStudentNumber() != null && NumberUtils.isNumber(c.getStudentNumber()) ? Integer
                .valueOf(c.getStudentNumber()) : null);
        tempBean.setFiscalCode(c.getFiscalNumber());
        tempBean.setStudentName(c.getCandidacyName());
        tempBean.setDegreeCode(c.getDegreeCode());
        tempBean.setDocumentNumber(c.getDocIdNumber());
        tempBean.setDocumentTypeName(c.getDocIdType().name());

        try {
            Registration registration = service.getRegistrationByAbstractScholarshipStudentBean(tempBean, request);
            if (registration != null) {
                c.setRegistration(registration);
                c.setFirstYear(c.getRegistration().getFirstEnrolmentExecutionYear() == c.getExecutionYear());
            }

        } catch (FillScholarshipException e) {
            SasScholarshipDataChangeLog log = new SasScholarshipDataChangeLog();

            log.setDate(new DateTime());
            log.setDescription(service.formatObservations(tempBean));
            log.setStudentName(c.getCandidacyName());
            log.setStudentNumber(c.getStudentNumber());

            c.addSasScholarshipDataChangeLogs(log);

            c.setState(SasScholarshipCandidacyState.PROCESSED_ERRORS);
            c.setStateDate(log.getDate());

        }
    }

    private IDDocumentType convertCandidacyDocumentType(TipoDocumentoIdentificacao idDocumentType) {
        if (idDocumentType == TipoDocumentoIdentificacao.BI) {
            return IDDocumentType.IDENTITY_CARD;
        } else if (idDocumentType == TipoDocumentoIdentificacao.PASSAPORTE) {
            return IDDocumentType.PASSPORT;
        } else if (idDocumentType == TipoDocumentoIdentificacao.AUTORIZACAO_RESIDENCIA) {
            return IDDocumentType.RESIDENCE_AUTHORIZATION;
        } else if (idDocumentType == TipoDocumentoIdentificacao.BI_NAO_NACIONAL) {
            return IDDocumentType.FOREIGNER_IDENTITY_CARD;
        } else if (idDocumentType == TipoDocumentoIdentificacao.OUTROS) {
            return IDDocumentType.OTHER;
        }

        return null;
    }

    private CandidacyState convertCandidacyState(ResultadoEstadoCandidatura candidacyState) {
        if (candidacyState == ResultadoEstadoCandidatura.INDEFERIDA) {
            return CandidacyState.DISMISSED;
        } else if (candidacyState == ResultadoEstadoCandidatura.DEFERIDA) {
            return CandidacyState.DEFERRED;
        } else if (candidacyState == ResultadoEstadoCandidatura.NAO_DETERMINADO) {
            return CandidacyState.UNDEFINED;
        }

        return null;
    }

    @Atomic
    public void processAllSasScholarshipCandidacies() {
        SasScholarshipCandidacy.findAll().stream().forEach(c -> fillCandidacyData(c));
    }

    @Atomic
    public void processSasScholarshipCandidacies(List<SasScholarshipCandidacy> list2Process) {
        list2Process.stream().forEach(c -> fillCandidacyData(c));
    }

    private void fillCandidacyData(SasScholarshipCandidacy c) {
        final AbstractFillScholarshipService service;

        final AbstractScholarshipStudentBean bean;
        
        if (c.getFirstYear() == null) {
            c.setState(SasScholarshipCandidacyState.PROCESSED_ERRORS);
            return;
        }

        if (c.getFirstYear()) {
            bean = new ScholarshipStudentFirstYearBean();
            service = new FillScholarshipFirstYearService();
        } else {
            bean = new ScholarshipStudentOtherYearBean();
            service = new FillScholarshipServiceOtherYearService();

        }

        // commons bean fields
        bean.setStudentNumber(c.getStudentNumber() != null && NumberUtils.isNumber(c.getStudentNumber()) ? Integer
                .valueOf(c.getStudentNumber()) : null);
        bean.setGratuityAmount(c.getGratuityAmount());
        bean.setCetQualificationOwner(c.getCetQualificationOwner());
        bean.setCtspQualificationOwner(c.getCetQualificationOwner());
        bean.setDegreeQualificationOwner(c.getDegreeQualificationOwner());
        bean.setMasterQualificationOwner(c.getMasterQualificationOwner());
        bean.setPhdQualificationOwner(c.getPhdQualificationOwner());
        bean.setFiscalCode(c.getFiscalNumber());
        bean.setInstitutionCode(c.getInstitutionCode() != null ? String.valueOf(c.getInstitutionCode()) : null);
        bean.setInstitutionName(c.getInstitutionName());
        bean.setCandidacyNumber(c.getCandidacyNumber() != null ? String.valueOf(c.getCandidacyNumber()) : null);
        bean.setStudentName(c.getCandidacyName());
        bean.setDegreeCode(c.getDegreeCode());
        bean.setDegreeName(c.getDegreeName());
        bean.setDocumentNumber(c.getDocIdNumber());

        // TODO check if is it ok       
        bean.setDocumentTypeName(c.getDocIdType().name());

        // TODO field document BI number from candidacy data?
        //bean.setDocumentBINumber();

        // TODO before was read from code in file!!
        // bean.setDegreeTypeName(c.getDe); 

        //bean.getDegreeTypeName()

        // TODO
        ScholarshipReportRequest request = new ScholarshipReportRequest(c.getExecutionYear(), false, false, "/", "".getBytes());

        if (c.getRegistration() == null) {
            return;
        }

        service.fillAllInfo(request, bean, c.getRegistration());

        SasScholarshipData data = convertBean2SasScholarshipData(bean);
        
        LocalDate enrolmentDate = RegistrationServices.getEnrolmentDate(c.getRegistration(), c.getExecutionYear());
        data.setEnrolmentDate(enrolmentDate != null ? enrolmentDate : null);
        data.setRegistrationYear(String.valueOf(c.getRegistration().getStartExecutionYear().getBeginCivilYear()));
        
        if (c.getSasScholarshipData() != null) {
            c.getSasScholarshipData().delete();
        }
        c.setSasScholarshipData(data);

        if (data.getObservations().contains(AbstractFillScholarshipService.ERROR_OBSERVATION)) {
            c.setState(SasScholarshipCandidacyState.PROCESSED_ERRORS);
        } else if (data.getObservations().contains(AbstractFillScholarshipService.WARNING_OBSERVATION)) {
            c.setState(SasScholarshipCandidacyState.PROCESSED_WARNINGS);
        } else {
            c.setState(SasScholarshipCandidacyState.PROCESSED);
        }

        c.setStateDate(new DateTime());

        c.getSasScholarshipDataChangeLogsSet().add(new SasScholarshipDataChangeLog(c.getStateDate(), c.getStudentNumber(),
                c.getCandidacyName(), !StringUtils.isEmpty(data.getObservations()) ? data.getObservations() : "Candidatura processada com sucesso."));

    }

    private SasScholarshipData convertBean2SasScholarshipData(AbstractScholarshipStudentBean bean) {

        SasScholarshipData data = new SasScholarshipData();

        data.setCetQualificationOwner(bean.getCetQualificationOwner());
        data.setCtspQualificationOwner(bean.getCtspQualificationOwner());

        data.setCycleNumberOfEnrolmentYears(bean.getCycleNumberOfEnrolmentYears());
        data.setDegreeQualificationOwner(bean.getDegreeQualificationOwner());
        //data.setEnrolmnetDate(bean.getE);

        data.setFirstMonthGratuity(bean.getFirstMonthExecutionYear());
        data.setGratuityAmount(String.valueOf(bean.getGratuityAmount()));

        data.setMasterQualificationOwner(bean.getMasterQualificationOwner());

        data.setNumberOfEnrolledECTS(bean.getNumberOfEnrolledECTS());
        data.setNumberOfMonthsGratuity(bean.getNumberOfMonthsExecutionYear());
        data.setObservations(bean.getObservations());
        data.setPhdQualificationOwner(bean.getPhdQualificationOwner());
        data.setRegime(bean.getRegime());
        data.setRegistered(bean.getRegistered());

        if (bean instanceof ScholarshipStudentOtherYearBean) {
            // TODO NADIR & Gilberto 
            data.setNumberOfApprovedEcts(((ScholarshipStudentOtherYearBean) bean).getNumberOfApprovedEcts());
            data.setNumberOfApprovedEctsLastYear(((ScholarshipStudentOtherYearBean) bean).getNumberOfApprovedEctsLastYear());
            data.setNumberOfDegreeChanges(((ScholarshipStudentOtherYearBean) bean).getNumberOfDegreeChanges());
            data.setHasMadeDegreeChangeOnCurrentYear(
                    ((ScholarshipStudentOtherYearBean) bean).getHasMadeDegreeChangeOnCurrentYear());
            data.setLastEnrolmentYear(String.valueOf(((ScholarshipStudentOtherYearBean) bean).getLastEnrolmentYear()));
            data.setLastAcademicActDateLastYear(
                    String.valueOf(((ScholarshipStudentOtherYearBean) bean).getLastAcademicActDateLastYear()));
            
            data.setNumberOfDegreeCurricularYears(((ScholarshipStudentOtherYearBean) bean).getNumberOfDegreeCurricularYears());
            
            data.setCycleNumberOfEnrolmentYears((((ScholarshipStudentOtherYearBean) bean).getCycleNumberOfEnrolmentsYearsInIntegralRegime()));
            data.setNumberOfEnrolledEctsLastYear(((ScholarshipStudentOtherYearBean) bean).getNumberOfEnrolledEctsLastYear());
            data.setNumberOfEnrolmentsYears(((ScholarshipStudentOtherYearBean) bean).getCycleNumberOfEnrolmentYears());
            
            data.setCurricularYear(String.valueOf(((ScholarshipStudentOtherYearBean) bean).getCurricularYear()));
            
        }

        return data;
    }

    @Atomic
    public void sendAllSasScholarshipsData() {
        SasScholarshipCandidacy.findAll().stream().forEach(c -> sendCandidacyData(c.getSasScholarshipData()));
    }

    @Atomic
    public void sendSasScholarshipsData(List<SasScholarshipCandidacy> list2Process) {
        list2Process.stream().forEach(c -> sendCandidacyData(c.getSasScholarshipData()));
    }

    private void sendCandidacyData(SasScholarshipData c) {
        // TODO Auto-generated method stub

        // TODO update candidacy "export date"

    }

    @Atomic
    public void removeAllSasScholarshipsCandidacies() {
        SasScholarshipCandidacy.findAll().stream().forEach(c -> removeCandidacyData(c));
    }

    @Atomic
    public void removeSasScholarshipsCandidacies(List<SasScholarshipCandidacy> list2Remove) {
        list2Remove.stream().forEach(c -> removeCandidacyData(c));
    }

    private void removeCandidacyData(SasScholarshipCandidacy c) {
        c.delete();
    }

    private void sendFirstTimeAcademicData(SasScholarshipData data) {
        AlterarDadosAcademicosPrimeiraVezRequest request = new AlterarDadosAcademicosPrimeiraVezRequest();

        request.setAnoInscricaoCurso(/*bean.getRegistrationDate()*/null);
        request.setCodigoCurso(data.getSasScholarshipCandidacy().getDegreeCode());
        request.setCodigoInstituicaoEnsino(Integer.valueOf(data.getSasScholarshipCandidacy().getInstitutionCode()));
        request.setDataInscricaoAnoLectivo(/*bean.getRegistrationDate()*/null);

        IdentificadorCandidatura idCandidatura = new IdentificadorCandidatura();
        idCandidatura.setAnoLectivo(-1);

        ObjectFactory factory = new ObjectFactory();

        idCandidatura.setDocumentoIdentificacao(
                factory.createIdentificadorCandidaturaDocumentoIdentificacao(data.getSasScholarshipCandidacy().getDocIdNumber()));
        idCandidatura.setNif(factory.createIdentificadorCandidaturaNif(data.getSasScholarshipCandidacy().getFiscalNumber()));
        idCandidatura
                .setTipoDocumentoIdentificacao(/*factory.createTipoDocumentoIdentificacao(TipoDocumentoIdentificacao.)*/null);

        request.setIdentificadorCandidatura(idCandidatura);
        request.setIInscritoAnoLectivoActual(false);
        request.setMesPrimeiroPagamento(-1);

        request.setNumeroAluno(null);
        request.setNumeroAnosCurso(-1);
        request.setNumeroECTSActualInscrito(null);
        request.setNumeroMatriculas(-1);
        request.setNumeroMesesPropina(-1);
        request.setObservacoes(null);

        request.setRegime(null);
        request.setTitularCET(false);
        request.setTitularCSTP(false);
        request.setTitularDoutoramento(false);
        request.setTitularLicenciatura(false);
        request.setTitularMestrado(false);

        request.setValorPropina(null);

    }

    private void sendContratualizationAcademicData() {
        AlterarDadosAcademicosContratualizacaoRequest request = new AlterarDadosAcademicosContratualizacaoRequest();
        request.setCodigoCurso(null);
        request.setCodigoInstituicaoEnsino(null);
        request.setDataInscricaoAnoLectivo(null);

        IdentificadorCandidatura idCandidatura = new IdentificadorCandidatura();
        idCandidatura.setAnoLectivo(-1);
        idCandidatura.setDocumentoIdentificacao(null);
        idCandidatura.setNif(null);
        idCandidatura.setTipoDocumentoIdentificacao(null);

        request.setIdentificadorCandidatura(idCandidatura);
        request.setIInscritoAnoLectivoActual(null);
        request.setMesPrimeiroPagamento(null);
        request.setNumeroAluno(null);
        request.setNumeroECTSActualmenteInscrito(null);
        request.setNumeroMesesPropina(null);
        request.setNumeroOcorrenciasMudancaCurso(null);
        request.setPresenteAnoMudouDeCurso(null);
        request.setRegime(null);
        request.setValorPropina(null);

    }

    private void sendOtherAcademicData() {
        AlterarDadosAcademicosRestantesCasosRequest request = new AlterarDadosAcademicosRestantesCasosRequest();

        request.setAnoInscricaoCurso(-1);
        request.setAnoLectivoActual(-1);
        request.setCodigoCurso(null);
        request.setCodigoInstituicaoEnsino(-1);
        request.setDataConclusaoAtosAcademicosUltimoAnoLectivoInscrito(null);
        request.setDataInscricaoAnoLectivo(null);

        IdentificadorCandidatura idCandidatura = new IdentificadorCandidatura();
        idCandidatura.setAnoLectivo(-1);
        idCandidatura.setDocumentoIdentificacao(null);
        idCandidatura.setNif(null);
        idCandidatura.setTipoDocumentoIdentificacao(null);

        request.setIdentificadorCandidatura(idCandidatura);
        request.setIInscritoAnoLectivoActual(false);
        request.setMesPrimeiroPagamento(-1);

        request.setNumeroAluno(null);
        request.setNumeroAnosCurso(-1);
        request.setNumeroECTSActualmenteInscrito(null);
        request.setNumeroECTSObtidosUltimoAnoInscrito(null);
        request.setNumeroECTSUltimoAnoInscrito(null);
        request.setNumeroInscricoesCicloEstudosTempoIntegral(-1);
        request.setNumeroMatriculas(-1);

        request.setNumeroMesesPropina(-1);
        request.setNumeroOcorrenciasMudancaCurso(-1);
        request.setObservacoes(null);

        request.setPresenteAnoMudouDeCurso(false);
        request.setRegime(null);
        request.setTitularCET(false);
        request.setTitularCSTP(false);
        request.setTitularDoutoramento(false);
        request.setTitularLicenciatura(false);
        request.setTitularMestrado(false);
        request.setTotalECTScursoAtingirGrau(null);
        request.setUltimoAnoInscrito(-1);
        request.setValorPropina(null);

    }

    /** TODO: to be done **/
    private void sendRegistrationStudent() {

        RegistarMatriculaAlunoRequest request = new RegistarMatriculaAlunoRequest();

        request.setCodigoCurso(null);
        request.setCodigoInstituicaoEnsino(-1);
        request.setDataMatricula(null);

        IdentificadorCandidatura idCandidatura = new IdentificadorCandidatura();
        idCandidatura.setAnoLectivo(-1);
        idCandidatura.setDocumentoIdentificacao(null);
        idCandidatura.setNif(null);
        idCandidatura.setTipoDocumentoIdentificacao(null);

        request.setIdentificadorCandidatura(idCandidatura);

    }

    private void updateCandidacyState() {

        ObterEstadoCandidaturaRequest request = new ObterEstadoCandidaturaRequest();

        IdentificadorCandidatura idCandidatura = new IdentificadorCandidatura();
        idCandidatura.setAnoLectivo(-1);
        idCandidatura.setDocumentoIdentificacao(null);
        idCandidatura.setNif(null);
        idCandidatura.setTipoDocumentoIdentificacao(null);

        request.setIdentificadorCandidatura(idCandidatura);

    }

    private void getSubmittedCandidacies() {

        ObterEstadoCandidaturaRequest request = new ObterEstadoCandidaturaRequest();

        IdentificadorCandidatura idCandidatura = new IdentificadorCandidatura();
        idCandidatura.setAnoLectivo(-1);
        idCandidatura.setDocumentoIdentificacao(null);
        idCandidatura.setNif(null);
        idCandidatura.setTipoDocumentoIdentificacao(null);
        request.setIdentificadorCandidatura(idCandidatura);

    }

    private void changeInstitutionDegree() {

        AlterarCursoInsituicaoRequest request = new AlterarCursoInsituicaoRequest();

        IdentificadorCandidatura idCandidatura = new IdentificadorCandidatura();
        idCandidatura.setAnoLectivo(-1);
        idCandidatura.setDocumentoIdentificacao(null);
        idCandidatura.setNif(null);
        idCandidatura.setTipoDocumentoIdentificacao(null);

        request.setIdentificadorCandidatura(idCandidatura);
        request.setCodigoCurso(null);
        request.setCodigoInstituicaoEnsino(-1);
        request.setDataMudanca(null);

    }
}
