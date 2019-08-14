package com.example.interestcalculator

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.example.interestcalculator.content.PortfolioContent
import com.example.interestcalculator.content.RatesContent

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [PortfolioFragment.OnListFragmentInteractionListener] interface.
 */

class PortfolioListItem {
    var instrument: String = "N/A"
    var units: Int = 0
    var interest: Float = 0.toFloat()
    var side: String = "N/A"
}

class PortfolioFragment : LifecycleOwner, Fragment() {

    private var columnCount = 1
    private var listener: OnListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_portfolio_list, container, false)
        var fragment = this
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                view.adapter = PortfolioRecyclerViewAdapter(PortfolioContent.ITEMS, listener)
                val readyObserver = Observer<String> { _ ->
                    view.adapter!!.notifyDataSetChanged()
                }
                RatesContent.isready.observe(fragment, readyObserver)
                val preferences = PreferenceManager.getDefaultSharedPreferences(fragment.context)
                PortfolioContent.getPortfolio(preferences)
            }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        PortfolioContent.getPortfolio(preferences)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(item: PortfolioListItem?)
    }

    companion object {

        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            PortfolioFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}
