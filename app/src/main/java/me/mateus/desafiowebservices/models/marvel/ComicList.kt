package me.mateus.desafiowebservices.models.marvel

import com.google.gson.annotations.SerializedName
import me.mateus.desafiowebservices.models.marvel.ComicSummary

data class ComicList(
    @SerializedName("available") val available: Int,
    @SerializedName("collectionURI") val collectionURI: String,
    @SerializedName("items") val items: List<ComicSummary>,
    @SerializedName("returned") val returned: Int
)