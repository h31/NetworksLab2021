package com.example.delservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ShopListController {

    @GetMapping("/")
    public String home() {
        System.out.println("home hello))");
        return "hello";
    }

    @GetMapping("/shops")
    public Map<String, Integer> list() {
        List<Map<String, Integer>> shopsList = new ArrayList<Map<String, Integer>>();
        Map<String, Integer> bufferMap = new HashMap<>();
        bufferMap.put("Перекресток", 1);
        
        bufferMap.put("Перекресток1", 3);
        bufferMap.put("Перекресток2", 2);
        shopsList.add(bufferMap);

        return bufferMap;
    }
}
