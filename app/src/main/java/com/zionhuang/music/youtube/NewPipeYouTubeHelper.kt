package com.zionhuang.music.youtube

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.channel.ChannelInfo
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.playlist.PlaylistInfo
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.io.IOException

@Suppress("BlockingMethodInNonBlockingContext")
object NewPipeYouTubeHelper {
    private val service = NewPipe.getService(ServiceList.YouTube.serviceId) as YoutubeService

    /**
     * Search
     */
    suspend fun search(query: String, contentFilter: List<String>): SearchInfo = checkCache("${query}$${contentFilter[0]}") {
        SearchInfo.getInfo(service, service.searchQHFactory.fromQuery(query, contentFilter, ""))
    }

    suspend fun search(query: String, contentFilter: List<String>, page: Page): InfoItemsPage<InfoItem> = checkCache("${query}$${contentFilter[0]}$${page.hashCode()}") {
        SearchInfo.getMoreItems(service, service.searchQHFactory.fromQuery(query, contentFilter, ""), page)
    }

    suspend fun suggestionsFor(query: String): List<String> = withContext(IO) {
        service.suggestionExtractor.suggestionList(query)
    }

    /**
     * Playlist
     */

    suspend fun getPlaylist(id: String): PlaylistInfo = checkCache(id) {
        PlaylistInfo.getInfo(service, service.playlistLHFactory.getUrl(id))
    }

    suspend fun getPlaylist(id: String, page: Page): InfoItemsPage<StreamInfoItem> = checkCache("$id$${page.hashCode()}") {
        PlaylistInfo.getMoreItems(service, service.playlistLHFactory.getUrl(id), page)
    }

    /**
     * Channel
     */
    suspend fun getChannel(id: String): ChannelInfo = checkCache(id) {
        ChannelInfo.getInfo(service, service.channelLHFactory.getUrl(id))
    }

    suspend fun getChannel(id: String, page: Page): InfoItemsPage<StreamInfoItem> = checkCache("$id$${page.hashCode()}") {
        ChannelInfo.getMoreItems(service, service.channelLHFactory.getUrl(id), page)
    }

    @Throws(IOException::class, ExtractionException::class)
    suspend fun getStreamInfo(id: String): StreamInfo = checkCache("stream$$id") {
        StreamInfo.getInfo(service, service.streamLHFactory.getUrl(id))
    }

    private suspend fun <T : Any> checkCache(id: String, loadFromNetwork: suspend () -> T): T =
        loadFromCache(id) ?: withContext(IO) {
            loadFromNetwork().also {
                InfoCache.putInfo(id, it)
            }
        }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> loadFromCache(id: String): T? =
        InfoCache.getFromKey(id) as T?
}