package de.ikas.iotrec.account.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.ikas.iotrec.account.data.LoginRepository
import de.ikas.iotrec.app.IotRecApplication
import de.ikas.iotrec.network.IotRecApiInit

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
class LoginViewModelFactory(val context: Context) : ViewModelProvider.Factory {

    var app = context.applicationContext as IotRecApplication
    var loginRepository = app.loginRepository

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(loginRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
