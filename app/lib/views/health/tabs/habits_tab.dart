import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class Habit {
  const Habit({required this.id, required this.name, required this.category});

  final String id;
  final String name;
  final String category;

  Map<String, String> toJson() =>
      {'id': id, 'name': name, 'category': category};

  factory Habit.fromJson(Map<String, dynamic> json) => Habit(
        id: json['id'] as String,
        name: json['name'] as String,
        category: json['category'] as String,
      );
}

class HabitsTab extends StatefulWidget {
  const HabitsTab({super.key});

  @override
  State<HabitsTab> createState() => _HabitsTabState();
}

class _HabitsTabState extends State<HabitsTab> {
  static const _storageKey = 'fitness_habits';
  static const _categories = [
    'Saude',
    'Alimentacao',
    'Atividade fisica',
    'Sono',
    'Bem-estar',
    'Outro',
  ];

  final _nameController = TextEditingController();
  final List<Habit> _habits = [];
  String? _category;

  @override
  void initState() {
    super.initState();
    _loadHabits();
  }

  @override
  void dispose() {
    _nameController.dispose();
    super.dispose();
  }

  Future<void> _loadHabits() async {
    final preferences = await SharedPreferences.getInstance();
    final data = preferences.getString(_storageKey);
    if (data == null) return;

    try {
      final items = jsonDecode(data) as List<dynamic>;
      if (!mounted) return;
      setState(() {
        _habits
          ..clear()
          ..addAll(items
              .map((item) => Habit.fromJson(item as Map<String, dynamic>)));
      });
    } catch (_) {
      await preferences.remove(_storageKey);
    }
  }

  Future<void> _saveHabits() async {
    final preferences = await SharedPreferences.getInstance();
    await preferences.setString(
      _storageKey,
      jsonEncode(_habits.map((habit) => habit.toJson()).toList()),
    );
  }

  Future<void> _addHabit() async {
    final name = _nameController.text.trim();
    if (name.isEmpty || _category == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
            content: Text('Informe o nome e a categoria do habito.')),
      );
      return;
    }

    setState(() {
      _habits.add(Habit(
        id: DateTime.now().microsecondsSinceEpoch.toString(),
        name: name,
        category: _category!,
      ));
      _nameController.clear();
      _category = null;
    });
    await _saveHabits();
  }

  Future<void> _removeHabit(Habit habit) async {
    setState(() => _habits.removeWhere((item) => item.id == habit.id));
    await _saveHabits();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
          title: const Text('Habitos'), automaticallyImplyLeading: false),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            children: [
              TextField(
                controller: _nameController,
                textCapitalization: TextCapitalization.sentences,
                decoration: const InputDecoration(
                  labelText: 'Nome do habito',
                  hintText: 'Ex.: Beber 2 litros de agua',
                  border: OutlineInputBorder(),
                ),
              ),
              const SizedBox(height: 12),
              DropdownButtonFormField<String>(
                initialValue: _category,
                decoration: const InputDecoration(
                  labelText: 'Categoria',
                  border: OutlineInputBorder(),
                ),
                items: _categories
                    .map((category) => DropdownMenuItem(
                        value: category, child: Text(category)))
                    .toList(),
                onChanged: (value) => setState(() => _category = value),
              ),
              const SizedBox(height: 12),
              SizedBox(
                width: double.infinity,
                child: FilledButton.icon(
                  onPressed: _addHabit,
                  icon: const Icon(Icons.add),
                  label: const Text('Adicionar habito'),
                ),
              ),
              const SizedBox(height: 20),
              Expanded(
                child: _habits.isEmpty
                    ? const Center(child: Text('Nenhum habito cadastrado.'))
                    : ListView.separated(
                        itemCount: _habits.length,
                        separatorBuilder: (_, index) => const SizedBox(height: 8),
                        itemBuilder: (context, index) {
                          final habit = _habits[index];
                          return Card(
                            child: ListTile(
                              leading:
                                  const CircleAvatar(child: Icon(Icons.check)),
                              title: Text(habit.name),
                              subtitle: Text(habit.category),
                              trailing: IconButton(
                                icon: const Icon(Icons.delete_outline),
                                tooltip: 'Remover habito',
                                onPressed: () => _removeHabit(habit),
                              ),
                            ),
                          );
                        },
                      ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
