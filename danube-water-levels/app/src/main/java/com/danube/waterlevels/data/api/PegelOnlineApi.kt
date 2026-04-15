package com.danube.waterlevels.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service for the PEGELONLINE REST API v2.
 * Documentation: https://www.pegelonline.wsv.de/webservices/rest-api/v2/
 */
interface PegelOnlineApi {

    @GET("stations.json")
    suspend fun getStations(
        @Query("waters") waters: String = "DONAU"
    ): List<StationResponse>

    @GET("stations/{uuid}/W/currentmeasurement.json")
    suspend fun getCurrentMeasurement(
        @Path("uuid") stationUuid: String
    ): MeasurementResponse

    @GET("stations/{uuid}/W/measurements.json")
    suspend fun getMeasurements(
        @Path("uuid") stationUuid: String,
        @Query("start") start: String = "P7D"
    ): List<MeasurementResponse>

    @GET("stations/{uuid}/W.json")
    suspend fun getWaterInfo(
        @Path("uuid") stationUuid: String
    ): WaterInfoResponse

    companion object {
        const val BASE_URL = "https://www.pegelonline.wsv.de/webservices/rest-api/v2/"
    }
}

data class StationResponse(
    val uuid: String,
    val number: String,
    val shortname: String,
    val longname: String,
    val km: Double,
    val agency: String,
    val longitude: Double?,
    val latitude: Double?,
    val water: WaterResponse
)

data class WaterResponse(
    val shortname: String,
    val longname: String
)

data class MeasurementResponse(
    val timestamp: String,
    val value: Double,
    val stateMnwMhw: String?,
    val stateNswHsw: String?
)

data class WaterInfoResponse(
    val shortname: String,
    val longname: String,
    val unit: String,
    val equidistance: Int?,
    val gaugeZero: GaugeZeroResponse?
)

data class GaugeZeroResponse(
    val unit: String,
    val value: Double,
    val validFrom: String?
)
