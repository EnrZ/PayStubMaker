package dev.ez.PayStubMaker.controllers;

import dev.ez.PayStubMaker.data.StubData;
import dev.ez.PayStubMaker.models.Stub;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
        BigDecimal taxCalculations, contributionCalculations, deductionCalculations, currentTotalDeduction;
        BigDecimal YTDDeduction, YTDDeductionCalculations, netPay, netPayCalculations, YTDnetPay, YTDnetPayCalculations;
        BigDecimal YTDFedCalculations, YTDFed, YTDStateCalculations, YTDState, YTDSocSecCalculations, YTDSocSec, YTDMedicareCalculations,YTDMedicare;

        List<String> daysOfWeek = new ArrayList<>(Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"));
        String beginningDay;
        int daysLong;


            for (int num : stub.getHoursWorkedEachDay()) {
                totalHours += num;
            }


       stub.setTotalHours(totalHours);


            hourlyPayRateConverted = BigDecimal.valueOf(stub.getHourlyPayRate());

            TGICalculations = hourlyPayRateConverted.multiply(BigDecimal.valueOf(totalHours));
            totalGrossIncome = TGICalculations.setScale(2, RoundingMode.HALF_UP);

            stub.setTotalGrossIncome(totalGrossIncome);

            YTDGICalculations = totalGrossIncome.add(stub.getYearlyPreviousGross());
            YTDGrossIncome = YTDGICalculations.setScale(2, RoundingMode.HALF_UP);
            stub.setYTDGrossIncome(YTDGrossIncome);

            SOCSECCalculations = totalGrossIncome.multiply(BigDecimal.valueOf(0.062));
            socSecContribution = SOCSECCalculations.setScale(2, RoundingMode.UP);
            stub.setSocSecContribution(socSecContribution);

            //Soc Sec YTD
            YTDSocSecCalculations = socSecContribution.add(stub.getYearlyPreviousSocSec());
            YTDSocSec = YTDSocSecCalculations.setScale(2,RoundingMode.UP);
            stub.setYTDSocSec(YTDSocSec);

            MCCalculations = totalGrossIncome.multiply(BigDecimal.valueOf(0.0145));
            medicareContribution = MCCalculations.setScale(2, RoundingMode.UP);
            stub.setMedicareContribution(medicareContribution);

            //Medicare YTD
            YTDMedicareCalculations = medicareContribution.add(stub.getYearlyPreviousMedicare());
            YTDMedicare = YTDMedicareCalculations.setScale(2,RoundingMode.UP);
            stub.setYTDMedicare(YTDMedicare);

            //Calling this method to calculate state tax
            stateTax = stateTaxFormula(stub.getStateTaxFiling(), totalGrossIncome);
            stub.setStateTax(stateTax);

            federalTax = federalTaxFormula(stub.getFederalTaxFiling(),totalGrossIncome);
            stub.setFederalTax(federalTax);

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
    public String createUpdatedStub(@RequestParam(value = "newCheckNumber", required = false) String newCheckNumber, @RequestParam("findId") int findId, @RequestParam(value = "newPayDay", required = false) String newPayDay) {

        for (Stub stub : stubs) {
            if (stub.getId() == findId) {
                if (newCheckNumber != null && !newCheckNumber.isEmpty()) {
                    stub.setCheckNumber(newCheckNumber);
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
    public String renderCreateStubForm() {

        return "stubs/create";
    }

    @PostMapping("create")
    public String createStub(@ModelAttribute Stub newStub) {
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

}

