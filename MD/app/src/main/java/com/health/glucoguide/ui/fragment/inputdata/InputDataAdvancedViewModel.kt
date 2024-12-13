package com.health.glucoguide.ui.fragment.inputdata

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.health.glucoguide.data.UserRepository
import com.health.glucoguide.data.remote.request.UserData
import com.health.glucoguide.data.remote.response.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InputDataAdvancedViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    fun getSession(): LiveData<UserSession> {
        return repository.getSession().asLiveData()
    }

    fun sendUserData(userData: UserData) = repository.setUserPredictData(userData)
}

