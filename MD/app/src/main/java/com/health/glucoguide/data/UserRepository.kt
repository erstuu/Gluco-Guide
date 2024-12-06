package com.health.glucoguide.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.google.gson.Gson
import com.health.glucoguide.R
import com.health.glucoguide.data.local.HistoryDao
import com.health.glucoguide.data.local.UserPreference
import com.health.glucoguide.data.remote.ApiService
import com.health.glucoguide.data.remote.request.UserInputProfile
import com.health.glucoguide.data.remote.request.UserLoginRequest
import com.health.glucoguide.data.remote.response.UserLoginResponse
import com.health.glucoguide.data.remote.response.UserProfileResponse
import com.health.glucoguide.data.remote.request.UserRegisterRequest
import com.health.glucoguide.data.remote.response.User
import com.health.glucoguide.data.remote.response.UserHistoriesResponse
import com.health.glucoguide.data.remote.response.UserRegisterResponse
import com.health.glucoguide.data.remote.response.UserSession
import com.health.glucoguide.models.HistoryEntity
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val historyDao: HistoryDao,
    private val userPreference: UserPreference
) {
    suspend fun saveSession(user: UserSession) {
        userPreference.saveSession(user)
    }

    fun getSession() = userPreference.getSession()

    suspend fun clearSession() {
        userPreference.clearSession()
    }

    fun getHistories(token: String): LiveData<ResultState<List<HistoryEntity>>> = liveData {
        emit(ResultState.Loading)
        try {
            val response = apiService.getUserHistories("Bearer $token")
            val histories = response.histories

            val listHistory = histories.map{
                HistoryEntity(
                    diagnosa = it.diagnosa,
                    tanggalCek = it.tanggalCek,
                    keluhan = it.keluhan
                )
            }
            historyDao.insertHistories(listHistory)
            emit(ResultState.Success(listHistory))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, UserHistoriesResponse::class.java)
            val errorMessage = errorBody.error
            emit(ResultState.Error(errorMessage.toString()))
        } catch (e: ConnectException) {
            emit(ResultState.Error(R.string.network_connection_error.toString()))
        }
        catch (e: IOException) {
            emit(ResultState.Error(R.string.network_connection_error.toString()))
        } catch (e: Exception) {
            emit(ResultState.Error(R.string.an_unexpected_error_occurred.toString()))
        }
        val localData: LiveData<ResultState<List<HistoryEntity>>> = historyDao.getHistories().map {
            ResultState.Success(it)
        }
        emitSource(localData)
    }

    fun getUserData(token: String): LiveData<ResultState<User>> = liveData {
        emit(ResultState.Loading)

        try {
            val response = apiService.getUserProfile("Bearer $token")
            val remoteData = response.user

            if (remoteData != null) {
                val userData = User(remoteData.name)

                userPreference.saveUser(userData)

                emit(ResultState.Success(userData))
            } else {
                emit(ResultState.Error(R.string.network_connection_error.toString()))
            }
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, UserProfileResponse::class.java)
            val errorMessage = errorBody.message
            emit(ResultState.Error(errorMessage.toString()))
        } catch (e: ConnectException) {
            emit(ResultState.Error(R.string.network_connection_error.toString()))
        }
        catch (e: IOException) {
            emit(ResultState.Error(R.string.network_connection_error.toString()))
        } catch (e: Exception) {
            emit(ResultState.Error(R.string.an_unexpected_error_occurred.toString()))
        }
        val localData: LiveData<ResultState<User>> = userPreference.getUser().map {
            ResultState.Success(it)
        }.asLiveData()

        emitSource(localData)
    }

    fun setUserData(token: String, userData: UserInputProfile) = liveData {
        emit(ResultState.Loading)
        try {
            val successResponse = apiService.putUserProfile("Bearer $token", userData)
            emit(ResultState.Success(successResponse))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, UserRegisterResponse::class.java)
            val errorMessage = errorBody.message
            emit(ResultState.Error(errorMessage.toString()))
        } catch (e: ConnectException) {
            emit(ResultState.Error("Failed to connect. Please check your internet connection."))
        }
        catch (e: IOException) {
            emit(ResultState.Error(R.string.network_connection_error.toString()))
        } catch (e: Exception) {
            emit(ResultState.Error(R.string.an_unexpected_error_occurred.toString()))
        }
    }

    fun registerUser(request: UserRegisterRequest) = liveData {
        emit(ResultState.Loading)
        try {
            val successResponse = apiService.postUserRegister(request)
            emit(ResultState.Success(successResponse))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, UserRegisterResponse::class.java)
            val errorMessage = errorBody.message
            emit(ResultState.Error(errorMessage.toString()))
        } catch (e: ConnectException) {
            emit(ResultState.Error("Failed to connect. Please check your internet connection."))
        }
        catch (e: IOException) {
            emit(ResultState.Error(R.string.network_connection_error.toString()))
        } catch (e: Exception) {
            emit(ResultState.Error(R.string.an_unexpected_error_occurred.toString()))
        }
    }

    fun loginUser(request: UserLoginRequest) = liveData {
        emit(ResultState.Loading)
        try {
            val successResponse = apiService.postUserLogin(request)
            emit(ResultState.Success(successResponse))
        } catch (e: ConnectException) {
            emit(ResultState.Error("Failed to connect. Please check your internet connection."))
        }
        catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, UserLoginResponse::class.java)
            val errorMessage = errorBody.message
            emit(ResultState.Error(errorMessage.toString()))
        } catch (e: IOException) {
            emit(ResultState.Error(R.string.network_connection_error.toString()))
        } catch (e: Exception) {
            emit(ResultState.Error(R.string.an_unexpected_error_occurred.toString()))
        }
    }
}