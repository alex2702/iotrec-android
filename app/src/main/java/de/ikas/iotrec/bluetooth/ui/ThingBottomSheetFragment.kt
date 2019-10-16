package de.ikas.iotrec.bluetooth.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import de.ikas.iotrec.R;
import de.ikas.iotrec.database.model.Thing
import kotlinx.android.synthetic.main.fragment_thing_bottom_sheet.*
import java.io.Serializable

class ThingBottomSheetFragment : BottomSheetDialogFragment() {

    //private lateinit var thingViewModel: ThingViewModel

    companion object {
        fun newInstance(thing: Thing, loginStatus: Boolean): ThingBottomSheetFragment =
            ThingBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("CLICKED_THING", thing)
                    putInt("LOGIN_STATUS", if (loginStatus) 1 else 0)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val thing = arguments!!.getParcelable("CLICKED_THING") as Thing
        val loginStatus = arguments!!.getInt("LOGIN_STATUS", 0)
        val loginStatusBoolean = loginStatus == 1

        val view = inflater.inflate(R.layout.fragment_thing_bottom_sheet, container, false)

        val textViewTitle: TextView = view.findViewById(R.id.thing_title)
        textViewTitle.text = thing.title

        val textViewDescription: TextView = view.findViewById(R.id.thing_description)
        if(loginStatusBoolean) {
            if(thing.description == "") {
                textViewDescription.text = "This beacon's details have not been fetched yet or could not be found."
            } else {
                textViewDescription.text = thing.description
            }
        } else {
            textViewDescription.text = "Please log in or sign up to view a beacon's details."
        }


        return view
    }

    /*
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        thingViewModel = ViewModelProviders.of(this).get(ThingViewModel::class.java)

        list.layoutManager = LinearLayoutManager(context)
        val adapter = LocalListAdapter(viewModel)
        list.adapter = adapter

        val items = mutableListOf<ViewItem>().apply {
            add(ViewItem.TitleView("Actions"))
            add(ViewItem.ActionView("Create new album", R.drawable.ic_add_black_24dp, viewModel.newAlbumEvent))
        }

        setupObserver()
    }
    */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //list.layoutManager = LinearLayoutManager(context)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parent = parentFragment
        if (parent != null) {

        } else {

        }
    }

    override fun onDetach() {
        super.onDetach()
    }


}
