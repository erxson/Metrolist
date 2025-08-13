package com.metrolist.music.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.metrolist.music.R
import com.metrolist.music.ui.utils.top

val LocalBottomSheetPageState = compositionLocalOf { BottomSheetPageState() }

@Stable
class BottomSheetPageState(
    isVisible: Boolean = false,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    var isVisible by mutableStateOf(isVisible)
    var content by mutableStateOf(content)

    fun show(content: @Composable ColumnScope.() -> Unit) {
        isVisible = true
        this.content = content
    }

    fun dismiss() {
        isVisible = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetPage(
    modifier: Modifier = Modifier,
    state: BottomSheetPageState,
    background: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation),
) {
    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
        confirmValueChange = { value ->
            when (value) {
                SheetValue.Hidden -> {
                    state.dismiss()
                    false
                }
                else -> true
            }
        }
    )

    LaunchedEffect(state.isVisible) {
        if (state.isVisible) {
            focusManager.clearFocus()
        }
    }

    if (state.isVisible) {
        ModalBottomSheet(
            onDismissRequest = { state.dismiss() },
            sheetState = sheetState,
            dragHandle = { 
                DragHandle(
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            },
            windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
            shape = RoundedCornerShape(
                topStart = 28.dp,
                topEnd = 28.dp
            ),
            containerColor = background,
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                state.content(this)
            }
        }
    }
}
