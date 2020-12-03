package me.mateus.desafiowebservices.models.marvel

import com.google.gson.annotations.SerializedName

data class TextObject(
    @SerializedName("type") val type: String,
    @SerializedName("language") val language: String,
    @SerializedName("text") val text: String
)