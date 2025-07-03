package com.arabseed

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Document

class Arabseed : MainAPI() {
    override var mainUrl = "https://a.asd.homes"
    override var name = "Arabseed"
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Movie)

    override suspend fun getMainPage(): HomePageResponse {
        val doc = app.get(mainUrl).document
        val movies = doc.select("div.MovieBlock").mapNotNull {
            val href = it.selectFirst("a")?.attr("href") ?: return@mapNotNull null
            val title = it.selectFirst("h4")?.text()?.trim() ?: return@mapNotNull null
            val poster = it.selectFirst("img")?.attr("data-src")
            newMovieSearchResponse(title, href, TvType.Movie) {
                this.posterUrl = poster
            }
        }
        return newHomePageResponse(listOf(HomePageList("أحدث الأفلام", movies)))
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1,h2,h4")?.text()?.trim() ?: return newMovieLoadResponse("فيلم بدون اسم", url, TvType.Movie, emptyList()) {}
        val poster = doc.selectFirst("img[src*='uploads']")?.attr("src")
        val description = doc.selectFirst("div.Story")?.text()?.trim()
        return newMovieLoadResponse(title, url, TvType.Movie, emptyList()) {
            this.posterUrl = poster
            this.plot = description
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val doc = app.get(data).document
        val iframe = doc.selectFirst("iframe")?.attr("src")
        if (iframe != null) {
            callback.invoke(
                ExtractorLink(
                    name = this.name,
                    source = "Arabseed Player",
                    url = iframe,
                    referer = mainUrl,
                    quality = Qualities.Unknown.value
                )
            )
        }
    }
}
