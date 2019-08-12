package com.example.interestcalculator.content

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import com.example.interestcalculator.PortfolioListItem
import okhttp3.*
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.locks.ReentrantLock

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 */

class PositionSide(var pl: String,
                   var resettablePL: String,
                   var units: String,
                   var unrealizedPL: String)

class Position(var instrument: String,
               var long: PositionSide,
               var pl: String,
               var resettablePL: String,
               var short: PositionSide,
               var unrealizedPL: String)

class PositionResponse(var lastTransactionID: String,
                       var positions: List<Position>)

object PortfolioContent {

    /**
     * An array of sample (dummy) items.
     */
    val ITEMS: MutableList<PortfolioListItem> = ArrayList()
    private var positions: PositionResponse? = null
    var isready: MutableLiveData<String> = MutableLiveData<String>()
    private val ITEM_MAP: MutableMap<String, PortfolioListItem> = HashMap()
    private val client = OkHttpClient()
    private val lock = ReentrantLock()

    init {
        println("Init as not ready")
        isready.value = "not"
    }

    fun getPortfolio(preferences: SharedPreferences) {
        lock.lock()
        try {
            privatePortfolio(preferences)
        } finally {
            lock.unlock()
        }
    }

    private fun privatePortfolio(preferences: SharedPreferences) {
        val accessToken: String? = preferences.getString("access_token", "")
        val accountId: String? = preferences.getString("account_id", "")
        val host: String? = preferences.getString("host", "")
        // val port: String? = preferences!!.getString("port", "443")
        val urlString = "https://$host/v3/accounts/$accountId/openPositions"
        val request = Request.Builder()
            .url(urlString)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ${accessToken}")
            .build()
        println("---------------------------CALL IN QUEUE-----------------")
        println("Account ID: ${accountId}")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response)  {
                val response_string = response.body()!!.string()
                println("---------------------------------CALL RESPONDED-------------------------")
                println(response_string)
                try {
                    positions = Klaxon().parse<PositionResponse>(response_string)
                } catch (e: KlaxonException) {
                    println(e)
                    return
                }
                val ordered_list = ArrayList<PortfolioListItem>()
                var totalInterest: Float = 0.toFloat()
                var totalUnits: Int = 0
                for (posi in positions!!.positions) {
                    var newitem = PortfolioListItem()
                    newitem.instrument = posi.instrument.replace("_", "/")
                    newitem.units = posi.long.units.toInt() + posi.short.units.toInt()
                    totalUnits += newitem.units
                    if ( newitem.units > 0 ) {
                        newitem.side = "long"
                    } else {
                        newitem.side = "short"
                    }
                    // Make sure RatesContent is ready to go at this point
                    while (RatesContent.isready.value != "ready") {
                        Thread.sleep(100)
                    }
                    newitem.interest = RatesContent.getInterest(posi.instrument.replace("_", "/"), newitem.units, 24.toFloat())
                    totalInterest += newitem.interest
                    ordered_list.add(newitem)
                }
                ordered_list.sortByDescending { it.interest }
                var summary = PortfolioListItem()
                summary.instrument = "Total Interest"
                summary.interest = totalInterest
                summary.side = ""
                summary.units = totalUnits
                ordered_list.add(summary)
                for (position in ordered_list) {
                    addItem(position)
                }
                isready.postValue("ready")
            }
        })
    }

    private fun addItem(item: PortfolioListItem) {
        if (ITEMS.contains(item).not()) {
            ITEMS.removeAll { it.instrument == item.instrument }
            ITEMS.add(item)
            ITEM_MAP[item.instrument] = item
        }
    }

    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0..position - 1) {
            builder.append("\nMore details information here.")
        }
        return builder.toString()
    }

    /**
     * A dummy item representing a piece of content.
     */
    data class DummyItem(val id: String, val content: String, val details: String) {
        override fun toString(): String = content
    }
}
