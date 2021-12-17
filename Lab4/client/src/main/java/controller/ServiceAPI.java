package controller;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface ServiceAPI {


    @POST("register")
    Call<JsonObject> register(@Query("username") String username,
                              @Query("password") String password);

    @POST("login")
    Call<JsonObject> login(@Query("username") String username,
                           @Query("password") String password);


    @GET("fast/sum")
    Call<List<String>> getSum(@Query("args") String array);

    @GET("fast/sub")
    Call<List<String>> getSub(@Query("args") String array);

    @GET("fast/mul")
    Call<List<String>> getMul(@Query("args") String array);

    @GET("fast/div")
    Call<List<String>> getDiv(@Query("args") String array);

    @GET("slow/fact")
    Call<JsonObject> getFact(@Query("args") String array);

    @GET("slow/sqrt")
    Call<JsonObject> getSqrt(@Query("args") String array);

    @GET("/result")
    Call<JsonObject> getResult(@Query("id") String id);
}
