package dev.ez.PayStubMaker.controllers;

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

    private static List<String> stubs = new ArrayList<>();

    // /stubs/create route
    @GetMapping("create")
    public String renderCreateStubForm(){
        return "stubs/create";
    }

    @PostMapping("create")
    public String createStub(@RequestParam String companyName) {
        stubs.add(companyName);
        return "redirect:";
    }

    @GetMapping
    public String displayAllStubs(Model model){
        model.addAttribute("stubs" , stubs);
        return "stubs/index";
    }
}

