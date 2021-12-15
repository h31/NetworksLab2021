package com.example.delservice.service;

import com.example.delservice.dto.MarketDTO;
import com.example.delservice.dto.OrderDTO;
import com.example.delservice.dto.OrderPriceDTO;
import com.example.delservice.model.Market;

import java.util.Map;

public interface MarketService {

    OrderPriceDTO calculateTheOrder(OrderDTO orderDTO);

    public boolean addNewMarket(MarketDTO marketDTO);
}
