package com.github.rooneyandshadows.lightbulb.calendarsdemo.fragment

import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentConfiguration
import com.github.rooneyandshadows.lightbulb.annotation_processors.annotations.FragmentScreen
import com.github.rooneyandshadows.lightbulb.application.activity.slidermenu.drawable.ShowMenuDrawable
import com.github.rooneyandshadows.lightbulb.application.fragment.base.BaseFragment
import com.github.rooneyandshadows.lightbulb.application.fragment.cofiguration.ActionBarConfiguration
import com.github.rooneyandshadows.lightbulb.calendarsdemo.R
import com.github.rooneyandshadows.lightbulb.commons.utils.ResourceUtils

@FragmentScreen(screenName = "MonthCalendar", screenGroup = "Demo")
@FragmentConfiguration(layoutName = "fragment_demo_calendar", hasLeftDrawer = false)
class FragmentCalendarMonth : BaseFragment() {

    @Override
    override fun configureActionBar(): ActionBarConfiguration {
        val title = ResourceUtils.getPhrase(requireContext(), R.string.app_name)
        val subTitle = ResourceUtils.getPhrase(requireContext(), R.string.demo_calendar_month)
        return ActionBarConfiguration(R.id.toolbar)
            .withActionButtons(false)
            .attachToDrawer(false)
            .withTitle(title)
            .withSubTitle(subTitle)
    }

    private fun setupDrawerButton() {
        val actionBarDrawable = ShowMenuDrawable(requireContext())
        actionBarDrawable.setEnabled(false)
        actionBarDrawable.backgroundColor = ResourceUtils.getColorByAttribute(requireContext(), R.attr.colorError)
        actionBarManager.setHomeIcon(actionBarDrawable)
    }
}