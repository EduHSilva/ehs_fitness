import 'package:flutter/foundation.dart';

import '../config/app_config.dart';
import '../models/health/diet_model.dart';
import '../services/diet_service.dart';
import 'base_view_model.dart';

class DietViewModel extends BaseViewModel {
  DietViewModel({DietService? dietService})
      : _dietService = dietService ?? DietService();

  final DietService _dietService;
  final foods = ValueNotifier<List<Food>>([]);
  final meals = ValueNotifier<List<Meal>>([]);

  Future<void> fetchFoods(search) async {
    foods.value = [];
    final response = await execute(
      () => _dietService.fetchFoods(search),
      failureMessage: 'Error fetching foods',
    );
    if (response != null) {
      foods.value = response;
    }
  }

  Future<MealResponse?> deleteMeal(int id) async {
    try {
      isLoading.value = true;
      MealResponse? response = await _dietService.deleteMeal(id);
      if (response?.meal != null) {
        await fetchMeals();
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

  Future<void> fetchMeals() async {
    meals.value = [];
    final response = await execute(
      _dietService.fetchMeals,
      failureMessage: 'Error fetching meals',
    );
    if (response != null) {
      meals.value = response;
    }
  }

  Future<MealResponse?> getMeal(int id) async {
    try {
      isLoading.value = true;

      MealResponse? response = await _dietService.getMeal(id);

      if (response.meal == null) {
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

  Future<MealResponse?> addMeal(CreateMealRequest request) async {
    try {
      isLoading.value = true;
      MealResponse? response = await _dietService.addMeal(request);
      if (response?.meal != null) {
        await fetchMeals();
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

  Future<MealResponse?> editMeal(int id, UpdateMealRequest request) async {
    try {
      isLoading.value = true;
      MealResponse? response = await _dietService.editMeal(id, request);
      if (response?.meal != null) {
        await fetchMeals();
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
