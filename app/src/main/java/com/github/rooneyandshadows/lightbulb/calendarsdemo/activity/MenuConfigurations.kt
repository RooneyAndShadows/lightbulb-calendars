package com.github.rooneyandshadows.lightbulb.calendarsdemo.activity

import android.annotation.SuppressLint
import com.github.rooneyandshadows.lightbulb.application.activity.BaseActivity
import com.github.rooneyandshadows.lightbulb.application.activity.slidermenu.SliderMenu
import com.github.rooneyandshadows.lightbulb.application.activity.slidermenu.config.SliderMenuConfiguration
import com.github.rooneyandshadows.lightbulb.application.activity.slidermenu.items.PrimaryMenuItem
import com.github.rooneyandshadows.lightbulb.calendarsdemo.R
import com.github.rooneyandshadows.lightbulb.commons.utils.ResourceUtils

object MenuConfigurations {
    @SuppressLint("InflateParams")
    fun getConfiguration(activity: BaseActivity): SliderMenuConfiguration {
        return SliderMenuConfiguration(R.layout.demo_drawer_header_view).apply {
            itemsList.apply {
                add(
                    PrimaryMenuItem(
                        -1,
                        ResourceUtils.getPhrase(activity, R.string.demo_calendar_month),
                        null,
                        null,
                        1
                    ) { _: SliderMenu ->
                        //slider.closeSlider()
                        //MainActivityNavigator.route().toDemoAlert().replace()
                    }
                )
            }
        }
    }
}