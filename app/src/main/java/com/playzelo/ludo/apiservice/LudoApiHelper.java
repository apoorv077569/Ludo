//package com.playzelo.ludo.apiservice;
//
//import androidx.annotation.NonNull;
//
//import com.playzelo.ludo.models.AutoMatchRequest;
//import com.playzelo.ludo.models.LudoRoomResponse;
//import com.playzelo.ludo.models.MoveTokenRequest;
//import com.playzelo.ludo.models.MoveTokenResponse;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.http.Header;
//
//public class LudoApiHelper {
//
////    private final LudoApiService apiService;
//    private String authToken;
//
////    private LudoApiHelper(@NonNull String authToken) {
////        this.authToken = authToken;
////        // ApiClient.getClient() ka use karke LudoApiService ko banate hain.
////        apiService = ApiClient.getClient(authToken).create(LudoApiService.class);
////    }
//
//    public static LudoApiHelper getInstance(@NonNull String authToken) {
////        return new LudoApiHelper(authToken);
//    }
//
//    /**
//     * Automatch API
//     */
//    public void automatch(double entryFee, double winPrize, @NonNull String type, @NonNull Callback<LudoRoomResponse> callback) {
//        AutoMatchRequest request = new AutoMatchRequest(entryFee, winPrize, type);
////        Call<LudoRoomResponse> call = apiService.automatch(request, authToken);
//        call.enqueue(callback);
//    }
//
//    public void getGameById(@NonNull String roomId, @Header("Authorization") String authToken, @NonNull Callback<LudoRoomResponse> callback) {
//        Call<LudoRoomResponse> call = apiService.getGameById(roomId, authToken);
//        call.enqueue(callback);
//    }
//
////    public void rollDice(String roomId, Callback<LudoRoomResponse> callback) {
////        Call<LudoRoomResponse> call = apiService.rollDice(roomId, "Bearer " + authToken);
////        call.enqueue(callback);
////    }
//
////    public static void moveToken(String roomId, MoveTokenRequest request,
////                                 String authToken, Callback<MoveTokenResponse> callback) {
////        LudoApiService apiService = ApiClient.getClient(authToken).create(LudoApiService.class);
////
////        Call<MoveTokenResponse> call = apiService.moveToken(
////                roomId,
////                "Bearer " + authToken,
////                request
////        );
////        call.enqueue(callback);
////    }
//}