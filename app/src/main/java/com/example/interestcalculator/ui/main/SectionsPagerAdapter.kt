package com.example.interestcalculator.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
// import android.support.v4.app.FragmentPagerAdapter
import com.example.interestcalculator.R
import com.example.interestcalculator.content.SettingsActivity
import com.example.interestcalculator.PortfolioFragment
import com.example.interestcalculator.ratesFragment

private val TAB_TITLES = arrayOf(
    R.string.rates_fragment,
    R.string.portfolio_fragment,
    R.string.button_settings
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        when (position) {
            0 -> {
                return ratesFragment()
            }
            1 -> {
                return PortfolioFragment()
            }
            2 -> {
                return SettingsActivity.SettingsFragment()
            }
            else -> return ratesFragment()
        }
        // return PlaceholderFragment.newInstance(position + 1)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return 3
    }
}