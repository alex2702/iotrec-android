package de.ikas.iotrec.network

import de.ikas.iotrec.account.data.model.LoggedInUser
import de.ikas.iotrec.account.data.model.RegisteredUser
import de.ikas.iotrec.account.data.model.User
import de.ikas.iotrec.database.model.*
import retrofit2.Response
import retrofit2.http.*

interface IotRecApi {

    @FormUrlEncoded
    @POST("users/")
    suspend fun register(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<RegisteredUser>

    @FormUrlEncoded
    @POST("login/")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoggedInUser>

    @PATCH("users/{userId}/")
    suspend fun updateUserFromApp(
        @Path("userId") userId: Int,
        @Body user: User
    ): Response<User>

    // TODO
    //suspend fun updateUserToApp(
    //)

    @GET("things/{id}/")
    suspend fun getThing(
        @Path("id") id: String
    ): Response<Thing>

    @FormUrlEncoded
    @POST("verify-token/")
    suspend fun verifyToken(
        @Field("token") token: String
    ): Response<LoggedInUser>

    @GET("categories-flat/")
    suspend fun getCategories(): Response<List<Category>>

    @DELETE("users/{userId}/preferences/{preferenceId}/")
    suspend fun deletePreference(
        @Path("userId") userId: Int,
        @Path("preferenceId") preferenceId: String
    ): Response<Unit>

    @PATCH("users/{userId}/preferences/{preferenceId}/")
    suspend fun updatePreference(
        @Path("userId") userId: Int,
        @Path("preferenceId") preferenceId: String,
        @Body preference: Preference
    ): Response<Preference>

    @POST("users/{userId}/preferences/")
    suspend fun addPreference(
        @Path("userId") userId: Int,
        @Body preference: Preference
    ): Response<Preference>

    @POST("recommendations/")
    suspend fun createRecommendation(
        @Body recommendation: Recommendation
    ): Response<Recommendation>

    @POST("recommendations/{recommendationId}/feedback/")
    suspend fun createFeedback(
        @Path("recommendationId") recommendationId: String,
        @Body feedback: Feedback
    ): Response<Feedback>
}