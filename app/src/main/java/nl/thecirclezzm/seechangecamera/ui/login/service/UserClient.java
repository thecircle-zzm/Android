package nl.thecirclezzm.seechangecamera.ui.login.service;

import nl.thecirclezzm.seechangecamera.ui.login.model.User;
import nl.thecirclezzm.seechangecamera.ui.login.model.StreamingKey;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface UserClient {
    @GET("user/{username}")
    Call<User> loginAccount(@Path("username") String username);


}