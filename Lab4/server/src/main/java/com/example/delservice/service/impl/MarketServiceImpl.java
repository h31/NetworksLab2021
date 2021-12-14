package com.example.delservice.service.impl;

import com.example.delservice.dto.OrderDTO;
import com.example.delservice.model.Market;
import com.example.delservice.model.MarketGoods;
import com.example.delservice.repository.MarketGoodsRepository;
import com.example.delservice.repository.MarketRepository;
import com.example.delservice.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MarketServiceImpl implements MarketService {

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private MarketGoodsRepository marketGoodsRepository;

    public Map<String, Integer> calculateTheOrder(OrderDTO orderDTO) {
        Map<String, Integer> result = new HashMap<>();

        Market market;
        Optional<Market> marketContainer = marketRepository.findById(orderDTO.getMarketId());
        if (marketContainer.isPresent()) {
            market = marketContainer.get();
        } else {
            return null;
        }

        int price = 0;
        MarketGoods marketGoods;
        for (Long good_id : orderDTO.getGoodsIdArray()) {
            marketGoods = marketGoodsRepository.findByMarketIdAndGoodsId(market.getId(), good_id);
            if(marketGoods == null) { return null; }
            price += marketGoods.getPrice();
        }

        result.put("Order price", price);

        int deliveryPrice = 0;
        if(orderDTO.getUserArea().equals(market.getGeoArea())) {
            deliveryPrice = 1000;
        }
        else {
            deliveryPrice = (Math.abs(orderDTO.getUserArea() - market.getGeoArea()) + 1) * 1000;
        }

        result.put("Delivery price", deliveryPrice);

        return result;
    }


}
