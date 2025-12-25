package com.playzelo.ludomodule.apiservice;

import com.playzelo.ludomodule.models.AutoMatchRequest;
import com.playzelo.ludomodule.models.LudoRoomResponse;
import com.playzelo.ludomodule.models.MoveTokenRequest;
import com.playzelo.ludomodule.models.MoveTokenResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface LudoApiService {
    @POST("api/game/automatch")
    Call<LudoRoomResponse> automatch(@Body AutoMatchRequest request, @Header("Authorization") String authToken);

    @GET("api/game/{roomId}")
    Call<LudoRoomResponse> getGameById(@Path("roomId") String roomId, @Header("Authorization") String authToken);

    @POST("api/game/roll-dice/{roomId}")
    Call<LudoRoomResponse> rollDice(
            @Path("roomId") String roomId,
            @Header("Authorization") String authHeader
    );

    @POST("api/game/move-token/{roomId}")
    Call<MoveTokenResponse> moveToken(
            @Path("roomId") String roomId,
            @Header("Authorization") String authHeader,
            @Body MoveTokenRequest body
    );

}