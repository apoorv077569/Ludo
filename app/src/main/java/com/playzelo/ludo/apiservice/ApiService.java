package com.playzelo.ludo.apiservice;


import com.playzelo.ludo.models.CreateRoomRequest;
import com.playzelo.ludo.models.JoinRoomBody;
import com.playzelo.ludo.models.RoomListResponse;
import com.playzelo.ludo.models.RoomResponse;
import com.playzelo.ludo.models.User;
import com.playzelo.ludo.models.UserResponse;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    @POST("api/users/create")
    Call<User> signup(@Body Map<String, String> body);

    @POST("api/auth/login")
    Call<UserResponse> login(@Body Map<String, String> body);

    @POST("api/auth/google-play-login")
    Call<ResponseBody> playGamesLogin(@Body RequestBody body);


    @GET("api/rooms")
    Call<RoomListResponse> getRooms();

    @POST("api/rooms/create")
    Call<RoomResponse> createRoom(@Body CreateRoomRequest body);

    @PUT("api/rooms/join/{roomId}")
    Call<RoomResponse> joinRoom(
            @Path("roomId") String roomId,
            @Body JoinRoomBody body
    );


}

