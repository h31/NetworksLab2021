package com.shop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Good {

    private int id;
    private String name;
    private int count;

    @Override
    public String toString() {
        return "Item: \n \t id: " + id + "\n \t name: " + name + "\n \t count: " + count;
    }
}
