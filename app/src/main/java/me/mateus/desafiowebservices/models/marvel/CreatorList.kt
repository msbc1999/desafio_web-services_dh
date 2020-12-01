package me.mateus.desafiowebservices.models.marvel

import com.google.gson.annotations.SerializedName

data class CreatorList(
    @SerializedName("available") val available: Int,
    @SerializedName("collectionURI") val collectionURI: String,
    @SerializedName("items") val items: List<CreatorSummary>,
    @SerializedName("returned") val returned: Int
)