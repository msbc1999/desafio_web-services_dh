package me.mateus.desafiowebservices.services

import com.google.gson.JsonObject
import me.mateus.desafiowebservices.models.marvel.CharacterDataWrapper
import me.mateus.desafiowebservices.models.marvel.ComicDataWrapper
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.security.MessageDigest

//private val marvelPublicKey = "6eb7e8896ec5850c52515a8a23ee97f0"
//private val marvelPrivateKey = "0dd0c16fedb8a02985977eafca66b49f5e6a526f"


private val marvelPublicKey = "6eb7e8896ec5850c52515a8a23ee97f0"
private val marvelPrivateKey = "0dd0c16fedb8a02985977eafca66b49f5e6a526f"

private fun String.md5() =
    MessageDigest
        .getInstance("MD5")
        .digest(toByteArray(Charsets.UTF_8))
        .joinToString(separator = "") { b ->
            "%02x".format(b)
        }

private val marvelHttpClient = OkHttpClient().newBuilder()
    .addInterceptor { chain ->
        chain.request().let { original ->
            original.url().newBuilder()
                .addQueryParameter("ts", "1")
                .addQueryParameter("apikey", marvelPublicKey)
                .addQueryParameter("hash", "1$marvelPrivateKey$marvelPublicKey".md5())
                .build().let { url ->
                    original.newBuilder().url(url).build().let { request ->
                        chain.proceed(request)
                    }
                }
        }
    }
    .build()

private val marvelRetrofit = Retrofit.Builder()
    .client(marvelHttpClient)
    .baseUrl("https://gateway.marvel.com/v1/public/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val marvelRepository: MarvelRepository = marvelRetrofit.create(MarvelRepository::class.java)

interface MarvelRepository {

    @GET("characters")
    suspend fun getCharactersCollection(
        @Query("nameStartsWith") nameStartsWith: String,
        @Query("offset") offset: Int = 0
    ): CharacterDataWrapper

    @GET("comics")
    suspend fun getComicsCollection(
        @Query("characters") characters: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): ComicDataWrapper

}