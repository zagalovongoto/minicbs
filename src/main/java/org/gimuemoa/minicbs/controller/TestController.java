package org.gimuemoa.minicbs.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/htmx")
public class TestController {
    @GetMapping("/test1")
    public String test1() {

        return "<p>Yes we can!</p>";
    }

}

