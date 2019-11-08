package de.ikas.iotrec.account.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import androidx.appcompat.app.AlertDialog
import de.ikas.iotrec.app.IotRecApplication
import de.ikas.iotrec.app.ProfileFragment
import de.ikas.iotrec.extensions.hideKeyboard
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO


class LoginActivity : AppCompatActivity(), SignupFragment.OnFragmentInteractionListener {

    private lateinit var app: IotRecApplication
    private lateinit var loginViewModel: LoginViewModel
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = application as IotRecApplication

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


                // store user information in shared preferences
                //val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
                //val editor = sharedPrefs.edit()
                //editor.putString("user.username", loginResult.success.username)
                //editor.putString("user.email", loginResult.success.email)
                //editor.putString("user.token", loginResult.success.token)
                //editor.apply()


                scope.launch {
                    Log.d(TAG, "launched")
                    //sync categories
                    val result = app.iotRecApi.getCategories()
                    Log.d(TAG, "got result from getCategories")

                    // if successful, update database object
                    if (result.isSuccessful) {
                        Log.d(TAG, "result successful")
                        val resultCategories = result.body()
                        Log.d(TAG, resultCategories.toString())

                        // insert categories into database
                        app.categoryRepository.insertMultiple(*resultCategories!!.toTypedArray())
                        Log.d(TAG, "inserted categories")
                    }

                    val resultQ = app.iotRecApi.getQuestions()

                    Log.d(TAG, resultQ.toString())

                    // if successful, update database
                    if (resultQ.isSuccessful) {
                        val resultQuestions = resultQ.body()
                        Log.d(TAG, resultQuestions.toString())
                        app.questionRepository.insertMultiple(*resultQuestions!!.toTypedArray())
                    }
                }

                //runOnUiThread {
                Log.d(TAG, "updateUiWithUser")
                // show success message and profile tab
                updateUiWithUser(loginResult.success)

                Log.d(TAG, loginResult.success.preferences.toString())

                val resultIntent = Intent()
                resultIntent.putExtra("ACTION", "login")
                Log.d(TAG, "setResult")
                setResult(Activity.RESULT_OK, resultIntent)

                Log.d(TAG, "finish")
                //Complete and destroy login activity once successful
                finish()

            }
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
                .addToBackStack(null)
                .commit()
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val username = model.username

        //val preferences = model.preferences
        //Log.d(TAG, preferences.toString())

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
