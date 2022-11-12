package uncc.itis5280.bacapp.util;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import java.util.HashMap;

public interface RetrofitInterface {

    @POST("/api/auth")
    Call<UserResult> login(@Body HashMap<String, String> data);

    @POST("/api/signup")
    Call<UserResult> signup(@Body HashMap<String, String> data);

    @POST("/api/user/update")
    Call<UserResult> updateUser(@Header ("x-jwt-token") String token, @Body HashMap<String, Object> data);

    @POST("/api/user/find")
    Call<UserResult> getUserByToken(@Header ("x-jwt-token") String token, @Body HashMap<String, String> data);
}