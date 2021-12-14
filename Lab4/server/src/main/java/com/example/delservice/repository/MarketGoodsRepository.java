package com.example.delservice.repository;

import com.example.delservice.model.Goods;
import com.example.delservice.model.Market;
import com.example.delservice.model.MarketGoods;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MarketGoodsRepository extends CrudRepository<MarketGoods, Long> {

    MarketGoods findByMarketIdAndGoodsId(Long market_id, Long goods_id);

    List<MarketGoods> findAllByMarket(Market market);

}
