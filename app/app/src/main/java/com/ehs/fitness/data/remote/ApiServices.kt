package com.ehs.fitness.data.remote

import com.ehs.fitness.core.network.Envelope
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {
    @POST("users/auth/login")
    suspend fun login(@Body body: LoginRequest): Envelope<LoginDto>

    @GET("users/auth/me")
    suspend fun me(): Envelope<LoginDto>

    @POST("users/auth/register")
    suspend fun register(@Body body: RegisterRequest): Envelope<RegisterDto>

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body body: UpdateUserRequest): Envelope<RegisterDto>
}

interface WorkoutApi {
    @GET("fitness/workouts")
    suspend fun list(): Envelope<List<WorkoutDto>>

    @GET("fitness/workouts/week")
    suspend fun week(): Envelope<List<WorkoutWeekDayDto>>

    @PUT("fitness/workout/load")
    suspend fun updateLoad(@Body body: UpdateWorkoutLoadRequest): Envelope<WorkoutDto>
}

interface DietApi {
    @GET("fitness/diet/meals")
    suspend fun meals(): Envelope<List<MealDto>>
}

interface StudentApi {
    @GET("fitness/students/links")
    suspend fun links(): Envelope<List<MyLinkDto>>
}

interface TrackingApi {
    @GET("fitness/config/user")
    suspend fun config(): Envelope<UserConfigDto>

    @GET("fitness/config/history")
    suspend fun today(): Envelope<HistoryDto>

    @PUT("fitness/config/history")
    suspend fun updateToday(@Body body: UpdateHistoryRequest): Envelope<HistoryDto>
}
