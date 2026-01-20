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

import android.content.Context
import android.os.UserHandle
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import android.view.LayoutInflater
import android.widget.TextClock
import android.widget.TextView
import android.view.ViewGroup
import android.os.Handler
import android.os.Looper
import com.android.settings.R
import com.rising.settings.fragments.ui.fonts.FontManager
import com.android.internal.util.android.ThemeUtils
import com.android.settings.utils.SystemRestartUtils
import kotlinx.coroutines.launch

@Composable
fun getClockNameResource(context: Context, style: Int): String {
    return ClockConfig.getClockName(style)
}

data class ClockConfig(
    val style: Int = 0,
    val fontPackage: String = "default"
) {
    companion object {
        private val CLOCK_LAYOUTS = intArrayOf(
            R.layout.keyguard_clock_default,      // 0
            R.layout.keyguard_clock_oos,          // 1
            R.layout.keyguard_clock_center,       // 2
            R.layout.keyguard_clock_simple,       // 3
            R.layout.keyguard_clock_miui,         // 4
            R.layout.keyguard_clock_ide,          // 5
            R.layout.keyguard_clock_moto,         // 6
            R.layout.keyguard_clock_stylish,      // 7
            R.layout.keyguard_clock_stylish2,     // 8
            R.layout.keyguard_clock_stylish3,     // 9
            R.layout.keyguard_clock_stylish4,     // 10
            R.layout.keyguard_clock_stylish5,     // 11
            R.layout.keyguard_clock_stylish6,     // 12
            R.layout.keyguard_clock_stylish7,     // 13
            R.layout.keyguard_clock_stylish8,     // 14
            R.layout.keyguard_clock_stylish9,     // 15
            R.layout.keyguard_clock_stylish10,    // 16
            R.layout.keyguard_clock_small_cute,   // 17
            R.layout.keyguard_clock_thin_long,    // 18
            R.layout.keyguard_clock_more_more_thin, // 19
            R.layout.keyguard_clock_normal_time,  // 20
            R.layout.keyguard_clock_guoguo_type2, // 21
            R.layout.keyguard_clock_guoguo_type3, // 22
            R.layout.keyguard_clock_guoguo_type4, // 23
            R.layout.keyguard_clock_ntype,        // 24
            R.layout.keyguard_clock_ndot,         // 25
            R.layout.keyguard_clock_graphic,      // 26
            R.layout.keyguard_clock_london_ug     // 27
        )
        
        private val CLOCK_NAMES = arrayOf(
            "Default", "OnePlus", "IOS", "Simple", "MIUI", "IDE", "Moto",
            "Stylish", "Stylish 2", "Stylish 3", "Stylish 4", "Stylish 5",
            "Stylish 6", "Stylish 7", "Stylish 8", "Stylish 9", "Stylish 10",
            "SmallCute", "ThinLong", "MoreMoreThin", "NormalTime",
            "Guoguo2", "Guoguo3", "Guoguo4", "NType", "NDot", "Graphic", "LondonUG"
        )
        
        private val UNSUPPORTED_CLOCKS = intArrayOf(17, 18, 19, 20, 21, 22, 23)
        private val UNSUPPORTED_CLOCK_FONT = intArrayOf(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)

        fun load(context: Context): ClockConfig {
            val style = Settings.Secure.getInt(
                context.contentResolver,
                "clock_style",
                0
            )
            return ClockConfig(style = style)
        }
        
        @Composable
        fun getClockName(style: Int): String {
            return if (style in CLOCK_NAMES.indices) CLOCK_NAMES[style] else stringResource(R.string.unknown)
        }

        fun getLayoutId(style: Int): Int {
            return if (style in CLOCK_LAYOUTS.indices) CLOCK_LAYOUTS[style] else CLOCK_LAYOUTS[0]
        }

        fun getTotalStyles(): Int = CLOCK_LAYOUTS.size

        fun supportsCustomization(style: Int): Boolean {
            return style !in UNSUPPORTED_CLOCKS
        }

        fun supportsFontCustomization(style: Int): Boolean {
            return style !in UNSUPPORTED_CLOCK_FONT
        }
    }
    
    fun save(context: Context) {
        Settings.Secure.putInt(
            context.contentResolver,
            "clock_style",
            style
        )
        Settings.Secure.putInt(
            context.contentResolver,
            "lock_screen_custom_clock_face",
            0
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClockConfigContent(
    config: ClockConfig,
    currentPagerPage: Int,
    onUpdate: (ClockConfig) -> Unit,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fontManager = remember { FontManager(context as android.app.Activity, true) }
    
    var selectedFontIndex by remember { mutableStateOf(-1) }
    var showFontPicker by remember { mutableStateOf(false) }
    var isApplying by remember { mutableStateOf(false) }
    
    val selectedStyle = currentPagerPage
    val supportsCustomization = ClockConfig.supportsCustomization(selectedStyle)
    val supportsFontCustomization = ClockConfig.supportsFontCustomization(selectedStyle)
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.clock_style),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = stringResource(R.string.swipe_to_browse_styles),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDarkTheme) 
                Color.White.copy(alpha = 0.7f) 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = if (isDarkTheme)
                Color.White.copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isDarkTheme)
                        Color.White.copy(alpha = 0.15f)
                    else
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if (isDarkTheme)
                                Color.White
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.current_style),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isDarkTheme)
                            Color.White.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getClockNameResource(context, selectedStyle),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme)
                            Color.White
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${selectedStyle + 1}/${ClockConfig.getTotalStyles()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkTheme)
                        Color.White.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        
        if (!supportsCustomization) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = if (isDarkTheme)
                    Color(0xFFFFA500).copy(alpha = 0.15f)
                else
                    Color(0xFFFFA500).copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFFFA500)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.limited_customization),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.limited_customization_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkTheme)
                                Color.White.copy(alpha = 0.7f)
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        if (supportsFontCustomization) {
            Surface(
                onClick = { showFontPicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = if (isDarkTheme)
                    Color.White.copy(alpha = 0.05f)
                else
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isDarkTheme)
                            Color.White.copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.FontDownload,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (isDarkTheme)
                                    Color.White.copy(alpha = 0.8f)
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.clock_font),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (selectedFontIndex >= 0) {
                                fontManager.getLabel(
                                    context,
                                    fontManager.allFontPackages[selectedFontIndex]
                                )
                            } else stringResource(R.string.system_default),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkTheme)
                                Color.White.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = stringResource(R.string.select_font),
                        tint = if (isDarkTheme)
                            Color.White.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        Button(
            onClick = {
                if (!isApplying) {
                    isApplying = true
                    scope.launch {
                        applyClockChangesAndRestart(
                            context = context,
                            clockPosition = selectedStyle,
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
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDarkTheme)
                    Color.White
                else
                    MaterialTheme.colorScheme.primary,
                contentColor = if (isDarkTheme)
                    Color.Black
                else
                    MaterialTheme.colorScheme.onPrimary
            ),
            enabled = !isApplying
        ) {
            if (isApplying) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = if (isDarkTheme) Color.Black else MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isDarkTheme) Color.Black else MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isApplying) stringResource(R.string.applying) else stringResource(R.string.apply_clock_style),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isDarkTheme) Color.Black else MaterialTheme.colorScheme.onPrimary
            )
        }
    }
    
    if (showFontPicker) {
        AlertDialog(
            onDismissRequest = { showFontPicker = false },
            title = { 
                Text(
                    stringResource(R.string.select_font),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
                ) 
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    fontManager.allFontPackages.forEachIndexed { index, fontPackage ->
                        Surface(
                            onClick = {
                                selectedFontIndex = index
                                showFontPicker = false
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = when {
                                selectedFontIndex == index && isDarkTheme -> Color.White.copy(alpha = 0.15f)
                                selectedFontIndex == index -> MaterialTheme.colorScheme.primaryContainer
                                isDarkTheme -> Color.White.copy(alpha = 0.05f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = fontManager.getLabel(context, fontPackage),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f),
                                    color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                if (selectedFontIndex == index) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = stringResource(R.string.active),
                                        tint = if (isDarkTheme)
                                            Color.White
                                        else
                                            MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showFontPicker = false },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        stringResource(R.string.close),
                        color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.primary
                    )
                }
            },
            containerColor = if (isDarkTheme) Color(0xFF121212) else MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp)
        )
    }
}

private suspend fun applyClockChangesAndRestart(
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
