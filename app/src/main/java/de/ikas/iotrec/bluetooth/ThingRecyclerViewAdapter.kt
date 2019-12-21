package de.ikas.iotrec.bluetooth

import android.content.Context
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
    private val mListener: ThingListFragment.OnListFragmentInteractionListener?
) : RecyclerView.Adapter<ThingRecyclerViewAdapter.ThingViewHolder>() {

    private val mOnClickListener: View.OnClickListener = View.OnClickListener { view ->
        val item = view.tag as Thing
        mListener?.onThingListFragmentInteraction(item)
    }

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var things = emptyList<Thing>() // Cached copy of things
    private var sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

    // view holder that handles a single list item
    inner class ThingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thingTitle: TextView = itemView.findViewById(R.id.thing_title)
        val thingDistance: TextView = itemView.findViewById(R.id.thing_distance)
        val thingLastSeenPrefix: TextView = itemView.findViewById(R.id.thing_last_seen_header)
        val thingLastSeen: TextView = itemView.findViewById(R.id.thing_last_seen_time)
        val moreInformationIndicator: LinearLayout = itemView.findViewById(R.id.thing_more_information)
        val thingImage: ImageView = itemView.findViewById(R.id.thing_image)
        val currentScenario = sharedPrefs.getString("experimentCurrentScenario", "")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThingViewHolder {
        val itemView = inflater.inflate(R.layout.fragment_thing_list_item, parent, false)
        return ThingViewHolder(itemView)
    }

    // when binding a view holder, fill in correct information of the currentThing
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

        // load thing image
        if(currentThing.image != "") {
            Picasso.get().load(currentThing.image).into(holder.thingImage)
        }

        // if information has been loaded before (i.e. lastQueried is set), indicate that
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
}
