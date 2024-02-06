package dev.ez.PayStubMaker.models;

public class Stub {

    private String name;
    private String employee;

    //all arre required fields so a constructor with all will do
    public Stub(String name,String employee) {
        this.name = name;
        this.employee = employee;
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

    @Override
    public String toString() {
        return name;
    }
}
