import 'package:flutter/material.dart';

import '../../../services/professional_workspace_service.dart';
import '../health_view.dart';

class StudentsTab extends StatefulWidget {
  const StudentsTab({super.key});

  @override
  State<StudentsTab> createState() => _StudentsTabState();
}

class _StudentsTabState extends State<StudentsTab> {
  final _nameController = TextEditingController();
  final _emailController = TextEditingController();
  final _goalController = TextEditingController();
  List<StudentProfile> _students = [];

  @override
  void initState() {
    super.initState();
    _loadStudents();
  }

  @override
  void dispose() {
    _nameController.dispose();
    _emailController.dispose();
    _goalController.dispose();
    super.dispose();
  }

  Future<void> _loadStudents() async {
    final students = await ProfessionalWorkspaceService.students();
    if (mounted) setState(() => _students = students);
  }

  Future<void> _addStudent() async {
    if (_nameController.text.trim().isEmpty ||
        _emailController.text.trim().isEmpty) {
      return;
    }
    await ProfessionalWorkspaceService.addStudent(StudentProfile(
      id: DateTime.now().microsecondsSinceEpoch.toString(),
      name: _nameController.text.trim(),
      email: _emailController.text.trim(),
      goal: _goalController.text.trim().isEmpty
          ? null
          : _goalController.text.trim(),
    ));
    _nameController.clear();
    _emailController.clear();
    _goalController.clear();
    await _loadStudents();
  }

  void _manage(StudentProfile student, int index) {
    Navigator.of(context).push(MaterialPageRoute(
      builder: (_) => HealthView(initialIndex: index, student: student),
    ));
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: const Text('Alunos')),
        body: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            const Text('Cadastrar aluno',
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
            const SizedBox(height: 12),
            TextField(
                controller: _nameController,
                decoration: const InputDecoration(labelText: 'Nome')),
            const SizedBox(height: 10),
            TextField(
                controller: _emailController,
                decoration: const InputDecoration(labelText: 'E-mail')),
            const SizedBox(height: 10),
            TextField(
                controller: _goalController,
                decoration:
                    const InputDecoration(labelText: 'Objetivo (opcional)')),
            const SizedBox(height: 12),
            FilledButton.icon(
                onPressed: _addStudent,
                icon: const Icon(Icons.person_add),
                label: const Text('Cadastrar aluno')),
            const SizedBox(height: 24),
            const Text('Seus alunos',
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
            ..._students.map((student) => Card(
                  child: Padding(
                    padding: const EdgeInsets.all(12),
                    child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(student.name,
                              style:
                                  const TextStyle(fontWeight: FontWeight.bold)),
                          Text(student.email),
                          if (student.goal != null) Text(student.goal!),
                          const SizedBox(height: 8),
                          Wrap(spacing: 8, children: [
                            OutlinedButton.icon(
                                onPressed: () => _manage(student, 2),
                                icon: const Icon(Icons.fitness_center),
                                label: const Text('Treino')),
                            OutlinedButton.icon(
                                onPressed: () => _manage(student, 1),
                                icon: const Icon(Icons.restaurant),
                                label: const Text('Dieta')),
                            OutlinedButton.icon(
                                onPressed: () => _manage(student, 3),
                                icon: const Icon(Icons.checklist),
                                label: const Text('Hábitos')),
                          ]),
                        ]),
                  ),
                )),
          ],
        ),
      );
}
