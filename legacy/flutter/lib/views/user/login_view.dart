import 'package:easy_localization/easy_localization.dart';
import 'package:flutter/material.dart';
import 'package:google_sign_in/google_sign_in.dart';

import '../../config/app_config.dart';
import '../../config/design_system.dart';
import '../../config/helper.dart';
import '../../models/user/login_model.dart';
import '../../view_models/user_viewmodel.dart';
import '../../widgets/custom_text_field.dart';
import 'register_view.dart';

class LoginView extends StatefulWidget {
  const LoginView({super.key});

  @override
  LoginViewState createState() => LoginViewState();
}

class LoginViewState extends State<LoginView> {
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();
  final _userViewModel = UserViewModel();
  final _formKey = GlobalKey<FormState>();

  @override
  void dispose() {
    _usernameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _login() async {
    if (!(_formKey.currentState?.validate() ?? false)) return;
    try {
      final response = await _userViewModel.login(
          _usernameController.text, _passwordController.text);
      _handleResponse(response);
    } catch (_) {
      if (mounted) showErrorBar(context, null);
    }
  }

  Future<void> _loginWithGoogle() async {
    try {
      final googleUser = await GoogleSignIn(
        scopes: const ['email'],
        serverClientId: AppConfig.googleClientId,
      ).signIn();
      if (googleUser == null) return;
      final response =
          await _userViewModel.loginWithGoogle(googleUser.email, googleUser.id);
      _handleResponse(response);
    } catch (_) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Nao foi possivel entrar com o Google.')),
      );
    }
  }

  void _handleResponse(LoginResponse? response) {
    if (response?.user != null) {
      Navigator.pushReplacementNamed(context, '/home');
      return;
    }
    if (mounted) showErrorBar(context, response);
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        body: SafeArea(
          child: Center(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(24),
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 440),
                child: Form(
                  key: _formKey,
                  child: ValueListenableBuilder(
                    valueListenable: _userViewModel.isLoading,
                    builder: (context, isLoading, _) => Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        IconButton(
                          tooltip: 'Voltar',
                          onPressed: () => Navigator.of(context).maybePop(),
                          icon: const Icon(Icons.arrow_back),
                        ),
                        const SizedBox(height: 24),
                        Container(
                          width: 54,
                          height: 54,
                          decoration: BoxDecoration(
                            color: AppColors.surfaceVariant,
                            borderRadius: BorderRadius.circular(16),
                          ),
                          child: const Icon(Icons.bolt_rounded,
                              color: AppColors.primary, size: 30),
                        ),
                        const SizedBox(height: 24),
                        const Text('Que bom ter voce de volta!',
                            style: TextStyle(
                                fontSize: 29, fontWeight: FontWeight.w800)),
                        const SizedBox(height: 8),
                        const Text(
                            'Entre para continuar cuidando da sua rotina.',
                            style: TextStyle(
                                color: AppColors.muted, fontSize: 15)),
                        const SizedBox(height: 32),
                        CustomTextField(
                          labelText: 'email'.tr(),
                          prefixIcon: Icons.mail_outline,
                          keyboardType: TextInputType.emailAddress,
                          controller: _usernameController,
                          validator: requiredFieldValidator,
                        ),
                        const SizedBox(height: 14),
                        CustomTextField(
                          isPassword: true,
                          prefixIcon: Icons.lock_outline,
                          labelText: 'password'.tr(),
                          controller: _passwordController,
                          validator: requiredFieldValidator,
                        ),
                        Align(
                          alignment: Alignment.centerRight,
                          child: TextButton(
                              onPressed: () {},
                              child: Text('lostPassword'.tr())),
                        ),
                        const SizedBox(height: 8),
                        SizedBox(
                          width: double.infinity,
                          child: FilledButton(
                            onPressed: isLoading ? null : _login,
                            child: isLoading
                                ? const SizedBox(
                                    width: 20,
                                    height: 20,
                                    child: CircularProgressIndicator(
                                        strokeWidth: 2, color: Colors.white))
                                : Text('login'.tr()),
                          ),
                        ),
                        const SizedBox(height: 18),
                        const Row(children: [
                          Expanded(child: Divider()),
                          Padding(
                              padding: EdgeInsets.symmetric(horizontal: 12),
                              child: Text('ou continue com')),
                          Expanded(child: Divider())
                        ]),
                        const SizedBox(height: 18),
                        SizedBox(
                          width: double.infinity,
                          child: OutlinedButton.icon(
                            onPressed: isLoading ? null : _loginWithGoogle,
                            icon: const Icon(Icons.g_mobiledata, size: 28),
                            label: const Text('Entrar com Google'),
                          ),
                        ),
                        const SizedBox(height: 22),
                        Center(
                          child: Wrap(
                            crossAxisAlignment: WrapCrossAlignment.center,
                            children: [
                              const Text('Ainda nao possui uma conta?'),
                              TextButton(
                                onPressed: () => Navigator.of(context).push(
                                    MaterialPageRoute(
                                        builder: (_) => const RegisterView())),
                                child: const Text('Criar conta'),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
      );
}
