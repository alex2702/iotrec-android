package de.ikas.iotrec.experiment

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.ikas.iotrec.R
import de.ikas.iotrec.app.IotRecApplication
import kotlinx.coroutines.*
import android.widget.SeekBar
import de.ikas.iotrec.database.model.Question
import de.ikas.iotrec.database.model.Reply


class QuestionRecyclerViewAdapter internal constructor(
    context: Context,
    private val mListener: ExperimentFragment.OnQuestionListFragmentInteractionListener?
) : RecyclerView.Adapter<QuestionRecyclerViewAdapter.ViewHolder>() {

    private val TAG = "QuestionRecyclerViewAd"
    private val VIEW_TYPE_BUTTON = 0
    private val VIEW_TYPE_ITEM = 1
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var questions = emptyList<Question>() // Cached copy of questions
    private var replies = listOf(
        Reply(0, 1, 0),
        Reply(0, 2, 0),
        Reply(0, 3, 0),
        Reply(0, 4, 0),
        Reply(0, 5, 0),
        Reply(0, 6, 0),
        Reply(0, 7, 0),
        Reply(0, 8, 0),
        Reply(0, 9, 0),
        Reply(0, 10, 0),
        Reply(0, 11, 0),
        Reply(0, 12, 0)
    )

    var app = context.applicationContext as IotRecApplication
    var questionRepository = app.questionRepository

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private val mOnClickListener: View.OnClickListener = View.OnClickListener { view ->
        val item = view.tag as Question
        mListener?.onQuestionListFragmentInteraction(item)
    }

    private var listener: ((replies: List<Reply>) -> Unit)? = null
    fun setOnItemClickListener(listener: (replies: List<Reply>) -> Unit) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if(viewType == VIEW_TYPE_ITEM) {
            val itemView = inflater.inflate(R.layout.experiment_list_item, parent, false)
            return ViewHolder(itemView)
        } else {
            val buttonView = inflater.inflate(R.layout.experiment_list_button, parent, false)
            return ViewHolder(buttonView)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position != questions.size) {
            val currentQuestion = questions[position]

            holder.questionTextView.text = currentQuestion.text

            with(holder.itemView) {
                tag = currentQuestion
                setOnClickListener(mOnClickListener)
            }

            holder.seekbar.setOnSeekBarChangeListener(null)
            holder.seekbar.progress = replies[position].value

            holder.seekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    replies[position].value = progress
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
        }
    }

    override fun getItemCount(): Int {
        if(questions.size > 0) {
            return questions.size+1
        } else {
            return questions.size
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var questionTextView: TextView
        lateinit var seekbar: SeekBar
        lateinit var button: Button

        init {
            if(itemView.findViewById<Button>(R.id.rate_test_run_button_new) != null) {
                button = itemView.findViewById(R.id.rate_test_run_button_new)
                button.setOnClickListener { listener?.invoke(replies) }
            } else {
                questionTextView = itemView.findViewById(R.id.question)
                seekbar = itemView.findViewById(R.id.seekbar)
            }
        }

        override fun toString(): String {
            return super.toString()
        }
    }

    internal fun setQuestions(questions: List<Question>) {
        this.questions = questions
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        if(position == questions.size) {
            return VIEW_TYPE_BUTTON
        } else {
            return VIEW_TYPE_ITEM
        }
    }
}
