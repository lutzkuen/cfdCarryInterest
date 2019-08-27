package com.example.interestcalculator.content

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import com.example.interestcalculator.PortfolioListItem
import com.example.interestcalculator.R
import okhttp3.*
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.locks.ReentrantLock

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 */

class PositionSide(
    var pl: String,
    var resettablePL: String,
    var units: String,
    var unrealizedPL: String
)

class Position(
    var instrument: String,
    var long: PositionSide,
    var pl: String,
    var resettablePL: String,
    var short: PositionSide,
    var unrealizedPL: String
)

class PositionResponse(
    var lastTransactionID: String,
    var positions: List<Position>
)

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
        val duration: String? = preferences.getString("duration", "24")
        // val port: String? = preferences!!.getString("port", "443")
        val urlString = "https://$host/v3/accounts/$accountId/openPositions"
        val request = Request.Builder()
            .url(urlString)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ${accessToken}")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val responseString = response.body()!!.string()
                try {
                    positions = Klaxon().parse<PositionResponse>(responseString)
                } catch (e: KlaxonException) {
                    println(responseString)
                    println(e)
                    return
                }
                val orderedList = ArrayList<PortfolioListItem>()
                var totalInterest: Float = 0.toFloat()
                var totalUnits = 0
                for (posi in positions!!.positions) {
                    val newItem = PortfolioListItem()
                    newItem.instrument = posi.instrument.replace("_", "/")
                    newItem.units = posi.long.units.toInt() + posi.short.units.toInt()
                    totalUnits += newItem.units
                    if (newItem.units > 0) {
                        newItem.side = "long"
                    } else {
                        newItem.side = "short"
                    }
                    // Make sure RatesContent is ready to go at this point
                    while (RatesContent.isready.value != "ready") {
                        Thread.sleep(100)
                    }
                    newItem.interest =
                        RatesContent.getInterest(posi.instrument.replace("_", "/"), newItem.units, duration!!.toFloat())
                    totalInterest += newItem.interest
                    orderedList.add(newItem)
                }
                orderedList.sortByDescending { it.interest }
                for (i in 0..(orderedList.size-1)) {
                    val position = orderedList[i]
                    for (i_before in 0..i) {
                        val pos_before = orderedList[i_before]
                        if (RatesContent.ITEM_MAP[pos_before.instrument]!!.interest < RatesContent.ITEM_MAP[position.instrument]!!.interest) {
                            position.recommend = "Increase Position"
                        }
                    }
                    for (i_after in i..(orderedList.size-1)) {
                        val pos_after = orderedList[i_after]
                        if (RatesContent.ITEM_MAP[pos_after.instrument]!!.interest > RatesContent.ITEM_MAP[position.instrument]!!.interest) {
                            position.recommend = "Decrease Position"
                        }
                    }
                    if ( position.interest < 0 ) {
                        position.recommend = "Close Position"
                    }
                    addItem(position)
                }
                val summary = PortfolioListItem()
                summary.instrument = "Total Interest"
                summary.interest = totalInterest
                summary.side = ""
                summary.units = totalUnits
                addItem(summary)
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
}
