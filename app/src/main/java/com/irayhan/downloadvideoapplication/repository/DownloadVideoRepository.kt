package com.irayhan.downloadvideoapplication.repository

import com.irayhan.downloadvideoapplication.base.BaseRepository
import com.irayhan.downloadvideoapplication.networking.APIService

class DownloadVideoRepository(private val api: APIService) : BaseRepository() {

    suspend fun getVideo() = safeApiCall {
        api.getVideo()
    }

}