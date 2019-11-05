package de.ikas.iotrec.preferences.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.ikas.iotrec.R
import de.ikas.iotrec.app.IotRecApplication
import de.ikas.iotrec.preferences.ui.PreferenceListFragment
import de.ikas.iotrec.database.model.Category
import de.ikas.iotrec.database.model.Preference
import kotlinx.coroutines.*
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape




class PreferenceRecyclerViewAdapter internal constructor(
    context: Context,
    //private val mValues: List<DummyItem>,
    private val mListener: PreferenceListFragment.OnListFragmentInteractionListener?
) : RecyclerView.Adapter<PreferenceRecyclerViewAdapter.ViewHolder>() {

    private val TAG = "PreferenceRecViewAdapt"
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var categories = emptyList<Category>() // Cached copy of categories
    var preferences = mutableListOf<Preference>()
    //var categoryCounts:

    var app = context.applicationContext as IotRecApplication
    var categoryRepository = app.categoryRepository
    var preferenceRepository = app.preferenceRepository

    val iconColors = arrayListOf(
        R.color.materialLightRed,
        R.color.materialLightBlue,
        R.color.materialLightOrange,
        R.color.materialLightGreen,
        R.color.materialLightPink,
        R.color.materialLightTeal,
        R.color.materialLightPurple
    )

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private val mOnClickListener: View.OnClickListener = View.OnClickListener { view ->
        //Log.d(TAG, "a list item was clicked")
        val item = view.tag as Category
        mListener?.onPreferenceListFragmentInteraction(item)    // double listener?
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = inflater.inflate(R.layout.fragment_preference_list_item, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentCategory = categories[position]

        holder.categoryItemView.text = currentCategory.name
        holder.categoryIconView.text = currentCategory.name.substring(0, 1)

        val color: Int

        // color depends on third letter
        color = when (currentCategory.name.substring(2, 3).toLowerCase()) {
            "a", "h", "o", "v" -> iconColors[0]
            "b", "i", "p", "w" -> iconColors[1]
            "c", "j", "q", "x" -> iconColors[2]
            "d", "k", "r", "y" -> iconColors[3]
            "e", "l", "s", "z" -> iconColors[4]
            "f", "m", "t" -> iconColors[5]
            "g", "n", "u" -> iconColors[6]
            else -> iconColors[0]
        }

        val drawable = ShapeDrawable(OvalShape())
        drawable.mutate()
        drawable.paint.color = app.resources.getColor(color)
        holder.categoryIconView.setBackgroundDrawable(drawable)

        /*
        GlobalScope.launch(Dispatchers.IO) {
            val nrOfCategories = categoryRepository.getNumberOfSubCategories(currentCategory.textId)
            val nrOfPreferences = preferenceRepository.getNumberOfPreferences(currentCategory.textId)

            holder.textViewSelectedCounter.text = nrOfPreferences.toString()
            holder.textViewAllCounter.text = nrOfCategories.toString()
        }
        */

        with(holder.itemView) {
            tag = currentCategory
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = categories.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryItemView: TextView = itemView.findViewById(R.id.content)
        val categoryIconView: TextView = itemView.findViewById(R.id.letter_icon)
        /*
        val textViewSelectedCounter: TextView = itemView.findViewById(R.id.selected_counter)
        val textViewAllCounter: TextView = itemView.findViewById(R.id.all_counter)
        */

        override fun toString(): String {
            return super.toString() + " '" + categoryItemView.text + "'"
        }
    }

    internal fun setTopLevelCategories(categories: List<Category>) {
        this.categories = categories
        notifyDataSetChanged()
    }

}
