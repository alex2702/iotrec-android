package de.ikas.iotrec.preferences.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import de.ikas.iotrec.R
import de.ikas.iotrec.account.data.LoginRepository
import de.ikas.iotrec.app.IotRecApplication
import de.ikas.iotrec.app.MainActivity
import de.ikas.iotrec.database.model.Category
import de.ikas.iotrec.preferences.adapter.PreferenceDialogViewAdapter

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val CATEGORY = "category"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [PreferenceSelectDialogFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [PreferenceSelectDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class PreferenceSelectDialogFragment : DialogFragment() {

    private lateinit var mainActivity: MainActivity
    lateinit var app: IotRecApplication
    private lateinit var loginRepository: LoginRepository

    private var category: Category? = null
    private var listener: OnFragmentInteractionListener? = null
    private val TAG = "PrefSelectDlgFragment"

    private lateinit var preferenceViewModel: PreferenceViewModel
    //private lateinit var currentPreferences: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            category = it.getParcelable(CATEGORY)
        }

        Log.d(TAG, category.toString())

        mainActivity = activity as MainActivity
        app = mainActivity.application as IotRecApplication
        loginRepository = app.loginRepository

        preferenceViewModel = ViewModelProviders.of(this).get(PreferenceViewModel::class.java)
        preferenceViewModel.updateSubCategories(category!!.textId)
        preferenceViewModel.updateSelectedSubCategories(category!!.textId)

        // get categories already saved in the user profile
        //currentPreferences = loginRepository.user!!.preferences
        //Log.d(TAG, currentPreferences.toString())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_preference_select_dialog, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.dialog_preference_choice_list)

        // Set the adapter
        if (recyclerView is RecyclerView) {
            with(recyclerView) {
                layoutManager = GridLayoutManager(context, 3) as RecyclerView.LayoutManager?

                adapter = PreferenceDialogViewAdapter(context, listener)

                //Log.d(TAG, "setting selectedSubCategories")
                //(adapter as PreferenceDialogViewAdapter).selectedSubCategories = loginRepository.user!!.preferences

                preferenceViewModel.subCategories.observe(viewLifecycleOwner, Observer { subCategories ->
                    // Update the cached copy of the categories in the adapter.
                    Log.d(TAG, "sC in observer: $subCategories")
                    subCategories?.let { (adapter as PreferenceDialogViewAdapter).setSubCategories(it) }
                })

                preferenceViewModel.selectedSubCategories.observe(viewLifecycleOwner, Observer { selectedSubCategories ->
                    // Update the cached copy of the categories in the adapter.
                    Log.d(TAG, "sSC in observer: $selectedSubCategories")
                    selectedSubCategories?.let { (adapter as PreferenceDialogViewAdapter).setSelectedSubCategories(it) }
                })


            }
        }

        // make dialog non-cancelable and set close button
        this.isCancelable = false
        val closeButton = view.findViewById(R.id.dialog_preference_choice_close) as Button
        closeButton.setOnClickListener {

            // drop all changes
            //mainActivity.userPreferencesToBeRemoved.clear()
            //mainActivity.userPreferencesToBeAdded.clear()

            this.dismiss()
        }

        /*
        val saveButton = view.findViewById(R.id.dialog_preference_choice_save) as Button
        saveButton.setOnClickListener {
            // create the final selected-categories list
            val preferencesToBeSaved = currentPreferences.minus(mainActivity.userPreferencesToBeRemoved).plus(mainActivity.userPreferencesToBeAdded)

            // save them to LoginRepository
            loginRepository.updatePreferences(preferencesToBeSaved)

            // hide the dialog
            this.dismiss()
        }
        */

        return view
    }

    /*
    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(category: Category) {
        listener?.onPreferenceSelectDialogFragmentInteraction(category)
    }
    */

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "onAttach")

        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onResume() {
        super.onResume()

        Log.d(TAG, "onResume")

        // set dialog width
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        fun onPreferenceSelectDialogFragmentInteraction(category: Category)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param category The selected Category.
         * @return A new instance of fragment PreferenceSelectDialogFragment.
         */
        @JvmStatic
        fun newInstance(category: Category) =
            PreferenceSelectDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(CATEGORY, category)
                }
            }
    }
}
