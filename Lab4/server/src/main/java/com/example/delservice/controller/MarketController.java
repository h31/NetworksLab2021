package com.example.delservice.controller;

import com.example.delservice.config.View;
import com.example.delservice.dto.MarketDTO;
import com.example.delservice.model.Market;
import com.example.delservice.repository.MarketRepository;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class MarketController {

    @Autowired
    private MarketRepository marketRepository;;

    @ApiOperation(
            value = "Получить список магазинов",
            notes = "Возвращает JSON со списком всех магазинов с идентификатором, названием и зоной",
            response = MarketDTO.class,
            responseContainer = "List")
    @GetMapping("/markets")
    @JsonView(View.Public.class)
    public List<Market> list() {
        return (List<Market>) marketRepository.findAll();
    }

    @ApiOperation(
            value = "Информация о магазине",
            notes = "Возвращает JSON со списком с подробной информацией по указанному магазину",
            response = Market.class)
    @GetMapping("/markets/{market_id}")
    @JsonView(View.ExtendedModel.class)
    public Optional<Market> chosenMarket(
            @PathVariable @ApiParam(value = "Идентификатор магазине", example = "12") String market_id) {
        return marketRepository.findById(Long.valueOf(market_id));
    }

}
