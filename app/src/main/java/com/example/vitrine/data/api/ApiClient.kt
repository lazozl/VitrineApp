package com.example.vitrine.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL =
        "https://hbbrfjspweksdqpoplci.supabase.co/"

    private const val ANON_KEY =
        "sb_publishable_3QVoVl_xzWQf5hVkIy7_HQ_xXB_e7tH"

    private val okHttpClient: OkHttpClient by lazy {

        OkHttpClient.Builder()
            .addInterceptor { chain ->

                val request = chain.request()
                    .newBuilder()
                    .header("apikey", ANON_KEY)
                    .header("Accept", "application/json")
                    .build()

                chain.proceed(request)
            }
            .build()
    }

    val retrofit: Retrofit by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val service: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}