package com.example.delservice.controller;

import com.example.delservice.dto.OrderDTO;
import com.example.delservice.dto.OrderPriceDTO;
import com.example.delservice.service.MarketService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;



@RestController
public class OrderController {

    private static final Logger logger = LogManager.getLogger(OrderController.class);


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
            logger.error("Threw a JsonProcessingException in OrderController::order," +
                    " full stack trace follows:", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect data");
        }

        return marketService.calculateTheOrder(userOrder);
    }


}
