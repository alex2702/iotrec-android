package de.ikas.iotrec.network

import de.ikas.iotrec.account.data.model.LoggedInUser
import de.ikas.iotrec.account.data.model.RegisteredUser
import de.ikas.iotrec.account.data.model.User
import de.ikas.iotrec.database.model.Category
import de.ikas.iotrec.database.model.Thing
import okhttp3.ResponseBody
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

    @PATCH("current-user/")
    suspend fun updateUserFromApp(
        @Body user: User
    ): Response<User>

    // TODO
    //suspend fun updateUserToApp(
    //)

    @GET("things/{id}")
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
}