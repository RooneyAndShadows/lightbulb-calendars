package com.github.rooneyandshadows.lightbulb.calendarsdemo.activity

import android.annotation.SuppressLint
import android.view.View
import com.github.rooneyandshadows.lightbulb.application.activity.BaseActivity
import com.github.rooneyandshadows.lightbulb.application.activity.slidermenu.SliderMenu
import com.github.rooneyandshadows.lightbulb.application.activity.slidermenu.config.SliderMenuConfiguration
import com.github.rooneyandshadows.lightbulb.application.activity.slidermenu.items.PrimaryMenuItem
import com.github.rooneyandshadows.lightbulb.calendarsdemo.R
import com.github.rooneyandshadows.lightbulb.commons.utils.ResourceUtils

object MenuConfigurations {
    @SuppressLint("InflateParams")
    fun getConfiguration(activity: BaseActivity): SliderMenuConfiguration {
        val headingView: View = activity.layoutInflater.inflate(R.layout.demo_drawer_header_view, null)
        val configuration = SliderMenuConfiguration()
        configuration.withHeaderView(headingView)
        configuration.addMenuItem(
            PrimaryMenuItem(
                -1,
                ResourceUtils.getPhrase(activity, R.string.demo_calendar_month),
                null,
                null,
                1
            ) { slider: SliderMenu ->
                //slider.closeSlider()
                //MainActivityNavigator.route().toDemoAlert().replace()
            }
        )
        return configuration
    }
}