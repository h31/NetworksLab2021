package controller;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Response;
import util.ControllerUtil;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class RetrofitController {


    public boolean registration(String username, String password) throws IOException {
        ServiceAPI api = ControllerUtil.getAPINoAuth();
        Call<JsonObject> call = api.register(username, password);
        return authControllerLogic(call);
    }

    public boolean authentication(String username, String password) throws IOException {
        ControllerUtil.initializeHttpClient(username, password);
        ServiceAPI api = ControllerUtil.getAPI();
        Call<JsonObject> call = api.login(username, password);
        return authControllerLogic(call);
    }

    private boolean authControllerLogic(Call<JsonObject> call) throws IOException {
        Response<JsonObject> response;
        try {
            response = call.execute();
        } catch (SocketTimeoutException exception) {
            return false;
        }

        JsonObject jsonObject = response.body();
        if (response.code() == 200) {
            if (jsonObject != null) {
                System.out.println(jsonObject.get("message"));
            }
            return true;
        } else {
            if (jsonObject != null) {
                System.out.println(jsonObject.toString());
                System.out.println(jsonObject.get("message"));
            }
            return false;
        }
    }


    public void getSum(String numbers) throws IOException {
        ServiceAPI api = ControllerUtil.getAPI();
        Call<List<String>> call = api.getSum(numbers);
        getFast(call, "sum");
    }

    public void getSub(String numbers) throws IOException {
        ServiceAPI api = ControllerUtil.getAPI();
        Call<List<String>> call = api.getSub(numbers);
        getFast(call, "sub");
    }

    public void getMul(String numbers) throws IOException {
        ServiceAPI api = ControllerUtil.getAPI();
        Call<List<String>> call = api.getMul(numbers);
        getFast(call, "mul");
    }

    public void getDiv(String numbers) throws IOException {
        ServiceAPI api = ControllerUtil.getAPI();
        Call<List<String>> call = api.getDiv(numbers);
        getFast(call, "div");
    }

    private void getFast(Call<List<String>> call, String opType) throws IOException {
        Response<List<String>> response = call.execute();
        if (response.code() == 200) {
            if (response.body() != null) {
                System.out.println("The result of " + opType + " operation a set of numbers = " + response.body());
            }
            else {
                System.out.println("Something goes wrong, try again");
            }
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
        ServiceAPI api = ControllerUtil.getAPI();
        Call<List<String>> call = api.getSqrt(requestArray.toString());
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
