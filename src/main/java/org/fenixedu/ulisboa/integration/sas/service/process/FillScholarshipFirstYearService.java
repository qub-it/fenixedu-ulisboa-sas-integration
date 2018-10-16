package org.fenixedu.ulisboa.integration.sas.service.process;

import org.fenixedu.academic.domain.ExecutionYear;
import org.fenixedu.academic.domain.student.Registration;
import org.fenixedu.ulisboa.integration.sas.domain.SocialServicesConfiguration;
import org.fenixedu.ulisboa.integration.sas.dto.AbstractScholarshipStudentBean;
import org.fenixedu.ulisboa.specifications.domain.services.RegistrationServices;

public class FillScholarshipFirstYearService extends AbstractFillScholarshipService {

    @Override
    protected void fillSpecificInfo(AbstractScholarshipStudentBean bean, Registration registration, ExecutionYear requestYear) {
        if (SocialServicesConfiguration.getInstance().ingressionTypeRequiresExternalData(registration)) {
            addWarning(bean, "message.warning.ingression.type.requires.external.data",
                    registration.getIngressionType().getLocalizedName(),
                    RegistrationServices.getCurriculum(registration, requestYear).getSumEctsCredits().toString());
        }
    }

}