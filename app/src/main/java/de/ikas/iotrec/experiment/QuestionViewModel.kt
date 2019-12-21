package de.ikas.iotrec.experiment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import de.ikas.iotrec.app.IotRecApplication
import de.ikas.iotrec.database.model.Question
import de.ikas.iotrec.database.repository.QuestionRepository

class QuestionViewModel public constructor(application: Application) : AndroidViewModel(application) {

    private val questionRepository: QuestionRepository
    val questions: LiveData<List<Question>>

    init {
        val app = application as IotRecApplication
        questionRepository = app.questionRepository
        questions = questionRepository.questions
    }
}