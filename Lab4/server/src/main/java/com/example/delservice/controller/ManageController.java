package com.example.delservice.controller;

import com.example.delservice.dto.MarketDTO;
import com.example.delservice.dto.OrderDTO;
import com.example.delservice.service.MarketService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ManageController {

    @Autowired
    private MarketService marketService;

    @PostMapping("/manage/add/market")
    @ResponseBody
    public void addNewMarket(@RequestBody String json) {
        ObjectMapper objectMapper = new ObjectMapper();

        MarketDTO marketDTO;
        try {
            marketDTO = objectMapper.readValue(json, MarketDTO.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect data");
        }

        boolean result = marketService.addNewMarket(marketDTO);

        if (!result) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect data");
        }

        throw new ResponseStatusException(HttpStatus.OK);
    }
}
