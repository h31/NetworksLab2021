package com.example.delservice.controller;

import com.example.delservice.config.View;
import com.example.delservice.model.Market;
import com.example.delservice.repository.MarketGoodsRepository;
import com.example.delservice.repository.MarketRepository;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class MarketController {

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private MarketGoodsRepository marketGoodsRepository;

    @GetMapping("/markets")
    @JsonView(View.Public.class)
    public Iterable<Market> list() {

        return marketRepository.findAll();
    }

    @GetMapping("/markets/{market_id}")
    @JsonView(View.ExtendedModel.class)
    public Optional<Market> chosenMarket(@PathVariable String market_id) {
        //System.out.println(marketGoodsRepository.findById(1L).get().getMarket().getName());
        return marketRepository.findById(Long.valueOf(market_id));
    }

}
