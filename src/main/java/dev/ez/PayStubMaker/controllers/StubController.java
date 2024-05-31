package dev.ez.PayStubMaker.controllers;

import dev.ez.PayStubMaker.data.StubData;
import dev.ez.PayStubMaker.models.Stub;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.math.BigDecimal;

@Controller
@RequestMapping("stubs")
public class StubController {

    private static List<Stub> stubs = new ArrayList<>();

    @GetMapping
    public String displayAllStubs(Model model) {

        //Goes through each stub
        for (Stub stub : stubs) {
            model.addAttribute("stubs", stubs);

            int totalHours = 0, decimalPlaces = 2;
            BigDecimal TGICalculations, YTDGICalculations, SOCSECCalculations, MCCalculations;
            BigDecimal totalGrossIncome, YTDGrossIncome, socSecContribution, medicareContribution;
            BigDecimal hourlyPayRateConverted;
            BigDecimal federalTax, stateTax;
            BigDecimal taxCalculations, contributionCalculations, deductionCalculations, currentTotalDeduction, regularCalculations, overtimeCalculations, overtimePayRate;
            BigDecimal YTDDeduction, YTDDeductionCalculations, netPay, netPayCalculations, YTDnetPay, YTDnetPayCalculations;
            BigDecimal YTDFedCalculations, YTDFed, YTDStateCalculations, YTDState, YTDSocSecCalculations, YTDSocSec, YTDMedicareCalculations, YTDMedicare;
            BigDecimal holidayHours, holidayPay,extraPay;

            List<String> daysOfWeek = new ArrayList<>(Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"));
            String beginningDay;
            int daysLong, regularHours, overtimeHours;


            for (int num : stub.getHoursWorkedEachDay()) {
                totalHours += num;
            }

            regularHours = Math.min(totalHours, 40);
            overtimeHours = Math.max(totalHours - 40, 0);

            stub.setTotalHours(totalHours);

            hourlyPayRateConverted = BigDecimal.valueOf(stub.getHourlyPayRate());
            regularCalculations = hourlyPayRateConverted.multiply(BigDecimal.valueOf(regularHours));

            overtimePayRate = BigDecimal.valueOf(1.5).multiply(hourlyPayRateConverted);
            overtimeCalculations = overtimePayRate.multiply(BigDecimal.valueOf(overtimeHours));

            TGICalculations = regularCalculations.add(overtimeCalculations);
            totalGrossIncome = TGICalculations.setScale(2, RoundingMode.HALF_UP);

            //Accounting for holiday time paid
            holidayHours = stub.getHolidayHours();
            if(holidayHours.compareTo(BigDecimal.ZERO) > 0){
                //adding the .5 in 1.5 pay to the totalGrossIncome if there is holiday day
                extraPay = BigDecimal.valueOf(.5).multiply(hourlyPayRateConverted);
                holidayPay = extraPay.multiply(holidayHours);
                totalGrossIncome = totalGrossIncome.add(holidayPay);
            }

            stub.setTotalGrossIncome(totalGrossIncome);

            YTDGICalculations = totalGrossIncome.add(stub.getYearlyPreviousGross());
            YTDGrossIncome = YTDGICalculations.setScale(2, RoundingMode.HALF_UP);
            stub.setYTDGrossIncome(YTDGrossIncome);

            SOCSECCalculations = totalGrossIncome.multiply(BigDecimal.valueOf(0.062));
            socSecContribution = SOCSECCalculations.setScale(2, RoundingMode.UP);
            stub.setSocSecContribution(socSecContribution);

            //Soc Sec YTD
            YTDSocSecCalculations = socSecContribution.add(stub.getYearlyPreviousSocSec());
            YTDSocSec = YTDSocSecCalculations.setScale(2, RoundingMode.UP);
            stub.setYTDSocSec(YTDSocSec);

            MCCalculations = totalGrossIncome.multiply(BigDecimal.valueOf(0.0145));
            medicareContribution = MCCalculations.setScale(2, RoundingMode.UP);
            stub.setMedicareContribution(medicareContribution);

            //Medicare YTD
            YTDMedicareCalculations = medicareContribution.add(stub.getYearlyPreviousMedicare());
            YTDMedicare = YTDMedicareCalculations.setScale(2, RoundingMode.UP);
            stub.setYTDMedicare(YTDMedicare);


            if (stub.getDaysLong() == 14||stub.getDaysLong() == 15||stub.getDaysLong() == 16) {
                //Calling this method to calculate state tax
                stateTax = stateTaxFormula(stub.getStateTaxFiling(), totalGrossIncome);
                stub.setStateTax(stateTax);

                federalTax = federalTaxFormula(stub.getFederalTaxFiling(), totalGrossIncome);
                stub.setFederalTax(federalTax.setScale(2,RoundingMode.UP));
            } else if (stub.getDaysLong() == 1){
                    stateTax = stateTaxFormulaDaily(stub.getStateTaxFiling(), totalGrossIncome);
                    stub.setStateTax(stateTax);

                    federalTax = federalTaxFormulaDaily(stub.getFederalTaxFiling(), totalGrossIncome);
                stub.setFederalTax(federalTax.setScale(2,RoundingMode.UP));
                }
             else {
                stateTax = stateTaxFormulaWeekly(stub.getStateTaxFiling(), totalGrossIncome);
                stub.setStateTax(stateTax.setScale(2,RoundingMode.UP));

                federalTax = federalTaxFormulaWeekly(stub.getFederalTaxFiling(), totalGrossIncome);
                stub.setFederalTax(federalTax.setScale(2,RoundingMode.UP));
            }
            //Fed and State YTD Calculations
            YTDFedCalculations = federalTax.add(stub.getYearlyPreviousFed());
            YTDFed = YTDFedCalculations.setScale(2,RoundingMode.UP);
            stub.setYTDFed(YTDFed);
            YTDStateCalculations = stateTax.add(stub.getYearlyPreviousState());
            YTDState = YTDStateCalculations.setScale(2,RoundingMode.UP);
            stub.setYTDState(YTDState);

            taxCalculations = stub.getFederalTax().add(stateTax);

            contributionCalculations = medicareContribution.add(socSecContribution);
            deductionCalculations = taxCalculations.add(contributionCalculations);
            currentTotalDeduction = deductionCalculations.setScale(2, RoundingMode.UP);
            stub.setCurrentTotalDeduction(currentTotalDeduction);

            YTDDeductionCalculations = currentTotalDeduction.add(stub.getPreviousDeduction());
            YTDDeduction = YTDDeductionCalculations.setScale(2, RoundingMode.UP);
            stub.setYTDDeduction(YTDDeduction);

            netPayCalculations = totalGrossIncome.subtract(currentTotalDeduction);
            netPay = netPayCalculations.setScale(2, RoundingMode.UP);
            stub.setNetPay(netPay);

            YTDnetPayCalculations = netPay.add(stub.getPreviousNetPay());
            YTDnetPay = YTDnetPayCalculations.setScale(2, RoundingMode.UP);
            stub.setYTDnetPay(YTDnetPay);

            beginningDay = stub.getPayPeriodBeginning();
            if (daysOfWeek.contains(beginningDay)) {
                int index = daysOfWeek.indexOf(beginningDay);
                List<String> updatedDays = daysOfWeek.subList(index, daysOfWeek.size());
                stub.setUpdatedDays(updatedDays);
            }

            List<Integer> hoursWorked = new ArrayList<>(stub.getHoursWorkedEachDay());
            List<Integer> timeWorkedStart = new ArrayList<>(stub.getStartTime());
            List<String> timeWorkedEnd = new ArrayList<>();

            for (int i = 0; i < hoursWorked.size(); i++) {
                int endingHour = timeWorkedStart.get(i) + hoursWorked.get(i);
                String formattedTime = formatTime(endingHour);
                timeWorkedEnd.add(formattedTime);
            }

            List<String> timeWorkedStartFormatted = new ArrayList<>();
            for (Integer integer : timeWorkedStart) {
                String formattedTime = formatTime(integer);
                timeWorkedStartFormatted.add(formattedTime);
            }

            stub.setHoursWorkedEachDay(hoursWorked);
            stub.setTimeWorkedStartFormatted(timeWorkedStartFormatted);
            stub.setTimeWorkedEnd(timeWorkedEnd);

            daysLong = stub.getDaysLong() - 1;
            stub.setDaysLong(daysLong);
        }
        return "stubs/index";
    }


    @GetMapping("update")
    public String renderUpdateForm() {

        return "stubs/update";
    }
    @PostMapping("update")
    public String createUpdatedStub(@RequestParam(value = "newPaymentNumber", required = false) String newPaymentNumber, @RequestParam("findId") int findId, @RequestParam(value = "newPayDay", required = false) String newPayDay) {

        for (Stub stub : stubs) {
            if (stub.getId() == findId) {
                if (newPaymentNumber != null && !newPaymentNumber.isEmpty()) {
                    stub.setPaymentNumber(newPaymentNumber);
                }
                if (newPayDay != null && !newPayDay.isEmpty()) {
                    stub.setPayDay(newPayDay);
                }
            }
        }
            return "redirect:/stubs";
        }

    // /stubs/create route
    @GetMapping("create")
    public String renderCreateStubForm(Model model) {
        model.addAttribute(new Stub());
        return "stubs/create";
    }

    @PostMapping("create")
    public String createStub(@ModelAttribute @Valid Stub newStub, Errors errors) {
        //Go back to form if there aee any errors
        if(errors.hasErrors()){
            return "stubs/create";
        }
        stubs.add(newStub);
        return "redirect:/stubs";
    }

    private static String formatTime(int hour) {
        if(hour == 24){
            return "12 PM";
        } //if hour total is 36(the PM hours are valued at 12 more than AM), then return 12 AM

        hour = hour % 24;
        if (hour > 0 && hour < 12) {
            return hour + " AM";
        } else if (hour == 12) {
            return "12 PM";
        } else if (hour > 12) {
            return (hour - 12) + " PM";
        } else {
            return " ";
        }
    }

    private static BigDecimal federalTaxFormula(List<String> status, BigDecimal wages) {
        BigDecimal result = null;
        int WagesInt = wages.intValue();
        if (status.get(0).equals(0)) {
            //Married Filing Jointly Standing withholding
            if (status.get(1).equals(0)) {
                if (WagesInt < 1215) {
                    result = BigDecimal.valueOf(0);
                } else if (WagesInt >= 1215 && WagesInt < 1230) {
                    result = BigDecimal.valueOf(1);
                }
            }
            //Married Filing Jointly W4 etc.
            if (status.get(1).equals(1)) {
                if (WagesInt < 615) {
                    result = BigDecimal.valueOf(0);
                } else if (WagesInt >= 615 && WagesInt < 630) {
                    result = BigDecimal.valueOf(1);
                }
            }
        }
        if (status.get(0).equals(1)) {
            //Head of Household Standing withholding
            if (status.get(1).equals(0)) {
                if (WagesInt < 915) {
                    result = BigDecimal.valueOf(0);
                } else if (WagesInt >= 915 && WagesInt < 930) {
                    result = BigDecimal.valueOf(1);
                }
            }
            //Head of Household W4 etc.
            if(status.get(1).equals(1)){
                if (WagesInt < 465) {
                    result = BigDecimal.valueOf(0);
                } else if (WagesInt >= 465 && WagesInt < 475) {
                    result = BigDecimal.valueOf(1);
                }
            }

        }
        if (status.get(0).equals("s") || status.get(0).equals("sep")) {
            //Single Standard withholding
            if (status.get(1).equals("0")) {
                result = (WagesInt < 615) ? BigDecimal.valueOf(0): (WagesInt < 630) ? BigDecimal.valueOf(1):(WagesInt < 645) ? BigDecimal.valueOf(3):(WagesInt < 660) ? BigDecimal.valueOf(4): (WagesInt < 675) ? BigDecimal.valueOf(6):(WagesInt < 690) ? BigDecimal.valueOf(7):(WagesInt < 705) ? BigDecimal.valueOf(9):(WagesInt < 720) ? BigDecimal.valueOf(10):(WagesInt < 735) ? BigDecimal.valueOf(12):(WagesInt < 750) ? BigDecimal.valueOf(13):(WagesInt < 765) ? BigDecimal.valueOf(15):(WagesInt < 780) ? BigDecimal.valueOf(16):(WagesInt < 795) ? BigDecimal.valueOf(18):(WagesInt < 810) ? BigDecimal.valueOf(19):(WagesInt < 825) ? BigDecimal.valueOf(21):
                (WagesInt < 840) ? BigDecimal.valueOf(22): (WagesInt < 855) ? BigDecimal.valueOf(24):(WagesInt < 870) ? BigDecimal.valueOf(25):(WagesInt < 885) ? BigDecimal.valueOf(27):(WagesInt < 900) ? BigDecimal.valueOf(28):(WagesInt < 915) ? BigDecimal.valueOf(30):(WagesInt < 930) ? BigDecimal.valueOf(31):(WagesInt < 945) ? BigDecimal.valueOf(33):(WagesInt < 960) ? BigDecimal.valueOf(34):(WagesInt < 975) ? BigDecimal.valueOf(36):(WagesInt < 990) ? BigDecimal.valueOf(37):(WagesInt < 1005) ? BigDecimal.valueOf(39):(WagesInt < 1020) ? BigDecimal.valueOf(40):(WagesInt < 1035) ? BigDecimal.valueOf(42):(WagesInt < 1050) ? BigDecimal.valueOf(43):
                (WagesInt < 1065) ? BigDecimal.valueOf(45):(WagesInt < 1080) ? BigDecimal.valueOf(46):(WagesInt < 1095) ? BigDecimal.valueOf(48):(WagesInt < 1110) ? BigDecimal.valueOf(50):(WagesInt < 1125) ? BigDecimal.valueOf(51):(WagesInt < 1140) ? BigDecimal.valueOf(53):(WagesInt < 1155) ? BigDecimal.valueOf(55):(WagesInt < 1170) ? BigDecimal.valueOf(57):(WagesInt < 1185) ? BigDecimal.valueOf(59):(WagesInt < 1200) ? BigDecimal.valueOf(60):(WagesInt < 1215) ? BigDecimal.valueOf(62):(WagesInt < 1230) ? BigDecimal.valueOf(64):(WagesInt < 1245) ? BigDecimal.valueOf(66):(WagesInt < 1260) ? BigDecimal.valueOf(68):(WagesInt < 1275) ? BigDecimal.valueOf(69):
                (WagesInt < 1290) ? BigDecimal.valueOf(71):(WagesInt < 1310) ? BigDecimal.valueOf(73):(WagesInt < 1330) ? BigDecimal.valueOf(76):(WagesInt < 1350) ? BigDecimal.valueOf(78):(WagesInt < 1370) ? BigDecimal.valueOf(81):(WagesInt < 1390) ? BigDecimal.valueOf(83):(WagesInt < 1410) ? BigDecimal.valueOf(85):(WagesInt < 1430) ? BigDecimal.valueOf(88):(WagesInt < 1450) ? BigDecimal.valueOf(90):(WagesInt < 1470) ? BigDecimal.valueOf(93):(WagesInt < 1490) ? BigDecimal.valueOf(95):(WagesInt < 1510) ? BigDecimal.valueOf(97):(WagesInt < 1530) ? BigDecimal.valueOf(100):(WagesInt < 1550) ? BigDecimal.valueOf(102):(WagesInt < 1570) ? BigDecimal.valueOf(105):
                (WagesInt < 1590) ? BigDecimal.valueOf(107):(WagesInt < 1610) ? BigDecimal.valueOf(109):(WagesInt < 1630) ? BigDecimal.valueOf(112):(WagesInt < 1650) ? BigDecimal.valueOf(114):(WagesInt < 1670) ? BigDecimal.valueOf(117):(WagesInt < 1690) ? BigDecimal.valueOf(119):(WagesInt < 1710) ? BigDecimal.valueOf(121):(WagesInt < 1730) ? BigDecimal.valueOf(124):(WagesInt < 1750) ? BigDecimal.valueOf(126):(WagesInt < 1770) ? BigDecimal.valueOf(129):(WagesInt < 1790) ? BigDecimal.valueOf(131):(WagesInt < 1810) ? BigDecimal.valueOf(133):(WagesInt < 1830) ? BigDecimal.valueOf(136):(WagesInt < 1850) ? BigDecimal.valueOf(138):(WagesInt < 1870) ? BigDecimal.valueOf(141):
                (WagesInt < 1890) ? BigDecimal.valueOf(143):
                BigDecimal.valueOf(-1);

            }

            if (status.get(1).equals("1")) {
                //Single W4 etc.
                if (WagesInt < 305) {
                    result = BigDecimal.valueOf(0);
                } else if (WagesInt >= 305 && WagesInt < 315) {
                    result = BigDecimal.valueOf(1);
                } else if (WagesInt >= 315 && WagesInt < 325) {
                    result = BigDecimal.valueOf(2);
                } else if (WagesInt >= 325 && WagesInt < 335) {
                    result = BigDecimal.valueOf(3);
                } else if (WagesInt >= 335 && WagesInt < 345) {
                    result = BigDecimal.valueOf(4);
                } else if (WagesInt >= 345 && WagesInt < 355) {
                    result = BigDecimal.valueOf(5);
                } else if (WagesInt >= 355 && WagesInt < 365) {
                    result = BigDecimal.valueOf(6);
                } else if (WagesInt >= 365 && WagesInt < 375) {
                    result = BigDecimal.valueOf(7);
                } else if (WagesInt >= 375 && WagesInt < 385) {
                    result = BigDecimal.valueOf(8);
                } else if (WagesInt >= 385 && WagesInt < 395) {
                    result = BigDecimal.valueOf(9);
                } else if (WagesInt >= 395 && WagesInt < 405) {
                    result = BigDecimal.valueOf(10);
                } else if (WagesInt >= 405 && WagesInt < 415) {
                    result = BigDecimal.valueOf(11);
                } else if (WagesInt >= 415 && WagesInt < 425) {
                    result = BigDecimal.valueOf(12);
                } else if (WagesInt >= 425 && WagesInt < 435) {
                    result = BigDecimal.valueOf(13);
                } else if (WagesInt >= 435 && WagesInt < 445) {
                    result = BigDecimal.valueOf(14);
                } else if (WagesInt >= 445 && WagesInt < 455) {
                    result = BigDecimal.valueOf(15);
                } else if (WagesInt >= 455 && WagesInt < 465) {
                    result = BigDecimal.valueOf(16);
                } else if (WagesInt >= 465 && WagesInt < 475) {
                    result = BigDecimal.valueOf(17);
                } else if (WagesInt >= 475 && WagesInt < 485) {
                    result = BigDecimal.valueOf(18);
                } else if (WagesInt >= 485 && WagesInt < 495) {
                    result = BigDecimal.valueOf(19);
                } else if (WagesInt >= 495 && WagesInt < 505) {
                    result = BigDecimal.valueOf(20);
                } else if (WagesInt >= 505 && WagesInt < 515) {
                    result = BigDecimal.valueOf(21);
                } else if (WagesInt >= 515 && WagesInt < 525) {
                    result = BigDecimal.valueOf(22);
                } else if (WagesInt >= 525 && WagesInt < 535) {
                    result = BigDecimal.valueOf(23);
                } else if (WagesInt >= 535 && WagesInt < 545) {
                    result = BigDecimal.valueOf(24);
                } else if (WagesInt >= 545 && WagesInt < 555) {
                    result = BigDecimal.valueOf(25);
                } else if (WagesInt >= 555 && WagesInt < 570) {
                    result = BigDecimal.valueOf(26);
                } else if (WagesInt >= 570 && WagesInt < 585) {
                    result = BigDecimal.valueOf(28);
                } else if (WagesInt >= 585 && WagesInt < 600) {
                    result = BigDecimal.valueOf(30);
                } else if (WagesInt >= 600 && WagesInt < 615) {
                    result = BigDecimal.valueOf(32);
                } else if (WagesInt >= 615 && WagesInt < 630) {
                    result = BigDecimal.valueOf(33);
                } else if (WagesInt >= 630 && WagesInt < 645) {
                    result = BigDecimal.valueOf(35);
                } else if (WagesInt >= 645 && WagesInt < 660) {
                    result = BigDecimal.valueOf(37);
                } else if (WagesInt >= 660 && WagesInt < 675) {
                    result = BigDecimal.valueOf(39);
                }else{
                        //started using ternary
                        result = (WagesInt < 690) ? BigDecimal.valueOf(41) :
                                (WagesInt < 705) ? BigDecimal.valueOf(42) :
                                (WagesInt < 720) ? BigDecimal.valueOf(44) :
                                (WagesInt < 735) ? BigDecimal.valueOf(46) :
                                (WagesInt < 750) ? BigDecimal.valueOf(48) :
                                (WagesInt < 765) ? BigDecimal.valueOf(50) :
                                (WagesInt < 780) ? BigDecimal.valueOf(51) :
                                (WagesInt < 795) ? BigDecimal.valueOf(53) :
                                (WagesInt < 810) ? BigDecimal.valueOf(55) :
                                (WagesInt < 825) ? BigDecimal.valueOf(57) :
                                (WagesInt < 840) ? BigDecimal.valueOf(59) : (WagesInt < 855) ? BigDecimal.valueOf(60) : (WagesInt < 870) ? BigDecimal.valueOf(62) :  (WagesInt < 885) ? BigDecimal.valueOf(64) : (WagesInt < 900) ? BigDecimal.valueOf(66) : (WagesInt < 915) ? BigDecimal.valueOf(68) : (WagesInt < 930) ? BigDecimal.valueOf(69) : (WagesInt < 945) ? BigDecimal.valueOf(71) : (WagesInt < 960) ? BigDecimal.valueOf(73) : (WagesInt < 975) ? BigDecimal.valueOf(75) : (WagesInt < 990) ? BigDecimal.valueOf(77) : (WagesInt < 1005) ? BigDecimal.valueOf(78) : (WagesInt < 1020) ? BigDecimal.valueOf(80) : (WagesInt < 1035) ? BigDecimal.valueOf(82) : (WagesInt < 1050) ? BigDecimal.valueOf(84) : (WagesInt < 1065) ? BigDecimal.valueOf(86) : (WagesInt < 1080) ? BigDecimal.valueOf(87) : (WagesInt < 1095) ? BigDecimal.valueOf(89) : (WagesInt < 1110) ? BigDecimal.valueOf(91) : (WagesInt < 1125) ? BigDecimal.valueOf(93) :
                                (WagesInt < 1140) ? BigDecimal.valueOf(95): (WagesInt < 1155) ? BigDecimal.valueOf(96):  (WagesInt < 1170) ? BigDecimal.valueOf(98): (WagesInt < 1185) ? BigDecimal.valueOf(100): (WagesInt < 1200) ? BigDecimal.valueOf(102): (WagesInt < 1215) ? BigDecimal.valueOf(104): (WagesInt < 1230) ? BigDecimal.valueOf(105): (WagesInt < 1245) ? BigDecimal.valueOf(107): (WagesInt < 1260) ? BigDecimal.valueOf(109): (WagesInt < 1275) ? BigDecimal.valueOf(111): (WagesInt < 1290) ? BigDecimal.valueOf(113):  (WagesInt < 1310) ? BigDecimal.valueOf(116): (WagesInt < 1330) ? BigDecimal.valueOf(120): (WagesInt < 1350) ? BigDecimal.valueOf(125): (WagesInt < 1370) ? BigDecimal.valueOf(129): (WagesInt < 1390) ? BigDecimal.valueOf(134): (WagesInt < 1410) ? BigDecimal.valueOf(138): (WagesInt < 1430) ? BigDecimal.valueOf(142): (WagesInt < 1450) ? BigDecimal.valueOf(147):
                                (WagesInt < 1470) ? BigDecimal.valueOf(151): (WagesInt < 1490) ? BigDecimal.valueOf(156): (WagesInt < 1510) ? BigDecimal.valueOf(160): (WagesInt < 1530) ? BigDecimal.valueOf(164): (WagesInt < 1550) ? BigDecimal.valueOf(169): (WagesInt < 1570) ? BigDecimal.valueOf(173): (WagesInt < 1590) ? BigDecimal.valueOf(178): (WagesInt < 1610) ? BigDecimal.valueOf(182): (WagesInt < 1630) ? BigDecimal.valueOf(186): (WagesInt < 1650) ? BigDecimal.valueOf(191): (WagesInt < 1670) ? BigDecimal.valueOf(195): (WagesInt < 1690) ? BigDecimal.valueOf(200): (WagesInt < 1710) ? BigDecimal.valueOf(204): (WagesInt < 1730) ? BigDecimal.valueOf(208): (WagesInt < 1750) ? BigDecimal.valueOf(213): (WagesInt < 1770) ? BigDecimal.valueOf(217): (WagesInt < 1790) ? BigDecimal.valueOf(222): (WagesInt < 1810) ? BigDecimal.valueOf(226): (WagesInt < 1830) ? BigDecimal.valueOf(230): (WagesInt < 1850) ? BigDecimal.valueOf(235): (WagesInt < 1870) ? BigDecimal.valueOf(239): (WagesInt < 1890) ? BigDecimal.valueOf(244):
                                        (WagesInt < 1910) ? BigDecimal.valueOf(248):(WagesInt < 1930) ? BigDecimal.valueOf(252):(WagesInt < 1950) ? BigDecimal.valueOf(257):(WagesInt < 1970) ? BigDecimal.valueOf(261):(WagesInt < 1990) ? BigDecimal.valueOf(266):(WagesInt < 2010) ? BigDecimal.valueOf(270):(WagesInt < 2030) ? BigDecimal.valueOf(274):(WagesInt < 2050) ? BigDecimal.valueOf(279):(WagesInt < 2070) ? BigDecimal.valueOf(283):
                                                //last condition
                                       BigDecimal.valueOf(-1);
                    }
                }
            }

        return result;
    }
    //MO
    private static BigDecimal stateTaxFormula(int status, BigDecimal wages) {
        BigDecimal result = null;
        int MOWages = wages.intValue();
        if (status == 0) {
            //these are only guaranteed valid for 2024 semi-monthly
            if (MOWages < 685) {
                result = BigDecimal.valueOf(0);
            } else if (MOWages >= 685 && MOWages < 730) {
                result = BigDecimal.valueOf(1);
            } else if (MOWages >= 730 && MOWages < 775) {
                result = BigDecimal.valueOf(2);
            } else if (MOWages >= 775 && MOWages < 805) {
                result = BigDecimal.valueOf(3);
            } else if (MOWages >= 805 && MOWages < 835) {
                result = BigDecimal.valueOf(4);
            } else if (MOWages >= 835 && MOWages < 865) {
                result = BigDecimal.valueOf(5);
            } else if (MOWages >= 865 && MOWages < 895) {
                result = BigDecimal.valueOf(6);
            } else if (MOWages >= 895 && MOWages < 910) {
                result = BigDecimal.valueOf(7);
            } else if (MOWages >= 910 && MOWages < 940) {
                result = BigDecimal.valueOf(8);
            } else if (MOWages >= 940 && MOWages < 955) {
                result = BigDecimal.valueOf(9);
            } else if (MOWages >= 955 && MOWages < 985) {
                result = BigDecimal.valueOf(10);
            } else if (MOWages >= 985 && MOWages < 1000) {
                result = BigDecimal.valueOf(11);
            } else if (MOWages >= 1000 && MOWages < 1030) {
                result = BigDecimal.valueOf(12);
            } else if (MOWages >= 1030 && MOWages < 1045) {
                result = BigDecimal.valueOf(13);
            } else if (MOWages >= 1045 && MOWages < 1060) {
                result = BigDecimal.valueOf(14);
            } else if (MOWages >= 1060 && MOWages < 1090) {
                result = BigDecimal.valueOf(15);
            } else if (MOWages >= 1090 && MOWages < 1105) {
                result = BigDecimal.valueOf(16);
            } else if (MOWages >= 1105 && MOWages < 1135) {
                result = BigDecimal.valueOf(17);
            } else if (MOWages >= 1135 && MOWages < 1150) {
                result = BigDecimal.valueOf(18);
            } else if (MOWages >= 1150 && MOWages < 1165) {
                result = BigDecimal.valueOf(19);
            } else if (MOWages >= 1165 && MOWages < 1195) {
                result = BigDecimal.valueOf(20);
            } else if (MOWages >= 1195 && MOWages < 1210) {
                result = BigDecimal.valueOf(21);
            } else if (MOWages >= 1210 && MOWages < 1240) {
                result = BigDecimal.valueOf(22);
            } else if (MOWages >= 1240 && MOWages < 1255) {
                result = BigDecimal.valueOf(23);
            } else if (MOWages >= 1255 && MOWages < 1270) {
                result = BigDecimal.valueOf(24);
            } else if (MOWages >= 1270 && MOWages < 1300) {
                result = BigDecimal.valueOf(25);
            } else if (MOWages >= 130 && MOWages < 1315) {
                result = BigDecimal.valueOf(26);
            } else if (MOWages >= 1315 && MOWages < 1330) {
                result = BigDecimal.valueOf(27);
            } else if (MOWages >= 1330 && MOWages < 1360) {
                result = BigDecimal.valueOf(28);
            } else if (MOWages >= 1360 && MOWages < 1375) {
                result = BigDecimal.valueOf(29);
            } else if (MOWages >= 1375 && MOWages < 1405) {
                result = BigDecimal.valueOf(30);
            } else if (MOWages >= 1405 && MOWages < 1420) {
                result = BigDecimal.valueOf(31);
            } else if (MOWages >= 1420 && MOWages < 1435) {
                result = BigDecimal.valueOf(32);
            } else if (MOWages >= 1435 && MOWages < 1465) {
                result = BigDecimal.valueOf(33);
            } else if (MOWages >= 1465 && MOWages < 1480) {
                result = BigDecimal.valueOf(34);
            } else if (MOWages >= 1480 && MOWages < 1510) {
                result = BigDecimal.valueOf(35);
            } else if (MOWages >= 1510 && MOWages < 1525) {
                result = BigDecimal.valueOf(36);
            } else if (MOWages >= 1525 && MOWages < 1540) {
                result = BigDecimal.valueOf(37);
            } else if (MOWages >= 1540 && MOWages < 1570) {
                result = BigDecimal.valueOf(38);
            } else if (MOWages >= 1570 && MOWages < 1585) {
                result = BigDecimal.valueOf(39);
            } else if (MOWages >= 1585 && MOWages < 1615) {
                result = BigDecimal.valueOf(40);
            } else if (MOWages >= 1615 && MOWages < 1630) {
                result = BigDecimal.valueOf(41);
            } else if (MOWages >= 1630 && MOWages < 1645) {
                result = BigDecimal.valueOf(42);
            } else if (MOWages >= 1645 && MOWages < 1675) {
                result = BigDecimal.valueOf(43);
            } else if (MOWages >= 1675 && MOWages < 1690) {
                result = BigDecimal.valueOf(44);
            } else if (MOWages >= 1690 && MOWages < 1705) {
                result = BigDecimal.valueOf(45);
            } else if (MOWages >= 1705 && MOWages < 1735) {
                result = BigDecimal.valueOf(46);
            } else if (MOWages >= 1735 && MOWages < 1750) {
                result = BigDecimal.valueOf(47);
            } else if (MOWages >= 1750 && MOWages < 1780) {
                result = BigDecimal.valueOf(48);
            } else if (MOWages >= 1780 && MOWages < 1795) {
                result = BigDecimal.valueOf(49);
            } else if (MOWages >= 1795 && MOWages < 1810) {
                result = BigDecimal.valueOf(50);
            } else if (MOWages >= 1810 && MOWages < 1840) {
                result = BigDecimal.valueOf(51);
            } else if (MOWages >= 1840 && MOWages < 1855) {
                result = BigDecimal.valueOf(52);
            } else if (MOWages >= 1855 && MOWages < 1885) {
                result = BigDecimal.valueOf(53);
            } else if (MOWages >= 1885 && MOWages < 1900) {
                result = BigDecimal.valueOf(54);
            } else if (MOWages >= 1900 && MOWages < 1915) {
                result = BigDecimal.valueOf(55);
            } else if (MOWages >= 1915 && MOWages < 1945) {
                result = BigDecimal.valueOf(56);
            } else if (MOWages >= 1945 && MOWages < 1960) {
                result = BigDecimal.valueOf(57);
            } else if (MOWages >= 1960 && MOWages < 1990) {
                result = BigDecimal.valueOf(58);
            } else if (MOWages >= 1990 && MOWages < 2005) {
                result = BigDecimal.valueOf(59);
            } else if (MOWages >= 2005 && MOWages < 2020) {
                result = BigDecimal.valueOf(60);
            } else if (MOWages >= 2020 && MOWages < 2050) {
                result = BigDecimal.valueOf(61);
            } else if (MOWages >= 2050 && MOWages < 2065) {
                result = BigDecimal.valueOf(62);
            } else if (MOWages >= 2065 && MOWages < 2080) {
                result = BigDecimal.valueOf(63);
            } else if (MOWages >= 2080 && MOWages < 2110) {
                result = BigDecimal.valueOf(64);
            } else if (MOWages >= 2110 && MOWages < 2125) {
                result = BigDecimal.valueOf(65);
            } else if (MOWages >= 2125 && MOWages < 2155) {
                result = BigDecimal.valueOf(66);
            } else if (MOWages >= 2155 && MOWages < 2170) {
                result = BigDecimal.valueOf(67);
            } else if (MOWages >= 2170 && MOWages < 2185) {
                result = BigDecimal.valueOf(68);
            } else if (MOWages >= 2185 && MOWages < 2215) {
                result = BigDecimal.valueOf(69);
            } else if (MOWages >= 2215 && MOWages < 2230) {
                result = BigDecimal.valueOf(70);
            } else if (MOWages >= 2230 && MOWages < 2260) {
                result = BigDecimal.valueOf(71);
            } else if (MOWages >= 2260 && MOWages < 2275) {
                result = BigDecimal.valueOf(72);
            } else if (MOWages >= 2275 && MOWages < 2290) {
                result = BigDecimal.valueOf(73);
            } else if (MOWages >= 2290 && MOWages < 2320) {
                result = BigDecimal.valueOf(74);
            } else if (MOWages >= 2320 && MOWages < 2335) {
                result = BigDecimal.valueOf(75);
            } else if (MOWages >= 2335 && MOWages < 2365) {
                result = BigDecimal.valueOf(76);
            } else if (MOWages >= 2365 && MOWages < 2380) {
                result = BigDecimal.valueOf(77);
            } else if (MOWages >= 2380 && MOWages < 2395) {
                result = BigDecimal.valueOf(78);
            } else if (MOWages >= 2395 && MOWages < 2425) {
                result = BigDecimal.valueOf(79);
            } else if (MOWages >= 2425 && MOWages < 2440) {
                result = BigDecimal.valueOf(80);
            } else if (MOWages >= 2440 && MOWages < 2455) {
                result = BigDecimal.valueOf(81);
            } else if (MOWages >= 2455 && MOWages < 2485) {
                result = BigDecimal.valueOf(82);
            } else if (MOWages >= 2485 && MOWages < 2500) {
                result = BigDecimal.valueOf(83);
            } else {
                result = BigDecimal.valueOf(84);

            }
        }
        if (status == 1) {
            if (MOWages < 985) {
                result = BigDecimal.valueOf(0);
            } else if (MOWages >= 985 && MOWages < 1030) {
                result = BigDecimal.valueOf(1);
            } else if (MOWages >= 1030 && MOWages < 1075) {
                result = BigDecimal.valueOf(2);
            } else if (MOWages >= 1075 && MOWages < 1105) {
                result = BigDecimal.valueOf(3);
            } else if (MOWages >= 1105 && MOWages < 1135) {
                result = BigDecimal.valueOf(4);
            } else if (MOWages >= 1135 && MOWages < 1165) {
                result = BigDecimal.valueOf(5);
            } else if (MOWages >= 1165 && MOWages < 1195) {
                result = BigDecimal.valueOf(6);
            } else if (MOWages >= 1195 && MOWages < 1225) {
                result = BigDecimal.valueOf(7);
            } else if (MOWages >= 1225 && MOWages < 1240) {
                result = BigDecimal.valueOf(8);
            } else if (MOWages >= 1240 && MOWages < 1270) {
                result = BigDecimal.valueOf(9);
            } else if (MOWages >= 1270 && MOWages < 1285) {
                result = BigDecimal.valueOf(10);
            } else if (MOWages >= 1285 && MOWages < 1315) {
                result = BigDecimal.valueOf(11);
            } else if (MOWages >= 1315 && MOWages < 1330) {
                result = BigDecimal.valueOf(12);
            } else if (MOWages >= 1330 && MOWages < 1345) {
                result = BigDecimal.valueOf(13);
            } else if (MOWages >= 1345 && MOWages < 1375) {
                result = BigDecimal.valueOf(14);
            } else if (MOWages >= 1375 && MOWages < 1390) {
                result = BigDecimal.valueOf(15);
            } else if (MOWages >= 1390 && MOWages < 1405) {
                result = BigDecimal.valueOf(16);
            } else if (MOWages >= 1405 && MOWages < 1435) {
                result = BigDecimal.valueOf(17);
            } else if (MOWages >= 1435 && MOWages < 1450) {
                result = BigDecimal.valueOf(18);
            } else if (MOWages >= 1450 && MOWages < 1480) {
                result = BigDecimal.valueOf(19);
            } else if (MOWages >= 1480 && MOWages < 1495) {
                result = BigDecimal.valueOf(20);
            } else if (MOWages >= 1495 && MOWages < 1510) {
                result = BigDecimal.valueOf(21);
            } else if (MOWages >= 1510 && MOWages < 1540) {
                result = BigDecimal.valueOf(22);
            } else if (MOWages >= 1540 && MOWages < 1555) {
                result = BigDecimal.valueOf(23);
            } else if (MOWages >= 1555 && MOWages < 1585) {
                result = BigDecimal.valueOf(24);
            } else if (MOWages >= 1585 && MOWages < 1600) {
                result = BigDecimal.valueOf(25);
            } else if (MOWages >= 1600 && MOWages < 1615) {
                result = BigDecimal.valueOf(26);
            } else if (MOWages >= 1615 && MOWages < 1645) {
                result = BigDecimal.valueOf(27);
            } else if (MOWages >= 1645 && MOWages < 1660) {
                result = BigDecimal.valueOf(28);
            } else if (MOWages >= 1660 && MOWages < 1690) {
                result = BigDecimal.valueOf(29);
            } else if (MOWages >= 1690 && MOWages < 1705) {
                result = BigDecimal.valueOf(30);
            } else if (MOWages >= 1705 && MOWages < 1720) {
                result = BigDecimal.valueOf(31);
            } else if (MOWages >= 1720 && MOWages < 1750) {
                result = BigDecimal.valueOf(32);
            } else if (MOWages >= 1750 && MOWages < 1765) {
                result = BigDecimal.valueOf(33);
            } else if (MOWages >= 1765 && MOWages < 1780) {
                result = BigDecimal.valueOf(34);
            } else if (MOWages >= 1780 && MOWages < 1810) {
                result = BigDecimal.valueOf(35);
            } else if (MOWages >= 1810 && MOWages < 1825) {
                result = BigDecimal.valueOf(36);
            } else if (MOWages >= 1825 && MOWages < 1855) {
                result = BigDecimal.valueOf(37);
            } else if (MOWages >= 1855 && MOWages < 1870) {
                result = BigDecimal.valueOf(38);
            } else if (MOWages >= 1870 && MOWages < 1885) {
                result = BigDecimal.valueOf(39);
            } else if (MOWages >= 1885 && MOWages < 1915) {
                result = BigDecimal.valueOf(40);
            } else if (MOWages >= 1915 && MOWages < 1930) {
                result = BigDecimal.valueOf(41);
            } else if (MOWages >= 1930 && MOWages < 1960) {
                result = BigDecimal.valueOf(42);
            } else if (MOWages >= 1960 && MOWages < 1975) {
                result = BigDecimal.valueOf(43);
            } else if (MOWages >= 1975 && MOWages < 1990) {
                result = BigDecimal.valueOf(44);
            } else if (MOWages >= 1990 && MOWages < 2020) {
                result = BigDecimal.valueOf(45);
            } else if (MOWages >= 2020 && MOWages < 2035) {
                result = BigDecimal.valueOf(46);
            } else if (MOWages >= 2035 && MOWages < 2065) {
                result = BigDecimal.valueOf(47);
            } else if (MOWages >= 2065 && MOWages < 2080) {
                result = BigDecimal.valueOf(48);
            } else if (MOWages >= 2080 && MOWages < 2095) {
                result = BigDecimal.valueOf(49);
            } else if (MOWages >= 2095 && MOWages < 2125) {
                result = BigDecimal.valueOf(50);
            } else if (MOWages >= 2125 && MOWages < 2140) {
                result = BigDecimal.valueOf(51);
            } else if (MOWages >= 2140 && MOWages < 2155) {
                result = BigDecimal.valueOf(51);
            } else if (MOWages >= 2140 && MOWages < 2155) {
                result = BigDecimal.valueOf(52);
            } else if (MOWages >= 2155 && MOWages < 2170) {
                result = BigDecimal.valueOf(53);
            } else if (MOWages >= 2170 && MOWages < 2185) {
                result = BigDecimal.valueOf(53);
            } else if (MOWages >= 2185 && MOWages < 2200) {
                result = BigDecimal.valueOf(54);
            } else if (MOWages >= 2200 && MOWages < 2215) {
                result = BigDecimal.valueOf(55);
            } else if (MOWages >= 2215 && MOWages < 2230) {
                result = BigDecimal.valueOf(55);
            } else if (MOWages >= 2230 && MOWages < 2245) {
                result = BigDecimal.valueOf(56);
            } else if (MOWages >= 2245 && MOWages < 2260) {
                result = BigDecimal.valueOf(57);
            } else if (MOWages >= 2260 && MOWages < 2275) {
                result = BigDecimal.valueOf(58);
            } else if (MOWages >= 2275 && MOWages < 2290) {
                result = BigDecimal.valueOf(58);
            } else if (MOWages >= 2290 && MOWages < 2305) {
                result = BigDecimal.valueOf(59);
            } else if (MOWages >= 2305 && MOWages < 2320) {
                result = BigDecimal.valueOf(60);
            } else if (MOWages >= 2320 && MOWages < 2335) {
                result = BigDecimal.valueOf(60);
            } else if (MOWages >= 2335 && MOWages < 2350) {
                result = BigDecimal.valueOf(61);
            } else if (MOWages >= 2350 && MOWages < 2365) {
                result = BigDecimal.valueOf(62);
            } else if (MOWages >= 2365 && MOWages < 2380) {
                result = BigDecimal.valueOf(63);
            } else if (MOWages >= 2380 && MOWages < 2395) {
                result = BigDecimal.valueOf(63);
            } else if (MOWages >= 2395 && MOWages < 2410) {
                result = BigDecimal.valueOf(64);
            } else if (MOWages >= 2410 && MOWages < 2425) {
                result = BigDecimal.valueOf(65);
            } else if (MOWages >= 2425 && MOWages < 2440) {
                result = BigDecimal.valueOf(65);
            } else if (MOWages >= 2440 && MOWages < 2455) {
                result = BigDecimal.valueOf(66);
            } else if (MOWages >= 2455 && MOWages < 2470) {
                result = BigDecimal.valueOf(67);
            } else if (MOWages >= 2470 && MOWages < 2485) {
                result = BigDecimal.valueOf(68);
            } else if (MOWages >= 2485 && MOWages < 2500) {
                result = BigDecimal.valueOf(68);
            } else {
                result = BigDecimal.valueOf(69);
            }

        }

        if (status == 2) {
            if (MOWages < 1300) {
                result = BigDecimal.valueOf(0);
            } else if (MOWages >= 1300 && MOWages < 1345) {
                result = BigDecimal.valueOf(1);
            }else if (MOWages >= 1345 && MOWages < 1375) {
                result = BigDecimal.valueOf(2);
            } else if (MOWages >= 1375 && MOWages < 1420) {
                result = BigDecimal.valueOf(3);
            } else if (MOWages >= 1420 && MOWages < 1450) {
                result = BigDecimal.valueOf(4);
            } else if (MOWages >= 1450 && MOWages < 1480) {
                result = BigDecimal.valueOf(5);
            } else if (MOWages >= 1480 && MOWages < 1495) {
                result = BigDecimal.valueOf(6);
            } else if (MOWages >= 1495 && MOWages < 1525) {
                result = BigDecimal.valueOf(7);
            } else if (MOWages >= 1525 && MOWages < 1540) {
                result = BigDecimal.valueOf(8);
            } else if (MOWages >= 1540 && MOWages < 1570) {
                result = BigDecimal.valueOf(9);
            } else if (MOWages >= 1570 && MOWages < 1585) {
                result = BigDecimal.valueOf(10);
            } else if (MOWages >= 1585 && MOWages < 1615) {
                result = BigDecimal.valueOf(11);
            } else if (MOWages >= 1615 && MOWages < 1630) {
                result = BigDecimal.valueOf(12);
            } else if (MOWages >= 1630 && MOWages < 1645) {
                result = BigDecimal.valueOf(13);
            } else if (MOWages >= 1645 && MOWages < 1660) {
                result = BigDecimal.valueOf(13);
            } else if (MOWages >= 1660 && MOWages < 1675) {
                result = BigDecimal.valueOf(14);
            } else if (MOWages >= 1675 && MOWages < 1690) {
                result = BigDecimal.valueOf(15);
            } else if (MOWages >= 1690 && MOWages < 1705) {
                result = BigDecimal.valueOf(16);
            } else if (MOWages >= 1705 && MOWages < 1720) {
                result = BigDecimal.valueOf(16);
            } else if (MOWages >= 1720 && MOWages < 1735) {
                result = BigDecimal.valueOf(17);
            } else if (MOWages >= 1735 && MOWages < 1750) {
                result = BigDecimal.valueOf(18);
            } else if (MOWages >= 1750 && MOWages < 1765) {
                result = BigDecimal.valueOf(18);
            } else if (MOWages >= 1765 && MOWages < 1780) {
                result = BigDecimal.valueOf(19);
            } else if (MOWages >= 1780 && MOWages < 1795) {
                result = BigDecimal.valueOf(20);
            } else if (MOWages >= 1795 && MOWages < 1810) {
                result = BigDecimal.valueOf(21);
            } else if (MOWages >= 1810 && MOWages < 1825) {
                result = BigDecimal.valueOf(21);
            } else if (MOWages >= 1825 && MOWages < 1840) {
                result = BigDecimal.valueOf(22);
            } else if (MOWages >= 1840 && MOWages < 1855) {
                result = BigDecimal.valueOf(23);
            } else if (MOWages >= 1855 && MOWages < 1870) {
                result = BigDecimal.valueOf(24);
            } else if (MOWages >= 1870 && MOWages < 1885) {
                result = BigDecimal.valueOf(24);
            } else if (MOWages >= 1885 && MOWages < 1900) {
                result = BigDecimal.valueOf(25);
            } else if (MOWages >= 1900 && MOWages < 1915) {
                result = BigDecimal.valueOf(26);
            } else if (MOWages >= 1915 && MOWages < 1930) {
                result = BigDecimal.valueOf(26);
            } else if (MOWages >= 1930 && MOWages < 1945) {
                result = BigDecimal.valueOf(27);
            } else if (MOWages >= 1945 && MOWages < 1960) {
                result = BigDecimal.valueOf(28);
            } else if (MOWages >= 1960 && MOWages < 1975) {
                result = BigDecimal.valueOf(29);
            } else if (MOWages >= 1975 && MOWages < 1990) {
                result = BigDecimal.valueOf(29);
            } else if (MOWages >= 1990 && MOWages < 2005) {
                result = BigDecimal.valueOf(30);
            } else if (MOWages >= 2005 && MOWages < 2020) {
                result = BigDecimal.valueOf(31);
            } else if (MOWages >= 2020 && MOWages < 2035) {
                result = BigDecimal.valueOf(31);
            } else if (MOWages >= 2035 && MOWages < 2050) {
                result = BigDecimal.valueOf(32);
            } else if (MOWages >= 2050 && MOWages < 2065) {
                result = BigDecimal.valueOf(33);
            } else if (MOWages >= 2065 && MOWages < 2080) {
                result = BigDecimal.valueOf(34);
            } else if (MOWages >= 2080 && MOWages < 2095) {
                result = BigDecimal.valueOf(34);
            } else if (MOWages >= 2095 && MOWages < 2110) {
                result = BigDecimal.valueOf(35);
            } else if (MOWages >= 2110 && MOWages < 2125) {
                result = BigDecimal.valueOf(36);
            } else if (MOWages >= 2125 && MOWages < 2140) {
                result = BigDecimal.valueOf(36);
            } else if (MOWages >= 2140 && MOWages < 2155) {
                result = BigDecimal.valueOf(37);
            } else if (MOWages >= 2155 && MOWages < 2170) {
                result = BigDecimal.valueOf(38);
            } else if (MOWages >= 2170 && MOWages < 2185) {
                result = BigDecimal.valueOf(39);
            } else if (MOWages >= 2185 && MOWages < 2200) {
                result = BigDecimal.valueOf(39);
            } else if (MOWages >= 2200 && MOWages < 2215) {
                result = BigDecimal.valueOf(40);
            } else if (MOWages >= 2215 && MOWages < 2230) {
                result = BigDecimal.valueOf(41);
            } else if (MOWages >= 2230 && MOWages < 2245) {
                result = BigDecimal.valueOf(42);
            } else if (MOWages >= 2245 && MOWages < 2260) {
                result = BigDecimal.valueOf(42);
            } else if (MOWages >= 2260 && MOWages < 2275) {
                result = BigDecimal.valueOf(43);
            } else if (MOWages >= 2275 && MOWages < 2290) {
                result = BigDecimal.valueOf(44);
            } else if (MOWages >= 2290 && MOWages < 2305) {
                result = BigDecimal.valueOf(44);
            } else if (MOWages >= 2305 && MOWages < 2320) {
                result = BigDecimal.valueOf(45);
            } else if (MOWages >= 2320 && MOWages < 2335) {
                result = BigDecimal.valueOf(46);
            } else if (MOWages >= 2335 && MOWages < 2350) {
                result = BigDecimal.valueOf(47);
            } else if (MOWages >= 2350 && MOWages < 2365) {
                result = BigDecimal.valueOf(47);
            } else if (MOWages >= 2365 && MOWages < 2380) {
                result = BigDecimal.valueOf(48);
            } else if (MOWages >= 2380 && MOWages < 2395) {
                result = BigDecimal.valueOf(49);
            } else if (MOWages >= 2395 && MOWages < 2410) {
                result = BigDecimal.valueOf(49);
            } else if (MOWages >= 2410 && MOWages < 2425) {
                result = BigDecimal.valueOf(50);
            } else if (MOWages >= 2425 && MOWages < 2440) {
                result = BigDecimal.valueOf(51);
            } else if (MOWages >= 2440 && MOWages < 2455) {
                result = BigDecimal.valueOf(52);
            } else if (MOWages >= 2455 && MOWages < 2470) {
                result = BigDecimal.valueOf(52);
            } else if (MOWages >= 2470 && MOWages < 2485) {
                result = BigDecimal.valueOf(53);
            } else if (MOWages >= 2485 && MOWages < 2500) {
                result = BigDecimal.valueOf(54);
            } else if (MOWages >= 2500 && MOWages < 2515) {
                result = BigDecimal.valueOf(54);
            } else  {
                result = BigDecimal.valueOf(55);
            }
        }


        return result;
    }

    private static BigDecimal federalTaxFormulaWeekly(List<String> status, BigDecimal wages) {
        BigDecimal result = null;
        int WagesInt = wages.intValue();
        if (status.get(0).equals("0")) {
            //Married Filing Jointly Standing withholding
            if (status.get(1).equals("0")) {
                result =  (WagesInt < 565) ? BigDecimal.valueOf(0) : (WagesInt < 575) ? BigDecimal.valueOf(1) : (WagesInt < 585) ? BigDecimal.valueOf(2) : (WagesInt < 595) ? BigDecimal.valueOf(3) : (WagesInt < 605) ? BigDecimal.valueOf(4) : (WagesInt < 615) ? BigDecimal.valueOf(5) :(WagesInt < 625) ? BigDecimal.valueOf(6) : (WagesInt < 635) ? BigDecimal.valueOf(7) : (WagesInt < 645) ? BigDecimal.valueOf(8) : (WagesInt < 655) ? BigDecimal.valueOf(9) : (WagesInt < 665) ? BigDecimal.valueOf(10) :(WagesInt < 675) ? BigDecimal.valueOf(11) : (WagesInt < 685) ? BigDecimal.valueOf(12) :
                        (WagesInt < 695) ? BigDecimal.valueOf(13) : (WagesInt < 705) ? BigDecimal.valueOf(14) : (WagesInt < 715) ? BigDecimal.valueOf(15) :(WagesInt < 725) ? BigDecimal.valueOf(16) : (WagesInt < 735) ? BigDecimal.valueOf(17) :(WagesInt < 745) ? BigDecimal.valueOf(18) : (WagesInt < 755) ? BigDecimal.valueOf(19) : (WagesInt < 765) ? BigDecimal.valueOf(20) : (WagesInt < 775) ? BigDecimal.valueOf(21) :(WagesInt < 785) ? BigDecimal.valueOf(22) : (WagesInt < 795) ? BigDecimal.valueOf(23) : (WagesInt < 805) ? BigDecimal.valueOf(24) :(WagesInt < 815) ? BigDecimal.valueOf(25) : (WagesInt < 825) ? BigDecimal.valueOf(26) : (WagesInt < 835) ? BigDecimal.valueOf(27) :(WagesInt < 845) ? BigDecimal.valueOf(28) : (WagesInt < 855) ? BigDecimal.valueOf(29) : (WagesInt < 865) ? BigDecimal.valueOf(30) :(WagesInt < 875) ? BigDecimal.valueOf(31) : (WagesInt < 885) ? BigDecimal.valueOf(32) : (WagesInt < 895) ? BigDecimal.valueOf(33) :
                                (WagesInt < 905) ? BigDecimal.valueOf(34) :(WagesInt < 915) ? BigDecimal.valueOf(35) : (WagesInt < 925) ? BigDecimal.valueOf(36) :(WagesInt < 935) ? BigDecimal.valueOf(37) :(WagesInt < 945) ? BigDecimal.valueOf(38) : (WagesInt < 955) ? BigDecimal.valueOf(39) : (WagesInt < 965) ? BigDecimal.valueOf(40) :(WagesInt < 975) ? BigDecimal.valueOf(41) : (WagesInt < 985) ? BigDecimal.valueOf(42) :(WagesInt < 995) ? BigDecimal.valueOf(43) : (WagesInt < 1005) ? BigDecimal.valueOf(44) :(WagesInt < 1015) ? BigDecimal.valueOf(45) : (WagesInt < 1025) ? BigDecimal.valueOf(46) :(WagesInt < 1035) ? BigDecimal.valueOf(47) : (WagesInt < 1045) ? BigDecimal.valueOf(48) :(WagesInt < 1055) ? BigDecimal.valueOf(50) : (WagesInt < 1065) ? BigDecimal.valueOf(51) :(WagesInt < 1075) ? BigDecimal.valueOf(52) : (WagesInt < 1085) ? BigDecimal.valueOf(53) :(WagesInt < 1095) ? BigDecimal.valueOf(54) : (WagesInt < 1105) ? BigDecimal.valueOf(56) :(WagesInt < 1115) ? BigDecimal.valueOf(57) : (WagesInt < 1125) ? BigDecimal.valueOf(58) :(WagesInt < 1135) ? BigDecimal.valueOf(59) : (WagesInt < 1145) ? BigDecimal.valueOf(60) :(WagesInt < 1155) ? BigDecimal.valueOf(62) : (WagesInt < 1165) ? BigDecimal.valueOf(63) :(WagesInt < 1175) ? BigDecimal.valueOf(64) : (WagesInt < 1185) ? BigDecimal.valueOf(65) :(WagesInt < 1195) ? BigDecimal.valueOf(66) :
                                        (WagesInt < 1205) ? BigDecimal.valueOf(68) : (WagesInt < 1215) ? BigDecimal.valueOf(69) : (WagesInt < 1225) ? BigDecimal.valueOf(70) : (WagesInt < 1235) ? BigDecimal.valueOf(71) : (WagesInt < 1245) ? BigDecimal.valueOf(72) :(WagesInt < 1255) ? BigDecimal.valueOf(74) : (WagesInt < 1265) ? BigDecimal.valueOf(75) : (WagesInt < 1275) ? BigDecimal.valueOf(76) : (WagesInt < 1285) ? BigDecimal.valueOf(77) : (WagesInt < 1295) ? BigDecimal.valueOf(78) : (WagesInt < 1305) ? BigDecimal.valueOf(80) : (WagesInt < 1315) ? BigDecimal.valueOf(81) : (WagesInt < 1325) ? BigDecimal.valueOf(82) : (WagesInt < 1335) ? BigDecimal.valueOf(83) : (WagesInt < 1345) ? BigDecimal.valueOf(84) : (WagesInt < 1355) ? BigDecimal.valueOf(86) :
                                                (WagesInt < 1365) ? BigDecimal.valueOf(87) :(WagesInt < 1375) ? BigDecimal.valueOf(88) :(WagesInt < 1385) ? BigDecimal.valueOf(89) : (WagesInt < 1395) ? BigDecimal.valueOf(90) : (WagesInt < 1405) ? BigDecimal.valueOf(92) : (WagesInt < 1415) ? BigDecimal.valueOf(93) : (WagesInt < 1425) ? BigDecimal.valueOf(94) :
(WagesInt < 1435) ? BigDecimal.valueOf(95):(WagesInt < 1445) ? BigDecimal.valueOf(96):(WagesInt < 1455) ? BigDecimal.valueOf(98):(WagesInt < 1465) ? BigDecimal.valueOf(99):(WagesInt < 1475) ? BigDecimal.valueOf(100):(WagesInt < 1485) ? BigDecimal.valueOf(101):(WagesInt < 1495) ? BigDecimal.valueOf(102):(WagesInt < 1505) ? BigDecimal.valueOf(104):(WagesInt < 1515) ? BigDecimal.valueOf(105):(WagesInt < 1525) ? BigDecimal.valueOf(106):(WagesInt < 1535) ? BigDecimal.valueOf(107):(WagesInt < 1545) ? BigDecimal.valueOf(108):(WagesInt < 1555) ? BigDecimal.valueOf(110):(WagesInt < 1565) ? BigDecimal.valueOf(111):(WagesInt < 1575) ? BigDecimal.valueOf(112):(WagesInt < 1585) ? BigDecimal.valueOf(113):(WagesInt < 1595) ? BigDecimal.valueOf(114):(WagesInt < 1605) ? BigDecimal.valueOf(116):(WagesInt < 1615) ? BigDecimal.valueOf(117):(WagesInt < 1625) ? BigDecimal.valueOf(118):(WagesInt < 1635) ? BigDecimal.valueOf(119):(WagesInt < 1645) ? BigDecimal.valueOf(120):(WagesInt < 1655) ? BigDecimal.valueOf(122):(WagesInt < 1665) ? BigDecimal.valueOf(123):(WagesInt < 1675) ? BigDecimal.valueOf(124):
(WagesInt < 1685) ? BigDecimal.valueOf(125):(WagesInt < 1695) ? BigDecimal.valueOf(126):(WagesInt < 1705) ? BigDecimal.valueOf(128):(WagesInt < 1715) ? BigDecimal.valueOf(129):(WagesInt < 1725) ? BigDecimal.valueOf(130):(WagesInt < 1735) ? BigDecimal.valueOf(131):(WagesInt < 1745) ? BigDecimal.valueOf(132):(WagesInt < 1755) ? BigDecimal.valueOf(134):(WagesInt < 1765) ? BigDecimal.valueOf(135):(WagesInt < 1775) ? BigDecimal.valueOf(136):(WagesInt < 1785) ? BigDecimal.valueOf(137):(WagesInt < 1795) ? BigDecimal.valueOf(138):(WagesInt < 1805) ? BigDecimal.valueOf(140):(WagesInt < 1815) ? BigDecimal.valueOf(141):(WagesInt < 1825) ? BigDecimal.valueOf(142):(WagesInt < 1835) ? BigDecimal.valueOf(143):(WagesInt < 1845) ? BigDecimal.valueOf(144):(WagesInt < 1855) ? BigDecimal.valueOf(146):(WagesInt < 1865) ? BigDecimal.valueOf(147):(WagesInt < 1875) ? BigDecimal.valueOf(148):(WagesInt < 1885) ? BigDecimal.valueOf(149):(WagesInt < 1895) ? BigDecimal.valueOf(150):(WagesInt < 1905) ? BigDecimal.valueOf(152):(WagesInt < 1915) ? BigDecimal.valueOf(153):(WagesInt < 1925) ? BigDecimal.valueOf(154):
                                                        BigDecimal.valueOf(-1);

            }
            //Married Filing Jointly W4 etc.
            if (status.get(1).equals("1")) {
                result = (WagesInt < 285) ? BigDecimal.valueOf(0) : (WagesInt < 295) ? BigDecimal.valueOf(1) : (WagesInt < 305) ? BigDecimal.valueOf(2) :(WagesInt < 315) ? BigDecimal.valueOf(3) : (WagesInt < 325) ? BigDecimal.valueOf(4) : (WagesInt < 335) ? BigDecimal.valueOf(5) :(WagesInt < 345) ? BigDecimal.valueOf(6) : (WagesInt < 355) ? BigDecimal.valueOf(7) : (WagesInt < 365) ? BigDecimal.valueOf(8) :(WagesInt < 375) ? BigDecimal.valueOf(9) : (WagesInt < 385) ? BigDecimal.valueOf(10) : (WagesInt < 395) ? BigDecimal.valueOf(11) :(WagesInt < 405) ? BigDecimal.valueOf(12) : (WagesInt < 415) ? BigDecimal.valueOf(13) : (WagesInt < 425) ? BigDecimal.valueOf(14) :(WagesInt < 435) ? BigDecimal.valueOf(15) : (WagesInt < 445) ? BigDecimal.valueOf(16) : (WagesInt < 455) ? BigDecimal.valueOf(17) :(WagesInt < 465) ? BigDecimal.valueOf(18) : (WagesInt < 475) ? BigDecimal.valueOf(19) : (WagesInt < 485) ? BigDecimal.valueOf(20) :(WagesInt < 495) ? BigDecimal.valueOf(21) :
                        (WagesInt < 505) ? BigDecimal.valueOf(22) : (WagesInt < 515) ? BigDecimal.valueOf(23) :(WagesInt < 525) ? BigDecimal.valueOf(24) : (WagesInt < 535) ? BigDecimal.valueOf(25) : (WagesInt < 545) ? BigDecimal.valueOf(27) : (WagesInt < 555) ? BigDecimal.valueOf(28) : (WagesInt < 565) ? BigDecimal.valueOf(29) :(WagesInt < 575) ? BigDecimal.valueOf(30) : (WagesInt < 585) ? BigDecimal.valueOf(31) : (WagesInt < 595) ? BigDecimal.valueOf(33) : (WagesInt < 605) ? BigDecimal.valueOf(34) : (WagesInt < 615) ? BigDecimal.valueOf(35) :(WagesInt < 625) ? BigDecimal.valueOf(36) : (WagesInt < 635) ? BigDecimal.valueOf(37) : (WagesInt < 645) ? BigDecimal.valueOf(39) : (WagesInt < 655) ? BigDecimal.valueOf(40) : (WagesInt < 665) ? BigDecimal.valueOf(41) :(WagesInt < 675) ? BigDecimal.valueOf(42) : (WagesInt < 685) ? BigDecimal.valueOf(43) :
                                (WagesInt < 695) ? BigDecimal.valueOf(45) : (WagesInt < 705) ? BigDecimal.valueOf(46) : (WagesInt < 715) ? BigDecimal.valueOf(47) :(WagesInt < 725) ? BigDecimal.valueOf(48) : (WagesInt < 735) ? BigDecimal.valueOf(49) :(WagesInt < 745) ? BigDecimal.valueOf(51) : (WagesInt < 755) ? BigDecimal.valueOf(52) : (WagesInt < 765) ? BigDecimal.valueOf(53) : (WagesInt < 775) ? BigDecimal.valueOf(54) :(WagesInt < 785) ? BigDecimal.valueOf(55) : (WagesInt < 795) ? BigDecimal.valueOf(57) : (WagesInt < 805) ? BigDecimal.valueOf(58) :(WagesInt < 815) ? BigDecimal.valueOf(59) : (WagesInt < 825) ? BigDecimal.valueOf(60) : (WagesInt < 835) ? BigDecimal.valueOf(61) :(WagesInt < 845) ? BigDecimal.valueOf(63) : (WagesInt < 855) ? BigDecimal.valueOf(64) : (WagesInt < 865) ? BigDecimal.valueOf(65) :(WagesInt < 875) ? BigDecimal.valueOf(66) : (WagesInt < 885) ? BigDecimal.valueOf(67) : (WagesInt < 895) ? BigDecimal.valueOf(69) :
                                        (WagesInt < 905) ? BigDecimal.valueOf(70) :(WagesInt < 915) ? BigDecimal.valueOf(71) : (WagesInt < 925) ? BigDecimal.valueOf(72) :(WagesInt < 935) ? BigDecimal.valueOf(73) :(WagesInt < 945) ? BigDecimal.valueOf(75) : (WagesInt < 955) ? BigDecimal.valueOf(76) : (WagesInt < 965) ? BigDecimal.valueOf(77) :(WagesInt < 975) ? BigDecimal.valueOf(78) : (WagesInt < 985) ? BigDecimal.valueOf(79) :(WagesInt < 995) ? BigDecimal.valueOf(81) : (WagesInt < 1005) ? BigDecimal.valueOf(82) :(WagesInt < 1015) ? BigDecimal.valueOf(83) : (WagesInt < 1025) ? BigDecimal.valueOf(84) :(WagesInt < 1035) ? BigDecimal.valueOf(85) : (WagesInt < 1045) ? BigDecimal.valueOf(87) :(WagesInt < 1055) ? BigDecimal.valueOf(88) : (WagesInt < 1065) ? BigDecimal.valueOf(89) :(WagesInt < 1075) ? BigDecimal.valueOf(90) : (WagesInt < 1085) ? BigDecimal.valueOf(91) :(WagesInt < 1095) ? BigDecimal.valueOf(93) : (WagesInt < 1105) ? BigDecimal.valueOf(94) :(WagesInt < 1115) ? BigDecimal.valueOf(95) : (WagesInt < 1125) ? BigDecimal.valueOf(96) :(WagesInt < 1135) ? BigDecimal.valueOf(97) : (WagesInt < 1145) ? BigDecimal.valueOf(99) :(WagesInt < 1155) ? BigDecimal.valueOf(100) : (WagesInt < 1165) ? BigDecimal.valueOf(101) :(WagesInt < 1175) ? BigDecimal.valueOf(102) : (WagesInt < 1185) ? BigDecimal.valueOf(103) :(WagesInt < 1195) ? BigDecimal.valueOf(105) :
                                                (WagesInt < 1205) ? BigDecimal.valueOf(107) : (WagesInt < 1215) ? BigDecimal.valueOf(109) : (WagesInt < 1225) ? BigDecimal.valueOf(111) : (WagesInt < 1235) ? BigDecimal.valueOf(114) : (WagesInt < 1245) ? BigDecimal.valueOf(116) :(WagesInt < 1255) ? BigDecimal.valueOf(118) : (WagesInt < 1265) ? BigDecimal.valueOf(120) : (WagesInt < 1275) ? BigDecimal.valueOf(122) : (WagesInt < 1285) ? BigDecimal.valueOf(125) : (WagesInt < 1295) ? BigDecimal.valueOf(127) : (WagesInt < 1305) ? BigDecimal.valueOf(129) : (WagesInt < 1315) ? BigDecimal.valueOf(131) : (WagesInt < 1325) ? BigDecimal.valueOf(133) : (WagesInt < 1335) ? BigDecimal.valueOf(136) : (WagesInt < 1345) ? BigDecimal.valueOf(138) : (WagesInt < 1355) ? BigDecimal.valueOf(140) :
                                                        (WagesInt < 1365) ? BigDecimal.valueOf(142) :(WagesInt < 1375) ? BigDecimal.valueOf(144) :(WagesInt < 1385) ? BigDecimal.valueOf(147) : (WagesInt < 1395) ? BigDecimal.valueOf(149) : (WagesInt < 1405) ? BigDecimal.valueOf(151) : (WagesInt < 1415) ? BigDecimal.valueOf(153) : (WagesInt < 1425) ? BigDecimal.valueOf(155) :
                                                                //left off end of page 14 fed tax pdf
(WagesInt < 1435) ? BigDecimal.valueOf(158):(WagesInt < 1445) ? BigDecimal.valueOf(160):(WagesInt < 1455) ? BigDecimal.valueOf(162):(WagesInt < 1465) ? BigDecimal.valueOf(164):(WagesInt < 1475) ? BigDecimal.valueOf(166):(WagesInt < 1485) ? BigDecimal.valueOf(169):(WagesInt < 1495) ? BigDecimal.valueOf(171):(WagesInt < 1505) ? BigDecimal.valueOf(173):(WagesInt < 1515) ? BigDecimal.valueOf(175):(WagesInt < 1525) ? BigDecimal.valueOf(177):(WagesInt < 1535) ? BigDecimal.valueOf(180):(WagesInt < 1545) ? BigDecimal.valueOf(182):(WagesInt < 1555) ? BigDecimal.valueOf(184):(WagesInt < 1565) ? BigDecimal.valueOf(186):(WagesInt < 1575) ? BigDecimal.valueOf(188):(WagesInt < 1585) ? BigDecimal.valueOf(191):(WagesInt < 1595) ? BigDecimal.valueOf(193):(WagesInt < 1605) ? BigDecimal.valueOf(195):(WagesInt < 1615) ? BigDecimal.valueOf(197):(WagesInt < 1625) ? BigDecimal.valueOf(199):(WagesInt < 1635) ? BigDecimal.valueOf(202):(WagesInt < 1645) ? BigDecimal.valueOf(204):(WagesInt < 1655) ? BigDecimal.valueOf(206):(WagesInt < 1665) ? BigDecimal.valueOf(208):(WagesInt < 1675) ? BigDecimal.valueOf(210):
(WagesInt < 1685) ? BigDecimal.valueOf(213):(WagesInt < 1695) ? BigDecimal.valueOf(215):(WagesInt < 1705) ? BigDecimal.valueOf(217):(WagesInt < 1715) ? BigDecimal.valueOf(219):(WagesInt < 1725) ? BigDecimal.valueOf(221):(WagesInt < 1735) ? BigDecimal.valueOf(224):(WagesInt < 1745) ? BigDecimal.valueOf(226):(WagesInt < 1755) ? BigDecimal.valueOf(228):(WagesInt < 1765) ? BigDecimal.valueOf(230):(WagesInt < 1775) ? BigDecimal.valueOf(232):(WagesInt < 1785) ? BigDecimal.valueOf(235):(WagesInt < 1795) ? BigDecimal.valueOf(237):(WagesInt < 1805) ? BigDecimal.valueOf(239):(WagesInt < 1815) ? BigDecimal.valueOf(241):(WagesInt < 1825) ? BigDecimal.valueOf(243):(WagesInt < 1835) ? BigDecimal.valueOf(246):(WagesInt < 1845) ? BigDecimal.valueOf(248):(WagesInt < 1855) ? BigDecimal.valueOf(250):(WagesInt < 1865) ? BigDecimal.valueOf(252):(WagesInt < 1875) ? BigDecimal.valueOf(254):(WagesInt < 1885) ? BigDecimal.valueOf(257):(WagesInt < 1895) ? BigDecimal.valueOf(259):(WagesInt < 1905) ? BigDecimal.valueOf(261):(WagesInt < 1915) ? BigDecimal.valueOf(263):(WagesInt < 1925) ? BigDecimal.valueOf(265):
                                                                BigDecimal.valueOf(-1);
            }
        }
        if (status.get(0).equals("1")) {
            //Head of Household Standing withholding
            if (status.get(1).equals("0")) {
                result = (WagesInt < 425) ? BigDecimal.valueOf(0) :(WagesInt < 435) ? BigDecimal.valueOf(1) : (WagesInt < 445) ? BigDecimal.valueOf(2) : (WagesInt < 455) ? BigDecimal.valueOf(3) :(WagesInt < 465) ? BigDecimal.valueOf(4) : (WagesInt < 475) ? BigDecimal.valueOf(5) : (WagesInt < 485) ? BigDecimal.valueOf(6) :(WagesInt < 495) ? BigDecimal.valueOf(7) :
                        (WagesInt < 505) ? BigDecimal.valueOf(8) : (WagesInt < 515) ? BigDecimal.valueOf(9) :(WagesInt < 525) ? BigDecimal.valueOf(10) : (WagesInt < 535) ? BigDecimal.valueOf(11) : (WagesInt < 545) ? BigDecimal.valueOf(12) : (WagesInt < 555) ? BigDecimal.valueOf(13) : (WagesInt < 565) ? BigDecimal.valueOf(14) :(WagesInt < 575) ? BigDecimal.valueOf(15) : (WagesInt < 585) ? BigDecimal.valueOf(16) : (WagesInt < 595) ? BigDecimal.valueOf(17) : (WagesInt < 605) ? BigDecimal.valueOf(19) : (WagesInt < 615) ? BigDecimal.valueOf(19) :(WagesInt < 625) ? BigDecimal.valueOf(20) : (WagesInt < 635) ? BigDecimal.valueOf(21) : (WagesInt < 645) ? BigDecimal.valueOf(22) : (WagesInt < 655) ? BigDecimal.valueOf(23) : (WagesInt < 665) ? BigDecimal.valueOf(24) :(WagesInt < 675) ? BigDecimal.valueOf(25) : (WagesInt < 685) ? BigDecimal.valueOf(26) :
                                (WagesInt < 695) ? BigDecimal.valueOf(27) : (WagesInt < 705) ? BigDecimal.valueOf(28) : (WagesInt < 715) ? BigDecimal.valueOf(29) :(WagesInt < 725) ? BigDecimal.valueOf(30) : (WagesInt < 735) ? BigDecimal.valueOf(31) :(WagesInt < 745) ? BigDecimal.valueOf(32) : (WagesInt < 755) ? BigDecimal.valueOf(33) : (WagesInt < 765) ? BigDecimal.valueOf(34) : (WagesInt < 775) ? BigDecimal.valueOf(35) :(WagesInt < 785) ? BigDecimal.valueOf(37) : (WagesInt < 795) ? BigDecimal.valueOf(38) : (WagesInt < 805) ? BigDecimal.valueOf(39) :(WagesInt < 815) ? BigDecimal.valueOf(40) : (WagesInt < 825) ? BigDecimal.valueOf(41) : (WagesInt < 835) ? BigDecimal.valueOf(43) :(WagesInt < 845) ? BigDecimal.valueOf(44) : (WagesInt < 855) ? BigDecimal.valueOf(45) : (WagesInt < 865) ? BigDecimal.valueOf(46) :(WagesInt < 875) ? BigDecimal.valueOf(47) : (WagesInt < 885) ? BigDecimal.valueOf(49) : (WagesInt < 895) ? BigDecimal.valueOf(50) :
                                        (WagesInt < 905) ? BigDecimal.valueOf(51) :(WagesInt < 915) ? BigDecimal.valueOf(52) : (WagesInt < 925) ? BigDecimal.valueOf(53) :(WagesInt < 935) ? BigDecimal.valueOf(55) :(WagesInt < 945) ? BigDecimal.valueOf(56) : (WagesInt < 955) ? BigDecimal.valueOf(57) : (WagesInt < 965) ? BigDecimal.valueOf(58) :(WagesInt < 975) ? BigDecimal.valueOf(59) : (WagesInt < 985) ? BigDecimal.valueOf(61) :(WagesInt < 995) ? BigDecimal.valueOf(62) : (WagesInt < 1005) ? BigDecimal.valueOf(63) :(WagesInt < 1015) ? BigDecimal.valueOf(64) : (WagesInt < 1025) ? BigDecimal.valueOf(65) :(WagesInt < 1035) ? BigDecimal.valueOf(67) : (WagesInt < 1045) ? BigDecimal.valueOf(68) :(WagesInt < 1055) ? BigDecimal.valueOf(69) : (WagesInt < 1065) ? BigDecimal.valueOf(70) :(WagesInt < 1075) ? BigDecimal.valueOf(71) : (WagesInt < 1085) ? BigDecimal.valueOf(73) :(WagesInt < 1095) ? BigDecimal.valueOf(74) : (WagesInt < 1105) ? BigDecimal.valueOf(75) :(WagesInt < 1115) ? BigDecimal.valueOf(76) : (WagesInt < 1125) ? BigDecimal.valueOf(77) :(WagesInt < 1135) ? BigDecimal.valueOf(79) : (WagesInt < 1145) ? BigDecimal.valueOf(80) :(WagesInt < 1155) ? BigDecimal.valueOf(81) : (WagesInt < 1165) ? BigDecimal.valueOf(82) :(WagesInt < 1175) ? BigDecimal.valueOf(83) : (WagesInt < 1185) ? BigDecimal.valueOf(85) :(WagesInt < 1195) ? BigDecimal.valueOf(86) :
                                                (WagesInt < 1205) ? BigDecimal.valueOf(87) : (WagesInt < 1215) ? BigDecimal.valueOf(88) : (WagesInt < 1225) ? BigDecimal.valueOf(89) : (WagesInt < 1235) ? BigDecimal.valueOf(91) : (WagesInt < 1245) ? BigDecimal.valueOf(92) :(WagesInt < 1255) ? BigDecimal.valueOf(93) : (WagesInt < 1265) ? BigDecimal.valueOf(94) : (WagesInt < 1275) ? BigDecimal.valueOf(95) : (WagesInt < 1285) ? BigDecimal.valueOf(97) : (WagesInt < 1295) ? BigDecimal.valueOf(98) : (WagesInt < 1305) ? BigDecimal.valueOf(99) : (WagesInt < 1315) ? BigDecimal.valueOf(100) : (WagesInt < 1325) ? BigDecimal.valueOf(101) : (WagesInt < 1335) ? BigDecimal.valueOf(103) : (WagesInt < 1345) ? BigDecimal.valueOf(104) : (WagesInt < 1355) ? BigDecimal.valueOf(105) :
                                                        (WagesInt < 1365) ? BigDecimal.valueOf(106) :(WagesInt < 1375) ? BigDecimal.valueOf(107) :(WagesInt < 1385) ? BigDecimal.valueOf(109) : (WagesInt < 1395) ? BigDecimal.valueOf(110) : (WagesInt < 1405) ? BigDecimal.valueOf(111) : (WagesInt < 1415) ? BigDecimal.valueOf(112) : (WagesInt < 1425) ? BigDecimal.valueOf(113) :
                                                                //left off end of page 14 fed tax pdf
(WagesInt < 1435) ? BigDecimal.valueOf(115):(WagesInt < 1445) ? BigDecimal.valueOf(116):(WagesInt < 1455) ? BigDecimal.valueOf(117):(WagesInt < 1465) ? BigDecimal.valueOf(118):(WagesInt < 1475) ? BigDecimal.valueOf(119):(WagesInt < 1485) ? BigDecimal.valueOf(121):(WagesInt < 1495) ? BigDecimal.valueOf(122):(WagesInt < 1505) ? BigDecimal.valueOf(123):(WagesInt < 1515) ? BigDecimal.valueOf(124):(WagesInt < 1525) ? BigDecimal.valueOf(125):(WagesInt < 1535) ? BigDecimal.valueOf(127):(WagesInt < 1545) ? BigDecimal.valueOf(128):(WagesInt < 1555) ? BigDecimal.valueOf(129):(WagesInt < 1565) ? BigDecimal.valueOf(130):(WagesInt < 1575) ? BigDecimal.valueOf(131):(WagesInt < 1585) ? BigDecimal.valueOf(133):(WagesInt < 1595) ? BigDecimal.valueOf(134):(WagesInt < 1605) ? BigDecimal.valueOf(135):(WagesInt < 1615) ? BigDecimal.valueOf(136):(WagesInt < 1625) ? BigDecimal.valueOf(137):(WagesInt < 1635) ? BigDecimal.valueOf(139):(WagesInt < 1645) ? BigDecimal.valueOf(140):(WagesInt < 1655) ? BigDecimal.valueOf(143):(WagesInt < 1665) ? BigDecimal.valueOf(145):(WagesInt < 1675) ? BigDecimal.valueOf(147):
(WagesInt < 1685) ? BigDecimal.valueOf(149):(WagesInt < 1695) ? BigDecimal.valueOf(151):(WagesInt < 1705) ? BigDecimal.valueOf(154):(WagesInt < 1715) ? BigDecimal.valueOf(156):(WagesInt < 1725) ? BigDecimal.valueOf(158):(WagesInt < 1735) ? BigDecimal.valueOf(160):(WagesInt < 1745) ? BigDecimal.valueOf(162):(WagesInt < 1755) ? BigDecimal.valueOf(165):(WagesInt < 1765) ? BigDecimal.valueOf(167):(WagesInt < 1775) ? BigDecimal.valueOf(169):(WagesInt < 1785) ? BigDecimal.valueOf(171):(WagesInt < 1795) ? BigDecimal.valueOf(173):(WagesInt < 1805) ? BigDecimal.valueOf(176):(WagesInt < 1815) ? BigDecimal.valueOf(178):(WagesInt < 1825) ? BigDecimal.valueOf(180):(WagesInt < 1835) ? BigDecimal.valueOf(182):(WagesInt < 1845) ? BigDecimal.valueOf(184):(WagesInt < 1855) ? BigDecimal.valueOf(187):(WagesInt < 1865) ? BigDecimal.valueOf(189):(WagesInt < 1875) ? BigDecimal.valueOf(191):(WagesInt < 1885) ? BigDecimal.valueOf(193):(WagesInt < 1895) ? BigDecimal.valueOf(195):(WagesInt < 1905) ? BigDecimal.valueOf(198):(WagesInt < 1915) ? BigDecimal.valueOf(200):(WagesInt < 1925) ? BigDecimal.valueOf(202):
                                                                BigDecimal.valueOf(-1);
            }
            //Head of Household W4 etc.
            if (status.get(1).equals("1")) {
                result = (WagesInt < 215) ? BigDecimal.valueOf(0): (WagesInt < 225) ? BigDecimal.valueOf(1): (WagesInt < 235) ? BigDecimal.valueOf(2):  (WagesInt < 245) ? BigDecimal.valueOf(3): (WagesInt < 255) ? BigDecimal.valueOf(4): (WagesInt < 265) ? BigDecimal.valueOf(5): (WagesInt < 275) ? BigDecimal.valueOf(6): (WagesInt < 285) ? BigDecimal.valueOf(7) : (WagesInt < 295) ? BigDecimal.valueOf(8) : (WagesInt < 305) ? BigDecimal.valueOf(9) :(WagesInt < 315) ? BigDecimal.valueOf(10) : (WagesInt < 325) ? BigDecimal.valueOf(11) : (WagesInt < 335) ? BigDecimal.valueOf(12) :(WagesInt < 345) ? BigDecimal.valueOf(13) : (WagesInt < 355) ? BigDecimal.valueOf(14) : (WagesInt < 365) ? BigDecimal.valueOf(15) :(WagesInt < 375) ? BigDecimal.valueOf(16) : (WagesInt < 385) ? BigDecimal.valueOf(17) : (WagesInt < 395) ? BigDecimal.valueOf(18) :(WagesInt < 405) ? BigDecimal.valueOf(20) : (WagesInt < 415) ? BigDecimal.valueOf(21) : (WagesInt < 425) ? BigDecimal.valueOf(22) :(WagesInt < 435) ? BigDecimal.valueOf(23) : (WagesInt < 445) ? BigDecimal.valueOf(24) : (WagesInt < 455) ? BigDecimal.valueOf(26) :(WagesInt < 465) ? BigDecimal.valueOf(27) : (WagesInt < 475) ? BigDecimal.valueOf(28) : (WagesInt < 485) ? BigDecimal.valueOf(29) :(WagesInt < 495) ? BigDecimal.valueOf(30) :
                        (WagesInt < 505) ? BigDecimal.valueOf(32) : (WagesInt < 515) ? BigDecimal.valueOf(33) :(WagesInt < 525) ? BigDecimal.valueOf(34) : (WagesInt < 535) ? BigDecimal.valueOf(35) : (WagesInt < 545) ? BigDecimal.valueOf(36) : (WagesInt < 555) ? BigDecimal.valueOf(38) : (WagesInt < 565) ? BigDecimal.valueOf(39) :(WagesInt < 575) ? BigDecimal.valueOf(40) : (WagesInt < 585) ? BigDecimal.valueOf(41) : (WagesInt < 595) ? BigDecimal.valueOf(42) : (WagesInt < 605) ? BigDecimal.valueOf(44) : (WagesInt < 615) ? BigDecimal.valueOf(45) :(WagesInt < 625) ? BigDecimal.valueOf(46) : (WagesInt < 635) ? BigDecimal.valueOf(47) : (WagesInt < 645) ? BigDecimal.valueOf(48) : (WagesInt < 655) ? BigDecimal.valueOf(50) : (WagesInt < 665) ? BigDecimal.valueOf(51) :(WagesInt < 675) ? BigDecimal.valueOf(52) : (WagesInt < 685) ? BigDecimal.valueOf(53) :
                                (WagesInt < 695) ? BigDecimal.valueOf(54) : (WagesInt < 705) ? BigDecimal.valueOf(56) : (WagesInt < 715) ? BigDecimal.valueOf(57) :(WagesInt < 725) ? BigDecimal.valueOf(58) : (WagesInt < 735) ? BigDecimal.valueOf(59) :(WagesInt < 745) ? BigDecimal.valueOf(60) : (WagesInt < 755) ? BigDecimal.valueOf(62) : (WagesInt < 765) ? BigDecimal.valueOf(63) : (WagesInt < 775) ? BigDecimal.valueOf(64) :(WagesInt < 785) ? BigDecimal.valueOf(65) : (WagesInt < 795) ? BigDecimal.valueOf(66) : (WagesInt < 805) ? BigDecimal.valueOf(68) :(WagesInt < 815) ? BigDecimal.valueOf(69) : (WagesInt < 825) ? BigDecimal.valueOf(70) : (WagesInt < 835) ? BigDecimal.valueOf(72) :(WagesInt < 845) ? BigDecimal.valueOf(75) : (WagesInt < 855) ? BigDecimal.valueOf(77) : (WagesInt < 865) ? BigDecimal.valueOf(79) :(WagesInt < 875) ? BigDecimal.valueOf(81) : (WagesInt < 885) ? BigDecimal.valueOf(83) : (WagesInt < 895) ? BigDecimal.valueOf(86) :
                                        (WagesInt < 905) ? BigDecimal.valueOf(88) :(WagesInt < 915) ? BigDecimal.valueOf(90) : (WagesInt < 925) ? BigDecimal.valueOf(92) :(WagesInt < 935) ? BigDecimal.valueOf(94) :(WagesInt < 945) ? BigDecimal.valueOf(97) : (WagesInt < 955) ? BigDecimal.valueOf(99) : (WagesInt < 965) ? BigDecimal.valueOf(101) :(WagesInt < 975) ? BigDecimal.valueOf(103) : (WagesInt < 985) ? BigDecimal.valueOf(105) :(WagesInt < 995) ? BigDecimal.valueOf(108) : (WagesInt < 1005) ? BigDecimal.valueOf(110) :(WagesInt < 1015) ? BigDecimal.valueOf(112) : (WagesInt < 1025) ? BigDecimal.valueOf(114) :(WagesInt < 1035) ? BigDecimal.valueOf(116) : (WagesInt < 1045) ? BigDecimal.valueOf(119) :(WagesInt < 1055) ? BigDecimal.valueOf(121) : (WagesInt < 1065) ? BigDecimal.valueOf(123) :(WagesInt < 1075) ? BigDecimal.valueOf(125) : (WagesInt < 1085) ? BigDecimal.valueOf(127) :(WagesInt < 1095) ? BigDecimal.valueOf(130) : (WagesInt < 1105) ? BigDecimal.valueOf(132) :(WagesInt < 1115) ? BigDecimal.valueOf(134) : (WagesInt < 1125) ? BigDecimal.valueOf(136) :(WagesInt < 1135) ? BigDecimal.valueOf(138) : (WagesInt < 1145) ? BigDecimal.valueOf(141) :(WagesInt < 1155) ? BigDecimal.valueOf(143) : (WagesInt < 1165) ? BigDecimal.valueOf(145) :(WagesInt < 1175) ? BigDecimal.valueOf(147) : (WagesInt < 1185) ? BigDecimal.valueOf(149) :(WagesInt < 1195) ? BigDecimal.valueOf(152) :
                                                (WagesInt < 1205) ? BigDecimal.valueOf(154) : (WagesInt < 1215) ? BigDecimal.valueOf(157) : (WagesInt < 1225) ? BigDecimal.valueOf(159) : (WagesInt < 1235) ? BigDecimal.valueOf(161) : (WagesInt < 1245) ? BigDecimal.valueOf(164) :(WagesInt < 1255) ? BigDecimal.valueOf(166) : (WagesInt < 1265) ? BigDecimal.valueOf(169) : (WagesInt < 1275) ? BigDecimal.valueOf(171) : (WagesInt < 1285) ? BigDecimal.valueOf(173) : (WagesInt < 1295) ? BigDecimal.valueOf(176) : (WagesInt < 1305) ? BigDecimal.valueOf(178) : (WagesInt < 1315) ? BigDecimal.valueOf(181) : (WagesInt < 1325) ? BigDecimal.valueOf(183) : (WagesInt < 1335) ? BigDecimal.valueOf(185) : (WagesInt < 1345) ? BigDecimal.valueOf(188) : (WagesInt < 1355) ? BigDecimal.valueOf(190) :
                                                        (WagesInt < 1365) ? BigDecimal.valueOf(193) :(WagesInt < 1375) ? BigDecimal.valueOf(195) :(WagesInt < 1385) ? BigDecimal.valueOf(197) : (WagesInt < 1395) ? BigDecimal.valueOf(200) : (WagesInt < 1405) ? BigDecimal.valueOf(202) : (WagesInt < 1415) ? BigDecimal.valueOf(205) : (WagesInt < 1425) ? BigDecimal.valueOf(207) :
                                                                //left off end of page 14 fed tax pdf
                (WagesInt < 1435) ? BigDecimal.valueOf(209):(WagesInt < 1445) ? BigDecimal.valueOf(212):(WagesInt < 1455) ? BigDecimal.valueOf(214):(WagesInt < 1465) ? BigDecimal.valueOf(217):(WagesInt < 1475) ? BigDecimal.valueOf(219):(WagesInt < 1485) ? BigDecimal.valueOf(221):(WagesInt < 1495) ? BigDecimal.valueOf(224):(WagesInt < 1505) ? BigDecimal.valueOf(226):(WagesInt < 1515) ? BigDecimal.valueOf(229):(WagesInt < 1525) ? BigDecimal.valueOf(231):(WagesInt < 1535) ? BigDecimal.valueOf(233):(WagesInt < 1545) ? BigDecimal.valueOf(236):(WagesInt < 1555) ? BigDecimal.valueOf(238):(WagesInt < 1565) ? BigDecimal.valueOf(241):(WagesInt < 1575) ? BigDecimal.valueOf(243):(WagesInt < 1585) ? BigDecimal.valueOf(245):(WagesInt < 1595) ? BigDecimal.valueOf(248):(WagesInt < 1605) ? BigDecimal.valueOf(250):(WagesInt < 1615) ? BigDecimal.valueOf(253):(WagesInt < 1625) ? BigDecimal.valueOf(255):(WagesInt < 1635) ? BigDecimal.valueOf(257):(WagesInt < 1645) ? BigDecimal.valueOf(260):(WagesInt < 1655) ? BigDecimal.valueOf(262):(WagesInt < 1665) ? BigDecimal.valueOf(265):(WagesInt < 1675) ? BigDecimal.valueOf(267):
                        (WagesInt < 1685) ? BigDecimal.valueOf(269):(WagesInt < 1695) ? BigDecimal.valueOf(272):(WagesInt < 1705) ? BigDecimal.valueOf(274):(WagesInt < 1715) ? BigDecimal.valueOf(277):(WagesInt < 1725) ? BigDecimal.valueOf(279):(WagesInt < 1735) ? BigDecimal.valueOf(281):(WagesInt < 1745) ? BigDecimal.valueOf(284):(WagesInt < 1755) ? BigDecimal.valueOf(286):(WagesInt < 1765) ? BigDecimal.valueOf(289):(WagesInt < 1775) ? BigDecimal.valueOf(291):(WagesInt < 1785) ? BigDecimal.valueOf(293):(WagesInt < 1795) ? BigDecimal.valueOf(296):(WagesInt < 1805) ? BigDecimal.valueOf(298):(WagesInt < 1815) ? BigDecimal.valueOf(301):(WagesInt < 1825) ? BigDecimal.valueOf(303):(WagesInt < 1835) ? BigDecimal.valueOf(305):(WagesInt < 1845) ? BigDecimal.valueOf(308):(WagesInt < 1855) ? BigDecimal.valueOf(310):(WagesInt < 1865) ? BigDecimal.valueOf(313):(WagesInt < 1875) ? BigDecimal.valueOf(315):(WagesInt < 1885) ? BigDecimal.valueOf(317):(WagesInt < 1895) ? BigDecimal.valueOf(320):(WagesInt < 1905) ? BigDecimal.valueOf(322):(WagesInt < 1915) ? BigDecimal.valueOf(325):(WagesInt < 1925) ? BigDecimal.valueOf(327):
                                                                BigDecimal.valueOf(-1);
            }

        }
        if (status.get(0).equals("s") || status.get(0).equals("sep")) {
            //Single Standard withholding
            if (status.get(1).equals("0")) {
                result = (WagesInt < 285) ? BigDecimal.valueOf(0) : (WagesInt < 295) ? BigDecimal.valueOf(1) : (WagesInt < 305) ? BigDecimal.valueOf(2) :(WagesInt < 315) ? BigDecimal.valueOf(3) : (WagesInt < 325) ? BigDecimal.valueOf(4) : (WagesInt < 335) ? BigDecimal.valueOf(5) :(WagesInt < 345) ? BigDecimal.valueOf(6) : (WagesInt < 355) ? BigDecimal.valueOf(7) : (WagesInt < 365) ? BigDecimal.valueOf(8) :(WagesInt < 375) ? BigDecimal.valueOf(9) : (WagesInt < 385) ? BigDecimal.valueOf(10) : (WagesInt < 395) ? BigDecimal.valueOf(11) :(WagesInt < 405) ? BigDecimal.valueOf(12) : (WagesInt < 415) ? BigDecimal.valueOf(13) : (WagesInt < 425) ? BigDecimal.valueOf(14) :(WagesInt < 435) ? BigDecimal.valueOf(15) : (WagesInt < 445) ? BigDecimal.valueOf(16) : (WagesInt < 455) ? BigDecimal.valueOf(17) :(WagesInt < 465) ? BigDecimal.valueOf(18) : (WagesInt < 475) ? BigDecimal.valueOf(19) : (WagesInt < 485) ? BigDecimal.valueOf(20) :(WagesInt < 495) ? BigDecimal.valueOf(21) :
                        (WagesInt < 505) ? BigDecimal.valueOf(22) : (WagesInt < 515) ? BigDecimal.valueOf(23) :(WagesInt < 525) ? BigDecimal.valueOf(24) : (WagesInt < 535) ? BigDecimal.valueOf(25) : (WagesInt < 545) ? BigDecimal.valueOf(27) : (WagesInt < 555) ? BigDecimal.valueOf(28) : (WagesInt < 565) ? BigDecimal.valueOf(29) :(WagesInt < 575) ? BigDecimal.valueOf(30) : (WagesInt < 585) ? BigDecimal.valueOf(31) : (WagesInt < 595) ? BigDecimal.valueOf(33) : (WagesInt < 605) ? BigDecimal.valueOf(34) : (WagesInt < 615) ? BigDecimal.valueOf(35) :(WagesInt < 625) ? BigDecimal.valueOf(36) : (WagesInt < 635) ? BigDecimal.valueOf(37) : (WagesInt < 645) ? BigDecimal.valueOf(39) : (WagesInt < 655) ? BigDecimal.valueOf(40) : (WagesInt < 665) ? BigDecimal.valueOf(41) :(WagesInt < 675) ? BigDecimal.valueOf(42) : (WagesInt < 685) ? BigDecimal.valueOf(43) :
                        (WagesInt < 695) ? BigDecimal.valueOf(45) : (WagesInt < 705) ? BigDecimal.valueOf(46) : (WagesInt < 715) ? BigDecimal.valueOf(47) :(WagesInt < 725) ? BigDecimal.valueOf(48) : (WagesInt < 735) ? BigDecimal.valueOf(49) :(WagesInt < 745) ? BigDecimal.valueOf(51) : (WagesInt < 755) ? BigDecimal.valueOf(52) : (WagesInt < 765) ? BigDecimal.valueOf(53) : (WagesInt < 775) ? BigDecimal.valueOf(54) :(WagesInt < 785) ? BigDecimal.valueOf(55) : (WagesInt < 795) ? BigDecimal.valueOf(57) : (WagesInt < 805) ? BigDecimal.valueOf(58) :(WagesInt < 815) ? BigDecimal.valueOf(59) : (WagesInt < 825) ? BigDecimal.valueOf(60) : (WagesInt < 835) ? BigDecimal.valueOf(61) :(WagesInt < 845) ? BigDecimal.valueOf(63) : (WagesInt < 855) ? BigDecimal.valueOf(64) : (WagesInt < 865) ? BigDecimal.valueOf(65) :(WagesInt < 875) ? BigDecimal.valueOf(66) : (WagesInt < 885) ? BigDecimal.valueOf(67) : (WagesInt < 895) ? BigDecimal.valueOf(69) :
                        (WagesInt < 905) ? BigDecimal.valueOf(70) :(WagesInt < 915) ? BigDecimal.valueOf(71) : (WagesInt < 925) ? BigDecimal.valueOf(72) :(WagesInt < 935) ? BigDecimal.valueOf(73) :(WagesInt < 945) ? BigDecimal.valueOf(75) : (WagesInt < 955) ? BigDecimal.valueOf(76) : (WagesInt < 965) ? BigDecimal.valueOf(77) :(WagesInt < 975) ? BigDecimal.valueOf(78) : (WagesInt < 985) ? BigDecimal.valueOf(79) :(WagesInt < 995) ? BigDecimal.valueOf(81) : (WagesInt < 1005) ? BigDecimal.valueOf(82) :(WagesInt < 1015) ? BigDecimal.valueOf(83) : (WagesInt < 1025) ? BigDecimal.valueOf(84) :(WagesInt < 1035) ? BigDecimal.valueOf(85) : (WagesInt < 1045) ? BigDecimal.valueOf(87) :(WagesInt < 1055) ? BigDecimal.valueOf(88) : (WagesInt < 1065) ? BigDecimal.valueOf(89) :(WagesInt < 1075) ? BigDecimal.valueOf(90) : (WagesInt < 1085) ? BigDecimal.valueOf(91) :(WagesInt < 1095) ? BigDecimal.valueOf(93) : (WagesInt < 1105) ? BigDecimal.valueOf(94) :(WagesInt < 1115) ? BigDecimal.valueOf(95) : (WagesInt < 1125) ? BigDecimal.valueOf(96) :(WagesInt < 1135) ? BigDecimal.valueOf(97) : (WagesInt < 1145) ? BigDecimal.valueOf(99) :(WagesInt < 1155) ? BigDecimal.valueOf(100) : (WagesInt < 1165) ? BigDecimal.valueOf(101) :(WagesInt < 1175) ? BigDecimal.valueOf(102) : (WagesInt < 1185) ? BigDecimal.valueOf(103) :(WagesInt < 1195) ? BigDecimal.valueOf(105) :
                        (WagesInt < 1205) ? BigDecimal.valueOf(107) : (WagesInt < 1215) ? BigDecimal.valueOf(109) : (WagesInt < 1225) ? BigDecimal.valueOf(111) : (WagesInt < 1235) ? BigDecimal.valueOf(114) : (WagesInt < 1245) ? BigDecimal.valueOf(116) :(WagesInt < 1255) ? BigDecimal.valueOf(118) : (WagesInt < 1265) ? BigDecimal.valueOf(120) : (WagesInt < 1275) ? BigDecimal.valueOf(122) : (WagesInt < 1285) ? BigDecimal.valueOf(125) : (WagesInt < 1295) ? BigDecimal.valueOf(127) : (WagesInt < 1305) ? BigDecimal.valueOf(129) : (WagesInt < 1315) ? BigDecimal.valueOf(131) : (WagesInt < 1325) ? BigDecimal.valueOf(133) : (WagesInt < 1335) ? BigDecimal.valueOf(136) : (WagesInt < 1345) ? BigDecimal.valueOf(138) : (WagesInt < 1355) ? BigDecimal.valueOf(140) :
                        (WagesInt < 1365) ? BigDecimal.valueOf(142) :(WagesInt < 1375) ? BigDecimal.valueOf(144) :(WagesInt < 1385) ? BigDecimal.valueOf(147) : (WagesInt < 1395) ? BigDecimal.valueOf(149) : (WagesInt < 1405) ? BigDecimal.valueOf(151) : (WagesInt < 1415) ? BigDecimal.valueOf(153) : (WagesInt < 1425) ? BigDecimal.valueOf(155) :
                               //left off end of page 14 fed tax pdf
                                (WagesInt < 1435) ? BigDecimal.valueOf(158):(WagesInt < 1445) ? BigDecimal.valueOf(160):(WagesInt < 1455) ? BigDecimal.valueOf(162):(WagesInt < 1465) ? BigDecimal.valueOf(164):(WagesInt < 1475) ? BigDecimal.valueOf(166):(WagesInt < 1485) ? BigDecimal.valueOf(169):(WagesInt < 1495) ? BigDecimal.valueOf(171):(WagesInt < 1505) ? BigDecimal.valueOf(173):(WagesInt < 1515) ? BigDecimal.valueOf(175):(WagesInt < 1525) ? BigDecimal.valueOf(177):(WagesInt < 1535) ? BigDecimal.valueOf(180):(WagesInt < 1545) ? BigDecimal.valueOf(182):(WagesInt < 1555) ? BigDecimal.valueOf(184):(WagesInt < 1565) ? BigDecimal.valueOf(186):(WagesInt < 1575) ? BigDecimal.valueOf(188):(WagesInt < 1585) ? BigDecimal.valueOf(191):(WagesInt < 1595) ? BigDecimal.valueOf(193):(WagesInt < 1605) ? BigDecimal.valueOf(195):(WagesInt < 1615) ? BigDecimal.valueOf(197):(WagesInt < 1625) ? BigDecimal.valueOf(199):(WagesInt < 1635) ? BigDecimal.valueOf(202):(WagesInt < 1645) ? BigDecimal.valueOf(204):(WagesInt < 1655) ? BigDecimal.valueOf(206):(WagesInt < 1665) ? BigDecimal.valueOf(208):(WagesInt < 1675) ? BigDecimal.valueOf(210):
                                        (WagesInt < 1685) ? BigDecimal.valueOf(213):(WagesInt < 1695) ? BigDecimal.valueOf(215):(WagesInt < 1705) ? BigDecimal.valueOf(217):(WagesInt < 1715) ? BigDecimal.valueOf(219):(WagesInt < 1725) ? BigDecimal.valueOf(221):(WagesInt < 1735) ? BigDecimal.valueOf(224):(WagesInt < 1745) ? BigDecimal.valueOf(226):(WagesInt < 1755) ? BigDecimal.valueOf(228):(WagesInt < 1765) ? BigDecimal.valueOf(230):(WagesInt < 1775) ? BigDecimal.valueOf(232):(WagesInt < 1785) ? BigDecimal.valueOf(235):(WagesInt < 1795) ? BigDecimal.valueOf(237):(WagesInt < 1805) ? BigDecimal.valueOf(239):(WagesInt < 1815) ? BigDecimal.valueOf(241):(WagesInt < 1825) ? BigDecimal.valueOf(243):(WagesInt < 1835) ? BigDecimal.valueOf(246):(WagesInt < 1845) ? BigDecimal.valueOf(248):(WagesInt < 1855) ? BigDecimal.valueOf(250):(WagesInt < 1865) ? BigDecimal.valueOf(252):(WagesInt < 1875) ? BigDecimal.valueOf(254):(WagesInt < 1885) ? BigDecimal.valueOf(257):(WagesInt < 1895) ? BigDecimal.valueOf(259):(WagesInt < 1905) ? BigDecimal.valueOf(261):(WagesInt < 1915) ? BigDecimal.valueOf(263):(WagesInt < 1925) ? BigDecimal.valueOf(265):
                                                                BigDecimal.valueOf(-1);

            }
            else if (status.get(1).equals("1")) {
                //Single W4 etc.
                result = (WagesInt < 145) ? BigDecimal.valueOf(0): (WagesInt < 155) ? BigDecimal.valueOf(1):(WagesInt < 165) ? BigDecimal.valueOf(2):(WagesInt < 175) ? BigDecimal.valueOf(3):(WagesInt < 185) ? BigDecimal.valueOf(4):(WagesInt < 195) ? BigDecimal.valueOf(5):(WagesInt < 205) ? BigDecimal.valueOf(6):(WagesInt < 215) ? BigDecimal.valueOf(7):(WagesInt < 225) ? BigDecimal.valueOf(8):(WagesInt < 235) ? BigDecimal.valueOf(9):(WagesInt < 245) ? BigDecimal.valueOf(10):(WagesInt < 255) ? BigDecimal.valueOf(11):(WagesInt < 265) ? BigDecimal.valueOf(12):(WagesInt < 275) ? BigDecimal.valueOf(13):
                (WagesInt < 285) ? BigDecimal.valueOf(15) : (WagesInt < 295) ? BigDecimal.valueOf(16) : (WagesInt < 305) ? BigDecimal.valueOf(17) :(WagesInt < 315) ? BigDecimal.valueOf(18) : (WagesInt < 325) ? BigDecimal.valueOf(19) : (WagesInt < 335) ? BigDecimal.valueOf(21) :(WagesInt < 345) ? BigDecimal.valueOf(22) : (WagesInt < 355) ? BigDecimal.valueOf(23) : (WagesInt < 365) ? BigDecimal.valueOf(24) :(WagesInt < 375) ? BigDecimal.valueOf(25) : (WagesInt < 385) ? BigDecimal.valueOf(27) : (WagesInt < 395) ? BigDecimal.valueOf(28) :(WagesInt < 405) ? BigDecimal.valueOf(29) : (WagesInt < 415) ? BigDecimal.valueOf(30) : (WagesInt < 425) ? BigDecimal.valueOf(31) :(WagesInt < 435) ? BigDecimal.valueOf(33) : (WagesInt < 445) ? BigDecimal.valueOf(34) : (WagesInt < 455) ? BigDecimal.valueOf(35) :(WagesInt < 465) ? BigDecimal.valueOf(36) : (WagesInt < 475) ? BigDecimal.valueOf(37) : (WagesInt < 485) ? BigDecimal.valueOf(39) :(WagesInt < 495) ? BigDecimal.valueOf(40) :
                        (WagesInt < 505) ? BigDecimal.valueOf(41) : (WagesInt < 515) ? BigDecimal.valueOf(42) :(WagesInt < 525) ? BigDecimal.valueOf(43) : (WagesInt < 535) ? BigDecimal.valueOf(45) : (WagesInt < 545) ? BigDecimal.valueOf(46) : (WagesInt < 555) ? BigDecimal.valueOf(47) : (WagesInt < 565) ? BigDecimal.valueOf(48) :(WagesInt < 575) ? BigDecimal.valueOf(49) : (WagesInt < 585) ? BigDecimal.valueOf(51) : (WagesInt < 595) ? BigDecimal.valueOf(52) : (WagesInt < 605) ? BigDecimal.valueOf(54) : (WagesInt < 615) ? BigDecimal.valueOf(56) :(WagesInt < 625) ? BigDecimal.valueOf(58) : (WagesInt < 635) ? BigDecimal.valueOf(60) : (WagesInt < 645) ? BigDecimal.valueOf(62) : (WagesInt < 655) ? BigDecimal.valueOf(65) : (WagesInt < 665) ? BigDecimal.valueOf(67) :(WagesInt < 675) ? BigDecimal.valueOf(69) : (WagesInt < 685) ? BigDecimal.valueOf(71) :
                                (WagesInt < 695) ? BigDecimal.valueOf(73) : (WagesInt < 705) ? BigDecimal.valueOf(76) : (WagesInt < 715) ? BigDecimal.valueOf(78) :(WagesInt < 725) ? BigDecimal.valueOf(80) : (WagesInt < 735) ? BigDecimal.valueOf(82) :(WagesInt < 745) ? BigDecimal.valueOf(84) : (WagesInt < 755) ? BigDecimal.valueOf(87) : (WagesInt < 765) ? BigDecimal.valueOf(89) : (WagesInt < 775) ? BigDecimal.valueOf(91) :(WagesInt < 785) ? BigDecimal.valueOf(93) : (WagesInt < 795) ? BigDecimal.valueOf(95) : (WagesInt < 805) ? BigDecimal.valueOf(98) :(WagesInt < 815) ? BigDecimal.valueOf(100) : (WagesInt < 825) ? BigDecimal.valueOf(102) : (WagesInt < 835) ? BigDecimal.valueOf(104) :(WagesInt < 845) ? BigDecimal.valueOf(106) : (WagesInt < 855) ? BigDecimal.valueOf(109) : (WagesInt < 865) ? BigDecimal.valueOf(111) :(WagesInt < 875) ? BigDecimal.valueOf(113) : (WagesInt < 885) ? BigDecimal.valueOf(115) : (WagesInt < 895) ? BigDecimal.valueOf(117) :
                                        (WagesInt < 905) ? BigDecimal.valueOf(120) :(WagesInt < 915) ? BigDecimal.valueOf(122) : (WagesInt < 925) ? BigDecimal.valueOf(124) :(WagesInt < 935) ? BigDecimal.valueOf(126) :(WagesInt < 945) ? BigDecimal.valueOf(128) : (WagesInt < 955) ? BigDecimal.valueOf(131) : (WagesInt < 965) ? BigDecimal.valueOf(133) :(WagesInt < 975) ? BigDecimal.valueOf(135) : (WagesInt < 985) ? BigDecimal.valueOf(137) :(WagesInt < 995) ? BigDecimal.valueOf(139) : (WagesInt < 1005) ? BigDecimal.valueOf(142) :(WagesInt < 1015) ? BigDecimal.valueOf(144) : (WagesInt < 1025) ? BigDecimal.valueOf(146) :(WagesInt < 1035) ? BigDecimal.valueOf(148) : (WagesInt < 1045) ? BigDecimal.valueOf(150) :(WagesInt < 1055) ? BigDecimal.valueOf(153) : (WagesInt < 1065) ? BigDecimal.valueOf(155) :(WagesInt < 1075) ? BigDecimal.valueOf(157) : (WagesInt < 1085) ? BigDecimal.valueOf(159) :(WagesInt < 1095) ? BigDecimal.valueOf(161) : (WagesInt < 1105) ? BigDecimal.valueOf(164) :(WagesInt < 1115) ? BigDecimal.valueOf(166) : (WagesInt < 1125) ? BigDecimal.valueOf(168) :(WagesInt < 1135) ? BigDecimal.valueOf(171) : (WagesInt < 1145) ? BigDecimal.valueOf(173) :(WagesInt < 1155) ? BigDecimal.valueOf(175) : (WagesInt < 1165) ? BigDecimal.valueOf(178) :(WagesInt < 1175) ? BigDecimal.valueOf(180) : (WagesInt < 1185) ? BigDecimal.valueOf(183) :(WagesInt < 1195) ? BigDecimal.valueOf(185) :
                                                (WagesInt < 1205) ? BigDecimal.valueOf(187) : (WagesInt < 1215) ? BigDecimal.valueOf(190) : (WagesInt < 1225) ? BigDecimal.valueOf(192) : (WagesInt < 1235) ? BigDecimal.valueOf(195) : (WagesInt < 1245) ? BigDecimal.valueOf(197) :(WagesInt < 1255) ? BigDecimal.valueOf(199) : (WagesInt < 1265) ? BigDecimal.valueOf(202) : (WagesInt < 1275) ? BigDecimal.valueOf(204) : (WagesInt < 1285) ? BigDecimal.valueOf(207) : (WagesInt < 1295) ? BigDecimal.valueOf(209) : (WagesInt < 1305) ? BigDecimal.valueOf(211) : (WagesInt < 1315) ? BigDecimal.valueOf(214) : (WagesInt < 1325) ? BigDecimal.valueOf(216) : (WagesInt < 1335) ? BigDecimal.valueOf(219) : (WagesInt < 1345) ? BigDecimal.valueOf(221) : (WagesInt < 1355) ? BigDecimal.valueOf(223) :
                                                        (WagesInt < 1365) ? BigDecimal.valueOf(226) :(WagesInt < 1375) ? BigDecimal.valueOf(228) :(WagesInt < 1385) ? BigDecimal.valueOf(231) : (WagesInt < 1395) ? BigDecimal.valueOf(233) : (WagesInt < 1405) ? BigDecimal.valueOf(235) : (WagesInt < 1415) ? BigDecimal.valueOf(238) : (WagesInt < 1425) ? BigDecimal.valueOf(240) :
                                                                //left off end of page 14 fed tax pdf
(WagesInt < 1435) ? BigDecimal.valueOf(243):(WagesInt < 1445) ? BigDecimal.valueOf(245):(WagesInt < 1455) ? BigDecimal.valueOf(247):(WagesInt < 1465) ? BigDecimal.valueOf(250):(WagesInt < 1475) ? BigDecimal.valueOf(252):(WagesInt < 1485) ? BigDecimal.valueOf(255):(WagesInt < 1495) ? BigDecimal.valueOf(257):(WagesInt < 1505) ? BigDecimal.valueOf(259):(WagesInt < 1515) ? BigDecimal.valueOf(262):(WagesInt < 1525) ? BigDecimal.valueOf(264):(WagesInt < 1535) ? BigDecimal.valueOf(267):(WagesInt < 1545) ? BigDecimal.valueOf(269):(WagesInt < 1555) ? BigDecimal.valueOf(271):(WagesInt < 1565) ? BigDecimal.valueOf(274):(WagesInt < 1575) ? BigDecimal.valueOf(276):(WagesInt < 1585) ? BigDecimal.valueOf(279):(WagesInt < 1595) ? BigDecimal.valueOf(281):(WagesInt < 1605) ? BigDecimal.valueOf(283):(WagesInt < 1615) ? BigDecimal.valueOf(286):(WagesInt < 1625) ? BigDecimal.valueOf(288):(WagesInt < 1635) ? BigDecimal.valueOf(291):(WagesInt < 1645) ? BigDecimal.valueOf(293):(WagesInt < 1655) ? BigDecimal.valueOf(295):(WagesInt < 1665) ? BigDecimal.valueOf(298):(WagesInt < 1675) ? BigDecimal.valueOf(300):
(WagesInt < 1685) ? BigDecimal.valueOf(303):(WagesInt < 1695) ? BigDecimal.valueOf(305):(WagesInt < 1705) ? BigDecimal.valueOf(307):(WagesInt < 1715) ? BigDecimal.valueOf(310):(WagesInt < 1725) ? BigDecimal.valueOf(312):(WagesInt < 1735) ? BigDecimal.valueOf(315):(WagesInt < 1745) ? BigDecimal.valueOf(317):(WagesInt < 1755) ? BigDecimal.valueOf(319):(WagesInt < 1765) ? BigDecimal.valueOf(322):(WagesInt < 1775) ? BigDecimal.valueOf(324):(WagesInt < 1785) ? BigDecimal.valueOf(327):(WagesInt < 1795) ? BigDecimal.valueOf(329):(WagesInt < 1805) ? BigDecimal.valueOf(331):(WagesInt < 1815) ? BigDecimal.valueOf(334):(WagesInt < 1825) ? BigDecimal.valueOf(336):(WagesInt < 1835) ? BigDecimal.valueOf(339):(WagesInt < 1845) ? BigDecimal.valueOf(341):(WagesInt < 1855) ? BigDecimal.valueOf(343):(WagesInt < 1865) ? BigDecimal.valueOf(346):(WagesInt < 1875) ? BigDecimal.valueOf(348):(WagesInt < 1885) ? BigDecimal.valueOf(351):(WagesInt < 1895) ? BigDecimal.valueOf(353):(WagesInt < 1905) ? BigDecimal.valueOf(355):(WagesInt < 1915) ? BigDecimal.valueOf(358):(WagesInt < 1925) ? BigDecimal.valueOf(360):
                                                                BigDecimal.valueOf(-1);
            }
        }
        return result;
    }

    private static BigDecimal stateTaxFormulaWeekly(int status, BigDecimal wages) {
        BigDecimal result = null;
        int MOWages = wages.intValue();

        if (status == 0) {
            //single,married filing etc.
            result = (MOWages < 330) ? BigDecimal.valueOf(0) : (MOWages < 370) ? BigDecimal.valueOf(1) : (MOWages < 400) ? BigDecimal.valueOf(2) : (MOWages < 420) ? BigDecimal.valueOf(3) : (MOWages < 450) ? BigDecimal.valueOf(4) : (MOWages < 470) ? BigDecimal.valueOf(5) : (MOWages < 490) ? BigDecimal.valueOf(6) : (MOWages < 510) ? BigDecimal.valueOf(7) : (MOWages < 530) ? BigDecimal.valueOf(8) :(MOWages < 550) ? BigDecimal.valueOf(9) :(MOWages < 570) ? BigDecimal.valueOf(10) :(MOWages < 590) ? BigDecimal.valueOf(11) : (MOWages < 610) ? BigDecimal.valueOf(12) : (MOWages < 620) ? BigDecimal.valueOf(13) : (MOWages < 630) ? BigDecimal.valueOf(13) :(MOWages < 640) ? BigDecimal.valueOf(14) : (MOWages < 650) ? BigDecimal.valueOf(14) :(MOWages < 660) ? BigDecimal.valueOf(15) : (MOWages < 670) ? BigDecimal.valueOf(15) : (MOWages < 680) ? BigDecimal.valueOf(15) : (MOWages < 690) ? BigDecimal.valueOf(16) : (MOWages < 700) ? BigDecimal.valueOf(16) : (MOWages < 710) ? BigDecimal.valueOf(17) : (MOWages < 720) ? BigDecimal.valueOf(17) : (MOWages < 730) ? BigDecimal.valueOf(18) : (MOWages < 740) ? BigDecimal.valueOf(18) : (MOWages < 750) ? BigDecimal.valueOf(19) : (MOWages < 760) ? BigDecimal.valueOf(19) :
                    (MOWages < 770) ? BigDecimal.valueOf(20):(MOWages < 780) ? BigDecimal.valueOf(20):(MOWages < 790) ? BigDecimal.valueOf(21):(MOWages < 800) ? BigDecimal.valueOf(21): (MOWages < 810) ? BigDecimal.valueOf(22):(MOWages < 820) ? BigDecimal.valueOf(22):(MOWages < 830) ? BigDecimal.valueOf(23):(MOWages < 840) ? BigDecimal.valueOf(23):(MOWages < 850) ? BigDecimal.valueOf(24):(MOWages < 860) ? BigDecimal.valueOf(24):(MOWages < 870) ? BigDecimal.valueOf(25):(MOWages < 880) ? BigDecimal.valueOf(25):(MOWages < 890) ? BigDecimal.valueOf(26):(MOWages < 900) ? BigDecimal.valueOf(26):(MOWages < 910) ? BigDecimal.valueOf(27):(MOWages < 920) ? BigDecimal.valueOf(27): (MOWages < 930) ? BigDecimal.valueOf(27):(MOWages < 940) ? BigDecimal.valueOf(28):(MOWages < 950) ? BigDecimal.valueOf(28):(MOWages < 960) ? BigDecimal.valueOf(29):(MOWages < 970) ? BigDecimal.valueOf(29):(MOWages < 980) ? BigDecimal.valueOf(30):(MOWages < 990) ? BigDecimal.valueOf(30):(MOWages < 1000) ? BigDecimal.valueOf(31):(MOWages < 1010) ? BigDecimal.valueOf(31):(MOWages < 1020) ? BigDecimal.valueOf(32):(MOWages < 1030) ? BigDecimal.valueOf(32):(MOWages < 1040) ? BigDecimal.valueOf(33):
                    (MOWages < 1050) ? BigDecimal.valueOf(33): (MOWages < 1060) ? BigDecimal.valueOf(34):(MOWages < 1070) ? BigDecimal.valueOf(34):(MOWages < 1080) ? BigDecimal.valueOf(35):(MOWages < 1090) ? BigDecimal.valueOf(35):(MOWages < 1100) ? BigDecimal.valueOf(36):(MOWages < 1110) ? BigDecimal.valueOf(36):(MOWages < 1120) ? BigDecimal.valueOf(37):
                    //left off at less than 1120
                            BigDecimal.valueOf(-1);
        }
    return result;
    }

    private static BigDecimal federalTaxFormulaDaily(List<String> status, BigDecimal wages) {
        BigDecimal result = null;
        int WagesInt = wages.intValue();
        if (status.get(0).equals(0)) {
            //Filing status besides single/married filing combined etc. incorrect numbers

            //Married Filing Jointly Standing withholding this is all semimonthly
            if (status.get(1).equals(0)) {
                if (WagesInt < 1215) {
                    result = BigDecimal.valueOf(0);
                } else if (WagesInt >= 1215 && WagesInt < 1230) {
                    result = BigDecimal.valueOf(1);
                }
            }
            //Married Filing Jointly W4 etc.
            if (status.get(1).equals(1)) {
                if (WagesInt < 615) {
                    result = BigDecimal.valueOf(0);
                } else if (WagesInt >= 615 && WagesInt < 630) {
                    result = BigDecimal.valueOf(1);
                }
            }
        }
        if (status.get(0).equals(1)) {
            //Head of Household Standing withholding
            if (status.get(1).equals(0)) {
                if (WagesInt < 915) {
                    result = BigDecimal.valueOf(0);
                } else if (WagesInt >= 915 && WagesInt < 930) {
                    result = BigDecimal.valueOf(1);
                }
            }
            //Head of Household W4 etc.
            if (status.get(1).equals(1)) {
                if (WagesInt < 465) {
                    result = BigDecimal.valueOf(0);
                } else if (WagesInt >= 465 && WagesInt < 475) {
                    result = BigDecimal.valueOf(1);
                }
            }

        }
        if (status.get(0).equals("s") || status.get(0).equals("sep")) {
            //Single Standard withholding
            if (status.get(1).equals("0")) {
                result = (WagesInt < 55) ? BigDecimal.valueOf(0) : (WagesInt < 60) ? BigDecimal.valueOf(0.10) : (WagesInt < 65) ? BigDecimal.valueOf(0.60) :(WagesInt < 70) ? BigDecimal.valueOf(1.10) : (WagesInt < 75) ? BigDecimal.valueOf(1.60) : (WagesInt < 80) ? BigDecimal.valueOf(2.10) :(WagesInt < 85) ? BigDecimal.valueOf(2.60) : (WagesInt < 90) ? BigDecimal.valueOf(3.10) : (WagesInt < 95) ? BigDecimal.valueOf(3.60) :(WagesInt < 100) ? BigDecimal.valueOf(4.10) : (WagesInt < 105) ? BigDecimal.valueOf(4.70) :

                                                                BigDecimal.valueOf(-1);

            }
            else if (status.get(1).equals("1")) {
                //Single W4 etc.
                result = (WagesInt < 30) ? BigDecimal.valueOf(0): (WagesInt < 35) ? BigDecimal.valueOf(0.40):(WagesInt < 40) ? BigDecimal.valueOf(0.90):(WagesInt < 45) ? BigDecimal.valueOf(1.40):(WagesInt < 50) ? BigDecimal.valueOf(1.90):(WagesInt < 55) ? BigDecimal.valueOf(2.50):(WagesInt < 60) ? BigDecimal.valueOf(3.10):(WagesInt < 65) ? BigDecimal.valueOf(3.70):(WagesInt < 70) ? BigDecimal.valueOf(4.30):(WagesInt < 75) ? BigDecimal.valueOf(4.90):(WagesInt < 80) ? BigDecimal.valueOf(5.50):(WagesInt < 85) ? BigDecimal.valueOf(6.10):(WagesInt < 90) ? BigDecimal.valueOf(6.70):(WagesInt < 95) ? BigDecimal.valueOf(7.30):

                                                                        BigDecimal.valueOf(-1);
            }
        }
        return result;
    }

    private static BigDecimal stateTaxFormulaDaily(int status, BigDecimal wages) {
        BigDecimal result = null;
        int MOWages = wages.intValue();

        if (status == 0) {
            //single,married filing etc.
            result = (MOWages < 81) ? BigDecimal.valueOf(0) : (MOWages < 102) ? BigDecimal.valueOf(1) : (MOWages < 105) ? BigDecimal.valueOf(2) :

                                    BigDecimal.valueOf(-1);
        }
        return result;
    }

}

