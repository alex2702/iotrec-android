package de.ikas.iotrec.preferences.ui

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar
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
import de.ikas.iotrec.database.model.Preference
import de.ikas.iotrec.preferences.adapter.PreferenceDialogRecyclerViewAdapter

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
            category = it.getParcelable(Companion.CATEGORY)
        }

        Log.d(TAG, category.toString())

        mainActivity = activity as MainActivity
        app = mainActivity.application as IotRecApplication
        loginRepository = app.loginRepository

        preferenceViewModel = ViewModelProviders.of(this).get(PreferenceViewModel::class.java)
        preferenceViewModel.updateSubCategoriesInCategory(category!!.textId)
        preferenceViewModel.updatePreferencesInCategory(category!!.textId)
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
        val loadingCircle = view.findViewById(R.id.loading) as ProgressBar
        val closeButton = view.findViewById(R.id.dialog_preference_choice_close) as Button

        // Set the adapter
        if (recyclerView is RecyclerView) {
            with(recyclerView) {
                layoutManager = GridLayoutManager(context, 1) as RecyclerView.LayoutManager?

                adapter = PreferenceDialogRecyclerViewAdapter(context, listener)

                //Log.d(TAG, "setting selectedSubCategories")
                //(adapter as PreferenceDialogRecyclerViewAdapter).selectedSubCategories = loginRepository.user!!.preferences

                preferenceViewModel.subCategories.observe(viewLifecycleOwner, Observer { subCategories ->
                    // Update the cached copy of the categories in the adapter.
                    Log.d(TAG, "subCategories in observer: $subCategories")
                    subCategories?.let { (adapter as PreferenceDialogRecyclerViewAdapter).setSubCategories(it) }
                    loadingCircle.visibility = View.GONE
                    closeButton.visibility = View.VISIBLE
                })

                preferenceViewModel.preferences.observe(viewLifecycleOwner, Observer { preferences ->
                    // Update the cached copy of the categories in the adapter.
                    Log.d(TAG, "preferences in observer: $preferences")
                    preferences?.let { (adapter as PreferenceDialogRecyclerViewAdapter).setPreferences(it) }
                })


            }
        }

        // make dialog non-cancelable and set close button
        this.isCancelable = false

        closeButton.setOnClickListener {
            this.dismiss()
        }

        return view
    }

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

        dialog?.setOnKeyListener(
            DialogInterface.OnKeyListener { dialog, keyCode, _ ->
                if(keyCode == android.view.KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss()
                    return@OnKeyListener true
                } else {
                    return@OnKeyListener false
                }
            }
        )

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
        fun onPreferenceSelectDialogFragmentInteraction(preference: Preference)
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

        private const val CATEGORY = "category"
    }
}
