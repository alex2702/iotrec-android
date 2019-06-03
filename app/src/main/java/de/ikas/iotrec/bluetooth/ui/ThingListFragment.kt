package de.ikas.iotrec.bluetooth.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.ikas.iotrec.R
import de.ikas.iotrec.app.MainActivity

import de.ikas.iotrec.bluetooth.dummy.DummyContent
import de.ikas.iotrec.bluetooth.dummy.DummyContent.DummyItem
import java.util.logging.Level
import java.util.logging.Logger

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ThingListFragment.OnListFragmentInteractionListener] interface.
 */
class ThingListFragment : Fragment() {

    // TODO: Customize parameters
    private var columnCount = 1

    private var listener: OnListFragmentInteractionListener? = null

    val logger = Logger.getLogger(MainActivity::class.java.name)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        logger.log(Level.INFO, "ThingListFragment: in onCreate")

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_thing_list, container, false)
        logger.log(Level.INFO, "ThingListFragment: in onCreateView")

        // Set the adapter
        if (view is RecyclerView) {
            logger.log(Level.INFO, "ThingListFragment: in if in onCreateView")
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = ThingRecyclerViewAdapter(DummyContent.ITEMS, listener)
            }
        }
        return view
    }

    /* https://stackoverflow.com/a/37279212
    override fun onAttach(context: Context) {
        super.onAttach(context)
        logger.log(Level.INFO, "ThingListFragment: in onAttach")
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }
    }
    */

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
        // TODO: Update argument type and name
        fun onListFragmentInteraction(item: DummyItem?)
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            ThingListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}
