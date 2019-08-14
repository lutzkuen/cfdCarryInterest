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
import com.example.interestcalculator.content.RatesContent


/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ratesFragment.OnListFragmentInteractionListener] interface.
 */
class RatesListItem {
    var instrument: String = "N/A"
    var units: Float = 0.toFloat()
    var interest: Float = 0.toFloat()
    var side: String = "N/A"
    var price: Float = 0.toFloat()
}

class ratesFragment : LifecycleOwner, Fragment() {

    private var columnCount = 1
    // var swipeRefreshLayout: SwipeRefreshLayout? = null

    private var listener: OnListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
        // swipeRefreshLayout = findViewById(R.id.simpleSwipeRefreshLayout);
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.fragment_rates_list, container, false)
        // Set the adapter
        val fragment = this
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                view.adapter = RatesRecyclerViewAdapter(RatesContent.ITEMS, listener)
                val readyObserver = Observer<String> {_ ->
                    view.adapter!!.notifyDataSetChanged()
                }
                RatesContent.isready.observe(fragment, readyObserver)
                RatesContent.refresh()
            }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        RatesContent.refresh()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        }  else {
             throw RuntimeException("${context.toString()} must implement OnListFragmentInteractionListener")
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
        fun onListFragmentInteraction(item: RatesListItem?)
    }

    companion object {

        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            ratesFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}
