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
import de.ikas.iotrec.preferences.ui.PreferenceListFragment


import de.ikas.iotrec.database.model.Category

class PreferenceRecyclerViewAdapter internal constructor(
    context: Context,
    //private val mValues: List<DummyItem>,
    private val mListener: PreferenceListFragment.OnListFragmentInteractionListener?
) : RecyclerView.Adapter<PreferenceRecyclerViewAdapter.ViewHolder>() {

    private val TAG = "PreferenceRecViewAdapt"
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var categories = emptyList<Category>() // Cached copy of categories
    var selectedSubCategories = mutableListOf<String>()
    //var categoryCounts:

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

        // TODO show how many sub-categories are currently selected?
        // get sub-categories of current category => TODO
        // count how many sub-categories are selected in user profile already => TODO
        //val selectedSubCatsInThisCat = selectedCategories
        //.filterNot { rangedBeacons.any { x -> x.id == it.id } /* && (it.lastSeen < (now - 5 seconds))*/ }

        //Log.d(TAG, selectedSubCategories.toString())
        //Log.d(TAG, currentCategory.textId)
        //Log.d(TAG, currentCategory.textId.substringBefore('_'))
        val selectedCount = selectedSubCategories.count { x -> x.substringBefore('_') == currentCategory.textId }

        holder.textViewSelectedCounter.text = selectedCount.toString()




        //holder.mIdView.text = item.id
        //holder.mContentView.text = item.content

        with(holder.itemView) {
            tag = currentCategory
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = categories.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryItemView: TextView = itemView.findViewById(R.id.content)
        val textViewSelectedCounter: TextView = itemView.findViewById(R.id.selected_counter)
        //val mIdView: TextView = mView.item_number
        //val mContentView: TextView = mView.content

        override fun toString(): String {
            return super.toString() + " '" + categoryItemView.text + "'"
        }
    }

    internal fun setTopLevelCategories(categories: List<Category>) {
        this.categories = categories
        notifyDataSetChanged()
    }

}
