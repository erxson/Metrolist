package com.metrolist.innertube.pages

import com.metrolist.innertube.models.Album
import com.metrolist.innertube.models.AlbumItem
import com.metrolist.innertube.models.Artist
import com.metrolist.innertube.models.ArtistItem
import com.metrolist.innertube.models.MusicResponsiveListItemRenderer
import com.metrolist.innertube.models.SongItem
import com.metrolist.innertube.models.YTItem
import com.metrolist.innertube.models.oddElements
import com.metrolist.innertube.models.splitBySeparator

object SearchSuggestionPage {
    fun fromMusicResponsiveListItemRenderer(renderer: MusicResponsiveListItemRenderer): YTItem? {
        return when {
            renderer.isSong -> {
                SongItem(
                    id = renderer.playlistItemData?.videoId ?: return null,
                    title =
                        renderer.flexColumns
                            .firstOrNull()
                            ?.musicResponsiveListItemFlexColumnRenderer
                            ?.text
                            ?.runs
                            ?.firstOrNull()
                            ?.text ?: return null,
                    artists =
                        renderer.flexColumns
                            .getOrNull(1)
                            ?.musicResponsiveListItemFlexColumnRenderer
                            ?.text
                            ?.runs
                            ?.let { runs ->
                                // First approach: look for elements with navigationEndpoint
                                val artistsWithEndpoint = runs.mapNotNull { run ->
                                    run.navigationEndpoint?.browseEndpoint?.browseId?.let { browseId ->
                                        if (browseId.startsWith("UC") || browseId.startsWith("MPLA")) {
                                            Artist(name = run.text, id = browseId)
                                        } else null
                                    }
                                }
                                
                                if (artistsWithEndpoint.isNotEmpty()) {
                                    artistsWithEndpoint
                                } else {
                                    // Fallback: use splitBySeparator + oddElements approach
                                    runs.splitBySeparator().getOrNull(1)?.oddElements()?.mapNotNull { run ->
                                        when {
                                            run.text.matches(Regex("^\\d+.*")) -> null
                                            run.text.lowercase() in listOf("song", "songs", "•", "views", "view") -> null
                                            run.text.contains("views", ignoreCase = true) -> null
                                            run.text.contains("view", ignoreCase = true) -> null
                                            run.text.isBlank() || run.text.length <= 1 -> null
                                            else -> Artist(name = run.text, id = run.navigationEndpoint?.browseEndpoint?.browseId)
                                        }
                                    } ?: emptyList()
                                }
                            } ?: emptyList(),
                    album =
                        renderer.flexColumns
                            .getOrNull(
                                2,
                            )?.musicResponsiveListItemFlexColumnRenderer
                            ?.text
                            ?.runs
                            ?.firstOrNull()
                            ?.let {
                                Album(
                                    name = it.text,
                                    id = it.navigationEndpoint?.browseEndpoint?.browseId ?: return null,
                                )
                            },
                    duration = null,
                    thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                    explicit =
                        renderer.badges?.find {
                            it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                        } != null,
                )
            }
            renderer.isArtist -> {
                ArtistItem(
                    id = renderer.navigationEndpoint?.browseEndpoint?.browseId ?: return null,
                    title =
                        renderer.flexColumns
                            .firstOrNull()
                            ?.musicResponsiveListItemFlexColumnRenderer
                            ?.text
                            ?.runs
                            ?.firstOrNull()
                            ?.text
                            ?: return null,
                    thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                    shuffleEndpoint =
                        renderer.menu
                            ?.menuRenderer
                            ?.items
                            ?.find { it.menuNavigationItemRenderer?.icon?.iconType == "MUSIC_SHUFFLE" }
                            ?.menuNavigationItemRenderer
                            ?.navigationEndpoint
                            ?.watchPlaylistEndpoint,
                    radioEndpoint =
                        renderer.menu
                            ?.menuRenderer
                            ?.items
                            ?.find { it.menuNavigationItemRenderer?.icon?.iconType == "MIX" }
                            ?.menuNavigationItemRenderer
                            ?.navigationEndpoint
                            ?.watchPlaylistEndpoint,
                )
            }
            renderer.isAlbum -> {
                val secondaryLine =
                    renderer.flexColumns
                        .getOrNull(1)
                        ?.musicResponsiveListItemFlexColumnRenderer
                        ?.text
                        ?.runs
                        ?.splitBySeparator() ?: return null
                AlbumItem(
                    browseId = renderer.navigationEndpoint?.browseEndpoint?.browseId ?: return null,
                    playlistId =
                        renderer.menu
                            ?.menuRenderer
                            ?.items
                            ?.find {
                                it.menuNavigationItemRenderer?.icon?.iconType == "MUSIC_SHUFFLE"
                            }?.menuNavigationItemRenderer
                            ?.navigationEndpoint
                            ?.watchPlaylistEndpoint
                            ?.playlistId ?: return null,
                    title =
                        renderer.flexColumns
                            .firstOrNull()
                            ?.musicResponsiveListItemFlexColumnRenderer
                            ?.text
                            ?.runs
                            ?.firstOrNull()
                            ?.text ?: return null,
                    artists =
                        secondaryLine.getOrNull(1)?.let { runs ->
                            // First approach: look for elements with navigationEndpoint
                            val artistsWithEndpoint = runs.mapNotNull { run ->
                                run.navigationEndpoint?.browseEndpoint?.browseId?.let { browseId ->
                                    if (browseId.startsWith("UC") || browseId.startsWith("MPLA")) {
                                        Artist(name = run.text, id = browseId)
                                    } else null
                                }
                            }
                            
                            if (artistsWithEndpoint.isNotEmpty()) {
                                artistsWithEndpoint
                            } else {
                                // Fallback: use oddElements approach
                                runs.oddElements().mapNotNull { run ->
                                    when {
                                        run.text.matches(Regex("^\\d+.*")) -> null
                                        run.text.matches(Regex("^\\d{4}$")) -> null // years
                                        run.text.lowercase() in listOf("song", "songs", "•", "views", "view", "album", "albums") -> null
                                        run.text.contains("views", ignoreCase = true) -> null
                                        run.text.contains("view", ignoreCase = true) -> null
                                        run.text.isBlank() || run.text.length <= 1 -> null
                                        else -> Artist(name = run.text, id = run.navigationEndpoint?.browseEndpoint?.browseId)
                                    }
                                }
                            }
                        } ?: emptyList(),
                    year =
                        secondaryLine
                            .lastOrNull()
                            ?.firstOrNull()
                            ?.text
                            ?.toIntOrNull(),
                    thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                    explicit =
                        renderer.badges?.find {
                            it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                        } != null,
                )
            }
            else -> null
        }
    }
}
