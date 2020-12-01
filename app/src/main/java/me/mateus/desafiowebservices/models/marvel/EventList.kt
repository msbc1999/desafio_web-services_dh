package me.mateus.desafiowebservices.models.marvel

import com.google.gson.annotations.SerializedName

data class EventList(
    @SerializedName("available") val available: Int,
    @SerializedName("collectionURI") val collectionURI: String,
    @SerializedName("items") val items: List<String>,
    @SerializedName("returned") val returned: Int
)