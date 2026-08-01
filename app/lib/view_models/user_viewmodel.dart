import '../config/app_config.dart';
import '../models/user/login_model.dart';
import '../models/user/user_model.dart';
import '../services/user_service.dart';
import '../services/professional_workspace_service.dart';
import 'base_view_model.dart';

class UserViewModel extends BaseViewModel {
  UserViewModel({UserService? userService})
      : _userService = userService ?? UserService();

  final UserService _userService;

  Future<LoginResponse?> login(String email, String password) async {
    final response = await execute(
      () => _userService.login(LoginRequest(email: email, password: password)),
      failureMessage: 'Error on login',
    );
    if (response != null) {
      await AppConfig.saveToken(response.token);
      await AppConfig.saveUser(response.user);
      await _saveProfessionalRole(response.user?.accountRole);
    }
    return response;
  }

  Future<LoginResponse?> loginWithGoogle(
      String email, String googleAccountId) async {
    final response = await execute(
      () => _userService.login(
        LoginRequest(email: email, idContaGoogle: googleAccountId),
      ),
      failureMessage: 'Error on Google login',
    );
    if (response != null) {
      await AppConfig.saveToken(response.token);
      await AppConfig.saveUser(response.user);
      await _saveProfessionalRole(response.user?.accountRole);
    }
    return response;
  }

  Future<void> _saveProfessionalRole(String? role) async {
    final normalizedRole = role?.toLowerCase();
    if (normalizedRole == 'pj' ||
        normalizedRole == 'professional' ||
        normalizedRole == 'personal_trainer') {
      await ProfessionalWorkspaceService.saveRole('personal_trainer');
    } else if (normalizedRole == 'nutritionist') {
      await ProfessionalWorkspaceService.saveRole('nutritionist');
    }
  }

  Future<UserResponse?> register(
      String name, String email, String password, String accountRole) async {
    return execute<UserResponse?>(
      () => _userService.register(
        CreateUserRequest(
            name: name,
            email: email,
            password: password,
            accountRole: accountRole),
      ),
      failureMessage: 'Error on register',
    );
  }

  Future<UserResponse?> getUser(String id) async {
    final response = await execute(
      () => _userService.getUser(id),
      failureMessage: 'Error fetching user',
    );
    if (response?.user == null && response?.message != null) {
      errorMessage.value = response!.message;
    }
    return response;
  }

  Future<UserResponse?> updateUser(
      String id, String name, String email, String? photo) async {
    final response = await execute(
      () => _userService.updateUser(
        UpdateUserRequest(name: name, email: email, photo: photo),
        id,
      ),
      failureMessage: 'Error on update user',
    );
    if (response?.user != null) {
      await AppConfig.saveUser(response!.user);
    }
    return response;
  }
}
