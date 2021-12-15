package com.example.delservice.controller;

import com.example.delservice.dto.OrderDTO;
import com.example.delservice.dto.OrderPriceDTO;
import com.example.delservice.model.Market;
import com.example.delservice.service.MarketService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
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


    @ApiOperation(
            value = "Оформить заказ",
            notes = "Возвращает JSON с двумя полями: сумма заказа и сумма доставки",
            response = java.util.Map.class)
    @GetMapping("/order")
    @ResponseBody
    public OrderPriceDTO order(@RequestBody String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        OrderDTO userOrder;
        try {
            userOrder = objectMapper.readValue(json, OrderDTO.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect data");
        }

        OrderPriceDTO result = marketService.calculateTheOrder(userOrder);

        if (result == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect data");
        }

        return result;
    }


}
