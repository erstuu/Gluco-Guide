package com.health.glucoguide.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.google.gson.Gson
import com.health.glucoguide.R
import com.health.glucoguide.data.local.HistoryDao
import com.health.glucoguide.data.local.UserPreference
import com.health.glucoguide.data.remote.ApiService
import com.health.glucoguide.data.remote.PredictApiService
import com.health.glucoguide.data.remote.request.UserData
import com.health.glucoguide.data.remote.request.UserInputProfile
import com.health.glucoguide.data.remote.request.UserLoginRequest
import com.health.glucoguide.data.remote.response.UserLoginResponse
import com.health.glucoguide.data.remote.response.UserProfileResponse
import com.health.glucoguide.data.remote.request.UserRegisterRequest
import com.health.glucoguide.data.remote.response.User
import com.health.glucoguide.data.remote.response.UserRegisterResponse
import com.health.glucoguide.data.remote.response.UserSession
import com.health.glucoguide.models.HistoryEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val predictApiService: PredictApiService,
    private val historyDao: HistoryDao,
    private val userPreference: UserPreference,
    @ApplicationContext private val context: Context
) {
    suspend fun saveSession(user: UserSession) {
        userPreference.saveSession(user)
    }

    fun getSession() = userPreference.getSession()

    suspend fun clearSession() {
        userPreference.clearSession()
    }

    suspend fun saveLanguage(language: String) {
        userPreference.saveLanguage(language)
    }

    fun getLanguage(): LiveData<String> {
        return userPreference.getLanguageSetting().asLiveData()
    }

    fun getHistories(token: String): LiveData<ResultState<List<HistoryEntity>>> = liveData {
        emit(ResultState.Loading)
        try {
            val response = apiService.getUserHistories("Bearer $token")
            val histories = response.histories

            val listHistory = histories.map{
                HistoryEntity(
                    diabetesCategory = it.diabetesCategory,
                    hbA1cLevel = it.hbA1cLevel,
                    checkDate = it.checkDate,
                    bloodGlucoseLevel = it.bloodGlucoseLevel,
                )
            }
            historyDao.deleteHistories()
            historyDao.insertHistories(listHistory)
            emit(ResultState.Success(listHistory))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, UserProfileResponse::class.java)
            val errorMessage = errorBody.message
            emit(ResultState.Error(errorMessage.toString()))
        } catch (e: ConnectException) {
            emit(ResultState.Error(context.getString(R.string.network_connection_error)))
        } catch (e: SocketTimeoutException) {
            emit(ResultState.Error(context.getString(R.string.connection_timeout)))
        } catch (e: IOException) {
            emit(ResultState.Error(context.getString(R.string.http_error_default_message)))
        } catch (e: Exception) {
            emit(ResultState.Error(context.getString(R.string.an_unexpected_error_occurred)))
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
                emit(ResultState.Error(context.getString(R.string.an_unexpected_error_occurred)))
            }
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, UserProfileResponse::class.java)
            val errorMessage = errorBody.message
            emit(ResultState.Error(errorMessage.toString()))
        } catch (e: ConnectException) {
            emit(ResultState.Error(context.getString(R.string.network_connection_error)))
        } catch (e: SocketTimeoutException) {
            emit(ResultState.Error(context.getString(R.string.connection_timeout)))
        } catch (e: IOException) {
            emit(ResultState.Error(context.getString(R.string.http_error_default_message)))
        } catch (e: Exception) {
            emit(ResultState.Error(context.getString(R.string.an_unexpected_error_occurred)))
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
            emit(ResultState.Error(context.getString(R.string.network_connection_error)))
        } catch (e: IOException) {
            emit(ResultState.Error(context.getString(R.string.http_error_default_message)))
        } catch (e: Exception) {
            emit(ResultState.Error(context.getString(R.string.an_unexpected_error_occurred)))
        }
    }

    fun setUserPredictData(userData: UserData) = liveData {
        emit(ResultState.Loading)
        try {
            val successResponse = predictApiService.postUserPredict(userData)
            emit(ResultState.Success(successResponse))
        } catch (e: SocketTimeoutException) {
            emit(ResultState.Error(context.getString(R.string.connection_timeout)))
        } catch (e: ConnectException) {
            emit(ResultState.Error(context.getString(R.string.network_connection_error)))
        } catch (e: IOException) {
            emit(ResultState.Error(context.getString(R.string.http_error_default_message)))
        } catch (e: Exception) {
            emit(ResultState.Error(context.getString(R.string.an_unexpected_error_occurred)))
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
        } catch (e: SocketTimeoutException) {
            emit(ResultState.Error(context.getString(R.string.connection_timeout)))
        } catch (e: ConnectException) {
            emit(ResultState.Error(context.getString(R.string.network_connection_error)))
        } catch (e: IOException) {
            emit(ResultState.Error(context.getString(R.string.http_error_default_message)))
        } catch (e: Exception) {
            emit(ResultState.Error(context.getString(R.string.an_unexpected_error_occurred)))
        }
    }

    fun loginUser(request: UserLoginRequest) = liveData {
        emit(ResultState.Loading)
        try {
            val successResponse = apiService.postUserLogin(request)
            emit(ResultState.Success(successResponse))
        } catch (e: ConnectException) {
            emit(ResultState.Error(context.getString(R.string.network_connection_error)))
        } catch (e: SocketTimeoutException) {
            emit(ResultState.Error(context.getString(R.string.connection_timeout)))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, UserLoginResponse::class.java)
            val errorMessage = errorBody.message
            emit(ResultState.Error(errorMessage.toString()))
        } catch (e: ConnectException) {
            emit(ResultState.Error(context.getString(R.string.network_connection_error)))
        } catch (e: IOException) {
            emit(ResultState.Error(context.getString(R.string.http_error_default_message)))
        } catch (e: Exception) {
            emit(ResultState.Error(context.getString(R.string.an_unexpected_error_occurred)))
        }
    }
}