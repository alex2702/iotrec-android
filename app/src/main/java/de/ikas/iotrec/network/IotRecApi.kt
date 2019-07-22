package de.ikas.iotrec.network

import de.ikas.iotrec.account.data.model.LoggedInUser
import de.ikas.iotrec.database.model.Thing
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface IotRecApi {

    @FormUrlEncoded
    @POST("users/")
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("login/")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoggedInUser>


    @GET("things/{id}")
    suspend fun getThing(
        @Path("id") id: String
    ): Response<Thing>
}