package dev.ez.PayStubMaker.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Stub {

    private String name;
    private String companyAddress;
    private String companyEmail;
    private String employee;
    private String checkNumber;
    private String payPeriod;
    private String payPeriodBeginning;
    private String payDay;
    private List<Integer> hoursWorkedEachDay = new ArrayList<>();
    private int hourlyPayRate;
    private BigDecimal yearlyPreviousGross;

    private List<Integer> startTime = new ArrayList<>();


    //fed and state tax will be manual for now
    private BigDecimal federalTax;
    private BigDecimal stateTax;

    private BigDecimal previousDeduction;
    private int daysLong;

    private BigDecimal previousNetPay;



    //all arre required fields so a constructor with all will do
    public Stub(String name,String companyAddress, String companyEmail,String employee, String checkNumber, String payPeriod,String payPeriodBeginning, String payDate, List<Integer> hoursWorkedEachDay, int hourlyPayRate, BigDecimal yearlyPreviousGross, List<Integer> startTime, BigDecimal federalTax, BigDecimal stateTax, BigDecimal previousDeductions, int daysLong,BigDecimal previousNetPay) {
        this.name = name;
        this.companyAddress = companyAddress;
        this.companyEmail = companyEmail;
        this.employee = employee;
        this.checkNumber = checkNumber;
        this.payPeriod = payPeriod;
        this.payDay = payDay;
        this.hourlyPayRate = hourlyPayRate;
        this.yearlyPreviousGross = yearlyPreviousGross;
        this.previousDeduction = previousDeduction;
        this.payPeriodBeginning = payPeriodBeginning;
        this.daysLong = daysLong;
        this.startTime = startTime;
        this.previousNetPay = previousNetPay;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompanyAddress() { return companyAddress; }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public String getPayPeriod() {
        return payPeriod;
    }

    public String getPayPeriodBeginning() {
        return payPeriodBeginning;
    }

    public void setPayPeriodBeginning(String payPeriodBeginning) {
        this.payPeriodBeginning = payPeriodBeginning;
    }

    public void setPayPeriod(String payPeriod) {
        this.payPeriod = payPeriod;
    }

    public String getPayDay() {
        return payDay;
    }

    public void setPayDay(String payDay) {
        this.payDay = payDay;
    }

    public List<Integer> getHoursWorkedEachDay() {
        return hoursWorkedEachDay;
    }

    public void setHoursWorkedEachDay(List<Integer> hoursWorkedEachDay) {
        this.hoursWorkedEachDay = hoursWorkedEachDay;
    }

    public int getHourlyPayRate() {return hourlyPayRate;}

    public void setHourlyPayRate(int hourlyPayRate) {
        this.hourlyPayRate = hourlyPayRate;
    }

    public BigDecimal getYearlyPreviousGross() {
        return yearlyPreviousGross;
    }

    public void setYearlyPreviousGross(BigDecimal yearlyPreviousGross) {
        this.yearlyPreviousGross = yearlyPreviousGross;
    }

    public BigDecimal getFederalTax() {
        return federalTax;
    }

    public void setFederalTax(BigDecimal federalTax) {
        this.federalTax = federalTax;
    }

    public BigDecimal getStateTax() {
        return stateTax;
    }

    public void setStateTax(BigDecimal stateTax) {
        this.stateTax = stateTax;
    }

    public BigDecimal getPreviousDeduction() {
        return previousDeduction;
    }

    public void setPreviousDeduction(BigDecimal previousDeduction) {
        this.previousDeduction = previousDeduction;
    }

    public int getDaysLong() {
        return daysLong;
    }

    public void setDaysLong(int daysLong) {
        this.daysLong = daysLong;
    }

    public List<Integer> getStartTime() {
        return startTime;
    }

    public void setStartTime(List<Integer> startTime) {
        this.startTime = startTime;
    }

    public BigDecimal getPreviousNetPay() {
        return previousNetPay;
    }

    public void setPreviousNetPay(BigDecimal previousNetPay) {
        this.previousNetPay = previousNetPay;
    }

    @Override
    public String toString() {
        return name;
    }
}
