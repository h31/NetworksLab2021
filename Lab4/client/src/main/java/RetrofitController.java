import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RetrofitController {

    /*private String basicAuthUsername = "";
    private String basicAuthPassword = "";*/

    OkHttpClient.Builder httpClient = new OkHttpClient
            .Builder();


    protected ServiceAPI getAPI() {
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


    protected boolean registration(String username, String password) throws IOException {
        ServiceAPI api = getAPI();
        Call<JsonObject> call = api.register(username, password);
        return authControllerLogic(call);
    }

    protected boolean authentication(String username, String password) throws IOException {
        ServiceAPI api = getAPI();
        Call<JsonObject> call = api.login(username, password);
        httpClient.addInterceptor(new BasicAuthInterceptor(username, password));
        return authControllerLogic(call);
    }

    private boolean authControllerLogic(Call<JsonObject> call) throws IOException {
        Response<JsonObject> response;
        response = call.execute();

        JsonObject jsonObject = response.body();
        System.out.println(jsonObject.toString());
        if (response.code() == 200) {
            if (jsonObject != null) {
                System.out.println(jsonObject.get("message"));
            }
            return true;
        } else {
            if (jsonObject != null) {
                System.out.println(jsonObject.get("message"));
            }
            return false;
        }
    }

    protected void calcSqrt() throws IOException {
        List<Double> requestArray = new ArrayList<>();
        requestArray.add(-1.0);
        requestArray.add(-1.0);
        requestArray.add(-1.0);
        requestArray.add(-1.0);
        requestArray.add(-1.0);
        requestArray.add(-1.0);
        requestArray.add(49.0);
        ServiceAPI api = getAPI();
        Call<List<String>> call = api.calcSqrt(requestArray.toString());
        Response<List<String>> response;
        response = call.execute();
        System.out.println(response.code());

        if (response.code() == 200) {
            System.out.println("Успешный ответ от сервера на /sqrt");
            List<String> jsonObject = response.body();
            System.out.println(jsonObject.toString());
        }
    }
}
