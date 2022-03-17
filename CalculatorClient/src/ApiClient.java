import okhttp3.*;

import java.io.IOException;

public class ApiClient {
    String baseUrl;
    OkHttpClient client = new OkHttpClient();

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }


    String get(String url) {
        Request request = new Request.Builder()
                .url(baseUrl + url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    String post(String url, String data) {
        Request request = new Request.Builder()
                .url(baseUrl + url)
                .post(RequestBody.create(data, MediaType.parse("application/x-www-form-urlencoded")))
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
