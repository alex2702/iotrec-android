package de.ikas.iotrec.network

import android.content.Context
import de.ikas.iotrec.account.data.model.LoggedInUser
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.ikas.iotrec.account.data.model.RegisteredUser
import de.ikas.iotrec.account.data.model.User
import de.ikas.iotrec.database.model.*
import de.ikas.iotrec.network.adapter.*
import de.ikas.iotrec.network.adapter.RatingJsonAdapter
import de.ikas.iotrec.network.adapter.RecommendationJsonAdapter
import de.ikas.iotrec.network.adapter.ThingJsonAdapter
import de.ikas.iotrec.network.model.AnalyticsEvent
import de.ikas.iotrec.network.model.Questionnaire
import java.util.*
import java.util.concurrent.TimeUnit

class IotRecApiInit(context: Context) {

    private val iotRecApi: IotRecApi

    init {
        val tokenInterceptor = TokenInterceptor(context)
        val responseInterceptor = ResponseInterceptor(context)

        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(Level.BODY)

        // set up an okHttp client
        val iotRecApiClient = OkHttpClient().newBuilder()
            .addInterceptor(tokenInterceptor)
            //.addInterceptor(loggingInterceptor)   // the loggingInterceptor provides verbose data on successful and failed requests
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()

        // set up moshi to convert serialize and deserialize JSON
        val moshi = Moshi.Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
            .add(ArrayListJsonAdapter())
            .add(RecommendationJsonAdapter())
            .add(ExperimentJsonAdapter())
            .add(RatingJsonAdapter())
            .add(ThingJsonAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()

        // set up retrofit which sends the request
        val retrofit = Retrofit.Builder()
            .client(iotRecApiClient)
            .baseUrl("https://example.com/api/")   //TODO insert path to deployed API
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        iotRecApi = retrofit.create(IotRecApi::class.java)
    }

    suspend fun login(username: String, password: String): Response<LoggedInUser> {
        return iotRecApi.login(username, password)
    }

    suspend fun register(username: String, email: String, password: String): Response<RegisteredUser> {
        return iotRecApi.register(username, email, password)
    }

    suspend fun getThing(id: String): Response<Thing> {
        return iotRecApi.getThing(id)
    }

    suspend fun verifyToken(token: String): Response<LoggedInUser> {
        return iotRecApi.verifyToken(token)
    }

    suspend fun getCategories(): Response<List<Category>> {
        return iotRecApi.getCategories()
    }

    suspend fun updateUserFromApp(userId: Int, user: User): Response<User> {
        return iotRecApi.updateUserFromApp(userId, user)
    }

    suspend fun deletePreference(userId: Int, preferenceId: String) {
        iotRecApi.deletePreference(userId, preferenceId)
    }

    suspend fun updatePreference(userId: Int, preferenceId: String, preference: Preference): Response<Preference> {
        return iotRecApi.updatePreference(userId, preferenceId, preference)
    }

    suspend fun addPreference(userId: Int, preference: Preference): Response<Preference> {
        return iotRecApi.addPreference(userId, preference)
    }

    suspend fun createRecommendation(recommendation: Recommendation): Response<Recommendation> {
        return iotRecApi.createRecommendation(recommendation)
    }

    suspend fun createFeedback(recommendationId: String, feedback: Feedback): Response<Feedback> {
        return iotRecApi.createFeedback(recommendationId, feedback)
    }

    suspend fun createRating(recommendationId: String, rating: Rating): Response<Rating> {
        return iotRecApi.createRating(recommendationId, rating)
    }

    suspend fun createAnalyticsEvent(analyticsEvent: AnalyticsEvent): Response<Unit> {
        return iotRecApi.createAnalyticsEvent(analyticsEvent)
    }

    suspend fun updateExperiment(experiment: Experiment): Response<Experiment> {
        return iotRecApi.updateExperiment(experiment.id, experiment)
    }

    suspend fun getQuestions(): Response<List<Question>> {
        return iotRecApi.getQuestions()
    }

    suspend fun createReply(experimentId: Int, reply: Reply): Response<Reply> {
        return iotRecApi.createReply(experimentId, reply)
    }

    suspend fun createQuestionnaire(questionnaire: Questionnaire): Response<Unit> {
        return iotRecApi.createQuestionnaire(questionnaire)
    }
}