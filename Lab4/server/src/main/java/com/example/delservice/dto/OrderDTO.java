package com.example.delservice.dto;

public class OrderDTO {

    private Long marketId;

    private Long[] goodsIdArray;

    private Integer userArea;


    public Long getMarketId() {
        return marketId;
    }

    public void setMarketId(Long marketId) {
        this.marketId = marketId;
    }

    public Long[] getGoodsIdArray() {
        return goodsIdArray;
    }

    public void setGoodsIdArray(Long[] goodsIdArray) {
        this.goodsIdArray = goodsIdArray;
    }

    public Integer getUserArea() {
        return userArea;
    }

    public void setUserArea(Integer userArea) {
        this.userArea = userArea;
    }
}
