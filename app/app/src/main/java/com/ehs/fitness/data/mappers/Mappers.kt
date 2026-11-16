package com.ehs.fitness.data.mappers

import com.ehs.fitness.data.model.Meal
import com.ehs.fitness.data.model.MealFood
import com.ehs.fitness.data.model.WeekDay
import com.ehs.fitness.data.model.Workout
import com.ehs.fitness.data.model.WorkoutExercise
import com.ehs.fitness.data.remote.ExerciseDto
import com.ehs.fitness.data.remote.MealDto
import com.ehs.fitness.data.remote.MealFoodDto
import com.ehs.fitness.data.remote.WorkoutDto
import com.ehs.fitness.data.remote.WorkoutWeekDayDto

object Mappers {
    fun workoutExercise(dto: ExerciseDto): WorkoutExercise? {
        val id = dto.id ?: return null
        return WorkoutExercise(
            exerciseId = id,
            name = dto.name.orEmpty().ifBlank { "Exercício $id" },
            bodyPart = dto.bodyPart.orEmpty(),
            series = dto.series ?: 3,
            repetitions = dto.repetitions ?: 12,
            load = dto.load?.takeIf { it > 0.0 },
            restSeconds = dto.restSeconds?.takeIf { it > 0 },
            notes = dto.notes.orEmpty(),
        )
    }

    fun workout(dto: WorkoutDto): Workout? {
        val id = dto.id ?: return null
        return Workout(
            id = id,
            name = dto.name.orEmpty().ifBlank { "Treino $id" },
            dayOfWeek = dto.dayOfWeek ?: 0,
            exercises = dto.exercises.orEmpty().mapNotNull(::workoutExercise),
        )
    }

    fun weekDay(dto: WorkoutWeekDayDto): WeekDay = WeekDay(
        dayOfWeek = dto.dayOfWeek,
        workoutId = dto.workoutId,
        name = dto.name.orEmpty(),
    )

    fun mealFood(dto: MealFoodDto): MealFood? {
        val id = dto.foodId ?: dto.tagId ?: dto.food?.id ?: return null
        val name = dto.foodName ?: dto.name ?: dto.food?.name ?: "Alimento"
        return MealFood(
            foodId = id,
            name = name,
            quantity = dto.quantity ?: 0,
            observation = dto.observation ?: dto.obs.orEmpty(),
        )
    }

    fun meal(dto: MealDto): Meal? {
        val id = dto.id ?: return null
        return Meal(
            id = id,
            name = dto.name.orEmpty().ifBlank { "Refeição $id" },
            hour = dto.hour.orEmpty(),
            foods = dto.foods.orEmpty().mapNotNull(::mealFood),
        )
    }
}
