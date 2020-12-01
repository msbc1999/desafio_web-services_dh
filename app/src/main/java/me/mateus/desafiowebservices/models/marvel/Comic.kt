package me.mateus.desafiowebservices.models.marvel

import com.google.gson.annotations.SerializedName

data class Comic(
    @SerializedName("id") val id: Int,
    @SerializedName("digitalId") val digitalId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("issueNumber") val issueNumber: Int,
    @SerializedName("variantDescription") val variantDescription: String,
    @SerializedName("description") val description: String,
    @SerializedName("modified") val modified: String,
    @SerializedName("isbn") val isbn: String,
    @SerializedName("upc") val upc: Int,
    @SerializedName("diamondCode") val diamondCode: String,
    @SerializedName("ean") val ean: String,
    @SerializedName("issn") val issn: String,
    @SerializedName("format") val format: String,
    @SerializedName("pageCount") val pageCount: Int,
    @SerializedName("textObjects") val textObjects: List<String>,
    @SerializedName("resourceURI") val resourceURI: String,
    @SerializedName("urls") val urls: List<Url>,
    @SerializedName("series") val series: SeriesSummary,
    @SerializedName("variants") val variants: List<ComicSummary>,
    @SerializedName("collections") val collections: List<String>,
    @SerializedName("collectedIssues") val collectedIssues: List<String>,
    @SerializedName("dates") val dates: List<ComicDate>,
    @SerializedName("prices") val prices: List<ComicPrice>,
    @SerializedName("thumbnail") val thumbnail: Image,
    @SerializedName("images") val images: List<String>,
    @SerializedName("creators") val creators: CreatorList,
    @SerializedName("characters") val characters: CharacterList,
    @SerializedName("stories") val stories: StoryList,
    @SerializedName("events") val events: EventList
)