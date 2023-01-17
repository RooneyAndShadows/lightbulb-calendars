package com.github.rooneyandshadows.lightbulb.calendarsdemo.activity

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.ActivityConfiguration
import com.github.rooneyandshadows.lightbulb.application.activity.BaseActivity
import android.os.Bundle
import com.github.rooneyandshadows.lightbulb.calendarsdemo.R

@ActivityConfiguration
class MainActivity : BaseActivity() {
    @Override
    override fun doBeforeCreate(savedInstanceState: Bundle?) {
        super.doBeforeCreate(savedInstanceState)
        setTheme(R.style.DemoTheme)
    }

    @Override
    override fun doOnCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            updateMenuConfiguration(this, MainActivity::class.java) { activity: BaseActivity ->
                MenuConfigurations.getConfiguration(activity)
            }
            MainActivityNavigator.route().toDemoMonthCalendar().newRootScreen()
        }
    }
}