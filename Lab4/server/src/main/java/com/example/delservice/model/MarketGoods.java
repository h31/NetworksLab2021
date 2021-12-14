package com.example.delservice.model;

import com.example.delservice.config.View;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;

@Entity
@Table(name = "market_goods")
public class MarketGoods {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "market_id")
    @JsonView(View.Internal.class)
    private Market market;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "goods_id")
    @JsonView(View.Public.class)
    private Goods goods;

    @Column(name = "price")
    @JsonView(View.Public.class)
    private Integer price;


    public MarketGoods() {
    }

    public Market getMarket() {
        return market;
    }

    public void setMarket(Market market) {
        this.market = market;
    }

    public Goods getGoods() {
        return goods;
    }

    public void setGoods(Goods goods) {
        this.goods = goods;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
