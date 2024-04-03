package com.zionhuang.music.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.LocalPlayerConnection
import com.zionhuang.music.R
import com.zionhuang.music.ui.component.LocalMenuState
import com.zionhuang.music.ui.component.NavigationTitle
import com.zionhuang.music.ui.component.YouTubeGridItem
import com.zionhuang.music.ui.component.shimmer.GridItemPlaceHolder
import com.zionhuang.music.ui.component.shimmer.ShimmerHost
import com.zionhuang.music.ui.component.shimmer.TextPlaceholder
import com.zionhuang.music.ui.menu.YouTubeAlbumMenu
import com.zionhuang.music.viewmodels.ExploreViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExploreScreen(
    navController: NavController,
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val explorePage by viewModel.explorePage.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            Spacer(
                Modifier.height(
                    LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateTopPadding()
                )
            )
            explorePage?.newReleaseAlbums?.let { newReleaseAlbums ->
                NavigationTitle(
                    title = stringResource(R.string.new_release_albums),
                    onClick = {
                        navController.navigate("new_release")
                    }
                )

                LazyRow(
                    contentPadding = WindowInsets.systemBars
                        .only(WindowInsetsSides.Horizontal)
                        .asPaddingValues()
                ) {
                    items(
                        items = newReleaseAlbums,
                        key = { it.id }
                    ) { album ->
                        YouTubeGridItem(
                            item = album,
                            isActive = mediaMetadata?.album?.id == album.id,
                            isPlaying = isPlaying,
                            coroutineScope = coroutineScope,
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        navController.navigate("album/${album.id}")
                                    },
                                    onLongClick = {
                                        menuState.show {
                                            YouTubeAlbumMenu(
                                                albumItem = album,
                                                navController = navController,
                                                onDismiss = menuState::dismiss
                                            )
                                        }
                                    }
                                )
                                .animateItemPlacement()
                        )
                    }
                }
            }

            explorePage?.moodAndGenres?.let { moodAndGenres ->
                NavigationTitle(
                    title = stringResource(R.string.mood_and_genres),
                    onClick = {
                        navController.navigate("mood_and_genres")
                    }
                )

                LazyHorizontalGrid(
                    rows = GridCells.Fixed(4),
                    contentPadding = PaddingValues(6.dp),
                    modifier = Modifier.height((MoodAndGenresButtonHeight + 12.dp) * 4 + 12.dp)
                ) {
                    items(moodAndGenres) {
                        MoodAndGenresButton(
                            title = it.title,
                            onClick = {
                                navController.navigate("youtube_browse/${it.endpoint.browseId}?params=${it.endpoint.params}")
                            },
                            modifier = Modifier
                                .padding(6.dp)
                                .width(180.dp)
                        )
                    }
                }
            }

            if (explorePage == null) {
                ShimmerHost {
                    TextPlaceholder(
                        height = 36.dp,
                        modifier = Modifier
                            .padding(vertical = 12.dp, horizontal = 12.dp)
                            .width(250.dp)
                    )
                    Row{
                        repeat(2) {
                            GridItemPlaceHolder()
                        }
                    }
                    TextPlaceholder(
                        height = 36.dp,
                        modifier = Modifier
                            .padding(vertical = 12.dp, horizontal = 12.dp)
                            .width(250.dp)
                    )
                    repeat(4) {
                        Row {
                            repeat(2) {
                                TextPlaceholder(
                                    height = MoodAndGenresButtonHeight,
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp)
                                        .width(200.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
