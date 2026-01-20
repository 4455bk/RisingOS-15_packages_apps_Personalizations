/*
 * Copyright (C) 2025 AxionOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rising.settings.fragments.lockscreen

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.android.settings.R

@Composable
fun WidgetPickerBottomSheet(
    visible: Boolean,
    current: List<WidgetItem>,
    canAddBig: Boolean = true,
    canAddSmall: Boolean = true,
    onDismiss: () -> Unit,
    onSelect: (WidgetItem) -> Unit
) {
    var showSizeOptions by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val available = WidgetsList().filter { it !in current.map { it.name } }

    val titleText = showSizeOptions?.replaceFirstChar { it.uppercase() }
        ?: stringResource(R.string.widgets_title)

    val screenHeight = LocalDensity.current.run { context.resources.displayMetrics.heightPixels.toDp() }
    val sheetHeight = screenHeight * 0.4f
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else sheetHeight + 50.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "sheet_animation"
    )

    fun dismissSheet() {
        showSizeOptions = null
        onDismiss()
    }

    if (visible || offsetY < sheetHeight) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .pointerInput(Unit) { detectTapGestures(onTap = { dismissSheet() }) },
            contentAlignment = Alignment.BottomCenter
        ) {
            BottomSheetContainer(
                sheetHeight = sheetHeight,
                offsetY = offsetY,
                showSizeOptions = showSizeOptions,
                titleText = titleText,
                available = available,
                current = current,
                onSelect = onSelect,
                onDismiss = { dismissSheet() },
                onBack = { showSizeOptions = null },
                onPickSize = { showSizeOptions = it }
            )
        }
    }
}

@Composable
private fun BottomSheetContainer(
    sheetHeight: Dp,
    offsetY: Dp,
    showSizeOptions: String?,
    titleText: String,
    available: List<String>,
    current: List<WidgetItem>,
    onSelect: (WidgetItem) -> Unit,
    onDismiss: () -> Unit,
    onBack: () -> Unit,
    onPickSize: (String) -> Unit
) {
    val bigWidgetCount = current.count { it.span == 2 }
    val smallWidgetCount = current.count { it.span == 1 }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(sheetHeight)
            .offset(y = offsetY)
            .background(
                if (showSizeOptions != null) surface() else surfaceVariant(),
                RoundedCornerShape(topStart = Dimens.SheetCorner, topEnd = Dimens.SheetCorner)
            )
            .clip(RoundedCornerShape(topStart = Dimens.SheetCorner, topEnd = Dimens.SheetCorner))
            .clickable(enabled = false) {}
    ) {
        SheetTitle(titleText)

        if (showSizeOptions != null) {
            SizePicker(
                widgetName = showSizeOptions,
                current = current,
                onSelect = onSelect,
                onDismiss = onDismiss,
                onBack = onBack
            )
        } else {
            WidgetPager(
                available = available,
                sheetHeight = sheetHeight,
                onPickSize = onPickSize
            )
        }
    }
}

@Composable
private fun BoxScope.SheetTitle(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = Dimens.SheetTopPadding)
    ) {
        Text(
            text = title,
            fontSize = Dimens.SheetTitleFont,
            fontWeight = FontWeight.Thin,
            color = onSurface()
        )
    }
}

@Composable
private fun BoxScope.SizePicker(
    widgetName: String,
    current: List<WidgetItem>,
    onSelect: (WidgetItem) -> Unit,
    onDismiss: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(1, 2).forEach { span ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(Color.Transparent, RoundedCornerShape(Dimens.WidgetCorner))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        val bigCount = current.count { it.span == 2 }
                        val smallCount = current.count { it.span == 1 }
                        
                        val canAdd = if (span == 2) bigCount < 2 else smallCount < 4
                        
                        if (!canAdd) {
                            val message = if (span == 2) context.getString(R.string.row_1_full_message) else context.getString(R.string.row_2_full_message)
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        } else {
                            onSelect(WidgetItem(widgetName, span))
                            onDismiss()
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .width(if (span == 1) Dimens.WidgetSlot else Dimens.WidgetSlot * 2)
                        .height(Dimens.WidgetSlot),
                    contentAlignment = Alignment.Center
                ) {
                    WidgetItem(widgetName, span).Render(onRemove = {}, showRemove = false)
                }
                Spacer(Modifier.height(Dimens.SheetSpacerSmall))
                Text("${span}x1", color = onSurface())
            }
        }
    }

    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(Dimens.SheetTopPadding)
            .size(Dimens.SheetCloseSize)
            .clip(CircleShape)
            .clickable { onBack() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Close,
            contentDescription = stringResource(R.string.close),
            tint = onSurface()
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WidgetPager(
    available: List<String>,
    sheetHeight: Dp,
    onPickSize: (String) -> Unit
) {
    val itemsPerPage = 2
    val pageCount = (available.size + itemsPerPage - 1) / itemsPerPage
    val pagerState = rememberPagerState { pageCount }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.SheetPagerTop),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetHeight * 0.8f)
        ) { page ->
            WidgetPage(
                widgets = available.drop(page * itemsPerPage).take(itemsPerPage),
                onPickSize = onPickSize
            )
        }

        Spacer(Modifier.height(Dimens.SheetSpacerMedium))
        PageIndicator(pageCount = pageCount, currentPage = pagerState.currentPage)
    }
}

@Composable
private fun WidgetPage(
    widgets: List<String>,
    onPickSize: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.SheetPagerPadding),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SheetPagerSpacing)
    ) {
        widgets.forEach { widget ->
            WidgetTile(modifier = Modifier.weight(1f), widget = widget, onPickSize = onPickSize)
        }

        if (widgets.size < 2) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun WidgetTile(modifier: Modifier, widget: String, onPickSize: (String) -> Unit) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(Dimens.TileCorner))
                    .background(surface())
                    .border(Dimens.TileBorder, surface(), RoundedCornerShape(Dimens.TileCorner))
                    .clickable { onPickSize(widget) }
            ) {
                val cardSize = maxWidth
                val circleSize = cardSize * 1.4f
                Box(
                    modifier = Modifier
                        .size(circleSize)
                        .offset(
                            x = circleSize * 0.16f,
                            y = circleSize * 0.16f
                        )
                        .clip(CircleShape)
                        .background(surfaceVariant())
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = WidgetIcon(widget),
                        contentDescription = widget,
                        tint = onSurface(),
                        modifier = Modifier.size(cardSize * 0.2f)
                    )
                }
            }

            Icon(
                imageVector = WidgetIcon(widget),
                contentDescription = widget,
                tint = onSurface(),
                modifier = Modifier
                    .size(Dimens.TileIcon)
                    .align(Alignment.TopStart)
                    .padding(start = Dimens.SheetPagerPadding, top = Dimens.SheetPagerPadding)
            )
        }

        Spacer(Modifier.height(Dimens.TileTextSpacer))
        Text(
            widget.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium,
            color = onSurface()
        )
    }
}

@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val animProgress by animateFloatAsState(
                targetValue = if (index == currentPage) 1f else 0f,
                animationSpec = tween(durationMillis = 300)
            )

            val indicatorWidth = Dimens.TilePagerIndicator + (16.dp * animProgress)
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(Dimens.TilePagerIndicator)
                    .width(indicatorWidth * 2)
                    .clip(CircleShape)
                    .background(
                        if (index == currentPage)
                            MaterialTheme.colorScheme.primary
                        else
                            surface()
                    )
            )
        }
    }
}
