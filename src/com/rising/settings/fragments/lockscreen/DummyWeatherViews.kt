/*
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
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import com.android.settings.R

/**
 * Dummy WeatherImageView for Settings preview
 */
class DummyWeatherImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ImageView(context, attrs, defStyle) {
    
    init {
        setImageResource(R.drawable.ic_cloud)
        scaleType = ScaleType.FIT_CENTER
        visibility = View.GONE
    }
    
    fun setWeatherEnabled(enabled: Boolean) {
        visibility = if (enabled) View.VISIBLE else View.GONE
    }
}

/**
 * Dummy WeatherTextView for Settings preview
 * Mimics com.rising.settings.fragments.lockscreen.DummyWeatherTextView behavior
 */
class DummyWeatherTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : TextView(context, attrs, defStyle) {
    
    private var weatherEnabled = false
    private var showLocation = false
    private var showText = true
    
    init {
        updateWeatherText()
    }
    
    fun setWeatherEnabled(enabled: Boolean) {
        weatherEnabled = enabled
        updateWeatherText()
    }
    
    fun setShowLocation(show: Boolean) {
        showLocation = show
        updateWeatherText()
    }
    
    fun setShowText(show: Boolean) {
        showText = show
        updateWeatherText()
    }
    
    private fun updateWeatherText() {
        if (!weatherEnabled) {
            visibility = View.GONE
            text = ""
            return
        }
        
        val parts = mutableListOf<String>()
        
        parts.add(context.getString(R.string.weather_temperature_example))
        
        if (showLocation) {
            parts.add(context.getString(R.string.weather_location_example))
        }
        
        if (showText) {
            parts.add(context.getString(R.string.weather_text_example))
        }
        
        text = parts.joinToString(" · ")
        visibility = View.VISIBLE
    }
}
