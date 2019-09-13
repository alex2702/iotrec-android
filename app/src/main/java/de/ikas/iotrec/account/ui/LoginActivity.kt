package de.ikas.iotrec.account.ui

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import de.ikas.iotrec.R
import android.preference.PreferenceManager
import android.util.Log
import android.view.inputmethod.InputMethodManager
import de.ikas.iotrec.app.IotRecApplication
import de.ikas.iotrec.extensions.hideKeyboard


class LoginActivity : AppCompatActivity(), SignupFragment.OnFragmentInteractionListener {

    //var app = application as IotRecApplication

    private lateinit var loginViewModel: LoginViewModel

    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val usernameField = findViewById<EditText>(R.id.username)
        val passwordField = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login)
        val goToSignupButton = findViewById<Button>(R.id.go_to_signup)
        val loadingSpinner = findViewById<ProgressBar>(R.id.loading)

        loginViewModel = ViewModelProviders.of(this, LoginViewModelFactory(this))
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            loginButton.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                usernameField.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                passwordField.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loadingSpinner.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                // show success message and profile tab
                updateUiWithUser(loginResult.success)

                // store user information in shared preferences
                //val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
                //val editor = sharedPrefs.edit()
                //editor.putString("user.username", loginResult.success.username)
                //editor.putString("user.email", loginResult.success.email)
                //editor.putString("user.token", loginResult.success.token)
                //editor.apply()
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })


        usernameField.afterTextChanged {
            loginViewModel.loginDataChanged(
                usernameField.text.toString(),
                passwordField.text.toString()
            )
        }

        passwordField.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    usernameField.text.toString(),
                    passwordField.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            usernameField.text.toString(),
                            passwordField.text.toString()
                        )
                }
                false
            }

            loginButton.setOnClickListener {
                loadingSpinner.visibility = View.VISIBLE
                loginViewModel.login(usernameField.text.toString(), passwordField.text.toString())
            }
        }

        loginButton.setOnClickListener {
            // hide keyboard
            hideKeyboard(if (currentFocus == null) View(this) else currentFocus)
            // show loading spinner
            loadingSpinner.visibility = View.VISIBLE
            loginViewModel.login(usernameField.text.toString(), passwordField.text.toString())
        }


        goToSignupButton.setOnClickListener {
            setTitle(R.string.title_signup)
            val signupFragment = SignupFragment()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, signupFragment)
                .addToBackStack(null)   //TODO do I want this?
                .commit()
            //TODO transmit data that has already been entered into the login form?
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val username = model.username

        //val preferences = model.preferences
        //Log.d(TAG, preferences.toString())

        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $username",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    override fun onFragmentInteraction(uri: Uri) { }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
