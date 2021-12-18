package com.shop.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.shop.model.Good;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class RestService {

    ObjectMapper mapper = new ObjectMapper();
    public static final int PORT = 8080;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;

    public RestService(String username, String password) {
        this.client = new OkHttpClient.Builder().authenticator((route, response) -> {
            String credential = Credentials.basic(username, password);
            return response.request().newBuilder().header("Authorization", credential).build();
        }).build();
    }

    public List<Good> getAllGoods() throws IOException {
        Request request = new Request.Builder()
                .url(getDefaultUrlBuilder()
                        .addPathSegment("all")
                        .build())
                .build();
        Response response = client.newCall(request).execute();
        String json = Objects.requireNonNull(response.body()).string();
        response.close();
        return mapper.readValue(json, new TypeReference<>() {});
    }

    public String addGood(Good good) throws IOException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        RequestBody formBody = RequestBody.create(ow.writeValueAsString(good), JSON);
        Request request = new Request.Builder()
                .url(getDefaultUrlBuilder()
                        .addPathSegment("admin")
                        .addPathSegment("add")
                        .build())
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();
        return Objects.requireNonNull(response.body()).string();
    }

    public String buyGoods(Integer id, Integer count) throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("id", id.toString())
                .add("count", count.toString())
                .build();
        Request request = new Request.Builder()
                .url(getDefaultUrlBuilder()
                        .addPathSegment("buy")
                        .build())
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();
        String status = Objects.requireNonNull(response.body()).string();
        response.close();
        return status;
    }

    private HttpUrl.Builder getDefaultUrlBuilder() {
        return new HttpUrl.Builder()
                .scheme("http")
                .host("localhost")
                .port(PORT);
    }
}
