package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controller.ServiceAPI;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ControllerUtil {

    public static ServiceAPI getAPINoAuth() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        final String BASE_URL = "http://localhost:8080/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return retrofit.create(ServiceAPI.class);
    }

    public static ServiceAPI getAPI() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        final String BASE_URL = "http://localhost:8080/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();

        return retrofit.create(ServiceAPI.class);
    }

    private static OkHttpClient.Builder httpClient;

    public static void initializeHttpClient(String username, String password) {
        httpClient = new OkHttpClient
                .Builder()
                .addInterceptor(new BasicAuthInterceptor(username, password));
    }

}
