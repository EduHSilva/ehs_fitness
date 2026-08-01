sealed class ServiceResult<T> {
  const ServiceResult();
}

class ServiceSuccess<T> extends ServiceResult<T> {
  const ServiceSuccess(this.data);

  final T data;
}

class ServiceFailure<T> extends ServiceResult<T> {
  const ServiceFailure(this.message, {this.statusCode});

  final String message;
  final int? statusCode;
}
