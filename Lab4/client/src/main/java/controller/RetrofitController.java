package controller;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Response;
import util.ControllerUtil;

import java.io.IOException;
import java.net.SocketTimeoutException;
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
            } else {
                System.out.println("Something goes wrong, try again");
            }
        }
    }


    public void getSqrt(String numbers) throws IOException, InterruptedException {

        ServiceAPI api = ControllerUtil.getAPI();
        Call<JsonObject> call = api.getSqrt(numbers);
        Response<JsonObject> response = call.execute();
        if (response.code() == 200) {
            waitResult(response, call, api);
        }
    }

    public void getFact(String numbers) throws IOException, InterruptedException {

        ServiceAPI api = ControllerUtil.getAPI();
        Call<JsonObject> call = api.getFact(numbers);
        Response<JsonObject> response = call.execute();
        if (response.code() == 200) {
            waitResult(response, call, api);
        }
    }

    public void waitResult(Response<JsonObject> response, Call<JsonObject> call, ServiceAPI api)
            throws IOException, InterruptedException {

        boolean isOpDone = false;
        String opID;
        if (response.body() != null) {
            opID = response.body().get("id").getAsString();

            while (!isOpDone) {
                call = api.getResult(opID);
                response = call.execute();
                if (response.code() == 200) {
                    if (response.body() != null) {
                        if (response.body().get("result").getAsString().equals("Not ready yet")) { // in processing
                            System.out.println("Waiting...");
                            Thread.sleep(1500);
                        } else { // if done
                            isOpDone = true;
                            System.out.println("The result of "
                                    + "sqrt operation a set of numbers = " +
                                    response.body().get("result").getAsString());
                        }
                    }
                } else { // another response code
                    System.out.println(response.message());
                }
            }
        }
    }
}

