package de.ikas.iotrec.preferences.adapter

import android.content.Context
import android.graphics.Color
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

class PreferenceRecyclerViewAdapter internal constructor(
    context: Context,
    //private val mValues: List<DummyItem>,
    private val mListener: PreferenceListFragment.OnListFragmentInteractionListener?
) : RecyclerView.Adapter<PreferenceRecyclerViewAdapter.ViewHolder>() {

    private val TAG = "PreferenceRecViewAdapt"
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var categories = emptyList<Category>() // Cached copy of categories
    var preferences = mutableListOf<Preference>() // TODO is this needed?
    //var categoryCounts:

    var app = context.applicationContext as IotRecApplication
    var categoryRepository = app.categoryRepository
    var preferenceRepository = app.preferenceRepository

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
