package com.bmdu.dhanlaxmi.Api

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ✅ BASE_URL
    private const val BASE_URL = "http://65.0.122.72"

    private val gson = GsonBuilder().setLenient().create()

    // ── FIX 1: Accept: application/json header — Laravel ko JSON mode mein karo
    // Bina is header ke Laravel HTML return karta hai (admin login page)
    private val acceptJsonInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Accept", "application/json")       // ← KEY FIX
            .addHeader("Content-Type", "application/json")
            .build()
        chain.proceed(request)
    }

    // ── FIX 2: Lenient JsonReader — MalformedJson fix
    private val lenientConverterFactory = object : Converter.Factory() {
        override fun responseBodyConverter(
            type: Type,
            annotations: Array<out Annotation>,
            retrofit: Retrofit
        ): Converter<ResponseBody, *>? {
            val delegate = gson.getAdapter(TypeToken.get(type))
            return Converter<ResponseBody, Any?> { body ->
                val reader = JsonReader(InputStreamReader(body.byteStream(), Charsets.UTF_8))
                reader.isLenient = true
                delegate.read(reader)
            }
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(acceptJsonInterceptor)             // ← pehle ye
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    internal val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(lenientConverterFactory)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}