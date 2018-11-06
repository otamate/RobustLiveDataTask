package com.otamate.android.robustlivedatatask

import android.app.Application
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlin.concurrent.thread

class MainViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "MainViewModel"
        const val ITERATIONS = 100
        private const val SPEED = 50L
    }

    data class ViewStateData (
        var isBegun: Boolean = false,
        var isInProgress: Boolean = false,
        var isFinished: Boolean = false
    )

    data class ProgressData (
        var progress: Int = 0
    )

    private val liveDataViewStateData: MutableLiveData<ViewStateData> = MutableLiveData()
    private val liveDataProgressData: MutableLiveData<ProgressData> = MutableLiveData()

    init {
        liveDataViewStateData.value = ViewStateData()
        liveDataProgressData.value = ProgressData()
    }

    // Need to expose LiveData in order for it to be bound to an observer in Activity
    fun getViewStateLiveData(): LiveData<ViewStateData> {
       return liveDataViewStateData
    }

    // Need to expose LiveData in order for it to be bound to an observer in Activity
    fun getProgressLiveData(): LiveData<ProgressData> {
        return liveDataProgressData
    }

    // Local convenience wrapper
    fun getViewStateData(): ViewStateData {
        return liveDataViewStateData.value!!
    }

    // Local convenience wrapper
    fun getProgressData(): ProgressData {
        return liveDataProgressData.value!!
    }

    // Set the ViewState
    fun setViewStateData(newViewStateData: ViewStateData) {
        Log.d(TAG, "setViewStateData: " + newViewStateData)

        if (!getViewStateData().isBegun && newViewStateData.isInProgress) {
            newViewStateData.isBegun = true
        }

        if (!getViewStateData().isInProgress && newViewStateData.isInProgress) {
            start()
        }
        liveDataViewStateData.value = newViewStateData
    }

    fun start() {

        // Do the actual work in a thread for performance
        thread(start = true) {
            var intent = Intent()
            val app: Application = getApplication()

            Log.d(TAG, "Starting")

            intent.action = MainActivity.SHOW_STATUS_BAR_ICON
            LocalBroadcastManager.getInstance(app).sendBroadcast(intent)

            while (getProgressData().progress < ITERATIONS) {
                liveDataProgressData.postValue(getProgressData().copy(progress = getProgressData().progress + 1))
                Thread.sleep(SPEED)
            }

            intent = Intent()
            intent.action = MainActivity.HIDE_STATUS_BAR_ICON
            LocalBroadcastManager.getInstance(app).sendBroadcast(intent)

            liveDataViewStateData.postValue(getViewStateData().copy(isFinished = true, isInProgress = false))

            Log.d(TAG, "Done")
        }
    }
}
