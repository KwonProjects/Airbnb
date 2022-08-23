package co.kr.kwon.airbnb.data.api

import co.kr.kwon.airbnb.data.model.HouseDto
import retrofit2.Call
import retrofit2.http.GET

interface HouseService {
    @GET("/v3/ebbf94c5-acc0-4a5a-8a7a-874556c06985")
    fun getHouseList():Call<HouseDto>
}