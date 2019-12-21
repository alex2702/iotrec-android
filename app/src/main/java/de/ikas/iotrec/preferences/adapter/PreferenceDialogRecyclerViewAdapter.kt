package de.ikas.iotrec.preferences.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import de.ikas.iotrec.database.model.Category
import android.widget.TextView
import android.view.LayoutInflater
import android.widget.RadioGroup
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import de.ikas.iotrec.R
import de.ikas.iotrec.app.IotRecApplication
import de.ikas.iotrec.database.model.Preference
import de.ikas.iotrec.preferences.ui.PreferenceSelectDialogFragment

class PreferenceDialogRecyclerViewAdapter internal constructor(
    private var context: Context,
    private val listener: PreferenceSelectDialogFragment.OnFragmentInteractionListener?
) : RecyclerView.Adapter<PreferenceDialogRecyclerViewAdapter.ViewHolder>() {

    private var subCategories = emptyList<Category>()
    private var preferences = emptyList<Preference>()

    var app = context.applicationContext as IotRecApplication
    var categoryRepository = app.categoryRepository

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private val onCheckedChangeListener = RadioGroup.OnCheckedChangeListener { radioGroupView, checkedId ->
        // get the ToggleButton that was clicked
        for (i in 0 until radioGroupView.childCount) {
            val toggleButtonView = radioGroupView.getChildAt(i) as ToggleButton
            toggleButtonView.isChecked = toggleButtonView.id == checkedId
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = inflater.inflate(R.layout.fragment_preference_select_dialog_list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentCategory = subCategories[holder.adapterPosition]
        val currentPreference = preferences.find { x -> x.category == subCategories[holder.adapterPosition].textId}

        // fill in category text view
        holder.categoryItemView.text = currentCategory.name

        // remove onCheckedChangeListener and set all buttons to unchecked
        holder.radioGroup.setOnCheckedChangeListener(null)
        (holder.radioGroup.findViewById(R.id.button_positive) as ToggleButton).isChecked = false
        (holder.radioGroup.findViewById(R.id.button_neutral) as ToggleButton).isChecked = false
        (holder.radioGroup.findViewById(R.id.button_negative) as ToggleButton).isChecked = false

        // get the active toggle button (if there is none, set the neutral one)
        val toggleButton: ToggleButton
        if(currentPreference != null) {
            when {
                currentPreference.value > 0 -> {
                    toggleButton = holder.radioGroup.findViewById(R.id.button_positive) as ToggleButton
                }
                currentPreference.value < 0 -> {
                    toggleButton = holder.radioGroup.findViewById(R.id.button_negative) as ToggleButton
                }
                else -> {
                    toggleButton = holder.radioGroup.findViewById(R.id.button_neutral) as ToggleButton
                }
            }
        } else {
            toggleButton = holder.radioGroup.findViewById(R.id.button_neutral) as ToggleButton
        }

        // set the active toggle button as checked (which results in UI change)
        toggleButton.isChecked = true

        with(holder.itemView) {
            tag = currentCategory
        }

        with(holder.radioGroup) {
            tag = currentCategory
        }

        holder.radioGroup.setOnCheckedChangeListener(onCheckedChangeListener)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return subCategories.size
    }

    internal fun setSubCategories(subCategories: List<Category>) {
        this.subCategories = subCategories
        notifyDataSetChanged()
    }

    internal fun setPreferences(preferences: List<Preference>) {
        this.preferences = preferences
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryItemView: TextView = itemView.findViewById(R.id.content)
        val radioGroup: RadioGroup = itemView.findViewById(R.id.radio_group)

        override fun toString(): String {
            return super.toString() + " '" + categoryItemView.text + "'"
        }

    }
}