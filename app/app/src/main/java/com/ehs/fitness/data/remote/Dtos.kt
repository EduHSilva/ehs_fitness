package com.ehs.fitness.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// Autenticação (serviço users)
// ---------------------------------------------------------------------------

@Serializable
data class LoginRequest(
    val email: String? = null,
    val password: String? = null,
    val tokenGmail: String? = null,
    val app: String,
)

@Serializable
data class LoginDto(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val token: String? = null,
    val roles: List<String> = emptyList(),
    val apps: List<String> = emptyList(),
)

@Serializable
data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String,
    val role: String,
)

@Serializable
data class RegisterDto(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val status: String = "",
    val roles: List<String> = emptyList(),
)

@Serializable
data class UpdateUserRequest(val username: String)

// ---------------------------------------------------------------------------
// Treinos (serviço fitness)
// ---------------------------------------------------------------------------

@Serializable
data class ExerciseDto(
    val id: Int? = null,
    val name: String? = null,
    @SerialName("body_part") val bodyPart: String? = null,
    val target: String? = null,
    val instructions: String? = null,
    val load: Double? = null,
    val series: Int? = null,
    val repetitions: Int? = null,
    @SerialName("rest_seconds") val restSeconds: Int? = null,
    val notes: String? = null,
)

@Serializable
data class WorkoutDto(
    val id: Int? = null,
    val name: String? = null,
    @SerialName("dayOfWeek") val dayOfWeek: Int? = null,
    val exercises: List<ExerciseDto>? = null,
)

@Serializable
data class WorkoutWeekDayDto(
    val dayOfWeek: Int = 0,
    val workoutId: Int? = null,
    val name: String? = null,
)

// ---------------------------------------------------------------------------
// Alimentação (serviço fitness)
// ---------------------------------------------------------------------------

@Serializable
data class FoodDto(
    val id: Int? = null,
    val name: String? = null,
)

@Serializable
data class MealFoodDto(
    @SerialName("tag_id") val tagId: Int? = null,
    @SerialName("food_id") val foodId: Int? = null,
    @SerialName("food_name") val foodName: String? = null,
    val name: String? = null,
    val food: FoodDto? = null,
    val quantity: Int? = null,
    val observation: String? = null,
    val obs: String? = null,
)

@Serializable
data class MealDto(
    val id: Int? = null,
    val name: String? = null,
    val hour: String? = null,
    val foods: List<MealFoodDto>? = null,
)
