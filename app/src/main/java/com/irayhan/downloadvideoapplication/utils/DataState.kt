package com.irayhan.downloadvideoapplication.utils

import okhttp3.ResponseBody

sealed class DataState<out T> {

    data class Success<out T>(val value: T) : DataState<T>()
    data class Error(
        val isNetworkError: Boolean,
        val errorCode: Int?,
        val errorBody: ResponseBody?
    ) : DataState<Nothing>()
    object Loading : DataState<Nothing>()

}