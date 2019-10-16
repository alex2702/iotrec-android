package de.ikas.iotrec.preferences.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.ikas.iotrec.R
import de.ikas.iotrec.account.data.LoginRepository
import de.ikas.iotrec.app.IotRecApplication
import de.ikas.iotrec.app.MainActivity

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import de.ikas.iotrec.database.dao.CategoryDao
import de.ikas.iotrec.database.db.IotRecDatabase
import de.ikas.iotrec.database.model.Category
import de.ikas.iotrec.database.repository.CategoryRepository
import de.ikas.iotrec.preferences.adapter.PreferenceRecyclerViewAdapter


/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [PreferenceListFragment.OnListFragmentInteractionListener] interface.
 */
class PreferenceListFragment : Fragment() {

    private var columnCount = 1
    //private lateinit var iotRecApi: IotRecApiInit
    private var listener: OnListFragmentInteractionListener? = null
    private val TAG = "PreferenceListFragment"

    private lateinit var mainActivity: MainActivity

    lateinit var app: IotRecApplication

    private lateinit var categoriesDao: CategoryDao
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var categoryViewModel: PreferenceViewModel

    lateinit var loginRepository: LoginRepository

    //private var mainActivity = activity as MainActivity
    //private var categoryViewModel = ViewModelProviders.of(this).get(PreferenceViewModel::class.java)
    //private var categoriesDao = IotRecDatabase.getDatabase(mainActivity, GlobalScope).categoryDao()
    //private var categoryRepository = CategoryRepository(categoriesDao)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //retainInstance = true

        Log.d(TAG, "onCreate")

        mainActivity = activity as MainActivity

        app = mainActivity.application as IotRecApplication

        loginRepository = app.loginRepository

        categoryViewModel = ViewModelProviders.of(this).get(PreferenceViewModel::class.java)
        //categoriesDao = IotRecDatabase.getDatabase(mainActivity, GlobalScope).categoryDao()
        //categoryRepository = CategoryRepository(categoriesDao)
        categoryRepository = app.categoryRepository

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }

        if(loginRepository.isLoggedIn()) {
            // get categories and insert them into database
            GlobalScope.launch {
                try {
                    val result = app.iotRecApi.getCategories()

                    // if successful, update database object
                    if (result.isSuccessful) {
                        val resultCategories = result.body()
                        Log.d(TAG, resultCategories.toString())

                        // insert categories into database
                        categoryRepository.insertMultiple(*resultCategories!!.toTypedArray())
                    } else {
                        mainActivity.runOnUiThread {
                            Toast.makeText(
                                mainActivity,
                                "Could not get categories: ${result.message()}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch(e: Throwable) {
                    Log.e(TAG, e.toString())
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d(TAG, "onCreateView")

        val view = inflater.inflate(R.layout.fragment_preference_list, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        val notLoggedInText = view.findViewById<TextView>(R.id.categories_not_logged_in_text)
        val notLoggedInButton = view.findViewById<Button>(R.id.categories_not_logged_in_button)

        if(loginRepository.isLoggedIn()) {
            recyclerView.visibility = View.VISIBLE
            notLoggedInText.visibility = View.GONE
            notLoggedInButton.visibility = View.GONE

            // Set the adapter
            if (recyclerView is RecyclerView) {
                with(recyclerView) {
                    layoutManager = when {
                        columnCount <= 1 -> LinearLayoutManager(context)
                        else -> GridLayoutManager(context, columnCount)
                    }

                    adapter = PreferenceRecyclerViewAdapter(context, listener)

                    // TODO replace with live data
                    if (loginRepository.isLoggedIn()) {
                        (adapter as PreferenceRecyclerViewAdapter).preferences =
                            loginRepository.user!!.preferences   // TODO NPE on new setup // TODO is this needed?
                    }

                    categoryViewModel.topLevelCategories.observe(
                        viewLifecycleOwner,
                        Observer { categories ->
                            // Update the cached copy of the categories in the adapter.
                            categories?.let {
                                (adapter as PreferenceRecyclerViewAdapter).setTopLevelCategories(
                                    it
                                )
                            }
                        }
                    )
                }
            }

            return view
        } else {
            recyclerView.visibility = View.GONE
            notLoggedInText.visibility = View.VISIBLE
            notLoggedInButton.visibility = View.VISIBLE

            return view
        }
    }

    // https://stackoverflow.com/a/37279212
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener") as Throwable
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Log.d(TAG, "onHiddenChanged")
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
        fun onPreferenceListFragmentInteraction(category: Category)
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            PreferenceListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}
