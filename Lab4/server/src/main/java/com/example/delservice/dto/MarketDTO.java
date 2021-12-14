package com.example.delservice.dto;

public class MarketDTO {

    private String marketName;

    private Integer marketArea;

    private GoodsPriceDTO[] goodsPriceList;

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public Integer getMarketArea() {
        return marketArea;
    }

    public void setMarketArea(Integer marketArea) {
        this.marketArea = marketArea;
    }

    public GoodsPriceDTO[] getGoodsPriceList() {
        return goodsPriceList;
    }

    public void setGoodsPriceList(GoodsPriceDTO[] goodsPriceList) {
        this.goodsPriceList = goodsPriceList;
    }
}
