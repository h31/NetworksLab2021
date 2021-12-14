package com.example.delservice.service.impl;

import com.example.delservice.dto.GoodsPriceDTO;
import com.example.delservice.dto.MarketDTO;
import com.example.delservice.dto.OrderDTO;
import com.example.delservice.model.Goods;
import com.example.delservice.model.Market;
import com.example.delservice.model.MarketGoods;
import com.example.delservice.repository.GoodsRepository;
import com.example.delservice.repository.MarketGoodsRepository;
import com.example.delservice.repository.MarketRepository;
import com.example.delservice.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MarketServiceImpl implements MarketService {

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private MarketGoodsRepository marketGoodsRepository;

    @Autowired
    private GoodsRepository goodsRepository;

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
            if (marketGoods == null) {
                return null;
            }
            price += marketGoods.getPrice();
        }

        result.put("Order price", price);

        int deliveryPrice = 0;
        if (orderDTO.getUserArea().equals(market.getGeoArea())) {
            deliveryPrice = 1000;
        } else {
            deliveryPrice = (Math.abs(orderDTO.getUserArea() - market.getGeoArea()) + 1) * 1000;
        }

        result.put("Delivery price", deliveryPrice);

        return result;
    }

    public boolean addNewMarket(MarketDTO marketDTO) {
        Market newMarket;
        Optional<Market> marketContainer =
                marketRepository.findByNameAndGeoArea(marketDTO.getMarketName(), marketDTO.getMarketArea());
        if (marketContainer.isPresent()) {
            return false;
        }

        if (marketDTO.getMarketArea() < 0) {
            return false;
        }

        newMarket = new Market();
        newMarket.setName(marketDTO.getMarketName());
        newMarket.setGeoArea(marketDTO.getMarketArea());


        Set<MarketGoods> newMarketGoodsSet = new HashSet<>();

        MarketGoods newMarketGoods;
        Goods newGoods;
        for (GoodsPriceDTO item : marketDTO.getGoodsPriceList()) {
            if (!goodsRepository.existsByName(item.getGoodsName())) {
                newGoods = new Goods(item.getGoodsName());
                goodsRepository.save(newGoods);
                newMarketGoods = new MarketGoods(newMarket, newGoods, item.getGoodsPrice());
            } else {
                newMarketGoods = new MarketGoods(
                        newMarket,
                        goodsRepository.findByName(item.getGoodsName()),
                        item.getGoodsPrice());
            }
            newMarketGoodsSet.add(newMarketGoods);
        }

        newMarket.setMarketGoods(newMarketGoodsSet);

        marketRepository.save(newMarket);

        return true;

    }

}
