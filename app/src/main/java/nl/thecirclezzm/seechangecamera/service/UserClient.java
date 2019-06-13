package nl.thecirclezzm.seechangecamera.service;

import nl.thecirclezzm.seechangecamera.model.User;
import nl.thecirclezzm.seechangecamera.model.streamingKey;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserClient {
    @POST("user")
    Call<streamingKey> loginAccount(@Body User user);


}
