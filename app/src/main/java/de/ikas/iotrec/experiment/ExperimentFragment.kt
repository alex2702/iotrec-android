package de.ikas.iotrec.experiment

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import de.ikas.iotrec.R
import de.ikas.iotrec.account.data.model.User
import de.ikas.iotrec.app.IotRecApplication
import de.ikas.iotrec.app.MainActivity
import de.ikas.iotrec.database.model.Question
import de.ikas.iotrec.network.model.Questionnaire
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class ExperimentFragment : Fragment() {

    private var listener: OnQuestionListFragmentInteractionListener? = null
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var app: IotRecApplication
    private lateinit var mainActivity: MainActivity
    private lateinit var questionViewModel: QuestionViewModel

    private val TAG = "ExperimentFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivity = activity as MainActivity
        app = mainActivity.applicationContext as IotRecApplication
        questionViewModel = ViewModelProviders.of(this).get(QuestionViewModel::class.java)
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_experiment, container, false)

        // get UI elements from view
        val viewSelectPreferences = view.findViewById<ConstraintLayout>(R.id.select_preferences)
        val viewStartScenario = view.findViewById<ConstraintLayout>(R.id.start_scenario)
        val viewStartTestRun = view.findViewById<ConstraintLayout>(R.id.start_test_run)
        val viewPerformTestRun = view.findViewById<ConstraintLayout>(R.id.perform_test_run)
        val viewRateTestRun = view.findViewById<ConstraintLayout>(R.id.rate_test_run)
        val viewStartScenario2 = view.findViewById<ConstraintLayout>(R.id.start_scenario_2)
        val viewFillQuestionnaire = view.findViewById<ConstraintLayout>(R.id.fill_questionnaire)
        val viewThankYou = view.findViewById<ConstraintLayout>(R.id.thank_you)
        val viewPleaseLogIn = view.findViewById<ConstraintLayout>(R.id.please_log_in)

        // put all views (which correspond to steps, and thus "pages" of the experiments) in a list
        val views = arrayListOf(
            viewSelectPreferences,
            viewStartScenario,
            viewStartTestRun,
            viewPerformTestRun,
            viewRateTestRun,
            viewStartScenario2,
            viewFillQuestionnaire,
            viewThankYou,
            viewPleaseLogIn
        )

        // get current step from shared preferences
        val experimentCurrentStep = sharedPrefs.getString("experimentCurrentStep", "start")
        val experimentCurrentRun = sharedPrefs.getInt("experimentCurrentRun", 0)

        val json = sharedPrefs.getString("user", "{}")
        val moshi = Moshi.Builder().add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe()).build()
        val adapter = moshi.adapter(User::class.java)
        val user = adapter.fromJson(json!!)

        // only show something if user is logged in
        if(user != null && mainActivity.loginRepository.isLoggedIn()) {
            // depending on the current step, show a different view from the views set
            when (experimentCurrentStep) {
                "start" -> {
                    // check if preferences have been selected
                    if(user.preferences.isEmpty()) {
                        showView(views, 0)
                    } else {
                        showView(views, 1)
                    }
                }
                "start_test_run" -> {
                    showView(views, 2)
                }
                "perform_test_run" -> {
                    showView(views, 3)
                }
                "rate_test_run" -> {
                    showView(views, 4)
                }
                "start_scenario_2" -> {
                    showView(views, 5)
                }
                "fill_questionnaire" -> {
                    showView(views, 6)
                }
                "thank_you" -> {
                    showView(views, 7)
                }
                else -> {
                    showView(views, 8)
                }
            }
        } else {
            showView(views, 8)
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnQuestionListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ExperimentFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    fun showView(views: ArrayList<ConstraintLayout>, indexToShow: Int) {
        // hide all views
        for(view in views) {
            view.visibility = View.GONE
        }

        // get the user's current test run
        var experimentCurrentRun = sharedPrefs.getInt("experimentCurrentRun", 0)

        // get the user's experiment from the database
        GlobalScope.launch {
            val allExperiments = mainActivity.experimentRepository.getAllExperiments()
        }

        when(indexToShow) {
            //viewSelectPreferences
            0 -> {
                val button = views[indexToShow].findViewById<Button>(R.id.select_preferences_button)
                button.setOnClickListener {
                    (activity as MainActivity).navView.selectedItemId = R.id.navigation_preferences
                }
            }
            //viewStartScenario
            1 -> {
                val experimentNextRun = experimentCurrentRun + 1

                GlobalScope.launch {
                    val nextExperiment = mainActivity.experimentRepository.getExperimentByOrder(experimentNextRun)

                    mainActivity.runOnUiThread {
                        //show the scenario name
                        val scenarioTextView = views[indexToShow].findViewById<TextView>(R.id.start_scenario_title)
                        if (nextExperiment.scenario == "jobfair") {
                            scenarioTextView.text = "Job Fair"
                            scenarioTextView.background = ContextCompat.getDrawable(mainActivity, R.drawable.border_radius_red)
                        } else {
                            scenarioTextView.text = "Museum"
                            scenarioTextView.background = ContextCompat.getDrawable(mainActivity, R.drawable.border_radius_green)
                        }

                        // hook up the button
                        val button = views[indexToShow].findViewById<Button>(R.id.start_scenario_button)
                        button.setOnClickListener {
                            val editor = sharedPrefs.edit()
                            //update experimentCurrentStep in SP to start_test_run
                            editor.putString("experimentCurrentStep", "start_test_run")
                            //update experimentCurrentRun in SP to 1
                            editor.putInt("experimentCurrentRun", experimentNextRun)
                            editor.putString("experimentCurrentScenario", nextExperiment.scenario)
                            editor.apply()

                            //change to viewStartTestRun
                            showView(views, 2)
                        }
                    }
                }
            }
            //viewStartTestRun
            2 -> {
                GlobalScope.launch {
                    //get current experiment from db
                    val currentExperiment = mainActivity.experimentRepository.getExperimentByOrder(experimentCurrentRun)
                    Log.d(TAG, currentExperiment.toString())

                    mainActivity.runOnUiThread {
                        //update counter and scenario in textview
                        val textView = views[indexToShow].findViewById<TextView>(R.id.start_test_run_body)
                        val testRunWithinScenario = if(experimentCurrentRun > 2) experimentCurrentRun-2 else experimentCurrentRun
                        val scenarioPrint = if(currentExperiment.scenario == "jobfair") "Job Fair" else "Museum"
                        textView.text = "You're now ready to start test run ${testRunWithinScenario} out of 2 in the ${scenarioPrint} scenario."

                        //after button click
                        val button = views[indexToShow].findViewById<Button>(R.id.start_test_run_button)
                        button.text = "Start Test Run ${testRunWithinScenario}"
                        button.setOnClickListener {
                            //update experiment via API (start date)
                            currentExperiment.start = Date()
                            GlobalScope.launch {
                                val result = mainActivity.experimentRepository.updateExperiment(currentExperiment)
                                Log.d(TAG, result.toString())
                            }

                            val editor = sharedPrefs.edit()
                            //update experimentCurrentStep in SP to start_test_run
                            editor.putString("experimentCurrentStep", "perform_test_run")
                            editor.apply()

                            //change to viewStartTestRun
                            showView(views, 3)
                        }
                    }
                }
            }
            //viewPerformTestRun
            3 -> {
                GlobalScope.launch {
                    //get current experiment from db
                    val currentExperiment =
                        mainActivity.experimentRepository.getExperimentByOrder(experimentCurrentRun)

                    mainActivity.runOnUiThread {
                        //update counter and scenario in textview
                        val textViewCounter =
                            views[indexToShow].findViewById<TextView>(R.id.perform_test_run_counter)
                        val testRunWithinScenario =
                            if (experimentCurrentRun > 2) experimentCurrentRun - 2 else experimentCurrentRun
                        val textViewScenario =
                            views[indexToShow].findViewById<TextView>(R.id.perform_test_run_scenario)
                        textViewCounter.text = "${testRunWithinScenario}/2"
                        if (currentExperiment != null && currentExperiment.scenario == "jobfair") {
                            textViewScenario.text = "Job Fair"
                            textViewScenario.background = ContextCompat.getDrawable(
                                mainActivity,
                                R.drawable.border_radius_red
                            )
                        } else {
                            textViewScenario.text = "Museum"
                            textViewScenario.background = ContextCompat.getDrawable(
                                mainActivity,
                                R.drawable.border_radius_green
                            )
                        }

                        //update context (weather, temp, tod, lot) from experiment (use raw values for temp and lot)
                        val textViewWeather =
                            views[indexToShow].findViewById<TextView>(R.id.perform_test_run_weather)
                        if(currentExperiment != null && currentExperiment.context_weather == "SUNNY") {
                            textViewWeather.text = "Sunny/Clear"
                        } else {
                            textViewWeather.text = currentExperiment.context_weather.toLowerCase().capitalize()
                        }
                        val textViewTemp =
                            views[indexToShow].findViewById<TextView>(R.id.perform_test_run_temp)
                        textViewTemp.text = "${currentExperiment.context_temperature} degrees"
                        val textViewTod =
                            views[indexToShow].findViewById<TextView>(R.id.perform_test_run_tod)
                        textViewTod.text =
                            currentExperiment.context_time_of_day!!.replace("_", " ").toLowerCase()
                                .capitalize()
                        val textViewLot =
                            views[indexToShow].findViewById<TextView>(R.id.perform_test_run_lot)
                        var hoursOnTheMove = "${(currentExperiment.context_length_of_trip.toDouble() / 60).roundToInt()} hours"
                        if(hoursOnTheMove == "0 hours") {
                            hoursOnTheMove = "less than an hour"
                        } else if(hoursOnTheMove == "1 hours") {
                            hoursOnTheMove = "an hour"
                        }
                        textViewLot.text = "You've been on the move for ${hoursOnTheMove}."

                        //after button click
                        val button = views[indexToShow].findViewById<Button>(R.id.perform_test_run_button)
                        button.setOnClickListener {
                            //show "are you sure?" dialog
                            val builder = AlertDialog.Builder((activity as Activity))
                            builder
                                .setMessage("Do you want to finish the test run?")
                                .setPositiveButton("Yes, finish") { _, _ ->
                                    //update experiment via API (end date)
                                    currentExperiment.end = Date()
                                    GlobalScope.launch {
                                        val result = mainActivity.experimentRepository.updateExperiment(currentExperiment)
                                        Log.d(TAG, result.toString())

                                        // clear all things, recommendations, feedbacks and ratings to provide a fresh base for the next run
                                        app.thingRepository.deleteAll()
                                        app.recommendationRepository.deleteAll()
                                        app.feedbackRepository.deleteAll()
                                        app.ratingRepository.deleteAll()
                                    }

                                    val editor = sharedPrefs.edit()
                                    //update experimentCurrentStep in SP to rate_test_run
                                    editor.putString("experimentCurrentStep", "rate_test_run")
                                    editor.apply()

                                    //change to viewRateTestRun
                                    showView(views, 4)
                                }
                                .setNegativeButton("Not yet") { _, _ -> }

                            val alert = builder.create()
                            alert.setTitle("Are you sure?")
                            alert.show()
                        }
                    }
                }
            }
            //viewRateTestRun
            4 -> {
                GlobalScope.launch {
                    //get current experiment from db (using experimentCurrentRun)
                    val currentExperiment =
                        mainActivity.experimentRepository.getExperimentByOrder(experimentCurrentRun)

                    //get next experiment from db (using experimentCurrentRun+1)
                    val nextExperiment =
                        mainActivity.experimentRepository.getExperimentByOrder(experimentCurrentRun+1)

                    mainActivity.runOnUiThread {
                        //get questions and put them into the list
                        val recyclerView = views[indexToShow].findViewById<RecyclerView>(R.id.rate_test_run_list)
                        with(recyclerView) {
                            layoutManager = LinearLayoutManager(context)

                            adapter = QuestionRecyclerViewAdapter(context, listener)
                            (adapter as QuestionRecyclerViewAdapter).setOnItemClickListener { it ->
                                Log.d(TAG, "button clicked!")
                                Log.d(TAG, it.toString())

                                //send replies to API
                                GlobalScope.launch {
                                    for(reply in it) {
                                        reply.value++
                                        val result = mainActivity.replyRepository.sendReply(currentExperiment.id, reply)
                                        Log.d(TAG, result.toString())
                                    }
                                }

                                //if there are more experiments of this scenario (i.e. if currentExperiment.scenario == nextExperiment.scenario)
                                if(nextExperiment != null && currentExperiment.scenario == nextExperiment.scenario) {
                                    val editor = sharedPrefs.edit()
                                    //update experimentCurrentStep in SP to start_test_run
                                    editor.putString("experimentCurrentStep", "start_test_run")
                                    //increase experimentCurrentRun in SP by 1
                                    experimentCurrentRun++
                                    editor.putInt("experimentCurrentRun", experimentCurrentRun)
                                    editor.apply()

                                    //change to viewStartTestRun
                                    showView(views, 2)

                                // else if there are more experiments (but of a different scenario)
                                } else if(nextExperiment != null) {
                                    val editor = sharedPrefs.edit()
                                    //update experimentCurrentStep in SP to start_scenario_2
                                    editor.putString("experimentCurrentStep", "start_scenario_2")
                                    //increase experimentCurrentRun in SP by 1
                                    experimentCurrentRun++
                                    editor.putInt("experimentCurrentRun", experimentCurrentRun)
                                    editor.apply()

                                    //change to viewStartScenario2
                                    showView(views, 5)
                                //else (i.e. there are no scenarios left)
                                } else {
                                    val editor = sharedPrefs.edit()
                                    //update experimentCurrentStep in SP to fill_questionnaire
                                    editor.putString("experimentCurrentStep", "fill_questionnaire")
                                    editor.apply()

                                    //change to viewFillQuestionnaire
                                    showView(views, 6)
                                }
                            }

                            questionViewModel.questions.observe(
                                viewLifecycleOwner,
                                Observer { questions ->
                                    // Update the cached copy of the categories in the adapter.
                                    questions?.let {
                                        (adapter as QuestionRecyclerViewAdapter).setQuestions(
                                            it
                                        )
                                    }
                                }
                            )

                            val dividerItemDecoration = DividerItemDecoration(
                                recyclerView.context,
                                (layoutManager as LinearLayoutManager).orientation
                            )
                            recyclerView.addItemDecoration(dividerItemDecoration)
                        }
                    }


                }
            }
            //viewStartScenario2
            5 -> {
                GlobalScope.launch {
                    //get current experiment from db (using experimentCurrentRun)
                    val currentExperiment =
                        mainActivity.experimentRepository.getExperimentByOrder(experimentCurrentRun)

                    activity!!.runOnUiThread {
                        //update scenario text view of last experiment
                        //update scenario text view of current experiment
                        val textViewLastScenario = views[indexToShow].findViewById<TextView>(R.id.start_scenario_2_completed)
                        val textViewNextScenario = views[indexToShow].findViewById<TextView>(R.id.start_scenario_2_next)
                        if(currentExperiment.scenario == "jobfair") {
                            textViewNextScenario.text = "Job Fair"
                            textViewNextScenario.background = ContextCompat.getDrawable(mainActivity, R.drawable.border_radius_red)
                            textViewLastScenario.text = "Museum"
                            textViewLastScenario.background = ContextCompat.getDrawable(mainActivity, R.drawable.border_radius_green)
                        } else {
                            textViewNextScenario.text = "Museum"
                            textViewNextScenario.background = ContextCompat.getDrawable(mainActivity, R.drawable.border_radius_green)
                            textViewLastScenario.text = "Job Fair"
                            textViewLastScenario.background = ContextCompat.getDrawable(mainActivity, R.drawable.border_radius_red)
                        }

                        //after button click
                        val button = views[indexToShow].findViewById<Button>(R.id.start_scenario_2_button)
                        button.setOnClickListener {
                            val editor = sharedPrefs.edit()
                            //update experimentCurrentStep in SP to start_test_run
                            editor.putString("experimentCurrentStep", "start_test_run")
                            editor.putString("experimentCurrentScenario", currentExperiment.scenario)
                            editor.apply()

                            //change to viewStartTestRun
                            showView(views, 2)
                        }
                    }
                }
            }
            //viewFillQuestionnaire
            6 -> {
                GlobalScope.launch {
                    //get last experiment from db (using experimentCurrentRun)
                    val lastExperiment =
                        mainActivity.experimentRepository.getExperimentByOrder(experimentCurrentRun)

                    activity!!.runOnUiThread {
                        //update scenario text view of last experiment
                        val textViewLastScenario =
                            views[indexToShow].findViewById<TextView>(R.id.fill_questionnaire_completed)
                        if (lastExperiment.scenario == "jobfair") {
                            textViewLastScenario.text = "Job Fair"
                            textViewLastScenario.background = ContextCompat.getDrawable(
                                mainActivity,
                                R.drawable.border_radius_red
                            )
                        } else {
                            textViewLastScenario.text = "Museum"
                            textViewLastScenario.background = ContextCompat.getDrawable(
                                mainActivity,
                                R.drawable.border_radius_green
                            )
                        }
                    }
                }

                val button = views[indexToShow].findViewById<Button>(R.id.fill_questionnaire_button)

                //watch radio buttons and record questionnaire object
                var questionnaire = Questionnaire("", "", "", "")
                val radioGroupAge = views[indexToShow].findViewById<RadioGroup>(R.id.fill_questionnaire_age)
                val radioGroupGender = views[indexToShow].findViewById<RadioGroup>(R.id.fill_questionnaire_gender)
                val radioGroupQualification= views[indexToShow].findViewById<RadioGroup>(R.id.fill_questionnaire_qualification)
                val radioGroupSmartphoneUsage = views[indexToShow].findViewById<RadioGroup>(R.id.fill_questionnaire_usage)

                radioGroupAge.setOnCheckedChangeListener { group, checkedId ->
                    when(checkedId) {
                        R.id.fill_questionnaire_age_18 -> {
                            questionnaire.age = "youngerThan18"
                        }
                        R.id.fill_questionnaire_age_24 -> {
                            questionnaire.age = "18-24"
                        }
                        R.id.fill_questionnaire_age_29 -> {
                            questionnaire.age = "25-29"
                        }
                        R.id.fill_questionnaire_age_39 -> {
                            questionnaire.age = "30-39"
                        }
                        R.id.fill_questionnaire_age_49 -> {
                            questionnaire.age = "40-49"
                        }
                        R.id.fill_questionnaire_age_59 -> {
                            questionnaire.age = "50-59"
                        }
                        R.id.fill_questionnaire_age_60 -> {
                            questionnaire.age = "olderThan60"
                        }
                    }

                    button.isEnabled = questionnaire.age != "" && questionnaire.gender != "" && questionnaire.qualification != "" && questionnaire.smartphone_usage != ""
                }

                radioGroupGender.setOnCheckedChangeListener { group, checkedId ->
                    when(checkedId) {
                        R.id.fill_questionnaire_gender_male -> {
                            questionnaire.gender = "male"
                        }
                        R.id.fill_questionnaire_gender_female -> {
                            questionnaire.gender = "female"
                        }
                        R.id.fill_questionnaire_gender_other -> {
                            questionnaire.gender = "notListed"
                        }
                        R.id.fill_questionnaire_gender_none -> {
                            questionnaire.gender = "noAnswer"
                        }
                    }

                    button.isEnabled = questionnaire.age != "" && questionnaire.gender != "" && questionnaire.qualification != "" && questionnaire.smartphone_usage != ""
                }

                radioGroupQualification.setOnCheckedChangeListener { group, checkedId ->
                    when(checkedId) {
                        R.id.fill_questionnaire_qualification_hs -> {
                            questionnaire.qualification = "highSchool"
                        }
                        R.id.fill_questionnaire_qualification_vt -> {
                            questionnaire.qualification = "vocationalTraining"
                        }
                        R.id.fill_questionnaire_qualification_bd -> {
                            questionnaire.qualification = "bachelor"
                        }
                        R.id.fill_questionnaire_qualification_md -> {
                            questionnaire.qualification = "master"
                        }
                        R.id.fill_questionnaire_qualification_phd -> {
                            questionnaire.qualification = "doctorate"
                        }
                    }

                    button.isEnabled = questionnaire.age != "" && questionnaire.gender != "" && questionnaire.qualification != "" && questionnaire.smartphone_usage != ""
                }

                radioGroupSmartphoneUsage.setOnCheckedChangeListener { group, checkedId ->
                    when(checkedId) {
                        R.id.fill_questionnaire_usage_1 -> {
                            questionnaire.smartphone_usage = "lessThan1Hour"
                        }
                        R.id.fill_questionnaire_usage_2 -> {
                            questionnaire.smartphone_usage = "between1And2Hours"
                        }
                        R.id.fill_questionnaire_usage_3 -> {
                            questionnaire.smartphone_usage = "between2And4Hours"
                        }
                        R.id.fill_questionnaire_usage_4 -> {
                            questionnaire.smartphone_usage = "moreThan4Hours"
                        }
                    }

                    button.isEnabled = questionnaire.age != "" && questionnaire.gender != "" && questionnaire.qualification != "" && questionnaire.smartphone_usage != ""
                }

                //after button click
                button.setOnClickListener {
                    //send questionnaire to API
                    GlobalScope.launch {
                        try {
                            val result = app.iotRecApi.createQuestionnaire(questionnaire)
                        } catch (e: Throwable) {
                            Log.d(TAG, e.toString())
                            mainActivity.runOnUiThread {
                                Toast.makeText(
                                    mainActivity,
                                    "Error submitting questionnaire.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }

                    val editor = sharedPrefs.edit()
                    //update experimentCurrentStep in SP to thank_you
                    editor.putString("experimentCurrentStep", "thank_you")
                    editor.putInt("experimentCurrentRun", 0)
                    editor.putString("experimentCurrentScenario", "")
                    editor.apply()

                    //change to viewThankYou
                    showView(views, 7)
                }
            }
            //viewThankYou
            7 -> {
                // no dynamic steps needed for now
            }
            //viewPleaseLogIn
            8 -> {
                // no dynamic steps needed for now
            }
        }

        views[indexToShow].visibility = View.VISIBLE
    }

    interface OnQuestionListFragmentInteractionListener {
        fun onQuestionListFragmentInteraction(question: Question)
    }
}
