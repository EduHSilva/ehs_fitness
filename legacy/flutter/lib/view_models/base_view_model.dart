import 'package:flutter/foundation.dart';

import '../config/app_config.dart';
import '../config/async_state.dart';

abstract class BaseViewModel {
  final state = ValueNotifier<AsyncState>(const AsyncState.initial());
  final isLoading = ValueNotifier(false);
  final errorMessage = ValueNotifier<String?>(null);

  Future<T?> execute<T>(
    Future<T> Function() operation, {
    required String failureMessage,
  }) async {
    errorMessage.value = null;
    isLoading.value = true;
    state.value = const AsyncState.loading();
    try {
      final result = await operation();
      state.value = const AsyncState.success();
      return result;
    } catch (error, stackTrace) {
      errorMessage.value = failureMessage;
      state.value = AsyncState.failure(failureMessage);
      AppConfig.getLogger().e(error, stackTrace: stackTrace);
      return null;
    } finally {
      isLoading.value = false;
    }
  }

  void dispose() {
    state.dispose();
    isLoading.dispose();
    errorMessage.dispose();
  }
}
