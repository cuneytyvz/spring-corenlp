package com.gsu.knowledgebase.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

//@RequestMapping("/")
@Controller("KnowledgeBaseHomeController")
public class HomeController {

    @RequestMapping("/knowledge-base")
    public String home() {
        return "static/knowledge-base/index.html";
    }

    @RequestMapping("/memory")
    public String memory() {
        return "static/knowledge-base/memory.html";
    }
}