/*
 * Copyright (C) 2025 AxionOS
 * Copyright (C) 2025 Rising Revived Android Project
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

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.*
import com.android.settings.R

@Composable
fun WidgetGrid(
    widgetItems: List<WidgetItem>,
    onUpdate: (WidgetItem) -> Unit,
    onReorder: (List<WidgetItem>) -> Unit,
    onPickWidget: () -> Unit
) {
    val bigWidgets = widgetItems.filter { it.span == 2 }
    val smallWidgets = widgetItems.filter { it.span == 1 }
    val totalHeight = (Dimens.WidgetSlot * 2) + Dimens.WidgetSpacing + (Dimens.WidgetContainerPadding * 2)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
            .padding(horizontal = Dimens.WidgetContainerPadding),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    Dimens.WidgetBorderWidth,
                    Color.White.copy(alpha = 0.5f),
                    RoundedCornerShape(Dimens.WidgetCorner)
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onPickWidget()
                }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.WidgetSpacing),
            verticalArrangement = Arrangement.spacedBy(Dimens.WidgetSpacing)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.WidgetSlot),
                horizontalArrangement = if (bigWidgets.isEmpty()) {
                    Arrangement.Center
                } else if (bigWidgets.size == 1) {
                    Arrangement.Center
                } else {
                    Arrangement.spacedBy(Dimens.WidgetSpacing)
                }
            ) {
                when (bigWidgets.size) {
                    0 -> {
                        repeat(2) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }
                    1 -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .fillMaxHeight()
                        ) {
                            bigWidgets[0].Render(onRemove = { onUpdate(bigWidgets[0]) })
                        }
                    }
                    else -> {
                        bigWidgets.forEach { widget ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                widget.Render(onRemove = { onUpdate(widget) })
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.WidgetSlot),
                horizontalArrangement = when (smallWidgets.size) {
                    0, 4 -> Arrangement.spacedBy(Dimens.WidgetSpacing)
                    else -> Arrangement.Center
                }
            ) {
                when (smallWidgets.size) {
                    0 -> {
                        repeat(4) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }
                    1 -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.25f)
                                .fillMaxHeight()
                        ) {
                            smallWidgets[0].Render(onRemove = { onUpdate(smallWidgets[0]) })
                        }
                    }
                    2 -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(0.5f),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.WidgetSpacing)
                        ) {
                            smallWidgets.forEach { widget ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    widget.Render(onRemove = { onUpdate(widget) })
                                }
                            }
                        }
                    }
                    3 -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(0.75f),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.WidgetSpacing)
                        ) {
                            smallWidgets.forEach { widget ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    widget.Render(onRemove = { onUpdate(widget) })
                                }
                            }
                        }
                    }
                    else -> {
                        smallWidgets.forEach { widget ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                widget.Render(onRemove = { onUpdate(widget) })
                            }
                        }
                    }
                }
            }
        }
        if (widgetItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = stringResource(R.string.tap_to_add_widgets),
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
