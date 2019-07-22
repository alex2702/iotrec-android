package de.ikas.iotrec.account.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.ikas.iotrec.account.data.LoginRepository
import de.ikas.iotrec.network.IotRecApiInit

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
class LoginViewModelFactory(val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(
                loginRepository = LoginRepository(
                    iotRecApi = IotRecApiInit(context)
                )
                    //dataSource = LoginDataSource()
                //)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
