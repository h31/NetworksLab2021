package com.example.delservice.model;

import com.example.delservice.config.View;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "goods")
public class Goods {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(View.Public.class)
    private Long id;

    @Column(name = "name")
    @JsonView(View.Public.class)
    private String name;

    @OneToMany(mappedBy = "goods")
    private Set<MarketGoods> marketGoods = new HashSet<MarketGoods>();


    public Goods() {
    }

    public Goods(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<MarketGoods> getMarketGoods() {
        return marketGoods;
    }

    public void setMarketGoods(Set<MarketGoods> marketGoods) {
        this.marketGoods = marketGoods;
    }
}
