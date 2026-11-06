import 'dart:async';
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import '../../modules/proposals/models/proposal_model.dart';
import '../../modules/proposals/views/proposal_detail_sheet.dart';
import '../theme/app_colors.dart';
import '../theme/app_typography.dart';

/// Servicio para mostrar notificaciones push del sistema (Barra de estado / Notificaciones Android)
/// y dentro de la app (Heads-Up Banner flotante) cuando la IA Colaborativa genera una nueva propuesta.
class ProposalNotificationService {
  static final ProposalNotificationService instance =
      ProposalNotificationService._internal();

  factory ProposalNotificationService() => instance;
  ProposalNotificationService._internal();

  static GlobalKey<NavigatorState>? navigatorKey;

  final FlutterLocalNotificationsPlugin _localNotifications =
      FlutterLocalNotificationsPlugin();

  OverlayEntry? _currentOverlay;
  Timer? _dismissTimer;
  bool _inicializado = false;

  /// Inicializa las notificaciones nativas de Android y los canales del sistema
  Future<void> inicializar() async {
    if (_inicializado) return;

    const AndroidInitializationSettings androidSettings =
        AndroidInitializationSettings('@mipmap/ic_launcher');

    const InitializationSettings initSettings =
        InitializationSettings(android: androidSettings);

    await _localNotifications.initialize(
      settings: initSettings,
      onDidReceiveNotificationResponse: (NotificationResponse response) {
        if (response.payload != null && response.payload!.isNotEmpty) {
          try {
            final map = jsonDecode(response.payload!) as Map<String, dynamic>;
            final propuesta = ProposalModel.fromJson(map);
            final context = navigatorKey?.currentContext;
            if (context != null) {
              _abrirDetallePropuesta(context, propuesta);
            }
          } catch (_) {}
        }
      },
    );

    // Solicitar permisos en Android 13+ y crear el canal con máxima prioridad
    final androidImpl = _localNotifications.resolvePlatformSpecificImplementation<
        AndroidFlutterLocalNotificationsPlugin>();

    if (androidImpl != null) {
      await androidImpl.requestNotificationsPermission();

      const AndroidNotificationChannel channel = AndroidNotificationChannel(
        'codeforge_ia_propuestas',
        'Propuestas de IA CodeForge',
        description: 'Notificaciones nativas cuando la IA genera una propuesta de cambio',
        importance: Importance.max,
        playSound: true,
        enableVibration: true,
      );

      await androidImpl.createNotificationChannel(channel);
    }

    _inicializado = true;
    debugPrint('[ProposalNotification] Servicio nativo de notificaciones inicializado.');
  }

  /// Muestra la notificación nativa en la barra de tareas de Android y el banner flotante en la app
  Future<void> mostrarNotificacionPropuesta(ProposalModel propuesta) async {
    debugPrint('[ProposalNotification] Disparando notificación #${propuesta.idPropuesta}: ${propuesta.titulo}');

    // Asegurar inicialización nativa
    if (!_inicializado) {
      await inicializar();
    }

    // 1. DISPARAR NOTIFICACIÓN NATIVA DE ANDROID (Barra de estado / Cajón de notificaciones)
    try {
      final AndroidNotificationDetails androidDetails = AndroidNotificationDetails(
        'codeforge_ia_propuestas',
        'Propuestas de IA CodeForge',
        channelDescription: 'Notificaciones nativas cuando la IA genera una propuesta de cambio',
        importance: Importance.max,
        priority: Priority.high,
        showWhen: true,
        playSound: true,
        enableVibration: true,
        styleInformation: BigTextStyleInformation(
          'Proyecto #${propuesta.idProyecto} · ${propuesta.titulo}\n${propuesta.solucionPropuesta.isNotEmpty ? propuesta.solucionPropuesta : "Cambios de diseño generados por IA listos para revisión."}',
          contentTitle: '🔔 Se acaba de generar una propuesta',
          summaryText: 'CodeForge IA',
        ),
      );

      final NotificationDetails platformDetails =
          NotificationDetails(android: androidDetails);

      await _localNotifications.show(
        id: propuesta.idPropuesta,
        title: '🔔 Se acaba de generar una propuesta',
        body: '#${propuesta.idPropuesta}: ${propuesta.titulo}',
        notificationDetails: platformDetails,
        payload: jsonEncode(propuesta.toJson()),
      );
    } catch (e) {
      debugPrint('[ProposalNotification] Error mostrando notificación nativa: $e');
    }

    // 2. VIBRACIÓN HÁPTICA
    try {
      HapticFeedback.heavyImpact();
      Future.delayed(const Duration(milliseconds: 150), () {
        HapticFeedback.vibrate();
      });
    } catch (_) {}

    // 3. BANNER FLOTANTE HEADS-UP DENTRO DE LA APP
    final navState = navigatorKey?.currentState;
    final overlayState = navState?.overlay;
    final context = navigatorKey?.currentContext;

    if (overlayState != null && context != null) {
      _removerNotificacion();

      _currentOverlay = OverlayEntry(
        builder: (ctx) => _ProposalNotificationBanner(
          propuesta: propuesta,
          onVer: () {
            _removerNotificacion();
            _abrirDetallePropuesta(context, propuesta);
          },
          onCerrar: _removerNotificacion,
        ),
      );

      overlayState.insert(_currentOverlay!);

      _dismissTimer = Timer(const Duration(milliseconds: 6500), () {
        _removerNotificacion();
      });
    }
  }

  void _removerNotificacion() {
    _dismissTimer?.cancel();
    _dismissTimer = null;
    if (_currentOverlay != null) {
      try {
        _currentOverlay?.remove();
      } catch (_) {}
      _currentOverlay = null;
    }
  }

  void _abrirDetallePropuesta(BuildContext context, ProposalModel propuesta) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => ProposalDetailSheet(propuesta: propuesta),
    );
  }
}

class _ProposalNotificationBanner extends StatefulWidget {
  final ProposalModel propuesta;
  final VoidCallback onVer;
  final VoidCallback onCerrar;

  const _ProposalNotificationBanner({
    required this.propuesta,
    required this.onVer,
    required this.onCerrar,
  });

  @override
  State<_ProposalNotificationBanner> createState() =>
      _ProposalNotificationBannerState();
}

class _ProposalNotificationBannerState extends State<_ProposalNotificationBanner>
    with SingleTickerProviderStateMixin {
  late AnimationController _animController;
  late Animation<Offset> _slideAnim;
  late Animation<double> _fadeAnim;

  @override
  void initState() {
    super.initState();
    _animController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 400),
    );

    _slideAnim = Tween<Offset>(
      begin: const Offset(0, -1.2),
      end: Offset.zero,
    ).animate(CurvedAnimation(
      parent: _animController,
      curve: Curves.easeOutBack,
    ));

    _fadeAnim = CurvedAnimation(
      parent: _animController,
      curve: Curves.easeIn,
    );

    _animController.forward();
  }

  @override
  void dispose() {
    _animController.dispose();
    super.dispose();
  }

  void _cerrarConAnimacion() {
    _animController.reverse().then((_) {
      if (mounted) widget.onCerrar();
    });
  }

  @override
  Widget build(BuildContext context) {
    final p = widget.propuesta;

    return SafeArea(
      child: Align(
        alignment: Alignment.topCenter,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
          child: SlideTransition(
            position: _slideAnim,
            child: FadeTransition(
              opacity: _fadeAnim,
              child: Dismissible(
                key: ValueKey('notif_${p.idPropuesta}_${DateTime.now().millisecondsSinceEpoch}'),
                direction: DismissDirection.up,
                onDismissed: (_) => widget.onCerrar(),
                child: Material(
                  color: Colors.transparent,
                  child: Container(
                    decoration: BoxDecoration(
                      color: const Color(0xFF161928),
                      borderRadius: BorderRadius.circular(18),
                      border: Border.all(
                        color: AppColors.primaryLight.withAlpha(160),
                        width: 1.5,
                      ),
                      boxShadow: [
                        BoxShadow(
                          color: AppColors.primary.withAlpha(90),
                          blurRadius: 24,
                          spreadRadius: 2,
                          offset: const Offset(0, 8),
                        ),
                        BoxShadow(
                          color: Colors.black.withAlpha(180),
                          blurRadius: 16,
                          offset: const Offset(0, 4),
                        ),
                      ],
                    ),
                    child: ClipRRect(
                      borderRadius: BorderRadius.circular(18),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Padding(
                            padding: const EdgeInsets.symmetric(
                                horizontal: 14, vertical: 12),
                            child: Row(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Container(
                                  width: 44,
                                  height: 44,
                                  decoration: BoxDecoration(
                                    gradient: AppColors.brandGradient,
                                    borderRadius: BorderRadius.circular(12),
                                    boxShadow: [
                                      BoxShadow(
                                        color: AppColors.primary.withAlpha(140),
                                        blurRadius: 12,
                                        offset: const Offset(0, 3),
                                      ),
                                    ],
                                  ),
                                  child: const Icon(
                                    Icons.auto_awesome_rounded,
                                    color: Colors.white,
                                    size: 24,
                                  ),
                                ),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    mainAxisSize: MainAxisSize.min,
                                    children: [
                                      Row(
                                        children: [
                                          Container(
                                            padding: const EdgeInsets.symmetric(
                                                horizontal: 6, vertical: 2),
                                            decoration: BoxDecoration(
                                              color: AppColors.primary.withAlpha(50),
                                              borderRadius: BorderRadius.circular(6),
                                              border: Border.all(
                                                color: AppColors.primaryLight.withAlpha(100),
                                              ),
                                            ),
                                            child: const Text(
                                              'NUEVA PROPUESTA IA',
                                              style: TextStyle(
                                                color: AppColors.primaryLight,
                                                fontSize: 10,
                                                fontWeight: FontWeight.bold,
                                                letterSpacing: 0.5,
                                              ),
                                            ),
                                          ),
                                          const Spacer(),
                                          Text(
                                            'Ahora',
                                            style: AppTypography.bodySmall.copyWith(
                                              color: AppColors.textMuted,
                                              fontSize: 10,
                                            ),
                                          ),
                                        ],
                                      ),
                                      const SizedBox(height: 4),
                                      Text(
                                        'Se acaba de generar una propuesta',
                                        style: AppTypography.bodySmall.copyWith(
                                          color: Colors.white,
                                          fontWeight: FontWeight.bold,
                                        ),
                                      ),
                                      const SizedBox(height: 2),
                                      Text(
                                        '#${p.idPropuesta}: ${p.titulo}',
                                        style: AppTypography.bodyMedium.copyWith(
                                          color: AppColors.textPrimary,
                                        ),
                                        maxLines: 2,
                                        overflow: TextOverflow.ellipsis,
                                      ),
                                      const SizedBox(height: 4),
                                      Text(
                                        'Proyecto #${p.idProyecto} · ${p.totalOperaciones} modificaciones UML',
                                        style: AppTypography.bodySmall.copyWith(
                                          color: AppColors.textSecondary,
                                          fontSize: 10,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                                const SizedBox(width: 6),
                                InkWell(
                                  onTap: _cerrarConAnimacion,
                                  borderRadius: BorderRadius.circular(20),
                                  child: const Padding(
                                    padding: EdgeInsets.all(4),
                                    child: Icon(
                                      Icons.close_rounded,
                                      size: 18,
                                      color: AppColors.textMuted,
                                    ),
                                  ),
                                ),
                              ],
                            ),
                          ),
                          InkWell(
                            onTap: widget.onVer,
                            child: Container(
                              width: double.infinity,
                              padding: const EdgeInsets.symmetric(
                                  horizontal: 16, vertical: 9),
                              decoration: BoxDecoration(
                                color: AppColors.primary.withAlpha(35),
                                border: Border(
                                  top: BorderSide(
                                    color: AppColors.primary.withAlpha(60),
                                  ),
                                ),
                              ),
                              child: Row(
                                mainAxisAlignment: MainAxisAlignment.center,
                                children: [
                                  const Icon(
                                    Icons.visibility_rounded,
                                    size: 16,
                                    color: AppColors.primaryLight,
                                  ),
                                  const SizedBox(width: 8),
                                  Text(
                                    'Ver detalles de la propuesta',
                                    style: AppTypography.label.copyWith(
                                      color: AppColors.primaryLight,
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                  const SizedBox(width: 4),
                                  const Icon(
                                    Icons.arrow_forward_rounded,
                                    size: 14,
                                    color: AppColors.primaryLight,
                                  ),
                                ],
                              ),
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
        ),
      ),
    );
  }
}
