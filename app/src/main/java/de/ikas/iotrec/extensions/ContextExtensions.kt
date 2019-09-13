package de.ikas.iotrec.extensions

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

// source: https://stackoverflow.com/a/45857155
fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

// source: https://stackoverflow.com/a/45857155
fun Activity.hideKeyboard() {
    hideKeyboard(if (currentFocus == null) View(this) else currentFocus)
}

// source: https://stackoverflow.com/a/45857155
fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}