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

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.android.settings.R

enum class CustomizerTab {
    CLOCK,
    WIDGETS,
    NOWBAR,
    PEEK_DISPLAY,
    WEATHER
}

@Composable
fun LockscreenConfigPanel(
    visible: Boolean,
    selectedTab: CustomizerTab,
    onTabChange: (CustomizerTab) -> Unit,
    widgetItems: List<WidgetItem>,
    clockConfig: ClockConfig,
    nowBarConfig: NowBarConfig,
    peekConfig: PeekDisplayConfig,
    weatherConfig: WeatherConfig,
    currentPagerPage: Int,
    weatherTextView: DummyWeatherTextView? = null,
    onWidgetUpdate: (List<WidgetItem>) -> Unit,
    onClockUpdate: (ClockConfig) -> Unit,
    onNowBarUpdate: (NowBarConfig) -> Unit,
    onPeekUpdate: (PeekDisplayConfig) -> Unit,
    onWeatherUpdate: (WeatherConfig) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val screenHeight = LocalDensity.current.run { 
        context.resources.displayMetrics.heightPixels.toDp() 
    }
    val sheetHeight = screenHeight * 0.6f
    val isDarkTheme = isSystemInDarkTheme()
    
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else sheetHeight + 50.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )
    
    if (visible || offsetY < sheetHeight) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .pointerInput(Unit) { 
                    detectTapGestures(onTap = { onDismiss() }) 
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sheetHeight)
                    .offset(y = offsetY)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .clickable(enabled = false) {},
                color = if (isDarkTheme) Color.Black else MaterialTheme.colorScheme.surface,
                tonalElevation = if (isDarkTheme) 0.dp else 3.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(4.dp)
                                .background(
                                    if (isDarkTheme) 
                                        Color.White.copy(alpha = 0.3f)
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomizerTab.values().forEach { tab ->
                            CircularTab(
                                selected = selectedTab == tab,
                                onClick = { onTabChange(tab) },
                                icon = tab.icon(),
                                label = tab.displayName(),
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Divider(
                        color = if (isDarkTheme) 
                            Color.White.copy(alpha = 0.1f) 
                        else 
                            MaterialTheme.colorScheme.outlineVariant
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        when (selectedTab) {
                            CustomizerTab.CLOCK -> ClockConfigContent(
                                config = clockConfig,
                                currentPagerPage = currentPagerPage,
                                onUpdate = onClockUpdate,
                                isDarkTheme = isDarkTheme
                            )
                            CustomizerTab.WIDGETS -> WidgetConfigContent(
                                widgetItems = widgetItems,
                                onUpdate = onWidgetUpdate,
                                isDarkTheme = isDarkTheme,
                                currentClockStyle = currentPagerPage
                            )
                            CustomizerTab.NOWBAR -> NowBarConfigContent(
                                config = nowBarConfig,
                                onUpdate = onNowBarUpdate,
                                isDarkTheme = isDarkTheme,
                                currentClockStyle = currentPagerPage
                            )
                            CustomizerTab.PEEK_DISPLAY -> PeekDisplayConfigContent(
                                config = peekConfig,
                                onUpdate = onPeekUpdate,
                                isDarkTheme = isDarkTheme,
                                currentClockStyle = currentPagerPage
                            )
                            CustomizerTab.WEATHER -> WeatherConfigContent(
                                config = weatherConfig,
                                onUpdate = onWeatherUpdate,
                                weatherTextView = weatherTextView,
                                isDarkTheme = isDarkTheme,
                                currentClockStyle = currentPagerPage
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CircularTab(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isDarkTheme: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.animateContentSize()
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = when {
                selected && isDarkTheme -> Color.White.copy(alpha = 0.15f)
                selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                isDarkTheme -> Color.White.copy(alpha = 0.05f)
                else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            },
            modifier = Modifier.size(60.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = when {
                        selected && isDarkTheme -> Color.White
                        selected -> MaterialTheme.colorScheme.primary
                        isDarkTheme -> Color.White.copy(alpha = 0.6f)
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 6.dp),
                color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CustomizerTab.displayName(): String {
    return when (this) {
        CustomizerTab.CLOCK -> stringResource(R.string.clock_tab)
        CustomizerTab.WIDGETS -> stringResource(R.string.widgets_tab)
        CustomizerTab.NOWBAR -> stringResource(R.string.nowbar_tab)
        CustomizerTab.PEEK_DISPLAY -> stringResource(R.string.peek_tab)
        CustomizerTab.WEATHER -> stringResource(R.string.weather_tab)
    }
}

@Composable
private fun CustomizerTab.icon(): androidx.compose.ui.graphics.vector.ImageVector {
    return when (this) {
        CustomizerTab.CLOCK -> Icons.Default.Schedule
        CustomizerTab.WIDGETS -> Icons.Default.Widgets
        CustomizerTab.NOWBAR -> Icons.Default.ViewCarousel
        CustomizerTab.PEEK_DISPLAY -> Icons.Default.NotificationsActive
        CustomizerTab.WEATHER -> Icons.Default.Cloud
    }
}
