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
    public static final String HOST = "http://localhost:" + PORT;
    public static final String BASE_URL = "/goods";
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client;

    public RestService(String username, String password) {
        this.client = new OkHttpClient.Builder().authenticator((route, response) -> {
            String credential = Credentials.basic(username, password);
            return response.request().newBuilder().header("Authorization", credential).build();
        }).build();
    }

    public List<Good> getAllGoods() throws IOException {
        Request request = new Request.Builder()
                .url(HOST + BASE_URL + "/")
                .build();
        Response response = client.newCall(request).execute();
        String json = Objects.requireNonNull(response.body()).string();
        response.close();
        return mapper.readValue(json, new TypeReference<List<Good>>() {});
    }

    public String addGood(Good good) throws IOException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        RequestBody formBody = RequestBody.create(ow.writeValueAsString(good), JSON);
        Request request = new Request.Builder()
                .url(HOST + BASE_URL + "/admin/addGoods")
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();
        String status = Objects.requireNonNull(response.body()).string();
        return status;
    }

    public String buyGoods(Integer id) throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("id", id.toString())
                .build();
        Request request = new Request.Builder()
                .url(HOST + BASE_URL + "/buyGoods")
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();
        String status = Objects.requireNonNull(response.body()).string();
        response.close();
        return status;
    }
}
