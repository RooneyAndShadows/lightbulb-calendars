package com.github.rooneyandshadows.lightbulb.calendarsdemo.activity

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.ActivityConfiguration
import com.github.rooneyandshadows.lightbulb.application.activity.BaseActivity
import android.os.Bundle
import com.github.rooneyandshadows.lightbulb.application.activity.slidermenu.config.SliderMenuConfiguration
import com.github.rooneyandshadows.lightbulb.calendarsdemo.R

@ActivityConfiguration
class MainActivity : BaseActivity() {
    @Override
    override fun doBeforeCreate(savedInstanceState: Bundle?) {
        super.doBeforeCreate(savedInstanceState)
        setTheme(R.style.DemoTheme)
    }

    @Override
    override fun getMenuConfiguration(): SliderMenuConfiguration {
        return MenuConfigurations.getConfiguration(this)
    }

    @Override
    override fun doOnCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            MainActivityNavigator.route().toDemoMonthCalendar().newRootScreen()
        }
    }
}