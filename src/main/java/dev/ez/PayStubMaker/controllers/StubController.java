package dev.ez.PayStubMaker.controllers;

import dev.ez.PayStubMaker.models.Stub;
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
        model.addAttribute("stubs", stubs);

        int totalHours = 0, decimalPlaces = 2;
        BigDecimal TGICalculations, YTDGICalculations, SOCSECCalculations, MCCalculations;
        BigDecimal totalGrossIncome, YTDGrossIncome, socSecContribution, medicareContribution;
        BigDecimal hourlyPayRateConverted;
        BigDecimal federalTax, stateTax;
        BigDecimal taxCalculations, contributionCalculations, deductionCalculations, currentTotalDeduction;
        BigDecimal YTDDeduction, YTDDeductionCalculations, netPay, netPayCalculations, YTDnetPay, YTDnetPayCalculations;

        List<String> daysOfWeek = new ArrayList<>(Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"));
        String beginningDay;
        int daysLong;

        for (int num : stubs.get(0).getHoursWorkedEachDay()) {
            totalHours += num;
        }

        model.addAttribute("totalHours", totalHours);

        hourlyPayRateConverted = BigDecimal.valueOf(stubs.get(0).getHourlyPayRate());

        TGICalculations = hourlyPayRateConverted.multiply(BigDecimal.valueOf(totalHours));
        totalGrossIncome = TGICalculations.setScale(2, RoundingMode.HALF_UP);
        model.addAttribute("totalGrossIncome", totalGrossIncome);

        YTDGICalculations = totalGrossIncome.add(stubs.get(0).getYearlyPreviousGross());
        YTDGrossIncome = YTDGICalculations.setScale(2, RoundingMode.HALF_UP);
        model.addAttribute("YTDGrossIncome", YTDGrossIncome);


        SOCSECCalculations = totalGrossIncome.multiply(BigDecimal.valueOf(0.062));
        socSecContribution = SOCSECCalculations.setScale(2, RoundingMode.UP);
        model.addAttribute("socSecContribution", socSecContribution);

        MCCalculations = totalGrossIncome.multiply(BigDecimal.valueOf(0.0145));
        medicareContribution = MCCalculations.setScale(2, RoundingMode.UP);
        model.addAttribute("medicareContribution", medicareContribution);

        //Calling this method to calculate state tax
        stateTax = stateTaxFormula(stubs.get(0).getStateTaxFiling(), totalGrossIncome);
        model.addAttribute("stateTax", stateTax);


        taxCalculations = stubs.get(0).getFederalTax().add(stateTax);
        contributionCalculations = medicareContribution.add(socSecContribution);
        deductionCalculations = taxCalculations.add(contributionCalculations);
        currentTotalDeduction = deductionCalculations.setScale(2, RoundingMode.UP);
        model.addAttribute("currentTotalDeduction", currentTotalDeduction);

        YTDDeductionCalculations = currentTotalDeduction.add(stubs.get(0).getPreviousDeduction());
        YTDDeduction = YTDDeductionCalculations.setScale(2, RoundingMode.UP);
        model.addAttribute("YTDDeduction", YTDDeduction);

        netPayCalculations = totalGrossIncome.subtract(currentTotalDeduction);
        netPay = netPayCalculations.setScale(2, RoundingMode.UP);
        model.addAttribute("netPay", netPay);

        YTDnetPayCalculations = netPay.add(stubs.get(0).getPreviousNetPay());
        YTDnetPay = YTDnetPayCalculations.setScale(2, RoundingMode.UP);
        model.addAttribute("YTDnetPay", YTDnetPay);

        beginningDay = stubs.get(0).getPayPeriodBeginning();
        if (daysOfWeek.contains(beginningDay)) {
            int index = daysOfWeek.indexOf(beginningDay);
            List<String> updatedDays = daysOfWeek.subList(index, daysOfWeek.size());
            model.addAttribute("updatedDays", updatedDays);
        }

        List<Integer> hoursWorked = new ArrayList<>(stubs.get(0).getHoursWorkedEachDay());
        List<Integer> timeWorkedStart = new ArrayList(stubs.get(0).getStartTime());
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

        model.addAttribute("hoursWorked", hoursWorked);
        model.addAttribute("timeWorkedStart", timeWorkedStart);
        model.addAttribute("timeWorkedStartFormatted", timeWorkedStartFormatted);
        model.addAttribute("timeWorkedEnd", timeWorkedEnd);

        daysLong = stubs.get(0).getDaysLong() - 1;
        model.addAttribute("daysLong", daysLong);

        String companyName = stubs.get(0).getName() + " Pay Stub";
        model.addAttribute("companyName", companyName);
        String companyAddress = stubs.get(0).getCompanyAddress();
        model.addAttribute("companyAddress", companyAddress);
        String companyEmail = stubs.get(0).getCompanyEmail();
        model.addAttribute("companyEmail", companyEmail);

        return "stubs/index";
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
        hour = hour % 24;
        if (hour > 0 && hour < 12) {
            return hour + " AM";
        } else if (hour == 0) {
            return " ";
        } else if (hour == 12) {
            return "12 PM";
        } else {
            return (hour - 12) + " PM";
        }
    }

    private static BigDecimal stateTaxFormula(int status, BigDecimal wages) {
        BigDecimal result = null;
        int MOWages = wages.intValue();
        if (status == 0) {
            //these are only guaranteed valid for 2024 semi-monthly
            if (MOWages < 685) {
                result = BigDecimal.valueOf(0);
            } else if (MOWages >= 685 && MOWages < 730) {
                result = BigDecimal.valueOf(1);
            }
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
            ;

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

