package dev.ez.PayStubMaker.controllers;

import dev.ez.PayStubMaker.models.Stub;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("stubs")
public class StubController {

    private static List<Stub> stubs = new ArrayList<>();

    @GetMapping
    public String displayAllStubs(Model model){
        model.addAttribute("stubs" , stubs);

        int total = 0;

        for (int num: stubs.get(0).getHoursWorkedEachDay()){
            total +=num;
        }

        model.addAttribute("total" , total);

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

