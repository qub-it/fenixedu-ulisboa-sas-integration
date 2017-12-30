package org.fenixedu.ulisboa.integration.sas.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.signals.DomainObjectEvent;
import org.fenixedu.bennu.core.signals.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.dml.DeletionListener;

//Operations to allow "caching"/"indexing" which enrolments were performed in the current day
public class DailyEnrolmentsIndexing {

    private static Logger logger = LoggerFactory.getLogger(DailyEnrolmentsIndexing.class);

    public static void bindToSignals() {
        Signal.register(Enrolment.SIGNAL_CREATED, associateWithBennu());
        FenixFramework.getDomainModel().registerDeletionListener(Enrolment.class, new DeletionListener<Enrolment>() {

            @Override
            public void deleting(final Enrolment arg0) {
                arg0.setBennuForWhichIsDaily(null);
            }
        });
    }

    private static Consumer<DomainObjectEvent<Enrolment>> associateWithBennu() {
        return enrolmentEvent -> enrolmentEvent.getInstance().setBennuForWhichIsDaily(Bennu.getInstance());
    }

    private static Consumer<Enrolment> dessociateWithBennu() {
        return enrolment -> enrolment.setBennuForWhichIsDaily(null);
    }

    public static void performDailyClean() {
        logger.info("Starting daily index clean for enrolments....");
        List<Enrolment> dailyEnrolmentsSet = new ArrayList<>(Bennu.getInstance().getDailyEnrolmentsSet());
        int size = dailyEnrolmentsSet.size();
        int pageSize = 1000;
        int numberOfPages = size / pageSize + (size % pageSize > 0 ? 1 : 0);
        for (int i = 0; i < numberOfPages; i++) {
            logger.debug("Page : " + i + "/" + numberOfPages);
            int start = i * pageSize;
            int end = Math.min(start + pageSize, size);
            performDailyClean(dailyEnrolmentsSet.subList(start, end));
        }
        dailyEnrolmentsSet.forEach(dessociateWithBennu());
        logger.info("Finished daily index clean for enrolments....");
    }

    @Atomic
    private static void performDailyClean(final List<Enrolment> enrolmentsToDissociate) {
        enrolmentsToDissociate.forEach(dessociateWithBennu());
    }
}
