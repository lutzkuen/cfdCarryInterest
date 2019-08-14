package com.example.interestcalculator

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


import com.example.interestcalculator.PortfolioFragment.OnListFragmentInteractionListener

import kotlinx.android.synthetic.main.fragment_portfolio.view.*
import java.lang.Math.abs
import java.lang.String.format

class PortfolioRecyclerViewAdapter(
    private val mValues: MutableList<PortfolioListItem>,
    private val mListener: OnListFragmentInteractionListener?
) : RecyclerView.Adapter<PortfolioRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as PortfolioListItem
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_portfolio, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        inflate_view(holder, item)
        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    private fun inflate_view(holder: ViewHolder, item: PortfolioListItem) {
        holder.mIdView.text = item.instrument.padEnd(10)
        if (item.instrument == "Total Interest" ) {
            holder.mContentView.text = item.interest.toString()
        } else {
            holder.mContentView.text = """
            Side: ${item.side}
            Units: ${abs(item.units).toString()}
            Interest: ${format("%.5f", abs(item.interest).toString())}
        """.trimIndent() }
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
