import 'package:flutter/material.dart';

import 'fitness_home_tab.dart';
import 'tabs/diet_tab.dart';
import 'tabs/habits_tab.dart';
import 'tabs/students_tab.dart';
import 'tabs/workout_tab.dart';
import '../../services/professional_workspace_service.dart';

class HealthView extends StatefulWidget {
  final int initialIndex;
  final StudentProfile? student;

  const HealthView({super.key, this.initialIndex = 0, this.student});

  @override
  HealthViewState createState() => HealthViewState();
}

class HealthViewState extends State<HealthView> {
  late int _selectedIndex;
  bool _isProfessional = false;

  @override
  void initState() {
    super.initState();
    _selectedIndex = widget.initialIndex;
    _loadRole();
  }

  Future<void> _loadRole() async {
    final isProfessional = await ProfessionalWorkspaceService.isProfessional();
    if (mounted) setState(() => _isProfessional = isProfessional);
  }

  @override
  Widget build(BuildContext context) {
    final pages = [
      const FitnessHomeTab(),
      const DietTab(),
      const WorkoutTab(),
      const HabitsTab(),
      const StudentsTab()
    ];
    final destinations = [
      const NavigationDestination(
          icon: Icon(Icons.home_outlined),
          selectedIcon: Icon(Icons.home),
          label: 'Home'),
      const NavigationDestination(
          icon: Icon(Icons.restaurant_outlined),
          selectedIcon: Icon(Icons.restaurant),
          label: 'Diet'),
      const NavigationDestination(
          icon: Icon(Icons.fitness_center_outlined),
          selectedIcon: Icon(Icons.fitness_center),
          label: 'Workouts'),
      const NavigationDestination(
          icon: Icon(Icons.checklist_outlined),
          selectedIcon: Icon(Icons.checklist),
          label: 'Habits'),
      if (_isProfessional)
        const NavigationDestination(
            icon: Icon(Icons.people_outline),
            selectedIcon: Icon(Icons.people),
            label: 'Alunos'),
    ];

    return Scaffold(
      body: Column(children: [
        if (widget.student != null)
          MaterialBanner(
            content: Text(
                'Gerenciando ${widget.student!.name}. O envio para o aluno será integrado à API.'),
            leading: const Icon(Icons.person),
            actions: [
              TextButton(
                  onPressed: () => Navigator.of(context).pop(),
                  child: const Text('Voltar'))
            ],
          ),
        Expanded(
            child: IndexedStack(
                index: _selectedIndex,
                children: pages.take(destinations.length).toList())),
      ]),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _selectedIndex,
        onDestinationSelected: (index) =>
            setState(() => _selectedIndex = index),
        destinations: destinations,
      ),
    );
  }
}
