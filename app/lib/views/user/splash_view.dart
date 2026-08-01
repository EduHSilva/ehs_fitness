import 'package:flutter/material.dart';

import '../../config/design_system.dart';
import 'login_view.dart';
import 'register_view.dart';

class SplashView extends StatelessWidget {
  const SplashView({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: LayoutBuilder(
          builder: (context, constraints) => SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: ConstrainedBox(
              constraints:
                  BoxConstraints(minHeight: constraints.maxHeight - 48),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const _Brand(),
                  const SizedBox(height: 32),
                  Container(
                    width: double.infinity,
                    constraints: const BoxConstraints(maxHeight: 330),
                    padding: const EdgeInsets.all(26),
                    decoration: BoxDecoration(
                      gradient: AppColors.primaryGradient,
                      borderRadius: BorderRadius.circular(28),
                    ),
                    child: Image.asset('assets/images/welcome_image.png',
                        fit: BoxFit.contain),
                  ),
                  const SizedBox(height: 30),
                  const Text(
                    'Seu ritmo. Sua evolucao.',
                    style: TextStyle(
                      fontSize: 34,
                      height: 1.08,
                      fontWeight: FontWeight.w800,
                      color: AppColors.onBackground,
                    ),
                  ),
                  const SizedBox(height: 14),
                  const Text(
                    'Organize treinos, alimentacao e habitos em uma rotina que funciona para voce.',
                    style: TextStyle(
                        fontSize: 16, height: 1.5, color: AppColors.muted),
                  ),
                  const SizedBox(height: 24),
                  const Wrap(
                    spacing: 8,
                    runSpacing: 8,
                    children: [
                      _FeatureChip(
                          icon: Icons.fitness_center_outlined,
                          label: 'Treinos'),
                      _FeatureChip(
                          icon: Icons.restaurant_outlined,
                          label: 'Alimentacao'),
                      _FeatureChip(
                          icon: Icons.checklist_outlined, label: 'Habitos'),
                    ],
                  ),
                  const SizedBox(height: 32),
                  SizedBox(
                    width: double.infinity,
                    child: FilledButton(
                      onPressed: () => Navigator.of(context).push(
                        MaterialPageRoute(builder: (_) => const LoginView()),
                      ),
                      child: const Text('Entrar na minha conta'),
                    ),
                  ),
                  const SizedBox(height: 10),
                  SizedBox(
                    width: double.infinity,
                    child: OutlinedButton(
                      onPressed: () => Navigator.of(context).push(
                        MaterialPageRoute(builder: (_) => const RegisterView()),
                      ),
                      child: const Text('Criar uma conta'),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _Brand extends StatelessWidget {
  const _Brand();

  @override
  Widget build(BuildContext context) => const Row(
        children: [
          CircleAvatar(
            radius: 19,
            backgroundColor: AppColors.primary,
            child: Icon(Icons.bolt_rounded, color: Colors.white),
          ),
          SizedBox(width: 10),
          Text('EHS Fitness',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.w800)),
        ],
      );
}

class _FeatureChip extends StatelessWidget {
  const _FeatureChip({required this.icon, required this.label});

  final IconData icon;
  final String label;

  @override
  Widget build(BuildContext context) => Chip(
        avatar: Icon(icon, size: 17, color: AppColors.primary),
        label: Text(label),
        side: const BorderSide(color: AppColors.border),
        backgroundColor: Colors.white,
        labelStyle: const TextStyle(
            color: AppColors.onSurface, fontWeight: FontWeight.w600),
      );
}
