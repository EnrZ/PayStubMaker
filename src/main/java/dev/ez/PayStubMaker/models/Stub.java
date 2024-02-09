package dev.ez.PayStubMaker.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Stub {

    private String name;
    private String employee;
    private String checkNumber;
    private String payPeriod;
    private String payDay;
    private List<Integer> hoursWorkedEachDay = new ArrayList<>();
    private int hourlyPayRate;
    private BigDecimal yearlyPreviousGross;

    private List<String> timeOfDay = new ArrayList<>();


    //all arre required fields so a constructor with all will do
    public Stub(String name,String employee, String checkNumber, String payPeriod, String payDate, List<Integer> hoursWorkedEachDay, int hourlyPayRate, BigDecimal yearlyPreviousGross, List<String> timeOfDay) {
        this.name = name;
        this.employee = employee;
        this.checkNumber = checkNumber;
        this.payPeriod = payPeriod;
        this.payDay = payDay;
        this.hourlyPayRate = hourlyPayRate;
        this.yearlyPreviousGross = yearlyPreviousGross;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<String> getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(List<String> timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    @Override
    public String toString() {
        return name;
    }
}
