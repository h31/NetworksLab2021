import com.google.gson.JsonObject;
import model.User;
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

    @GET("slow/sqrt")
    Call<List<String>> calcSqrt(@Query("args") String array);
}
