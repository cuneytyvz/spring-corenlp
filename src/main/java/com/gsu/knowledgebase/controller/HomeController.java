package com.gsu.knowledgebase.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("KnowledgeBaseHomeController")
public class HomeController {

    @RequestMapping("/knowledgeBase")
    public String home() {
        return "static/knowledge-base/index.html";
    }
}