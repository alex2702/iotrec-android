package de.ikas.iotrec.bluetooth

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.iconics.view.IconicsTextView
import com.squareup.picasso.Picasso
import de.ikas.iotrec.R
import de.ikas.iotrec.database.model.Thing
import java.text.SimpleDateFormat
import java.util.*



class ThingRecyclerViewAdapter internal constructor(
    private val context: Context,
    //private val mValues: List<DummyItem>,
    private val mListener: ThingListFragment.OnListFragmentInteractionListener? // double listener?
) : RecyclerView.Adapter<ThingRecyclerViewAdapter.ThingViewHolder>() {


    private val mOnClickListener: View.OnClickListener = View.OnClickListener { view ->
        Log.d(TAG, "a list item was clicked")
        val item = view.tag as Thing
        mListener?.onThingListFragmentInteraction(item)    // double listener?
    }

    private val TAG = "ThingRecyclerViewAdapte"

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var things = emptyList<Thing>() // Cached copy of things
    private var sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

    inner class ThingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thingTitle: TextView = itemView.findViewById(R.id.thing_title)
        //val thingId: TextView = itemView.findViewById(R.id.thing_id)
        val thingDistance: TextView = itemView.findViewById(R.id.thing_distance)
        val thingLastSeenPrefix: TextView = itemView.findViewById(R.id.thing_last_seen_header)
        val thingLastSeen: TextView = itemView.findViewById(R.id.thing_last_seen_time)
        val moreInformationIndicator: LinearLayout = itemView.findViewById(R.id.thing_more_information)
        val thingImage: ImageView = itemView.findViewById(R.id.thing_image)
        val thingImagePlaceholder: IconicsTextView = itemView.findViewById(R.id.thing_image_placeholder)
        val currentScenario = sharedPrefs.getString("experimentCurrentScenario", "")

        /*
        val thingImage: ImageView = itemView.findViewById(R.id.thing_image)
        val bluetoothIconDrawable: IconicsDrawable = IconicsDrawable(context)
            .icon(GoogleMaterial.Icon.gmd_bluetooth)
            .color(IconicsColor.colorInt(R.color.colorSecondary))
            .size(IconicsSize.dp(96))
            //.style(Paint.Style.FILL)
        val thingImageBitmap: Bitmap = Bitmap.createBitmap(bluetoothIconDrawable.intrinsicWidth, bluetoothIconDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)//BitmapFactory.decodeResource(getResources(), bluetoothIconDrawable)
        val canvas: Canvas = Canvas(thingImageBitmap)
        val thingImageBitmapRound: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.resources, thingImageBitmap)
        */



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThingViewHolder {
        val itemView = inflater.inflate(R.layout.fragment_thing_list_item, parent, false)
        return ThingViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ThingViewHolder, position: Int) {
        val currentThing = things[position]

        // set to default layout
        holder.thingImage.setImageResource(0)
        holder.thingTitle.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
        holder.thingDistance.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
        holder.thingLastSeen.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
        holder.thingLastSeenPrefix.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))

        holder.thingTitle.text = currentThing.title


        // if a scenario is set, display everything not belonging to that scenario in grey
        if(
            (holder.currentScenario != "" && !currentThing.id.endsWith(holder.currentScenario) && currentThing.lastQueried!!.time > 0) ||
            (currentThing.lastQueried!!.time == 0L) ||
            (holder.currentScenario == "" && currentThing.id.endsWith("-museum")) ||
            (holder.currentScenario == "" && currentThing.id.endsWith("-jobfair"))
        ) {
            holder.thingTitle.setTextColor(ContextCompat.getColor(context, R.color.colorGray))
            holder.thingDistance.setTextColor(ContextCompat.getColor(context, R.color.colorGray))
            holder.thingLastSeen.setTextColor(ContextCompat.getColor(context, R.color.colorGray))
            holder.thingLastSeenPrefix.setTextColor(ContextCompat.getColor(context, R.color.colorGray))
        }

        holder.thingDistance.text = "%.2f".format(currentThing.distance) + " m"
        holder.thingLastSeen.text = SimpleDateFormat("HH:mm:ss").format(currentThing.lastSeen)

        if(currentThing.image != "") {
            Picasso.get().load(currentThing.image).into(holder.thingImage)
        }

        if(currentThing.lastQueried!!.equals(Date(0))) {
            holder.moreInformationIndicator.visibility = View.GONE
        } else {
            holder.moreInformationIndicator.visibility = View.VISIBLE
        }

        with(holder.itemView) {
            tag = currentThing
            setOnClickListener(mOnClickListener)
        }
    }

    internal fun setThings(things: List<Thing>) {
        this.things = things
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return things.size
    }

    /* old from dummy
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_thing_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mIdView.text = item.id
        holder.mContentView.text = item.content

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.item_number
        val mContentView: TextView = mView.content

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
    */
}