package de.ikas.iotrec.account.ui.login

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.ikas.iotrec.account.data.LoginRepository
import de.ikas.iotrec.account.data.Result
import de.ikas.iotrec.R
import de.ikas.iotrec.account.data.model.LoggedInUser
import kotlinx.coroutines.*

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val TAG = "LoginViewModel"

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String) {
        GlobalScope.launch {
            try {
                val result = loginRepository.login(username, password)

                Log.d(TAG, result.toString())

                //if(result is Result.Success)
                if (result.isSuccessful) {
                    withContext(Dispatchers.Main){ _loginResult.value = LoginResult(success = LoggedInUserView(displayName = result.body()!!.user.username, token = result.body()!!.token)) }
                } else {
                    withContext(Dispatchers.Main){ _loginResult.value = LoginResult(error = R.string.login_failed) }
                }
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
            }
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        //return if (username.contains('@')) {
        //    Patterns.EMAIL_ADDRESS.matcher(username).matches()
        //} else {
            return username.isNotBlank()
        //}
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 1
    }
}
