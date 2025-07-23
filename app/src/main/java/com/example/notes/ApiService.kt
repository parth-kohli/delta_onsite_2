package com.example.notes
import androidx.compose.runtime.MutableState
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
data class FcmTokenRequest(val fcm_token: String)
data class UserSignup(val username: String, val password: String)
data class ApiResponse(val message: String, val user_id: Int?)
data class User(val id:Int, val username: String)
data class Notes(val id: Int, val title: String, val note: String, val user_id: Int, val created_at: String)
data class TokenResponse(val access_token: String, val token_type: String, val user_id: Int, val username: String)
interface ApiService {

    @FormUrlEncoded
    @POST("create_user/")
    fun signupUser(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<ApiResponse>

    @FormUrlEncoded
    @POST("login/")
    fun loginUser(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<TokenResponse>

    @GET("notes/")
    fun getnotes(
        @Query("skip") skip: Int,
        @Query("limit") limit: Int,
        @Header("Authorization") authHeader: String
    ): Call<List<Notes>>

    @FormUrlEncoded
    @POST("create_notes/")
    fun sendnotes(
        @Field("title") title: String,
        @Field("note") note: String,
        @Header("Authorization") authHeader: String
    ): Call<ApiResponse>
    @FormUrlEncoded
    @PUT("edit_notes/{id}/")
    fun editnotes(
        @Path("id") id: Int,
        @Field("title") title: String,
        @Field("note") note: String,
        @Header("Authorization") authHeader: String
    ): Call<ApiResponse>
    @POST("loginwithtoken")
    fun loginWithToken(
        @Header("Authorization") authHeader: String
    ): Call<User>
    @DELETE("delete_notes/{id}/")
    fun deleteNote(
        @Path("id") id: Int,
        @Header("Authorization") authHeader: String
    ): Call<ApiResponse>


}
