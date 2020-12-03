package me.mateus.desafiowebservices.models.marvel

import com.google.gson.annotations.SerializedName

data class CharacterList(
    @SerializedName("available") val available: Int,
    @SerializedName("collectionURI") val collectionURI: String,
    @SerializedName("items") val items: List<CharacterSummary>,
    @SerializedName("returned") val returned: Int
)