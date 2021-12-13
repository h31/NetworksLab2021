package com.example.delservice.controller;

import com.example.delservice.model.Market;
import com.example.delservice.repository.MarketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShopListController {

    @Autowired
    private MarketRepository marketRepository;

    @GetMapping("/shops")
    public Iterable<Market> list() {

        return marketRepository.findAll();
    }
}
