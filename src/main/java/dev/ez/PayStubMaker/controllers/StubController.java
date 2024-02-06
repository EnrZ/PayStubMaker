package dev.ez.PayStubMaker.controllers;

import dev.ez.PayStubMaker.models.Stub;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("stubs")
public class StubController {

    private static List<Stub> stubs = new ArrayList<>();

    @GetMapping
    public String displayAllStubs(Model model){
        model.addAttribute("stubs" , stubs);
        return "stubs/index";
    }

    // /stubs/create route
    @GetMapping("create")
    public String renderCreateStubForm(){

        return "stubs/create";
    }

    @PostMapping("create")
    public String createStub(@RequestParam String companyName, @RequestParam String companyEmployee) {
        stubs.add(new Stub (companyName,companyEmployee));
        return "redirect:/stubs";
    }
}

