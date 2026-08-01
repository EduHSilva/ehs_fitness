import 'package:flutter/foundation.dart';

import '../config/app_config.dart';
import '../models/health/workout_model.dart';
import '../services/workout_service.dart';
import 'base_view_model.dart';

class WorkoutViewModel extends BaseViewModel {
  WorkoutViewModel({WorkoutService? workoutService})
      : _workoutService = workoutService ?? WorkoutService();

  final WorkoutService _workoutService;
  final exercises = ValueNotifier<List<Exercise>>([]);
  final workouts = ValueNotifier<List<Workout>>([]);

  Future<void> fetchExercises() async {
    exercises.value = [];
    final response = await execute(
      _workoutService.fetchExercises,
      failureMessage: 'Error fetching exercises',
    );
    if (response != null) {
      exercises.value = response;
    }
  }

  Future<WorkoutResponse?> deleteWorkout(int id) async {
    try {
      isLoading.value = true;
      WorkoutResponse? response = await _workoutService.deleteWorkout(id);
      if (response?.workout != null) {
        await fetchWorkouts();
      } else {
        errorMessage.value = response?.message;
      }
      return response;
    } catch (e) {
      AppConfig.getLogger().e(e);
    } finally {
      isLoading.value = false;
    }
    return null;
  }

  Future<void> fetchWorkouts() async {
    workouts.value = [];
    final response = await execute(
      _workoutService.fetchWorkouts,
      failureMessage: 'Error fetching workouts',
    );
    if (response != null) {
      workouts.value = response;
    }
  }

  Future<WorkoutResponse?> getWorkout(int id) async {
    try {
      isLoading.value = true;

      WorkoutResponse? response = await _workoutService.getWorkout(id);

      if (response.workout == null) {
        errorMessage.value = response.message;
      }

      return response;
    } catch (e) {
      AppConfig.getLogger().e(e);
    } finally {
      isLoading.value = false;
    }
    return null;
  }

  Future<WorkoutResponse?> addWorkout(CreateWorkoutRequest request) async {
    try {
      isLoading.value = true;
      WorkoutResponse? response = await _workoutService.addWorkout(request);
      if (response?.workout != null) {
        await fetchWorkouts();
      } else {
        errorMessage.value = errorMessage.value = response?.message;
      }
      return response;
    } catch (e) {
      AppConfig.getLogger().e(e);
    } finally {
      isLoading.value = false;
    }
    return null;
  }

  Future<WorkoutResponse?> editWorkout(
      int id, UpdateWorkoutRequest request) async {
    try {
      isLoading.value = true;
      WorkoutResponse? response =
          await _workoutService.editWorkout(id, request);
      if (response?.workout != null) {
        await fetchWorkouts();
      } else {
        errorMessage.value = errorMessage.value = response?.message;
      }
      return response;
    } catch (e) {
      AppConfig.getLogger().e(e);
    } finally {
      isLoading.value = false;
    }
    return null;
  }
}
