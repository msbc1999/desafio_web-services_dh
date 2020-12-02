package me.mateus.desafiowebservices.models.marvel

import com.google.gson.annotations.SerializedName

data class CharacterSummary(
    @SerializedName("resourceURI") val resourceURI: String,
    @SerializedName("name") val name: String,
    @SerializedName("role") val role: String
)