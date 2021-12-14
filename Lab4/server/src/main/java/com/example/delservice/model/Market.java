package com.example.delservice.model;

import com.example.delservice.config.View;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "market")
public class Market {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(View.Public.class)
    private Long id;

    @JsonView(View.Public.class)
    @Column(name = "name")
    private String name;

    @JsonView(View.Public.class)
    @Column(name = "geo_area")
    private Integer geoArea;

    @JsonView(View.ExtendedModel.class)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "market", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<MarketGoods> marketGoods = new HashSet<MarketGoods>();

    public Market() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Integer getGeoArea() {
        return geoArea;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGeoArea(Integer geoArea) {
        this.geoArea = geoArea;
    }

    public Set<MarketGoods> getMarketGoods() {
        return marketGoods;
    }

    public void setMarketGoods(Set<MarketGoods> marketGoods) {
        this.marketGoods = marketGoods;
    }
}
