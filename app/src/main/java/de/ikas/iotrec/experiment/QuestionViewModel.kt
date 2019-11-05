package de.ikas.iotrec.experiment

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import de.ikas.iotrec.app.IotRecApplication
import de.ikas.iotrec.database.model.Category
import de.ikas.iotrec.database.model.Preference
import de.ikas.iotrec.database.model.Question
import de.ikas.iotrec.database.repository.CategoryRepository
import de.ikas.iotrec.database.repository.PreferenceRepository
import de.ikas.iotrec.database.repository.QuestionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuestionViewModel public constructor(application: Application) : AndroidViewModel(application) {

    private val TAG = "QuestionViewModel"

    private val questionRepository: QuestionRepository
    val questions: LiveData<List<Question>>

    init {
        val app = application as IotRecApplication
        questionRepository = app.questionRepository
        questions = questionRepository.questions
    }
}