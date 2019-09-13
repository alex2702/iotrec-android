package de.ikas.iotrec.preferences.adapter

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import de.ikas.iotrec.database.model.Category
import android.widget.TextView
import android.graphics.Color
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import de.ikas.iotrec.R
import de.ikas.iotrec.app.IotRecApplication
import de.ikas.iotrec.preferences.ui.PreferenceSelectDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class PreferenceDialogViewAdapter internal constructor(
    private var context: Context,
    private val listener: PreferenceSelectDialogFragment.OnFragmentInteractionListener?
) : RecyclerView.Adapter<PreferenceDialogViewAdapter.ViewHolder>() {

    private var subCategories = emptyList<Category>()
    var selectedSubCategories = emptyList<Category>()

    var app = context.applicationContext as IotRecApplication
    var categoryRepository = app.categoryRepository

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private val TAG = "PreferenceDialogViewAda"
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val onClickListener: View.OnClickListener = View.OnClickListener { view ->
        val item = view.tag as Category

        if(selectedSubCategories.contains(item)) {
            // if item is currently selected, remove background color and remove from list
            view.setBackgroundColor(0)
            //selectedSubCategories.remove(index)
            //app.loginRepository.user!!.preferences.remove(item.textId)
            scope.launch {
                app.loginRepository.removePreferenceFromAccount(item)
            }

            //scope.launch {
            //    categoryRepository.setCategorySelected(item.textId, false)
            //}
        } else {
            // else, add background color and add to list
            view.setBackgroundColor(Color.GRAY)
            //selectedSubCategories.add(item.textId)
            //selected = true
            //app.loginRepository.user!!.preferences.add(item.textId)
            scope.launch {
                app.loginRepository.addPreferenceToAccount(item)
            }
            //scope.launch {
            //    categoryRepository.setCategorySelected(item.textId, true)
            //}
        }

        listener?.onPreferenceSelectDialogFragmentInteraction(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = inflater.inflate(R.layout.fragment_preference_select_dialog_list_item, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentCategory = subCategories[position]

        //Log.d(TAG, "sSC: $selectedSubCategories")

        holder.categoryItemView.text = currentCategory.name

        // check if currentCategory is part of pre-selected sub categories
        if(selectedSubCategories.any { x -> x.textId == currentCategory.textId }) {
            //Log.d(TAG, "found selected: " + currentCategory.textId)
            holder.itemView.setBackgroundColor(Color.GRAY)
            //holder.categoryItemView.setBackgroundColor(Color.GRAY)
        } else {
            holder.itemView.setBackgroundColor(0)
            //holder.categoryItemView.setBackgroundColor(0)
        }

        //holder.mIdView.text = item.id
        //holder.mContentView.text = item.content

        with(holder.itemView) {
            tag = currentCategory
            setOnClickListener(onClickListener)
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return subCategories.size
    }

    internal fun setSubCategories(subCategories: List<Category>) {
        this.subCategories = subCategories
        Log.d(TAG, "setSubCategories – notifyDataSetChanged")
        notifyDataSetChanged()
    }

    internal fun setSelectedSubCategories(selectedSubCategories: List<Category>) {
        this.selectedSubCategories = selectedSubCategories
        Log.d(TAG, "setSelectedSubCategories – notifyDataSetChanged")
        Log.d(TAG, "setSelectedSubCategories: " + selectedSubCategories.size.toString())
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryItemView: TextView = itemView.findViewById(R.id.content)
        //val mIdView: TextView = mView.item_number
        //val mContentView: TextView = mView.content

        override fun toString(): String {
            return super.toString() + " '" + categoryItemView.text + "'"
        }
    }

}