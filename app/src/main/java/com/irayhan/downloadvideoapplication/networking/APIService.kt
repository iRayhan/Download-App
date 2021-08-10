package com.irayhan.downloadvideoapplication.networking

import com.irayhan.downloadvideoapplication.core.AppConstants
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming

interface APIService {

    @GET(AppConstants.ENDPOINT_LINK)
    @Streaming
    suspend fun getVideo(): Response<ResponseBody>
}