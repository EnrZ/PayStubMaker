package dev.ez.PayStubMaker.controllers;

import dev.ez.PayStubMaker.models.Stub;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Controller
@RequestMapping("stubs")
public class StubController {

    private static List<Stub> stubs = new ArrayList<>();

    @GetMapping
    public String displayAllStubs(Model model){
        model.addAttribute("stubs" , stubs);

        int totalHours = 0, decimalPlaces = 2;
        BigDecimal TGICalculations, YTDGICalculations, SOCSECCalculations;
        BigDecimal totalGrossIncome, YTDGrossIncome, socSecContribution;
        BigDecimal hourlyPayRateConverted;

        for (int num: stubs.get(0).getHoursWorkedEachDay()){
            totalHours +=num;
        }

        model.addAttribute("totalHours" , totalHours);

        hourlyPayRateConverted = BigDecimal.valueOf(stubs.get(0).getHourlyPayRate());

        TGICalculations = hourlyPayRateConverted.multiply(BigDecimal.valueOf(totalHours));
        totalGrossIncome = TGICalculations.setScale(2, RoundingMode.HALF_UP);
        model.addAttribute("totalGrossIncome", totalGrossIncome);

        YTDGICalculations = totalGrossIncome.add(stubs.get(0).getYearlyPreviousGross());
        YTDGrossIncome = YTDGICalculations.setScale(2, RoundingMode.HALF_UP);
        model.addAttribute("YTDGrossIncome", YTDGrossIncome);

        socSecContribution = totalGrossIncome.multiply(BigDecimal.valueOf(0.062));
        model.addAttribute("socSecContribution", socSecContribution);
        return "stubs/index";
    }

    // /stubs/create route
    @GetMapping("create")
    public String renderCreateStubForm(){

        return "stubs/create";
    }

    @PostMapping("create")
    public String createStub(@ModelAttribute Stub newStub) {
        stubs.add(newStub);
        return "redirect:/stubs";
    }
}

