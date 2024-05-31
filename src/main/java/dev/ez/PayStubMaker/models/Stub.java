package dev.ez.PayStubMaker.models;




import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Stub {

    private int id;
    private static int nextId = 1;
    @NotBlank(message ="Company name can't be left blank when making paystub. Default value re-set")private String name;
    @NotBlank(message ="Company address can't be left blank when making paystub")private String companyAddress;
    @NotBlank(message ="Company phone number can't be left blank when making paystub. Default value re-set") private String phoneNumber;
    @NotBlank(message ="Company email can't be left blank when making paystub")
    private String companyEmail;
    @NotBlank(message ="Employee name can't be left blank when making paystub")private String employee;
    @NotBlank(message ="Employee address can't be left blank when making paystub") private String employeeAddress;

    @NotBlank(message ="Employee ID can't be left blank when making paystub")  private String employeeId;
    @NotBlank(message ="Payment type can't be left blank when making paystub") private String paymentNumber;
    @NotBlank(message ="Pay period can't be left blank when making paystub") private String payPeriod;
    @NotBlank(message ="Must select which day of the week pay period started")private String payPeriodBeginning;
    @NotBlank(message ="Pay day can't be left blank when making paystub") private String payDay;
    private List<Integer> hoursWorkedEachDay = new ArrayList<>();
    @Min(value = 10, message ="Hourly pay rate cant be less than 10 dollars in the pay stub")
    private int hourlyPayRate;
    private BigDecimal yearlyPreviousGross, yearlyPreviousFed,yearlyPreviousState, yearlyPreviousSocSec, yearlyPreviousMedicare;

    private List<Integer> startTime = new ArrayList<>();


    //fed will be manual for now
    private BigDecimal federalTax;
    private BigDecimal stateTax;

    private List<String> federalTaxFiling = new ArrayList<>();
    private int stateTaxFiling;
//Goal for later, fill YTD total gross, ytd fed, ytd, state, ytd soc sec, tyd med, prev deduction total and previous net pay
    //values using the last completed paystub(persistence)
@DecimalMin(value ="0.0", message ="Previous Deduction can't be less than zero")
    private BigDecimal previousDeduction;
    @Min(value = 1,message ="Must select length of the pay period 1-16")
    @Max(value = 16,message ="Must select length of the pay period 1-16")
    private int daysLong;
    @DecimalMin(value ="0.0", message ="Previous net pay can't be less than zero")
    private BigDecimal previousNetPay;
    @DecimalMin(value ="0.0", message ="YTD fields can't be less than zero")
    private BigDecimal YTDGrossIncome,YTDFed,YTDState,YTDSocSec,YTDMedicare;
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
    @DecimalMin(value ="0.0", message ="Holiday hours can't be less than zero")
    private BigDecimal holidayHours;
    //all are required fields so a constructor with all will do

    public Stub() {}
    public Stub(String name,String companyAddress, String phoneNumber,String companyEmail,String employee, String employeeAddress, String employeeId, String checkNumber, String payPeriod,String payPeriodBeginning, String payDate, List<Integer> hoursWorkedEachDay, int hourlyPayRate, BigDecimal yearlyPreviousGross, BigDecimal yearlyPreviousFed, BigDecimal yearlyPreviousState, BigDecimal yearlyPreviousSocSec, BigDecimal yearlyPreviousMedicare, List<Integer> startTime, BigDecimal federalTax, int stateTaxFiling, BigDecimal previousDeductions, int daysLong,BigDecimal previousNetPay, BigDecimal holidayHours) {
        this.name = name;
        this.companyAddress = companyAddress;
        this.companyEmail = companyEmail;
        this.employee = employee; this.employeeAddress = employeeAddress; this.employeeId = employeeId;
        this.paymentNumber = paymentNumber;
        this.payPeriod = payPeriod;
        this.payDay = payDay;
        this.hourlyPayRate = hourlyPayRate;
        this.yearlyPreviousGross = yearlyPreviousGross;
        this.yearlyPreviousFed = yearlyPreviousFed;
        this.yearlyPreviousState = yearlyPreviousState;
        this.yearlyPreviousSocSec = yearlyPreviousSocSec;
        this.yearlyPreviousMedicare = yearlyPreviousMedicare;
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
        this.holidayHours = holidayHours;

        //needs to be changed for updated stub to work
        this.id = nextId;
        nextId++;

    }

    public BigDecimal getHolidayHours() {
        return holidayHours;
    }

    public void setHolidayHours(BigDecimal holidayHours) {
        this.holidayHours = holidayHours;
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

    public String getEmployeeAddress() {
        return employeeAddress;
    }

    public void setEmployeeAddress(String employeeAddress) {
        this.employeeAddress = employeeAddress;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getPaymentNumber() {
        return paymentNumber;
    }

    public void setPaymentNumber(String paymentNumber) {
        this.paymentNumber = paymentNumber;
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

    public BigDecimal getYearlyPreviousFed() {
        return yearlyPreviousFed;
    }

    public void setYearlyPreviousFed(BigDecimal yearlyPreviousFed) {
        this.yearlyPreviousFed = yearlyPreviousFed;
    }

    public BigDecimal getYearlyPreviousState() {
        return yearlyPreviousState;
    }

    public void setYearlyPreviousState(BigDecimal yearlyPreviousState) {
        this.yearlyPreviousState = yearlyPreviousState;
    }

    public BigDecimal getYearlyPreviousSocSec() {
        return yearlyPreviousSocSec;
    }

    public void setYearlyPreviousSocSec(BigDecimal yearlyPreviousSocSec) {
        this.yearlyPreviousSocSec = yearlyPreviousSocSec;
    }

    public BigDecimal getYearlyPreviousMedicare() {
        return yearlyPreviousMedicare;
    }

    public void setYearlyPreviousMedicare(BigDecimal yearlyPreviousMedicare) {
        this.yearlyPreviousMedicare = yearlyPreviousMedicare;
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

    public BigDecimal getYTDFed() {
        return YTDFed;
    }

    public void setYTDFed(BigDecimal YTDFed) {
        this.YTDFed = YTDFed;
    }

    public BigDecimal getYTDState() {
        return YTDState;
    }

    public void setYTDState(BigDecimal YTDState) {
        this.YTDState = YTDState;
    }

    public BigDecimal getYTDSocSec() {
        return YTDSocSec;
    }

    public void setYTDSocSec(BigDecimal YTDSocSec) {
        this.YTDSocSec = YTDSocSec;
    }

    public BigDecimal getYTDMedicare() {
        return YTDMedicare;
    }

    public void setYTDMedicare(BigDecimal YTDMedicare) {
        this.YTDMedicare = YTDMedicare;
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

    public List<String> getFederalTaxFiling() {
        return federalTaxFiling;
    }

    public void setFederalTaxFiling(List<String> federalTaxFiling) {
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
