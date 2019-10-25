package de.ikas.iotrec.bluetooth.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.iconics.IconicsColor
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.IconicsSize
import com.mikepenz.iconics.IconicsSize.Companion
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.toIconicsColor
import com.mikepenz.iconics.utils.toIconicsSizeDp
import de.ikas.iotrec.R
import de.ikas.iotrec.database.model.Thing
import java.text.SimpleDateFormat
import java.util.*



class ThingRecyclerViewAdapter internal constructor(
    context: Context,
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

    inner class ThingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thingTitle: TextView = itemView.findViewById(R.id.thing_title)
        //val thingId: TextView = itemView.findViewById(R.id.thing_id)
        val thingDistance: TextView = itemView.findViewById(R.id.thing_distance)
        val thingLastSeen: TextView = itemView.findViewById(R.id.thing_last_seen_time)
        val moreInformationIndicator: LinearLayout = itemView.findViewById(R.id.thing_more_information)

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
        Log.d(TAG, "onCreateViewHolder")
        val itemView = inflater.inflate(R.layout.fragment_thing_list_item, parent, false)
        return ThingViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ThingViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder")

        val currentThing = things[position]

        Log.d(TAG, currentThing.lastQueried.toString())

        holder.thingTitle.text = currentThing.title
        //holder.thingId.text = currentThing.id
        holder.thingDistance.text = "%.2f".format(currentThing.distance) + " m"
        holder.thingLastSeen.text = SimpleDateFormat("HH:mm:ss").format(currentThing.lastSeen)

        /*
        //holder.bluetoothIconDrawable.setBounds(0, 0, holder.canvas.width, holder.canvas.height)
        holder.bluetoothIconDrawable.draw(holder.canvas)
        holder.thingImageBitmapRound.isCircular = true
        holder.thingImage.setImageDrawable(holder.thingImageBitmapRound)
        */

        if(currentThing.lastQueried!!.equals(Date(0))) {
            //holder.thingTitle.setTextColor(Color.GRAY)
            holder.moreInformationIndicator.visibility = View.GONE
        } else {
            //holder.thingTitle.setTextColor(Color.BLACK)
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
