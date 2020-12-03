package me.mateus.desafiowebservices.models.marvel

import com.google.gson.annotations.SerializedName

data class ComicPrice(
    @SerializedName("type") val type: String,
    @SerializedName("price") val price: String
)