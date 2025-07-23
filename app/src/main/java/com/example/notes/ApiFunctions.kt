package com.example.notes

import androidx.navigation.NavController
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiFunctions {
    val retrofit = Retrofit.Builder()
        .baseUrl("http://127.0.0.1:8000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val apiService = retrofit.create(ApiService::class.java)
    fun signupUser(user: UserSignup, onSignUp: ()->Unit){
        val call = apiService.signupUser(user.username, user.password)
        call.enqueue(object : Callback<ApiResponse>{
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                onSignUp()
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {

            }

        })
    }
    fun loginUser(user: UserSignup, onLogin: ()->Unit){
        val call = apiService.loginUser(user.username, user.password)
        call.enqueue(object : Callback<TokenResponse>{
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (!response.body()?.access_token.isNullOrEmpty()) SecurePrefs.saveToken( response.body()?.access_token.toString())
                onLogin()
            }
            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                println(t)
            }

        })
    }
    fun loginWithToken(token: String,navController: NavController, onLogin: ()->Unit){
        val call = apiService.loginWithToken("Bearer "+ token)
        call.enqueue(object : Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {
                navController.navigate("userpage")
                onLogin()

            }
            override fun onFailure(call: Call<User>, t: Throwable) {
                println(t)
            }

        })
    }
    fun getnotes(token: String, skip:Int, onDone: (List<Notes>?)->Unit){
        val call = apiService.getnotes(skip = skip, limit = 10, "Bearer "+ token)
        call.enqueue(object : Callback<List<Notes>>{
            override fun onResponse(call: Call<List<Notes>>, response: Response<List<Notes>>) {
                println(response.body())
                if (response.body()!=null) onDone(response.body())
            }
            override fun onFailure(call: Call<List<Notes>>, t: Throwable) {
                println(1)
            }

        })
    }
    fun sendnotes(title: String, body: String, token: String, onDone: (ApiResponse)->Unit){
        val call = apiService.sendnotes(title, body, "Bearer "+ token)
        call.enqueue(object : Callback<ApiResponse>{
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                println(response.body())
                if (response.body()!=null) onDone(response.body()!!)
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                println(1)
            }

        })

    }
    fun editNotes(id: Int, title: String, body: String, token: String, onDone: (ApiResponse)->Unit){
        val call = apiService.editnotes(id, title, body, "Bearer "+ token)
        call.enqueue(object : Callback<ApiResponse>{
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                println(response.body())

                if (response.body()!=null) onDone(response.body()!!)
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                println(1)
            }

        })
    }
    fun deleteNote(id: Int, token: String, onDone: (ApiResponse?) -> Unit) {
        val call = apiService.deleteNote(id, "Bearer $token")
        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    onDone(response.body())
                } else {
                    onDone(null)
                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                println("Delete failed: ${t.message}")
                onDone(null)
            }
        })
    }

}