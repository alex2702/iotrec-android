package de.ikas.iotrec.account.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.ikas.iotrec.R
import de.ikas.iotrec.app.IotRecApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SignupFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null
    private val TAG = "SignupFragment"
    private lateinit var signupViewModel: LoginViewModel

    private lateinit var loginActivity: LoginActivity
    private lateinit var app: IotRecApplication
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get an instance of the parent LoginActivity
        loginActivity = activity as LoginActivity
        app = loginActivity.application as IotRecApplication
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // get overall view and then fields and buttons
        val view = inflater.inflate(R.layout.fragment_signup, container, false)
        val usernameField = view.findViewById<EditText>(R.id.username)
        val emailField = view.findViewById<EditText>(R.id.email_address)
        val passwordField = view.findViewById<EditText>(R.id.password)
        val passwordConfirmField = view.findViewById<EditText>(R.id.password_confirm)
        val signupButton = view.findViewById<Button>(R.id.signup)
        val goToLoginButton = view.findViewById<Button>(R.id.go_to_login)
        val loadingSpinner = view.findViewById<ProgressBar>(R.id.loading)

        signupViewModel = ViewModelProviders.of(this, LoginViewModelFactory(activity!!))
            .get(LoginViewModel::class.java)

        signupViewModel.signupFormState.observe(this@SignupFragment, Observer {
            val signupState = it ?: return@Observer

            // disable signup button unless all mandatory fields are filled out
            signupButton.isEnabled = signupState.isDataValid

            if (signupState.usernameError != null) {
                usernameField.error = getString(signupState.usernameError)
            }
            if (signupState.passwordError != null) {
                passwordField.error = getString(signupState.passwordError)
            }
            if (signupState.passwordConfirmError != null) {
                passwordConfirmField.error = getString(signupState.passwordConfirmError)
            }
        })

        signupViewModel.signupResult.observe(this@SignupFragment, Observer {
            val signupResult = it ?: return@Observer

            loadingSpinner.visibility = View.GONE
            if (signupResult.error != null) {
                showSignupFailed(signupResult.error)
            }
            if (signupResult.success != null) {
                // if signup was successful, get categories and questions from backend and insert them into the database
                scope.launch {
                    //sync categories
                    val result = app.iotRecApi.getCategories()

                    // if successful, update database object
                    if (result.isSuccessful) {
                        val resultCategories = result.body()

                        // insert categories into database
                        app.categoryRepository.insertMultiple(*resultCategories!!.toTypedArray())
                    }

                    val resultQ = app.iotRecApi.getQuestions()

                    // if successful, update database
                    if (resultQ.isSuccessful) {
                        val resultQuestions = resultQ.body()
                        app.questionRepository.insertMultiple(*resultQuestions!!.toTypedArray())
                    }
                }

                // show success message and profile tab
                updateUiWithUser(signupResult.success)

                // store user information in shared preferences
                val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity)
                val editor = sharedPrefs.edit()
                editor.putString("userToken", signupResult.success.token)
                editor.apply()

                // set LoginActivity's result, so profilefragment (that called the activity) can respond appropriately
                val intent = Intent()
                intent.putExtra("ACTION", "signup")
                activity!!.setResult(Activity.RESULT_OK, intent)

                //Complete and destroy login activity once successful
                activity!!.finish()
            }
        })

        emailField.afterTextChanged {
            signupViewModel.signupDataChanged(
                usernameField.text.toString(),
                emailField.text.toString(),
                passwordField.text.toString(),
                passwordConfirmField.text.toString()
            )
        }

        usernameField.afterTextChanged {
            signupViewModel.signupDataChanged(
                usernameField.text.toString(),
                emailField.text.toString(),
                passwordField.text.toString(),
                passwordConfirmField.text.toString()
            )
        }

        passwordField.afterTextChanged {
            signupViewModel.signupDataChanged(
                usernameField.text.toString(),
                emailField.text.toString(),
                passwordField.text.toString(),
                passwordConfirmField.text.toString()
            )
        }

        passwordConfirmField.apply {
            afterTextChanged {
                signupViewModel.signupDataChanged(
                    usernameField.text.toString(),
                    emailField.text.toString(),
                    passwordField.text.toString(),
                    passwordConfirmField.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        signupViewModel.register(
                            usernameField.text.toString(),
                            emailField.text.toString(),
                            passwordField.text.toString()
                        )
                }
                false
            }

            signupButton.setOnClickListener {
                loadingSpinner.visibility = View.VISIBLE
                signupViewModel.register(usernameField.text.toString(), emailField.text.toString(), passwordField.text.toString())
            }
        }

        signupButton.setOnClickListener {
            loadingSpinner.visibility = View.VISIBLE
            signupViewModel.register(usernameField.text.toString(), emailField.text.toString(), passwordField.text.toString())
        }

        goToLoginButton.setOnClickListener {
            activity?.setTitle(R.string.title_login)
            activity?.supportFragmentManager?.popBackStack()
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
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

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val username = model.username

        Toast.makeText(
            activity,
            "$welcome $username",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showSignupFailed(@StringRes errorString: Int) {
        Toast.makeText(activity, errorString, Toast.LENGTH_SHORT).show()
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
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SignupFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignupFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
