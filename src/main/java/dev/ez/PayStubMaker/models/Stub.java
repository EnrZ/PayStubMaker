package dev.ez.PayStubMaker.models;

import java.util.ArrayList;
import java.util.List;

public class Stub {

    private String name;
    private String employee;
    private String checkNumber;
    private String payPeriod;
    private String payDay;
    private List<Integer> hoursWorkedEachDay = new ArrayList<>();



    //all arre required fields so a constructor with all will do
    public Stub(String name,String employee, String checkNumber, String payPeriod, String payDate, List<Integer> hoursWorkedEachDay) {
        this.name = name;
        this.employee = employee;
        this.checkNumber = checkNumber;
        this.payPeriod = payPeriod;
        this.payDay = payDay;
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

    @Override
    public String toString() {
        return name;
    }
}
