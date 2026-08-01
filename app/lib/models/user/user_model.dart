import '../response.dart';

class User {
  final String id;
  final String name;
  final String email;
  final String lastLogin;
  final String? photo;
  final String? accountRole;

  User(
      {required this.id,
      required this.name,
      required this.email,
      required this.lastLogin,
      this.photo,
      this.accountRole});

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
        id: json['id']?.toString() ?? '',
        name: (json['username'] ?? json['name'])?.toString() ?? '',
        email: json['email']?.toString() ?? '',
        lastLogin: json['lastLogin']?.toString() ?? '',
        photo: json['photo']?.toString(),
        accountRole:
            (json['account_role'] ?? json['role'] ?? json['type'])?.toString());
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'email': email,
      'lastLogin': lastLogin,
      'photo': photo,
      'account_role': accountRole,
    };
  }
}

class UserResponse extends DefaultResponse {
  final User? user;

  UserResponse({
    this.user,
    required super.message,
  });

  factory UserResponse.fromJson(Map<String, dynamic> json) {
    final data = json['data'];
    return UserResponse(
      user: data is Map<String, dynamic> ? User.fromJson(data) : null,
      message: json['message']?.toString() ?? '',
    );
  }
}

class CreateUserRequest {
  final String name;
  final String email;
  final String password;
  final String? accountRole;

  CreateUserRequest(
      {required this.name,
      required this.email,
      required this.password,
      this.accountRole});

  Map<String, String> toJson() => {
        'name': name,
        'email': email,
        'password': password,
        if (accountRole != null) 'account_role': accountRole!
      };
}

class UpdateUserRequest {
  final String name;
  final String email;
  final String? photo;

  UpdateUserRequest({required this.name, required this.email, this.photo});

  Map<String, String?> toJson() =>
      {'name': name, 'email': email, 'photo': photo};
}
