package com.example.interestcalculator.content

import android.content.SharedPreferences
import com.example.interestcalculator.RatesListItem
import java.util.ArrayList
import java.util.HashMap
import androidx.lifecycle.MutableLiveData
import okhttp3.*
import java.io.IOException
import java.lang.Math.abs
import java.util.concurrent.locks.ReentrantLock

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 */
object RatesContent {

    private val arrPairs = ArrayList<String>()
    private val arrRates = ArrayList<Float>()
    private val arrInterestCode = ArrayList<String>()
    private val arrInterestBorrow = ArrayList<Float>()
    private val arrInterestLend = ArrayList<Float>()
    var arrAllowedIns: List<String>? = null
    val ITEMS: MutableList<RatesListItem> = ArrayList<RatesListItem>()
    val ITEM_MAP: MutableMap<String, RatesListItem> = HashMap()
    var isready: MutableLiveData<String> = MutableLiveData<String>()
    val durationNormalization = 24.0 * 365.25
    private val lock = ReentrantLock()

    init {
        initArrays()
    }

    fun refresh(preferences: SharedPreferences) {
        lock.lock()
        try {
            initArrays()
            getArrays(preferences)
        } finally {
            lock.unlock()
        }
    }

    private fun initArrays() {
        isready.postValue("not")
        arrPairs.clear()
        arrRates.clear()
        arrInterestCode.clear()
        arrInterestBorrow.clear()
        arrInterestLend.clear()
    }

    private fun parseLine(_line: String): List<String> {
        var line = _line.replace(");", "")
        line = line.replace("\"", "")
        val lineSplit = line.split("(")
        val instrumentArray = lineSplit[1].split(",")
        return instrumentArray
    }

    private fun getArrays(preferences: SharedPreferences) {
        // Use next two lines for debugging in sequential mode
        // val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        // StrictMode.setThreadPolicy(policy)
        val duration = preferences.getString("duration", "24")
        val urlString = "https://www1.oanda.com/tools/fxcalculators/fxmath.js"

        val request = Request.Builder()
            .url(urlString)
            .build()

        val callback = object : Callback {
            override fun onResponse(call: Call?, response: Response) {
                arrAllowedIns = preferences.getString("allowed_ins", "")!!.split(";")
                val responseData = response.body()?.string()
                parseFxmath(responseData!!)
                getRates(duration!!.toFloat())
                isready.postValue("ready")
            }

            override fun onFailure(call: Call?, e: IOException?) {
                println("Request Failure.")
            }
        }
        val client = OkHttpClient()
        val call = client.newCall(request)
        call.enqueue(callback)
    }

    fun parseFxmath(fxmath_text: String) {
        val parts = fxmath_text.split('\n')
        for (part in parts) {
            if (part.indexOf("arrPairs") > 0) {
                val insArr = parseLine(part)
                for (ins in insArr) {
                    try {
                        arrPairs.add(ins)
                    } catch (e: ArrayIndexOutOfBoundsException) {
                        println("Failed to load instrument ${ins}")
                    }
                }
            }
            if (part.indexOf("arrRates") > 0) {
                val insArr = parseLine(part)
                for (ins in insArr) {
                    try {
                        arrRates.add(ins.toFloat())
                    } finally {

                    }
                }
            }
            if (part.indexOf("arrInterestCode") > 0) {
                val insArr = parseLine(part)
                for (ins in insArr) {
                    arrInterestCode.add(ins)
                }
            }
            if (part.indexOf("arrInterestBorrow") > 0) {
                val insArr = parseLine(part)
                for (ins in insArr) {
                    arrInterestBorrow.add(ins.toFloat())
                }
            }
            if (part.indexOf("arrInterestLend") > 0) {
                val insArr = parseLine(part)
                for (ins in insArr) {
                    arrInterestLend.add(ins.toFloat())
                }
            }
        }
    }

    private fun getPrice(ins: String): Float {
        val idx: Int = arrPairs.indexOf(ins)
        return arrRates[idx]
    }

    private fun getConversion(leading_currency: String): Float {
        val accountCurrency = "EUR"
        if (leading_currency == accountCurrency) {
            return 1.0.toFloat()
        }
        for (i_ins in 0..arrPairs.size) {
            val ins = arrPairs[i_ins]
            if ((ins.indexOf(leading_currency) >= 0) and (ins.indexOf(accountCurrency) >= 0)) {
                try {
                    val price = getPrice(ins)
                    if (ins.split("/")[0] == accountCurrency) {
                        return price
                    } else {
                        return 1.0.toFloat() / price
                    }
                } catch (e: IllegalStateException) {
                    println("Failed to get Price for ${ins}")
                }
            }
        }
        val euroUSDollar = getPrice("EUR/USD")
        for (ins in arrPairs) {
            if ((ins.indexOf(leading_currency) >= 0) and (ins.indexOf("USD") >= 0)) {
                val price = getPrice(ins)
                if (ins.split("_")[0] == "USD") {
                    return price / euroUSDollar
                } else {
                    return 1.0.toFloat() / (price * euroUSDollar)
                }
            }
        }
        return (-1.0).toFloat()
    }

    fun getInterest(instrument: String, units: Int, duration: Float): Float {
        lock.lock()
        var interest = 0.toFloat()
        try {
            interest = getInterestInternal(instrument, units, duration)
        } finally {
            lock.unlock()
            return  interest
        }
    }

    private fun getInterestInternal(instrument: String, units: Int, duration: Float): Float {
        val components = instrument.split("/")
        val base = components[0]
        val quote = components[1]
        val idx = arrPairs.indexOf(instrument)
        val price = arrRates[idx]
        val idxBase = arrInterestCode.indexOf(base)
        val idxQuote = arrInterestCode.indexOf(quote)
        var conversionBase: Float = getConversion(base)
        if (conversionBase <= 0) {
            conversionBase = getConversion(quote)
            conversionBase = price / conversionBase
        }

        var conversionQuote: Float = getConversion(quote)
        if (conversionQuote <= 0) {
            conversionQuote = getConversion(base)
            conversionQuote = price / conversionQuote
        }

        var interestTotal: Float
        if (units > 0) {
            var baseInt =
                abs(units) * (arrInterestBorrow[idxBase] / 100.0) * duration / (conversionBase * durationNormalization)
            var quoteInt =
                abs(units) * price * (arrInterestLend[idxQuote] / 100.0) * duration / (conversionQuote * durationNormalization)
            interestTotal = baseInt.toFloat() - quoteInt.toFloat()
        } else {
            var baseInt =
                abs(units) * (arrInterestLend[idxBase] / 100.0) * duration / (conversionBase * durationNormalization)
            var quoteInt =
                abs(units) * price * (arrInterestBorrow[idxQuote] / 100.0) * duration / (conversionQuote * durationNormalization)
            interestTotal = quoteInt.toFloat() - baseInt.toFloat()
            /* if ( instrument == "WTICO/USD" ) {
                println("$units ${arrInterestLend[idxBase]} $duration $conversionBase $conversionQuote $baseInt $quoteInt $interestTotal")
            } */
        }
        return interestTotal
    }

    private fun getRates(inputDuration: Float) {
        // now all the array should be in place to calculate the rates
        val units: Float = 1000.toFloat()
        val prelimList = ArrayList<RatesListItem>()
        val duration = inputDuration / 8766.toFloat()
        for (ins in arrAllowedIns!!) {
            val components = ins.split("/")
            if ( components.size < 2 ) {
                println("Received weird instrument $ins")
                continue
            }
            val base = components[0]
            val quote = components[1]
            val idx = arrPairs.indexOf(ins)
            if ( idx < 0 ) {
                continue
            }
            if (idx > arrRates.size) {
                continue
            }
            val price = arrRates[idx]
            val idxBase = arrInterestCode.indexOf(base)
            val idxQuote = arrInterestCode.indexOf(quote)
            var conversionBase: Float = getConversion(base)
            if (conversionBase <= 0) {
                conversionBase = getConversion(quote)
                conversionBase = price / conversionBase
            }
            var conversionQuote: Float = getConversion(quote)
            if (conversionQuote <= 0) {
                conversionQuote = conversionBase
                conversionQuote = price / conversionQuote
            }
            // long side
            var baseInt = units * (arrInterestBorrow[idxBase] / 100.0) * duration
            var quoteInt =
                (units * conversionBase) * price * (arrInterestLend[idxQuote] / 100.0) * duration / conversionQuote
            var interestTotal = baseInt - quoteInt
            if (interestTotal > 0) {
                val long_item = RatesListItem()
                long_item.instrument = ins
                long_item.interest = interestTotal.toFloat()
                long_item.side = "long"
                long_item.units = units * conversionBase
                long_item.price = price
                // addItem(long_item)
                prelimList.add(long_item)
            }
            // short side
            baseInt = units * (arrInterestLend[idxBase] / 100.0) * duration
            quoteInt =
                (units * conversionBase) * price * (arrInterestBorrow[idxQuote] / 100.0) * duration / conversionQuote
            interestTotal = quoteInt - baseInt
            if (interestTotal > 0) {
                val short_item = RatesListItem()
                short_item.instrument = ins
                short_item.interest = interestTotal.toFloat()
                short_item.side = "short"
                short_item.units = units * conversionBase
                short_item.price = price
                // addItem(short_item)
                prelimList.add(short_item)
            }
        }
        prelimList.sortByDescending({ it.interest })
        for (it in prelimList) {
            addItem(it)
        }
    }

    private fun addItem(item: RatesListItem) {
        if (ITEMS.contains(item).not()) {
            try {
                ITEMS.removeAll { it.instrument == item.instrument }
                ITEMS.add(item)
                // ITEM_MAP.remove(item.instrument)
                ITEM_MAP[item.instrument] = item
            } catch (e: IndexOutOfBoundsException) {
                println("Error while adding new rates item ${item.instrument}")
            }
        }
    }
}
