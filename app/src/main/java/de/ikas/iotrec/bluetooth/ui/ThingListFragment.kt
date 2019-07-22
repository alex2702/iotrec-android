package de.ikas.iotrec.bluetooth.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.ikas.iotrec.R
import de.ikas.iotrec.app.MainActivity
import de.ikas.iotrec.database.model.Thing
import java.util.logging.Level
import java.util.logging.Logger

class ThingListFragment : Fragment() {

    private var columnCount = 1
    private var listener: OnListFragmentInteractionListener? = null // double listener?
    private lateinit var thingViewModel: ThingViewModel

    val logger = Logger.getLogger(MainActivity::class.java.name)
    private val TAG = "ThingListFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        thingViewModel = ViewModelProviders.of(this).get(ThingViewModel::class.java)

        logger.log(Level.INFO, "ThingListFragment: in onCreate")

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }

        Log.d(TAG, "column count is $ARG_COLUMN_COUNT")
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
                adapter = ThingRecyclerViewAdapter(context,listener)     //, listener) // double listener?

                thingViewModel.allThingsInRange.observe(viewLifecycleOwner, Observer { things ->
                    // Update the cached copy of the things in the adapter.
                    things?.let { (adapter as ThingRecyclerViewAdapter).setThings(it) }
                })
            }
        }

        return view
    }

    /* https://stackoverflow.com/a/37279212 */

    override fun onAttach(context: Context) {
        super.onAttach(context)
        logger.log(Level.INFO, "ThingListFragment: in onAttach")
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }
    }



    override fun onDetach() {
        super.onDetach()
        listener = null // double listener?
    }


    /*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == NEW_THING_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.let {
                val thing = Thing(it.getStringExtra(NewWordActivity.EXTRA_REPLY))
                thingViewModel.insert(thing)
            }
        } else {
            Toast.makeText(
                context,
                "not saved",
                Toast.LENGTH_LONG).show()
        }
    }
    */

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
    interface OnListFragmentInteractionListener { // double listener?
        fun onListFragmentInteraction(thing: Thing) {
            Log.d("ThingListFragment", "a list item was clicked")
        }
    }


    companion object {

        const val NEW_THING_ACTIVITY_REQUEST_CODE = 1

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
