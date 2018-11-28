package com.example.demoflowfest;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Filip Hrisafov
 */
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello(Principal principal) {
        return "Hello " + principal.getName() + ", welcome FlowFest 2018";
    }

}
