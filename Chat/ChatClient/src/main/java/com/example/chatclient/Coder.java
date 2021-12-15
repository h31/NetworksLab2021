package com.example.chatclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Coder {
    public String encodeData(Map<String, String> data) {
        List<String> params = new ArrayList<>();
        for (Map.Entry<String, String> e : data.entrySet()) {
            String value = e.getValue();
            if (value == null) {
                params.add(e.getKey());
            }
            else {
                value = value.replaceAll("[&\\\\]", "\\\\$0");
                params.add(e.getKey() + "=" + value);
            }
        }
        return String.join("&", params);
    }

    public Map<String, String> decodeData(String msg) {
        Map<String, String> data = new HashMap<>();
        for (String pair : msg.split("((?<!\\\\)&)|((?<=\\\\\\\\)&)")) {
            String[] entry = pair.split("=", 2);
            data.put(
                    entry[0],
                    entry.length == 1 ?
                            null :
                            entry[1].replaceAll("\\\\\\\\", "\\\\")
            );
        }
        return data;
    }
}
