package org.fenixedu.ulisboa.integration.sas.dto;

import java.io.Serializable;

public class ActiveStudentBean implements Serializable {

    String name;
    String gender;
    String mifare;
    String identificationNumber;
    String fiscalIdentificationNumber;
    String dateOfBirth;
    String studentCode;
    String originCountry;

    String degreeCode;
    String oficialDegreeCode;
    String currentExecutionYear;
    String previousExecutionYear;
    String enroledECTTotal;
    String enroledECTTotalInPreviousYear;
    String approvedECTTotalInPreviousYear;
    String dateOfRegistration;
    String curricularYear;
    String regime;
    // Due to shared degrees, a student may be frequenting a school, but the payment is performed in another school
    Boolean isPayingSchool;

    public Boolean getIsPayingSchool() {
        return isPayingSchool;
    }

    public void setIsPayingSchool(Boolean isPayingSchool) {
        this.isPayingSchool = isPayingSchool;
    }

    public String getCurrentExecutionYear() {
        return currentExecutionYear;
    }

    public void setCurrentExecutionYear(String currentExecutionYear) {
        this.currentExecutionYear = currentExecutionYear;
    }

    public String getPreviousExecutionYear() {
        return previousExecutionYear;
    }

    public void setPreviousExecutionYear(String previousExecutionYear) {
        this.previousExecutionYear = previousExecutionYear;
    }

    public String getCurricularYear() {
        return curricularYear;
    }

    public void setCurricularYear(String curricularYear) {
        this.curricularYear = curricularYear;
    }

    public String getRegime() {
        return regime;
    }

    public void setRegime(String regime) {
        this.regime = regime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMifare() {
        return mifare;
    }

    public void setMifare(String mifare) {
        this.mifare = mifare;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public String getFiscalIdentificationNumber() {
        return fiscalIdentificationNumber;
    }

    public void setFiscalIdentificationNumber(String fiscalIdentificationNumber) {
        this.fiscalIdentificationNumber = fiscalIdentificationNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getDegreeCode() {
        return degreeCode;
    }

    public void setDegreeCode(String degreeCode) {
        this.degreeCode = degreeCode;
    }

    public String getEnroledECTTotal() {
        return enroledECTTotal;
    }

    public void setEnroledECTTotal(String enroledECTTotal) {
        this.enroledECTTotal = enroledECTTotal;
    }

    public String getEnroledECTTotalInPreviousYear() {
        return enroledECTTotalInPreviousYear;
    }

    public void setEnroledECTTotalInPreviousYear(String enroledECTTotalInPreviousYear) {
        this.enroledECTTotalInPreviousYear = enroledECTTotalInPreviousYear;
    }

    public String getApprovedECTTotalInPreviousYear() {
        return approvedECTTotalInPreviousYear;
    }

    public void setApprovedECTTotalInPreviousYear(String approvedECTTotalInPreviousYear) {
        this.approvedECTTotalInPreviousYear = approvedECTTotalInPreviousYear;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    public String getDateOfRegistration() {
        return dateOfRegistration;
    }

    public void setDateOfRegistration(String dateOfRegistration) {
        this.dateOfRegistration = dateOfRegistration;
    }

    public String getOficialDegreeCode() {
        return oficialDegreeCode;
    }

    public void setOficialDegreeCode(String oficialDegreeCode) {
        this.oficialDegreeCode = oficialDegreeCode;
    }

}
