enum AsyncStatus { initial, loading, success, failure }

class AsyncState {
  const AsyncState._(this.status, {this.message});

  const AsyncState.initial() : this._(AsyncStatus.initial);
  const AsyncState.loading() : this._(AsyncStatus.loading);
  const AsyncState.success() : this._(AsyncStatus.success);
  const AsyncState.failure(String message)
      : this._(AsyncStatus.failure, message: message);

  final AsyncStatus status;
  final String? message;

  bool get isLoading => status == AsyncStatus.loading;
  bool get hasError => status == AsyncStatus.failure;
}
