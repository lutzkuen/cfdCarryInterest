package com.example.interestcalculator

import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.interestcalculator.content.RatesContent
import com.example.interestcalculator.ui.main.SectionsPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : ratesFragment.OnListFragmentInteractionListener,
    PortfolioFragment.OnListFragmentInteractionListener, AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.addTab(tabs.newTab().setText(R.string.rates_fragment))
        tabs.addTab(tabs.newTab().setText(R.string.portfolio_fragment))
        tabs.addTab(tabs.newTab().setText(R.string.title_activity_settings))
        tabs.setupWithViewPager(viewPager)
        fab.setOnClickListener { view ->
            run {
                val preferences = PreferenceManager.getDefaultSharedPreferences(view.context)
                RatesContent.refresh(preferences)
            }
        }
    }
    override fun onListFragmentInteraction(item: RatesListItem?) {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onListFragmentInteraction(item: PortfolioListItem?) {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}