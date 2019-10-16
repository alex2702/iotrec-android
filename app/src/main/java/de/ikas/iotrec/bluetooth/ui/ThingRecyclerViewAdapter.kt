package de.ikas.iotrec.bluetooth.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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
        val thingId: TextView = itemView.findViewById(R.id.thing_id)
        val thingDistance: TextView = itemView.findViewById(R.id.thing_distance)
        val thingLastSeen: TextView = itemView.findViewById(R.id.thing_last_seen_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThingViewHolder {
        Log.d(TAG, "onCreateViewHolder")
        val itemView = inflater.inflate(R.layout.fragment_thing_list_item, parent, false)
        return ThingViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ThingViewHolder, position: Int) {
        val currentThing = things[position]

        holder.thingTitle.text = currentThing.title
        holder.thingId.text = currentThing.id
        holder.thingDistance.text = "%.2f".format(currentThing.distance) + " m"
        holder.thingLastSeen.text = SimpleDateFormat("HH:mm:ss").format(currentThing.lastSeen)

        if(currentThing.lastQueried!!.equals(Date(0))) {
            holder.thingTitle.setTextColor(Color.GRAY)
        } else {
            holder.thingTitle.setTextColor(Color.BLACK)
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
