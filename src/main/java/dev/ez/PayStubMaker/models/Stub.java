package dev.ez.PayStubMaker.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Stub {

    private int id;
    private static int nextId = 1;
    private String name;
    private String companyAddress;
    private String phoneNumber;
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


    //fed will be manual for now
    private BigDecimal federalTax;
    private BigDecimal stateTax;
    private List<Integer> federalTaxFiling = new ArrayList<>();
    private int stateTaxFiling;

    private BigDecimal previousDeduction;
    private int daysLong;

    private BigDecimal previousNetPay;
    private BigDecimal YTDGrossIncome;
    private int totalHours;
    private BigDecimal totalGrossIncome;
    private List<String> updatedDays;
    private List<String> timeWorkedStartFormatted;
    private List<String> timeWorkedEnd;
    private BigDecimal socSecContribution;
    private BigDecimal medicareContribution;
    private BigDecimal currentTotalDeduction;
    private BigDecimal YTDDeduction;
    private BigDecimal netPay;
    private BigDecimal YTDnetPay;
    //all are required fields so a constructor with all will do
    public Stub(String name,String companyAddress, String phoneNumber,String companyEmail,String employee, String checkNumber, String payPeriod,String payPeriodBeginning, String payDate, List<Integer> hoursWorkedEachDay, int hourlyPayRate, BigDecimal yearlyPreviousGross, List<Integer> startTime, BigDecimal federalTax, int stateTaxFiling, BigDecimal previousDeductions, int daysLong,BigDecimal previousNetPay) {
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
        this.stateTaxFiling = stateTaxFiling;
        this.totalHours = totalHours;
        this.totalGrossIncome = totalGrossIncome;
        this.updatedDays = updatedDays;
        this.phoneNumber = phoneNumber;

        this.id = nextId;
        nextId++;

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

    public String getPhoneNumber() {return phoneNumber;}

    public void setPhoneNumber(String phoneNumber) {this.phoneNumber = phoneNumber;}

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

    public int getStateTaxFiling() {
        return stateTaxFiling;
    }

    public void setStateTaxFiling(int stateTaxFiling) {
        this.stateTaxFiling = stateTaxFiling;
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

    public int getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(int totalHours) {
        this.totalHours = totalHours;
    }

    public BigDecimal getTotalGrossIncome() {
        return totalGrossIncome;
    }

    public void setTotalGrossIncome(BigDecimal totalGrossIncome) {
        this.totalGrossIncome = totalGrossIncome;
    }

    public List<String> getUpdatedDays() {
        return updatedDays;
    }

    public void setUpdatedDays(List<String> updatedDays) {
        this.updatedDays = updatedDays;
    }

    public BigDecimal getStateTax() {
        return stateTax;
    }

    public void setStateTax(BigDecimal stateTax) {
        this.stateTax = stateTax;
    }

    public List<String> getTimeWorkedStartFormatted() {
        return timeWorkedStartFormatted;
    }

    public void setTimeWorkedStartFormatted(List<String> timeWorkedStartFormatted) {
        this.timeWorkedStartFormatted = timeWorkedStartFormatted;
    }

    public List<String> getTimeWorkedEnd() {
        return timeWorkedEnd;
    }

    public void setTimeWorkedEnd(List<String> timeWorkedEnd) {
        this.timeWorkedEnd = timeWorkedEnd;
    }

    public BigDecimal getYTDGrossIncome() {
        return YTDGrossIncome;
    }

    public void setYTDGrossIncome(BigDecimal YTDGrossIncome) {
        this.YTDGrossIncome = YTDGrossIncome;
    }

    public BigDecimal getSocSecContribution() {
        return socSecContribution;
    }

    public void setSocSecContribution(BigDecimal socSecContribution) {
        this.socSecContribution = socSecContribution;
    }

    public BigDecimal getMedicareContribution() {
        return medicareContribution;
    }

    public void setMedicareContribution(BigDecimal medicareContribution) {
        this.medicareContribution = medicareContribution;
    }

    public BigDecimal getCurrentTotalDeduction() {
        return currentTotalDeduction;
    }

    public void setCurrentTotalDeduction(BigDecimal currentTotalDeduction) {
        this.currentTotalDeduction = currentTotalDeduction;
    }

    public BigDecimal getYTDDeduction() {
        return YTDDeduction;
    }

    public void setYTDDeduction(BigDecimal YTDDeduction) {
        this.YTDDeduction = YTDDeduction;
    }

    public BigDecimal getNetPay() {
        return netPay;
    }

    public void setNetPay(BigDecimal netPay) {
        this.netPay = netPay;
    }

    public BigDecimal getYTDnetPay() {
        return YTDnetPay;
    }

    public void setYTDnetPay(BigDecimal YTDnetPay) {
        this.YTDnetPay = YTDnetPay;
    }

    public List<Integer> getFederalTaxFiling() {
        return federalTaxFiling;
    }

    public void setFederalTaxFiling(List<Integer> federalTaxFiling) {
        this.federalTaxFiling = federalTaxFiling;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
