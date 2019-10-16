package de.ikas.iotrec.preferences.adapter

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import de.ikas.iotrec.database.model.Category
import android.widget.TextView
import android.graphics.Color
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.RadioGroup
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import de.ikas.iotrec.R
import de.ikas.iotrec.app.IotRecApplication
import de.ikas.iotrec.database.model.Preference
import de.ikas.iotrec.preferences.ui.PreferenceSelectDialogFragment
import kotlinx.coroutines.*




class PreferenceDialogRecyclerViewAdapter internal constructor(
    private var context: Context,
    private val listener: PreferenceSelectDialogFragment.OnFragmentInteractionListener?
) : RecyclerView.Adapter<PreferenceDialogRecyclerViewAdapter.ViewHolder>() {

    private var subCategories = emptyList<Category>()
    private var preferences = emptyList<Preference>()
    //var selectedSubCategories = emptyList<Category>()

    var app = context.applicationContext as IotRecApplication
    var categoryRepository = app.categoryRepository
    var preferenceRepository = app.preferenceRepository

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private val TAG = "PreferenceDialogViewAda"
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    /*
    private val onClickListener: View.OnClickListener = View.OnClickListener { view ->
        val item = view.tag as Preference

        if(preferences.contains(item)) {
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
    */

    private val onCheckedChangeListener = RadioGroup.OnCheckedChangeListener { radioGroupView, checkedId ->
        Log.d(TAG, "clicked view $radioGroupView")
        Log.d(TAG, "clicked ID $checkedId")

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

        Log.d(TAG, "currentCategory: $currentCategory")
        Log.d(TAG, "currentPreference: $currentPreference")

        //Log.d(TAG, "sSC: $selectedSubCategories")

        holder.categoryItemView.text = currentCategory.name

        // check if currentCategory is part of pre-selected sub categories
        //if(preferences.any { x -> x.category == currentCategory.textId }) {
        //    Log.d(TAG, "found pre-selected preference: ${currentCategory.textId}")
        //    val selectedPref = preferences.find { x -> x.category == currentCategory.textId }
        //    Log.d(TAG, "pre-selected preference is $selectedPref")

        // remove onCheckedChangeListener and set all buttons to unchecked
        holder.radioGroup.setOnCheckedChangeListener(null)
        (holder.radioGroup.findViewById(R.id.button_positive) as ToggleButton).isChecked = false
        (holder.radioGroup.findViewById(R.id.button_neutral) as ToggleButton).isChecked = false
        (holder.radioGroup.findViewById(R.id.button_negative) as ToggleButton).isChecked = false

        val toggleButton: ToggleButton
        if(currentPreference != null) {
            when {
                currentPreference.value > 0 -> {
                    //holder.radioGroup.check(R.id.button_positive)
                    toggleButton = holder.radioGroup.findViewById(R.id.button_positive) as ToggleButton
                }
                currentPreference.value < 0 -> {
                    //holder.radioGroup.check(R.id.button_negative)
                    toggleButton = holder.radioGroup.findViewById(R.id.button_negative) as ToggleButton
                }
                else -> {
                    //holder.radioGroup.check(R.id.button_neutral)
                    toggleButton = holder.radioGroup.findViewById(R.id.button_neutral) as ToggleButton
                }
            }
        } else {
            //holder.radioGroup.check(R.id.button_neutral)
            toggleButton = holder.radioGroup.findViewById(R.id.button_neutral) as ToggleButton
        }

        toggleButton.isChecked = true

        //holder.mIdView.text = item.id
        //holder.mContentView.text = item.content

        with(holder.itemView) {
            tag = currentCategory
            //setOnClickListener(onClickListener)
        }

        with(holder.radioGroup) {
            tag = currentCategory
        }

        holder.radioGroup.setOnCheckedChangeListener(onCheckedChangeListener)

        /*
        holder.buttonPositive.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked) {
                // preference was selected with value 1, save to user profile
                GlobalScope.launch {
                    app.loginRepository.setPreference(currentCategory.textId, 1)
                }
                // uncheck the other buttons
                holder.buttonNeutral.isChecked = false
                holder.buttonNegative.isChecked = false
            }
        }

        holder.buttonNeutral.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // preference was deselected, remove from user profile
                GlobalScope.launch {
                    app.loginRepository.setPreference(currentCategory.textId, 0)
                }
                // uncheck the other buttons
                holder.buttonPositive.isChecked = false
                holder.buttonNegative.isChecked = false
            }
        }

        holder.buttonNegative.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // preference was selected with value 0, save to user profile
                GlobalScope.launch {
                    app.loginRepository.setPreference(currentCategory.textId, -1)
                }
                // uncheck the other buttons
                holder.buttonNeutral.isChecked = false
                holder.buttonPositive.isChecked = false
            }
        }
        */
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

    internal fun setPreferences(preferences: List<Preference>) {
        this.preferences = preferences
        Log.d(TAG, "preferences – notifyDataSetChanged")
        Log.d(TAG, "preferences: " + preferences.size.toString())
        //notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryItemView: TextView = itemView.findViewById(R.id.content)
        /*
        val buttonNegative: ToggleButton = itemView.findViewById(R.id.button_negative)
        val buttonNeutral: ToggleButton = itemView.findViewById(R.id.button_neutral)
        val buttonPositive: ToggleButton = itemView.findViewById(R.id.button_positive)
        */

        val radioGroup: RadioGroup = itemView.findViewById(R.id.radio_group)

        //val mIdView: TextView = mView.item_number
        //val mContentView: TextView = mView.content

        override fun toString(): String {
            return super.toString() + " '" + categoryItemView.text + "'"
        }

    }



}