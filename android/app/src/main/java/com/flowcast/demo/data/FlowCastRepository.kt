package com.flowcast.demo.data

import com.flowcast.demo.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

data class FeedResult<T>(
    val items: List<T>,
    val isFallback: Boolean,
    val errorMessage: String? = null,
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
)

data class DetailResult<T>(
    val item: T?,
    val isFallback: Boolean,
    val errorMessage: String? = null,
)

object FlowCastRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
    private val client = OkHttpClient.Builder().build()
    private val baseUrl = BuildConfig.FLOWCAST_BASE_URL.trimEnd('/')
    private val pexelsVideoBaseUrl = BuildConfig.PEXELS_VIDEO_BASE_URL.trimEnd('/')
    private val pexelsApiKey = BuildConfig.PEXELS_API_KEY

    private var latestVideos: List<VideoItem> = DemoRepository.videos
    private var latestNews: List<NewsItem> = DemoRepository.news

    suspend fun loadVideos(
        cursor: String? = null,
        limit: Int = 5,
        query: String = "technology",
    ): FeedResult<VideoItem> = runCatching {
        if (pexelsApiKey.isBlank()) {
            throw IOException("Pexels API key is missing")
        }
        val page = cursor?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val perPage = limit.coerceIn(1, 50)
        val params = buildList {
            add("query=${query.encodeQuery()}")
            add("page=$page")
            add("per_page=$perPage")
            add("orientation=portrait")
            add("locale=zh-CN")
        }.joinToString("&")
        val response = get(
            url = "$pexelsVideoBaseUrl/search?$params",
            headers = mapOf("Authorization" to pexelsApiKey),
        )
        val feed = json.decodeFromString(PexelsVideoFeedResponse.serializer(), response)
        val videos = feed.videos.mapNotNull { it.toModel() }
        if (videos.isEmpty()) {
            throw IOException("No playable Pexels videos")
        }
        val hasMore = videos.isNotEmpty() && page * perPage < feed.totalResults
        VideoFeedResponse(
            items = videos,
            nextCursor = if (hasMore) (page + 1).toString() else null,
            hasMore = hasMore,
        )
    }.fold(
        onSuccess = { feed ->
            val videos = feed.items
            latestVideos = if (cursor.isNullOrBlank()) {
                videos
            } else {
                (latestVideos + videos).distinctBy { it.id }
            }
            FeedResult(
                items = videos,
                isFallback = false,
                nextCursor = feed.nextCursor,
                hasMore = feed.hasMore,
            )
        },
        onFailure = { error ->
            latestVideos = DemoRepository.videos
            FeedResult(
                items = DemoRepository.videos,
                isFallback = true,
                errorMessage = error.readableMessage(),
                nextCursor = null,
                hasMore = false,
            )
        },
    )

    suspend fun loadNews(channel: String? = null): FeedResult<NewsItem> = runCatching {
        val url = if (channel.isNullOrBlank() || channel == "全部") {
            "$baseUrl/news"
        } else {
            "$baseUrl/news?channel=${channel.encodeQuery()}"
        }
        val response = get(url)
        json.decodeFromString(NewsFeedResponse.serializer(), response).items.map { it.toModel() }
    }.fold(
        onSuccess = { news ->
            latestNews = news
            FeedResult(items = news, isFallback = false)
        },
        onFailure = { error ->
            latestNews = DemoRepository.news
            FeedResult(items = DemoRepository.news, isFallback = true, errorMessage = error.readableMessage())
        },
    )

    suspend fun loadNewsDetail(newsId: String): DetailResult<NewsItem> = runCatching {
        val response = get("$baseUrl/news/${newsId.encodePathSegment()}")
        json.decodeFromString(NewsDto.serializer(), response).toModel()
    }.fold(
        onSuccess = { news ->
            latestNews = latestNews.replaceById(news)
            DetailResult(item = news, isFallback = false)
        },
        onFailure = { error ->
            val fallback = latestNews.firstOrNull { it.id == newsId }
                ?: DemoRepository.news.firstOrNull { it.id == newsId }
            DetailResult(item = fallback, isFallback = fallback != null, errorMessage = error.readableMessage())
        },
    )

    fun findVideo(videoId: String): VideoItem? =
        latestVideos.firstOrNull { it.id == videoId } ?: DemoRepository.videos.firstOrNull { it.id == videoId }

    private suspend fun get(url: String, headers: Map<String, String> = emptyMap()): String = withContext(Dispatchers.IO) {
        val requestBuilder = Request.Builder().url(url).get()
        headers.forEach { (name, value) -> requestBuilder.header(name, value) }
        val request = requestBuilder.build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }
            body
        }
    }

    private fun Throwable.readableMessage(): String = message ?: "网络请求失败"
}

private data class VideoFeedResponse(
    val items: List<VideoItem> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false,
)

@Serializable
private data class PexelsVideoFeedResponse(
    val videos: List<PexelsVideoDto> = emptyList(),
    @SerialName("total_results") val totalResults: Int = 0,
)

@Serializable
private data class PexelsVideoDto(
    val id: Long = 0,
    val url: String = "",
    val image: String = "",
    val duration: Int = 0,
    val user: PexelsUserDto? = null,
    @SerialName("video_files") val videoFiles: List<PexelsVideoFileDto> = emptyList(),
) {
    fun toModel(): VideoItem? {
        val playUrl = videoFiles.bestPlayableUrl() ?: return null
        val title = url
            .substringBeforeLast("?")
            .trimEnd('/')
            .substringAfterLast('/')
            .replace("-", " ")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            .ifBlank { "Pexels Video $id" }
        return VideoItem(
            id = "pexels-$id",
            title = title,
            author = user?.name?.takeIf { it.isNotBlank() } ?: "Pexels Creator",
            source = "Pexels",
            description = url.ifBlank { "Pexels public video" },
            coverUrl = image,
            playUrl = playUrl,
            durationSeconds = duration,
            likes = "0",
            comments = "0",
            tags = listOf("Pexels", "Public Video"),
        )
    }
}

@Serializable
private data class PexelsUserDto(
    val name: String = "",
)

@Serializable
private data class PexelsVideoFileDto(
    val width: Int = 0,
    val height: Int = 0,
    val link: String = "",
    @SerialName("file_type") val fileType: String = "",
)

@Serializable
private data class NewsFeedResponse(
    val items: List<NewsDto> = emptyList(),
)

@Serializable
private data class NewsDto(
    val id: String,
    val title: String,
    val summary: String,
    val source: String,
    @SerialName("published_at") val publishedAt: String,
    @SerialName("key_points") val keyPoints: List<String> = emptyList(),
    @SerialName("source_url") val sourceUrl: String = "",
) {
    fun toModel(): NewsItem = NewsItem(
        id = id,
        title = title,
        summary = summary,
        source = source,
        publishedAt = publishedAt,
        keyPoints = keyPoints,
        sourceUrl = sourceUrl,
    )
}

private fun String.encodePathSegment(): String =
    java.net.URLEncoder.encode(this, Charsets.UTF_8.name()).replace("+", "%20")

private fun String.encodeQuery(): String = java.net.URLEncoder.encode(this, Charsets.UTF_8.name())

private fun List<NewsItem>.replaceById(item: NewsItem): List<NewsItem> =
    if (any { it.id == item.id }) map { if (it.id == item.id) item else it } else this + item

private fun List<PexelsVideoFileDto>.bestPlayableUrl(): String? =
    filter { it.link.isNotBlank() }
        .maxWithOrNull(
            compareBy<PexelsVideoFileDto>(
                { if (it.fileType.equals("video/mp4", ignoreCase = true)) 1 else 0 },
                { if (it.height >= it.width) 1 else 0 },
                { it.height },
                { it.width },
            ),
        )
        ?.link
