package com.playzelo.ludo.apiservice;


import com.playzelo.ludo.models.User;
import com.playzelo.ludo.models.UserResponse;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/users/create")
    Call<User> signup(@Body Map<String, String> body);

    @POST("api/users/login")
    Call<UserResponse> login(@Body Map<String, String> body);

}

