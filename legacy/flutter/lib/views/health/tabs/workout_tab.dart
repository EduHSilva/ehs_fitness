import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';

import '../../../config/design_system.dart';
import '../../../config/app_config.dart';
import '../../../config/helper.dart';
import '../../../models/health/workout_model.dart';
import '../../../view_models/workout_viewmodel.dart';
import '../../../widgets/custom_modal_delete.dart';
import '../new_workout_view.dart';
import '../workout_details_view.dart';
import '../workout_plan_view.dart';

class WorkoutTab extends StatefulWidget {
  const WorkoutTab({super.key});

  @override
  WorkoutTabState createState() => WorkoutTabState();
}

class WorkoutTabState extends State<WorkoutTab> {
  final WorkoutViewModel _workoutViewModel = WorkoutViewModel();

  String? _weekdayLabel(Workout workout) {
    final createdAt = workout.createAt;
    if (createdAt == null || createdAt.isEmpty) return null;

    final date = DateTime.tryParse(createdAt)?.toLocal();
    if (date == null) return null;

    const weekdayKeys = [
      'monday',
      'tuesday',
      'wednesday',
      'thursday',
      'friday',
      'saturday',
      'sunday',
    ];
    return weekdayKeys[date.weekday - 1].tr();
  }

  @override
  void initState() {
    super.initState();
    _workoutViewModel.fetchWorkouts();
  }

  _deleteWorkout(int id) async {
    WorkoutResponse? response = await _workoutViewModel.deleteWorkout(id);
    _handlerResponse(response);
  }

  _deleteWorkoutDialog(Workout workout) {
    showDialog(
      context: context,
      builder: (context) {
        return CustomModalDelete(
          title: "confirmDelete",
          onConfirm: () {
            _deleteWorkout(workout.id);
          },
        );
      },
    );
  }

  _handlerResponse(WorkoutResponse? response) {
    if (response?.workout != null) {
      _workoutViewModel.fetchWorkouts();
    } else {
      if (!mounted) return;
      showErrorBar(context, response);
    }
  }

  void _showPlanActions() {
    showModalBottomSheet(
      context: context,
      builder: (context) => SafeArea(
          child: Wrap(children: [
        ListTile(
            leading: const Icon(Icons.fitness_center),
            title: const Text('Criar treino'),
            onTap: () {
              Navigator.pop(context);
              Navigator.push(context,
                  MaterialPageRoute(builder: (_) => const NewWorkoutView()));
            }),
        ListTile(
            leading: const Icon(Icons.edit_note),
            title: const Text('Criar plano manual'),
            onTap: () {
              Navigator.pop(context);
              Navigator.push(context,
                  MaterialPageRoute(builder: (_) => const WorkoutPlanView()));
            }),
        ListTile(
            leading: const Icon(Icons.auto_awesome),
            title: const Text('Criar plano por IA'),
            subtitle: const Text('Em breve: geração pelo seu objetivo.'),
            onTap: () {
              Navigator.pop(context);
              ScaffoldMessenger.of(this.context).showSnackBar(const SnackBar(
                  content:
                      Text('A geração por IA será disponibilizada pela API.')));
            }),
      ])),
    );
  }

  @override
  Widget build(BuildContext context) {
    return ValueListenableBuilder(
      valueListenable: _workoutViewModel.isLoading,
      builder: (context, isLoading, child) {
        if (isLoading) {
          return const Center(child: CircularProgressIndicator());
        }
        return ValueListenableBuilder<List<Workout>>(
          valueListenable: _workoutViewModel.workouts,
          builder: (context, workouts, child) {
            final offline = AppConfig.isOffline.value;
            return Scaffold(
              appBar: AppBar(
                title: Text('workout'.tr()),
                automaticallyImplyLeading: false,
                actions: [
                  IconButton(
                    icon: const Icon(Icons.share),
                    onPressed: () async {
                      // await generateAndShareWorkoutPDF(
                      //     _workoutViewModel.workouts.value);
                    },
                  ),
                ],
              ),
              body: Column(children: [
                if (offline)
                  const MaterialBanner(
                      content: Text(
                          'Modo offline: exibindo o último treino salvo. Alterações estão indisponíveis.'),
                      actions: []),
                Expanded(
                    child: workouts.isEmpty
                        ? Center(child: Text('noData'.tr()))
                        : ListView.builder(
                            itemCount: workouts.length,
                            itemBuilder: (context, index) {
                              Workout workout = workouts[index];
                              final weekday = _weekdayLabel(workout);

                              return Card(
                                margin: const EdgeInsets.symmetric(
                                    vertical: 8.0, horizontal: 16.0),
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(12),
                                ),
                                child: ListTile(
                                  title: Text(
                                    workout.name,
                                    style: const TextStyle(
                                      fontSize: 18,
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                  subtitle: Text([
                                    if (weekday != null) weekday,
                                    '${workout.exercises.length} ${'exercises'.tr()}',
                                  ].join(' • ')),
                                  trailing: Row(
                                    mainAxisSize: MainAxisSize.min,
                                    children: [
                                      IconButton(
                                        icon: const Icon(
                                          Icons.edit,
                                          color: AppColors.primary,
                                        ),
                                        onPressed: offline
                                            ? null
                                            : () {
                                                Navigator.push(
                                                  context,
                                                  MaterialPageRoute(
                                                    builder: (context) =>
                                                        NewWorkoutView(
                                                            id: workout.id),
                                                  ),
                                                );
                                              },
                                      ),
                                      IconButton(
                                        icon: const Icon(
                                          Icons.delete,
                                          color: AppColors.error,
                                        ),
                                        onPressed: offline
                                            ? null
                                            : () {
                                                _deleteWorkoutDialog(workout);
                                              },
                                      ),
                                    ],
                                  ),
                                  onTap: offline
                                      ? null
                                      : () {
                                          Navigator.push(
                                            context,
                                            MaterialPageRoute(
                                              builder: (context) =>
                                                  WorkoutDetailView(
                                                      workout: workout),
                                            ),
                                          );
                                        },
                                ),
                              );
                            },
                          )),
              ]),
              floatingActionButton: FloatingActionButton(
                onPressed: offline ? null : _showPlanActions,
                child: const Icon(Icons.add),
              ),
            );
          },
        );
      },
    );
  }
}
