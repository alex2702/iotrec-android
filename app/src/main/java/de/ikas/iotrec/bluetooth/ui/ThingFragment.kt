package de.ikas.iotrec.bluetooth.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders

import de.ikas.iotrec.R

class ThingFragment : Fragment() {

    companion object {
        fun newInstance() = ThingFragment()
    }

    private lateinit var viewModel: ThingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.thing_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ThingViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
