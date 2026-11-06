import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../core/config/api_config.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/widgets/custom_button.dart';
import '../../../core/widgets/custom_text_field.dart';
import '../../../core/storage/secure_storage_service.dart';
import '../../../core/widgets/read_only_badge.dart';
import '../providers/auth_provider.dart';

class LoginView extends StatefulWidget {
  const LoginView({super.key});

  @override
  State<LoginView> createState() => _LoginViewState();
}

class _LoginViewState extends State<LoginView> {
  final _formKey = GlobalKey<FormState>();
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();

  @override
  void dispose() {
    _usernameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  void _ejecutarLogin() async {
    if (!_formKey.currentState!.validate()) return;

    final authProvider = context.read<AuthProvider>();
    final success = await authProvider.login(
      _usernameController.text,
      _passwordController.text,
    );

    if (!success && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          backgroundColor: AppColors.error,
          behavior: SnackBarBehavior.floating,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          content: Text(
            authProvider.error ?? 'Error al iniciar sesión',
            style: AppTypography.bodyMedium.copyWith(color: Colors.white),
          ),
        ),
      );
    }
  }

  void _mostrarConfigurarServidor() {
    final serverController = TextEditingController(text: ApiConfig.baseUrl);
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppColors.card,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: Text('Servidor Backend', style: AppTypography.h3),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Selecciona o ingresa la URL de la API Spring Boot:',
              style: AppTypography.bodySmall,
            ),
            const SizedBox(height: 12),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                ActionChip(
                  avatar: const Icon(Icons.usb_rounded, size: 16, color: AppColors.accent),
                  label: const Text('Cable USB (127.0.0.1)'),
                  backgroundColor: AppColors.surface,
                  labelStyle: AppTypography.bodySmall.copyWith(color: Colors.white),
                  onPressed: () {
                    serverController.text = 'http://127.0.0.1:8080/api';
                  },
                ),
                ActionChip(
                  avatar: const Icon(Icons.wifi_rounded, size: 16, color: AppColors.primary),
                  label: const Text('Wi-Fi (192.168.0.20)'),
                  backgroundColor: AppColors.surface,
                  labelStyle: AppTypography.bodySmall.copyWith(color: Colors.white),
                  onPressed: () {
                    serverController.text = 'http://192.168.0.20:8080/api';
                  },
                ),
                ActionChip(
                  avatar: const Icon(Icons.wifi_rounded, size: 16, color: AppColors.primary),
                  label: const Text('Wi-Fi (192.168.0.19)'),
                  backgroundColor: AppColors.surface,
                  labelStyle: AppTypography.bodySmall.copyWith(color: Colors.white),
                  onPressed: () {
                    serverController.text = 'http://192.168.0.19:8080/api';
                  },
                ),
              ],
            ),
            const SizedBox(height: 12),
            TextField(
              controller: serverController,
              style: AppTypography.bodyMedium,
              decoration: const InputDecoration(
                hintText: 'http://192.168.0.19:8080/api',
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: Text('Cancelar', style: AppTypography.label.copyWith(color: AppColors.textMuted)),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.primary,
              foregroundColor: Colors.white,
            ),
            onPressed: () async {
              final newUrl = serverController.text.trim();
              if (newUrl.isNotEmpty) {
                ApiConfig.setBaseUrl(newUrl);
                await SecureStorageService().guardarBaseUrl(newUrl);
                if (mounted) setState(() {});
              }
              if (ctx.mounted) Navigator.pop(ctx);
              if (mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(
                    content: Text('Servidor configurado en: ${ApiConfig.baseUrl}'),
                    behavior: SnackBarBehavior.floating,
                  ),
                );
              }
            },
            child: const Text('Guardar'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();
    final isLoading = authProvider.cargandoLogin;

    return Scaffold(
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 24),
            child: Form(
              key: _formKey,
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  // Logo e Identidad de CodeForge
                  Center(
                    child: Container(
                      width: 80,
                      height: 80,
                      decoration: BoxDecoration(
                        gradient: AppColors.brandGradient,
                        borderRadius: BorderRadius.circular(22),
                        boxShadow: [
                          BoxShadow(
                            color: AppColors.primary.withAlpha(90),
                            blurRadius: 24,
                            offset: const Offset(0, 8),
                          ),
                        ],
                      ),
                      child: const Icon(
                        Icons.account_tree_rounded,
                        size: 42,
                        color: Colors.white,
                      ),
                    ),
                  ),
                  const SizedBox(height: 20),

                  // Título
                  Text(
                    'CodeForge Mobile',
                    style: AppTypography.h1,
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 6),

                  // Badge de Solo Lectura
                  const Center(child: ReadOnlyBadge()),
                  const SizedBox(height: 12),

                  Text(
                    'Inicia sesión con tu cuenta registrada en la plataforma web para consultar tus proyectos de software.',
                    style: AppTypography.bodyMedium,
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 36),

                  // Formulario de Login
                  CustomTextField(
                    label: 'Usuario o Correo',
                    hint: 'ej. pablo o tu correo',
                    controller: _usernameController,
                    prefixIcon: Icons.person_outline_rounded,
                    validator: (v) =>
                        (v == null || v.trim().isEmpty) ? 'Ingresa tu usuario o correo' : null,
                  ),
                  const SizedBox(height: 20),

                  CustomTextField(
                    label: 'Contraseña',
                    hint: '••••••••',
                    isPassword: true,
                    controller: _passwordController,
                    prefixIcon: Icons.lock_outline_rounded,
                    validator: (v) =>
                        (v == null || v.isEmpty) ? 'Ingresa tu contraseña' : null,
                  ),
                  const SizedBox(height: 20),

                  // Mensaje de Error Visible
                  if (authProvider.error != null) ...[
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: AppColors.error.withAlpha(35),
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(color: AppColors.error.withAlpha(120)),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          Row(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              const Padding(
                                padding: EdgeInsets.only(top: 2),
                                child: Icon(Icons.error_outline_rounded,
                                    color: AppColors.error, size: 18),
                              ),
                              const SizedBox(width: 10),
                              Expanded(
                                child: Text(
                                  authProvider.error!,
                                  style: AppTypography.bodySmall
                                      .copyWith(color: AppColors.error),
                                ),
                              ),
                            ],
                          ),
                          if (authProvider.error!.contains('localhost') ||
                              authProvider.error!.contains('127.0.0.1') ||
                              authProvider.error!.contains('refused') ||
                              authProvider.error!.contains('SocketConnection')) ...[
                            const SizedBox(height: 10),
                            Wrap(
                              spacing: 8,
                              runSpacing: 8,
                              children: [
                                ElevatedButton.icon(
                                  icon: const Icon(Icons.usb_rounded, size: 15),
                                  label: const Text('Cable USB (127.0.0.1)'),
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor: AppColors.primary,
                                    foregroundColor: Colors.white,
                                    padding: const EdgeInsets.symmetric(
                                        horizontal: 10, vertical: 8),
                                    shape: RoundedRectangleBorder(
                                      borderRadius: BorderRadius.circular(8),
                                    ),
                                  ),
                                  onPressed: () async {
                                    ApiConfig.setBaseUrl('http://127.0.0.1:8080/api');
                                    await SecureStorageService().guardarBaseUrl('http://127.0.0.1:8080/api');
                                    if (mounted) setState(() {});
                                    _ejecutarLogin();
                                  },
                                ),
                                ElevatedButton.icon(
                                  icon: const Icon(Icons.wifi_rounded, size: 15),
                                  label: const Text('Wi-Fi (192.168.0.20)'),
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor: const Color(0xFF2E3248),
                                    foregroundColor: Colors.white,
                                    padding: const EdgeInsets.symmetric(
                                        horizontal: 10, vertical: 8),
                                    shape: RoundedRectangleBorder(
                                      borderRadius: BorderRadius.circular(8),
                                    ),
                                  ),
                                  onPressed: () async {
                                    ApiConfig.setBaseUrl('http://192.168.0.20:8080/api');
                                    await SecureStorageService().guardarBaseUrl('http://192.168.0.20:8080/api');
                                    if (mounted) setState(() {});
                                    _ejecutarLogin();
                                  },
                                ),
                              ],
                            ),
                          ],
                        ],
                      ),
                    ),
                    const SizedBox(height: 16),
                  ],

                  // Botón Iniciar Sesión
                  CustomButton(
                    text: 'Iniciar Sesión',
                    isLoading: isLoading,
                    icon: Icons.login_rounded,
                    onPressed: _ejecutarLogin,
                  ),
                  const SizedBox(height: 24),

                  // Acceso rápido a configuración de servidor API
                  Center(
                    child: TextButton.icon(
                      icon: const Icon(Icons.settings_ethernet_rounded,
                          size: 16, color: AppColors.textMuted),
                      label: Text(
                        'Configurar servidor (${ApiConfig.baseUrl})',
                        style: AppTypography.bodySmall,
                        overflow: TextOverflow.ellipsis,
                      ),
                      onPressed: _mostrarConfigurarServidor,
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
