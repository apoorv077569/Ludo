//package com.playzelo.ludomodule.apiservice;
//
//import kotlin.jvm.internal.PropertyReference0Impl;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//
//public class ApiClient {
//    private static final String BASE_URL = "https://playzelo-nrwt.onrender.com/";
//
//    public static Retrofit getClient(final String token) {
//        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
//        if (token != null && !token.trim().isEmpty()) {
//            httpClient.addInterceptor(chain -> {
//                Request original = chain.request();
//                Request.Builder requestBuilder = original.newBuilder()
//                        .header("Accept", "application/json")
//                        .header("Content-Type", "application/json");
//
//                String headerValue = token.startsWith("Bearer ") ? token : "Bearer " + token;
//                requestBuilder.header("Authorization", headerValue);
//
//                Request request = requestBuilder.method(original.method(), original.body()).build();
//                return chain.proceed(request);
//            });
//        }
//        OkHttpClient client = httpClient.build();
//        return new Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .client(client)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//    }
//
//    public static Retrofit getClient() {
//        return getClient(null);
//    }
//}

package com.playzelo.ludomodule.apiservice;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://playzelo-nrwt.onrender.com/";

    public static Retrofit getClient(final String token) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // wait up to 30s to connect
                .readTimeout(30, TimeUnit.SECONDS)    // wait up to 30s for server response
                .writeTimeout(30, TimeUnit.SECONDS);  // wait up to 30s for request body write

        // Add logging interceptor (for debugging requests/responses)
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(logging);

        // Add Authorization header if token exists
        if (token != null && !token.trim().isEmpty()) {
            httpClient.addInterceptor(chain -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json");

                String headerValue = token.startsWith("Bearer ") ? token : "Bearer " + token;
                requestBuilder.header("Authorization", headerValue);

                Request request = requestBuilder.method(original.method(), original.body()).build();
                return chain.proceed(request);
            });
        }

        OkHttpClient client = httpClient.build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

}
