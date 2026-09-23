package org.fenixedu.ulisboa.integration.sas.servlet;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import org.fenixedu.ulisboa.integration.sas.service.DailyEnrolmentsIndexing;
import org.fenixedu.ulisboa.integration.sas.webservices.ActiveStudentsWebService;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

@WebListener
public class ULisboaSasIntegrationInitializer implements ServletContextListener {

    @Override
    @Atomic(mode = TxMode.SPECULATIVE_READ)
    public void contextInitialized(ServletContextEvent event) {
        DailyEnrolmentsIndexing.bindToSignals();

        // Load the class so the update thread starts
        ActiveStudentsWebService.class.getName();
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }
}