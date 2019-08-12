package com.example.interestcalculator

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.SortedList


import com.example.interestcalculator.ratesFragment.OnListFragmentInteractionListener

import kotlinx.android.synthetic.main.fragment_rates.view.*
import java.lang.String.format

class MyratesRecyclerViewAdapter(
    private var mValues: MutableList<RatesListItem>,
    private val mListener: OnListFragmentInteractionListener?
) : RecyclerView.Adapter<MyratesRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as RatesListItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_rates, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        val digits = 2
        holder.mIdView.text = item.instrument.padEnd(10)
        holder.mContentView.text = """
            Side: ${item.side}         
            Units: ${format("%.${digits}f", item.units)}
            Interest: ${format("%.${digits}f", item.interest)} 
            Price: ${format("%.5f", item.price)}
        """.trimIndent()
        //item.units.toString() + " " + item.interest.toString()

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.item_number
        val mContentView: TextView = mView.content

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
