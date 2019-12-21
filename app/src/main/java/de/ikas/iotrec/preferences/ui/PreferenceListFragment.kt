package de.ikas.iotrec.preferences.ui

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.ikas.iotrec.R
import de.ikas.iotrec.account.data.LoginRepository
import de.ikas.iotrec.app.IotRecApplication
import de.ikas.iotrec.app.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import de.ikas.iotrec.database.model.Category
import de.ikas.iotrec.database.repository.CategoryRepository
import de.ikas.iotrec.preferences.adapter.PreferenceRecyclerViewAdapter

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [PreferenceListFragment.OnListFragmentInteractionListener] interface.
 */
class PreferenceListFragment : Fragment() {
    private var listener: OnListFragmentInteractionListener? = null
    private val TAG = "PreferenceListFragment"

    private lateinit var mainActivity: MainActivity
    lateinit var app: IotRecApplication

    private lateinit var categoryRepository: CategoryRepository
    private lateinit var categoryViewModel: PreferenceViewModel

    lateinit var loginRepository: LoginRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivity = activity as MainActivity
        app = mainActivity.application as IotRecApplication

        loginRepository = app.loginRepository

        categoryViewModel = ViewModelProviders.of(this).get(PreferenceViewModel::class.java)
        categoryRepository = app.categoryRepository

        if(loginRepository.isLoggedIn()) {
            // get categories and insert them into database
            GlobalScope.launch {
                try {
                    val result = app.iotRecApi.getCategories()

                    // if successful, update database object
                    if (result.isSuccessful) {
                        val resultCategories = result.body()

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

    /**
     *
     * @param inflater LayoutInflater
     * @param container ViewGroup?
     * @param savedInstanceState Bundle?
     * @return View?
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // get view and UI elements from layout files
        val view = inflater.inflate(R.layout.fragment_preference_list, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        val notLoggedInText = view.findViewById<TextView>(R.id.categories_not_logged_in_text)
        val notLoggedInButton = view.findViewById<Button>(R.id.categories_not_logged_in_button)
        val loadingCircle = view.findViewById(R.id.loading) as ProgressBar

        // show preference-related UI only when logged in
        if(loginRepository.isLoggedIn()) {
            recyclerView.visibility = View.VISIBLE
            notLoggedInText.visibility = View.GONE
            notLoggedInButton.visibility = View.GONE

            // Set the adapter
            if (recyclerView is RecyclerView) {
                with(recyclerView) {
                    layoutManager = LinearLayoutManager(context)

                    adapter = PreferenceRecyclerViewAdapter(context, listener)

                    if (loginRepository.isLoggedIn()) {
                        (adapter as PreferenceRecyclerViewAdapter).preferences =
                            loginRepository.user!!.preferences
                    }

                    categoryViewModel.topLevelCategories.observe(
                        viewLifecycleOwner,
                        Observer { categories ->
                            // Update the cached copy of the categories in the adapter.
                            categories?.let {
                                (adapter as PreferenceRecyclerViewAdapter).setTopLevelCategories(
                                    it
                                )
                                loadingCircle.visibility = View.GONE
                            }
                        }
                    )

                    // add a divider line between list items
                    val dividerItemDecoration = DividerItemDecoration(
                        recyclerView.context,
                        (layoutManager as LinearLayoutManager).orientation
                    )
                    recyclerView.addItemDecoration(dividerItemDecoration)
                }
            }

            // if the user hasn't selected any preferences yet, show an alert explaining the process
            if(loginRepository.user!!.preferences.size == 0) {
                val builder = AlertDialog.Builder((activity as Activity))
                builder
                    .setMessage("You haven't set any preferences yet.\n\nHere's how it works:\n" +
                            "The categories are structured in a hierarchy of two levels. " +
                            "Categories that you can select as your personal preferences are " +
                            "located on the second level. Click a top-level category to view " +
                            "the second-level options in that category.\n\nFor each second-level " +
                            "option, you can select whether you like it (thumbs up button) or " +
                            "you dislike it (thumbs down button). Neutral choices (i.e. the " +
                            "middle button) will not have an effect on the recommendations " +
                            "your receive.")
                    .setCancelable(false)
                    .setPositiveButton("Got it!") { _, _ -> }

                val alert = builder.create()
                alert.setTitle("Setting Preferences")
                alert.show()
            }

            return view
        } else {
            // if the user not logged in, show an explanation and a button
            loadingCircle.visibility = View.GONE
            recyclerView.visibility = View.GONE
            notLoggedInText.visibility = View.VISIBLE
            notLoggedInButton.visibility = View.VISIBLE

            // but lets the user move to the profile fragment
            notLoggedInButton.setOnClickListener {
                // move to profile fragment
                (activity as MainActivity).navView.selectedItemId = R.id.navigation_profile
            }

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
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
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
        @JvmStatic
        fun newInstance(columnCount: Int) =
            PreferenceListFragment().apply {}
    }
}
