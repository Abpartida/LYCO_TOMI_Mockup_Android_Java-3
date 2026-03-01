package com.lyco.tomi.network;

import android.util.Log;

import com.lyco.tomi.BuildConfig;
import com.squareup.moshi.Moshi;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * Lazily builds a Retrofit instance that speaks to the FastAPI backend.
 */
public final class FastApiClient {
    private static volatile FastApiService service;

    private FastApiClient() {
    }

    public static FastApiService getService() {
        if (service == null) {
            synchronized (FastApiClient.class) {
                if (service == null) {
                    service = buildService();
                }
            }
        }
        return service;
    }

    private static FastApiService buildService() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> Log.d("FastApiClient", message));
        logging.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient client = new OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(chain -> {
                Request requestWithHeaders = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .build();
                return chain.proceed(requestWithHeaders);
            })
            .addInterceptor(logging)
            .build();

        Moshi moshi = new Moshi.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(ApiEnvironment.getBaseUrl())
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build();

        return retrofit.create(FastApiService.class);
    }

    public static void reset() {
        service = null;
    }
}
