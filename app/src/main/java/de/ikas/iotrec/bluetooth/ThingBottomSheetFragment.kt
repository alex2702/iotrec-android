package de.ikas.iotrec.bluetooth

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import de.ikas.iotrec.R;
import de.ikas.iotrec.database.model.Thing

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
        val textViewCategoriesHeader: TextView = view.findViewById(R.id.thing_categories_header)
        val textViewCategories: TextView = view.findViewById(R.id.thing_categories)
        val textViewIdHeader: TextView = view.findViewById(R.id.thing_id_header)
        val textViewId: TextView = view.findViewById(R.id.thing_id)
        val textViewOccupationHeader: TextView = view.findViewById(R.id.thing_occupation_header)
        val textViewOccupation: TextView = view.findViewById(R.id.thing_occupation)
        val imageView: ImageView = view.findViewById(R.id.thing_image)

        if(loginStatusBoolean) {
            textViewCategories.visibility = View.VISIBLE
            textViewId.visibility = View.VISIBLE
            textViewOccupation.visibility = View.VISIBLE
            textViewCategoriesHeader.visibility = View.VISIBLE
            textViewIdHeader.visibility = View.VISIBLE
            textViewOccupationHeader.visibility = View.VISIBLE

            if(thing.description == "") {
                textViewDescription.text = "This beacon's details have not been fetched yet or could not be found."
            } else {
                textViewDescription.text = thing.description
            }

            val categories: String
            if(thing.categories != null && thing.categories != "") {
                categories = thing.categories!!.replace("_", "/").replace(";", "\n")
            } else {
                categories = "none"
            }
            textViewCategories.text = categories

            textViewId.text = thing.id

            if(thing.occupation == 0) {
                textViewOccupation.text = "nobody"
            } else if(thing.occupation == 1) {
                textViewOccupation.text = "1 user (you)"
            } else if(thing.occupation > 1) {
                textViewOccupation.text = "${thing.occupation} users (including you)"
            }

            if(thing.image != "") {
                Picasso.get().load(thing.image).into(imageView)
            }
        } else {
            textViewDescription.text = "Please log in or sign up to view a beacon's details."
            textViewCategories.visibility = View.GONE
            textViewId.visibility = View.GONE
            textViewOccupation.visibility = View.GONE
            textViewCategoriesHeader.visibility = View.GONE
            textViewIdHeader.visibility = View.GONE
            textViewOccupationHeader.visibility = View.GONE
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
    }

    override fun onDetach() {
        super.onDetach()
    }


}
