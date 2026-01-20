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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.*
import android.app.WallpaperManager
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.text.font.*
import androidx.compose.ui.viewinterop.AndroidView
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.TextClock
import android.view.ViewGroup
import android.widget.ImageView
import android.provider.Settings
import android.os.Handler
import android.os.Looper
import com.android.settings.R
import com.rising.settings.fragments.ui.fonts.FontManager
import com.android.internal.util.android.ThemeUtils
import com.android.settings.utils.SystemRestartUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LockscreenCustomizerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LockscreenPreviewTheme {
                LockscreenCustomizer()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LockscreenCustomizer() {
    val context = LocalContext.current
    val wallpaper = WallpaperManager.getInstance(context).drawable
    val scope = rememberCoroutineScope()
    
    var showConfigPanel by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(CustomizerTab.CLOCK) }

    var widgetItems by remember { mutableStateOf(load(context)) }
    
    var clockConfig by remember { mutableStateOf(ClockConfig.load(context)) }
    var nowBarConfig by remember { mutableStateOf(NowBarConfig.load(context)) }
    var peekConfig by remember { mutableStateOf(PeekDisplayConfig.load(context)) }
    var weatherConfig by remember { mutableStateOf(WeatherConfig.load(context)) }
    var weatherTextViewRef by remember { mutableStateOf<DummyWeatherTextView?>(null) }
    
    var currentTime by remember { mutableStateOf(Calendar.getInstance().time) }
    
    val pagerState = rememberPagerState(
        initialPage = clockConfig.style,
        pageCount = { ClockConfig.getTotalStyles() }
    )
    
    val clockHeights = remember { mutableStateMapOf<Int, Int>() }
    val density = LocalDensity.current
    
    var lastKnownHeight by remember { mutableStateOf(400) }
    
    val targetHeight = remember {
        derivedStateOf {
            val currentPage = pagerState.currentPage
            val nextPage = if (pagerState.currentPageOffsetFraction > 0) currentPage + 1 else currentPage - 1
            val currentHeight = clockHeights[currentPage] ?: lastKnownHeight
            val nextHeight = clockHeights[nextPage] ?: currentHeight
            
            if (clockHeights[currentPage] != null) {
                lastKnownHeight = clockHeights[currentPage]!!
            }
            
            val fraction = kotlin.math.abs(pagerState.currentPageOffsetFraction)
            (currentHeight * (1 - fraction) + nextHeight * fraction).toInt()
        }
    }
    
    val animatedHeight by animateIntAsState(
        targetValue = targetHeight.value,
        animationSpec = if (clockHeights.isEmpty()) {
            snap()
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        }
    )
    
    var isFabExpanded by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance().time
            kotlinx.coroutines.delay(1000L)
        }
    }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000L)
        isFabExpanded = false
    }
    
    LaunchedEffect(weatherTextViewRef, weatherConfig) {
        weatherTextViewRef?.let { view ->
            updateWeatherTextViewConfig(view, weatherConfig)
        }
    }

    LaunchedEffect(showConfigPanel) {
        val reloadedWidgets = load(context)
        widgetItems = reloadedWidgets
    }

    Box(modifier = Modifier.fillMaxSize()) {
        wallpaper?.toBitmap()?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                alignment = Alignment.Center,
                contentScale = ContentScale.Crop
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            with(density) {
                                (animatedHeight + 64.dp.toPx() + 16.dp.toPx() + 48.dp.toPx()).toDp()
                            }
                        )
                        .border(
                            2.dp,
                            Color.White.copy(alpha = 0.5f),
                            RoundedCornerShape(24.dp)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            selectedTab = CustomizerTab.CLOCK
                            showConfigPanel = true
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) { page ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .onGloballyPositioned { coordinates ->
                                        clockHeights[page] = coordinates.size.height
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                SwipeableClockPreview(
                                    layoutId = ClockConfig.getLayoutId(page),
                                    fontPackage = clockConfig.fontPackage,
                                    currentTime = currentTime,
                                    weatherEnabled = weatherConfig.enabled,
                                    onWeatherTextViewAvailable = { weatherTextViewRef = it },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        
                        Surface(
                            modifier = Modifier.wrapContentWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = ClockConfig.getClockName(pagerState.currentPage),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${pagerState.currentPage + 1}/${ClockConfig.getTotalStyles()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.weight(1f))
            
            if (widgetItems.isNotEmpty()) {
                WidgetGrid(
                    widgetItems = widgetItems,
                    onUpdate = { removed ->
                        val newItems = widgetItems.filter { it != removed }
                        widgetItems = newItems
                        save(context, newItems)
                    },
                    onReorder = { newItems ->
                        widgetItems = newItems
                        save(context, newItems)
                    },
                    onPickWidget = {
                        selectedTab = CustomizerTab.WIDGETS
                        showConfigPanel = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.weight(1f))

            if (peekConfig.enabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                2.dp,
                                Color.White.copy(alpha = 0.5f),
                                RoundedCornerShape(24.dp)
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                selectedTab = CustomizerTab.PEEK_DISPLAY
                                showConfigPanel = true
                            }
                    ) {
                        Box(modifier = Modifier.padding(12.dp)) {
                            PeekDisplayPreview(peekConfig)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            if (nowBarConfig.enabled) {
                Spacer(modifier = Modifier.height(nowBarConfig.marginBottom.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(260.dp)
                            .wrapContentHeight()
                            .border(
                                2.dp,
                                Color.White.copy(alpha = 0.5f),
                                RoundedCornerShape(36.dp)
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                selectedTab = CustomizerTab.NOWBAR
                                showConfigPanel = true
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .wrapContentHeight()
                        ) {
                            NowBarPreview(nowBarConfig)
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(42.dp))
            }
        }
        
        var isApplying by remember { mutableStateOf(false) }
        
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            ExtendedFloatingActionButton(
                onClick = {
                    if (!isApplying) {
                        isApplying = true
                        scope.launch {
                            applyChangesAndRestart(
                                context = context,
                                clockPosition = pagerState.currentPage,
                                onSuccess = {
                                    isApplying = false
                                },
                                onFailure = {
                                    isApplying = false
                                }
                            )
                        }
                    }
                },
                expanded = isFabExpanded,
                icon = {
                    if (isApplying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.apply_theme),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                text = {
                    AnimatedVisibility(
                        visible = isFabExpanded,
                        enter = expandHorizontally(
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            expandFrom = Alignment.End
                        ) + fadeIn(animationSpec = tween(300)),
                        exit = shrinkHorizontally(
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            shrinkTowards = Alignment.End
                        ) + fadeOut(animationSpec = tween(300))
                    ) {
                        Text(
                            text = if (isApplying) stringResource(R.string.applying) else stringResource(R.string.apply_theme),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.animateContentSize()
            )
            
            FloatingActionButton(
                onClick = { 
                    showConfigPanel = !showConfigPanel
                    if (showConfigPanel) {
                        isFabExpanded = false
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (showConfigPanel) Icons.Default.Close else Icons.Default.Settings,
                    contentDescription = stringResource(R.string.configure)
                )
            }
        }
        
        LockscreenConfigPanel(
            visible = showConfigPanel,
            selectedTab = selectedTab,
            onTabChange = { selectedTab = it },
            widgetItems = widgetItems,
            clockConfig = clockConfig,
            nowBarConfig = nowBarConfig,
            peekConfig = peekConfig,
            weatherConfig = weatherConfig,
            currentPagerPage = pagerState.currentPage,
            weatherTextView = weatherTextViewRef,
            onWidgetUpdate = { newItems ->
                widgetItems = newItems
            },
            onClockUpdate = { config ->
                clockConfig = config
                config.save(context)
                scope.launch {
                    pagerState.animateScrollToPage(config.style)
                }
            },
            onNowBarUpdate = { config ->
                nowBarConfig = config
                config.save(context)
            },
            onPeekUpdate = { config ->
                peekConfig = config
                config.save(context)
            },
            onWeatherUpdate = { config ->
                weatherConfig = config
                config.save(context)
            },
            onDismiss = { showConfigPanel = false }
        )
    }
}

private suspend fun applyChangesAndRestart(
    context: android.content.Context,
    clockPosition: Int,
    onSuccess: () -> Unit,
    onFailure: () -> Unit
) {
    try {
        android.provider.Settings.Secure.putInt(
            context.contentResolver,
            "clock_style",
            clockPosition
        )
        android.provider.Settings.Secure.putInt(
            context.contentResolver,
            "lock_screen_custom_clock_face",
            0
        )
        
        if (!ClockConfig.supportsFontCustomization(clockPosition)) {
            android.provider.Settings.Secure.putString(
                context.contentResolver,
                "lock_screen_clock_font_package",
                null
            )
        }

        if (!ClockConfig.supportsCustomization(clockPosition)) {
            android.provider.Settings.System.putInt(
                context.contentResolver,
                "lockscreen_widgets_enabled",
                0
            )
            android.provider.Settings.System.putInt(
                context.contentResolver,
                "lockscreen_weather_enabled",
                0
            )
            android.provider.Settings.System.putInt(
                context.contentResolver,
                "keyguard_now_bar_enabled",
                0
            )
            android.provider.Settings.Secure.putInt(
                context.contentResolver,
                "peek_display_notifications",
                0
            )
        }

        val themeUtils = ThemeUtils.getInstance(context)
        val mCenterClocks = intArrayOf(2, 3, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15, 16, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)
        
        themeUtils.setOverlayEnabled(
            "android.theme.customization.hideclock",
            if (clockPosition != 0) "com.android.systemui.clocks.hideclock" else "android",
            "android"
        )
        themeUtils.setOverlayEnabled(
            "android.theme.customization.smartspace",
            if (clockPosition != 0) "com.android.systemui.hide.smartspace" else "com.android.systemui",
            "com.android.systemui"
        )
        themeUtils.setOverlayEnabled(
            "android.theme.customization.smartspace_offset",
            if (clockPosition != 0 && mCenterClocks.contains(clockPosition))
                "com.android.systemui.smartspace_offset.smartspace"
            else
                "com.android.systemui",
            "com.android.systemui"
        )
        
        kotlinx.coroutines.delay(1000)
        
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                SystemRestartUtils.restartSystemUI(context)
                Handler(Looper.getMainLooper()).postDelayed({
                    android.widget.Toast.makeText(
                        context,
                        context.getString(R.string.settings_applied_successfully),
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    onSuccess()
                }, 500)
            } catch (e: Exception) {
                android.widget.Toast.makeText(
                    context,
                    context.getString(R.string.settings_saved_restart_message),
                    android.widget.Toast.LENGTH_LONG
                ).show()
                onFailure()
            }
        }, 100)
    } catch (e: Exception) {
        Handler(Looper.getMainLooper()).post {
            android.widget.Toast.makeText(
                context,
                context.getString(R.string.settings_saved_restart_message),
                android.widget.Toast.LENGTH_LONG
            ).show()
            onFailure()
        }
    }
}

@Composable
fun SwipeableClockPreview(
    layoutId: Int,
    fontPackage: String,
    currentTime: Date,
    weatherEnabled: Boolean,
    onWeatherTextViewAvailable: (DummyWeatherTextView?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fontManager = remember { FontManager(context as android.app.Activity, true) }

    var weatherTextView by remember { mutableStateOf<DummyWeatherTextView?>(null) }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                val inflater = LayoutInflater.from(ctx)
                val view = inflater.inflate(layoutId, null, false)
                
                if (fontPackage != "default") {
                    val typeface = fontManager.getTypeface(ctx, fontPackage)
                    if (typeface != null) {
                        applyTypefaceToView(view, typeface)
                    }
                }
                
                val shouldScale = layoutId in listOf(
                    R.layout.keyguard_clock_stylish,
                    R.layout.keyguard_clock_stylish2,
                    R.layout.keyguard_clock_stylish3,
                    R.layout.keyguard_clock_stylish4,
                    R.layout.keyguard_clock_stylish5,
                    R.layout.keyguard_clock_stylish6,
                    R.layout.keyguard_clock_stylish7,
                    R.layout.keyguard_clock_stylish8,
                    R.layout.keyguard_clock_stylish9,
                    R.layout.keyguard_clock_stylish10
                )
                
                if (shouldScale) {
                    view.scaleX = 0.7f
                    view.scaleY = 0.7f
                }
                
                val extractedWeatherTextView = extractWeatherTextView(view)
                weatherTextView = extractedWeatherTextView
                onWeatherTextViewAvailable(extractedWeatherTextView)
                
                enableWeatherViews(view, weatherEnabled)

                view
            },
            update = { view ->
                enableWeatherViews(view, weatherEnabled)
            },
            modifier = Modifier.wrapContentSize()
        )
    }
}

private fun extractWeatherTextView(view: android.view.View): DummyWeatherTextView? {
    return when (view) {
        is DummyWeatherTextView -> view
        is ViewGroup -> {
            for (i in 0 until view.childCount) {
                val result = extractWeatherTextView(view.getChildAt(i))
                if (result != null) return result
            }
            null
        }
        else -> null
    }
}

private fun enableWeatherViews(view: android.view.View, enabled: Boolean) {
    when (view) {
        is DummyWeatherImageView -> {
            view.setWeatherEnabled(enabled)
        }
        is DummyWeatherTextView -> {
            view.setWeatherEnabled(enabled)
        }
        is ViewGroup -> {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                when (child) {
                    is DummyWeatherImageView -> child.setWeatherEnabled(enabled)
                    is DummyWeatherTextView -> child.setWeatherEnabled(enabled)
                    is ViewGroup -> enableWeatherViews(child, enabled)
                }
            }
        }
    }
}

private fun updateWeatherTextViewConfig(textView: DummyWeatherTextView?, config: WeatherConfig) {
    textView?.let {
        it.setWeatherEnabled(config.enabled)
        it.setShowLocation(config.showLocation)
        it.setShowText(config.showText)
    }
}

private fun applyTypefaceToView(view: android.view.View, typeface: android.graphics.Typeface) {
    when (view) {
        is TextView, is TextClock -> {
            (view as TextView).typeface = typeface
        }
        is ViewGroup -> {
            for (i in 0 until view.childCount) {
                applyTypefaceToView(view.getChildAt(i), typeface)
            }
        }
    }
}
