package de.ikas.iotrec.account.ui

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.ikas.iotrec.account.data.LoginRepository
import de.ikas.iotrec.R
import kotlinx.coroutines.*

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val TAG = "LoginViewModel"

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _signupForm = MutableLiveData<LoginFormState>()
    val signupFormState: LiveData<LoginFormState> = _signupForm

    private val _signupResult = MutableLiveData<LoginResult>()
    val signupResult: LiveData<LoginResult> = _signupResult

    fun login(username: String, password: String) {
        GlobalScope.launch {
            try {
                val result = loginRepository.login(username, password)

                Log.d(TAG, result.toString())

                //if(result is Result.Success)
                if (result.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        _loginResult.value = LoginResult(
                            success = LoggedInUserView(
                                username = result.body()!!.user.username,
                                email = result.body()!!.user.email,
                                token = result.body()!!.token,
                                preferences = result.body()!!.user.preferences
                            )
                        )
                    }
                } else {
                    withContext(Dispatchers.Main){ _loginResult.value =
                        LoginResult(error = R.string.login_failed)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        GlobalScope.launch {
            try {
                val result = loginRepository.register(username, email, password)

                Log.d(TAG, result.toString())

                if (result.isSuccessful) {
                    withContext(Dispatchers.Main){ _signupResult.value = LoginResult(
                        success = LoggedInUserView(
                            username = result.body()!!.username,
                            email = result.body()!!.email,
                            token = result.body()!!.token,
                            preferences = result.body()!!.preferences
                        )
                    )
                    }
                } else {
                    withContext(Dispatchers.Main){ _signupResult.value =
                        LoginResult(error = R.string.signup_failed)
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
            }
        }
    }

    fun loginDataChanged(username: String, password: String) {
        /*
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }*/

        if(username.isNotEmpty() && password.isNotEmpty()) {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    fun signupDataChanged(username: String, email: String, password: String, passwordConfirm: String) {
        if (!isUserNameValid(username)) {
            _signupForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isEmailValid(email)) {
            _signupForm.value = LoginFormState(emailError = R.string.invalid_email)
        } else if (!isPasswordValid(password)) {
            _signupForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else if (!doPasswordsMatch(password, passwordConfirm)) {
            _signupForm.value = LoginFormState(passwordConfirmError = R.string.passwords_do_not_match)
        } else {
            _signupForm.value = LoginFormState(isDataValid = true)
        }
    }

    private fun isUserNameValid(username: String): Boolean {
        return username.isNotBlank()
    }

    private fun isEmailValid(email: String): Boolean {
        return if (email.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        } else {
            false
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 1
    }

    private fun doPasswordsMatch(password: String, passwordConfirm: String): Boolean {
        return password == passwordConfirm
    }
}
