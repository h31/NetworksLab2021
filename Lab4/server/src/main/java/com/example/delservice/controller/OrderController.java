package com.example.delservice.controller;

import com.example.delservice.dto.OrderDTO;
import com.example.delservice.service.MarketService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
public class OrderController {

    @Autowired
    private MarketService marketService;

    @GetMapping("/order")
    @ResponseBody
    public Map<String, Integer> order(@RequestBody String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        OrderDTO userOrder;
        try {
            userOrder = objectMapper.readValue(json, OrderDTO.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect data");
        }

        Map<String, Integer> result = marketService.calculateTheOrder(userOrder);

        if (result == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect data");
        }

        return result;
    }


}
