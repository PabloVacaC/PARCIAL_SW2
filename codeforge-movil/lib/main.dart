import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'core/config/api_config.dart';
import 'core/network/offline_state.dart';
import 'core/services/proposal_notification_service.dart';
import 'core/storage/secure_storage_service.dart';
import 'core/theme/app_colors.dart';
import 'core/theme/app_theme.dart';
import 'modules/auth/providers/auth_provider.dart';
import 'modules/auth/views/login_view.dart';
import 'modules/projects/providers/proyecto_provider.dart';
import 'modules/projects/views/proyectos_list_view.dart';

final GlobalKey<NavigatorState> appNavigatorKey = GlobalKey<NavigatorState>();

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Configurar navigator global para notificaciones flotantes Heads-Up
  ProposalNotificationService.navigatorKey = appNavigatorKey;

  // Inicializar servicio de notificaciones nativas del sistema Android
  await ProposalNotificationService.instance.inicializar();

  final savedUrl = await SecureStorageService().obtenerBaseUrl();
  if (savedUrl != null && savedUrl.isNotEmpty) {
    ApiConfig.setBaseUrl(savedUrl);
  }

  runApp(const CodeForgeApp());
}

class CodeForgeApp extends StatelessWidget {
  const CodeForgeApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider.value(value: OfflineState.instance),
        ChangeNotifierProvider(create: (_) => AuthProvider()..verificarSesionExistente()),
        ChangeNotifierProvider(create: (_) => ProyectoProvider()),
      ],
      child: MaterialApp(
        navigatorKey: appNavigatorKey,
        title: 'CodeForge Mobile',
        debugShowCheckedModeBanner: false,
        theme: AppTheme.darkTheme,
        home: const AuthGate(),
      ),
    );
  }
}

class AuthGate extends StatelessWidget {
  const AuthGate({super.key});

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();

    switch (authProvider.status) {
      case AuthStatus.cargando:
      case AuthStatus.inicial:
        return const Scaffold(
          body: Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                CircularProgressIndicator(color: AppColors.primary),
                SizedBox(height: 16),
                Text('Cargando CodeForge...', style: TextStyle(color: AppColors.textSecondary)),
              ],
            ),
          ),
        );
      case AuthStatus.autenticado:
        return const ProyectosListView();
      case AuthStatus.noAutenticado:
        return const LoginView();
    }
  }
}
