import 'dart:convert';

import 'package:shared_preferences/shared_preferences.dart';

class StudentProfile {
  const StudentProfile({
    required this.id,
    required this.name,
    required this.email,
    this.goal,
  });

  final String id;
  final String name;
  final String email;
  final String? goal;

  Map<String, String?> toJson() => {
        'id': id,
        'name': name,
        'email': email,
        'goal': goal,
      };

  factory StudentProfile.fromJson(Map<String, dynamic> json) => StudentProfile(
        id: json['id']?.toString() ?? '',
        name: json['name']?.toString() ?? '',
        email: json['email']?.toString() ?? '',
        goal: json['goal']?.toString(),
      );
}

class ProfessionalWorkspaceService {
  static const _roleKey = 'fitness_account_role';
  static const _studentsKey = 'fitness_students';

  static Future<void> saveRole(String role) async =>
      (await SharedPreferences.getInstance()).setString(_roleKey, role);

  static Future<bool> isProfessional() async {
    final role = (await SharedPreferences.getInstance()).getString(_roleKey);
    return role == 'personal_trainer' || role == 'nutritionist';
  }

  static Future<List<StudentProfile>> students() async {
    final saved =
        (await SharedPreferences.getInstance()).getString(_studentsKey);
    if (saved == null) return [];
    try {
      final data = jsonDecode(saved) as List<dynamic>;
      return data
          .map((item) => StudentProfile.fromJson(item as Map<String, dynamic>))
          .toList();
    } catch (_) {
      return [];
    }
  }

  static Future<void> addStudent(StudentProfile student) async {
    final current = await students();
    current.add(student);
    await (await SharedPreferences.getInstance()).setString(
      _studentsKey,
      jsonEncode(current.map((student) => student.toJson()).toList()),
    );
  }
}
