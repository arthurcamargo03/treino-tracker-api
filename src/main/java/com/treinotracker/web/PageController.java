package com.treinotracker.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "redirect:/exercises";
    }

    @GetMapping("/exercises")
    public String exercises() {
        return "exercises";
    }

    @GetMapping("/exercises/{id}")
    public String exerciseDetail(@PathVariable Long id, Model model) {
        model.addAttribute("exerciseId", id);
        return "exercise-detail";
    }

    @GetMapping("/water")
    public String water() {
        return "water";
    }
}
