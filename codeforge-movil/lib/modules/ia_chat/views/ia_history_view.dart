import 'package:flutter/material.dart';
import 'package:flutter_markdown/flutter_markdown.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/widgets/empty_state_view.dart';
import '../../../core/widgets/error_retry_view.dart';
import '../models/chat_message_model.dart';
import '../services/ia_history_service.dart';

class IaHistoryView extends StatefulWidget {
  final int idProyecto;

  const IaHistoryView({super.key, required this.idProyecto});

  @override
  State<IaHistoryView> createState() => _IaHistoryViewState();
}

class _IaHistoryViewState extends State<IaHistoryView> {
  final _historyService = IaHistoryService();

  List<ChatMessageModel> _mensajes = [];
  bool _cargando = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _cargarHistorial();
  }

  void _cargarHistorial() async {
    setState(() {
      _cargando = true;
      _error = null;
    });

    try {
      final list = await _historyService.obtenerHistorialConversacion(widget.idProyecto);
      setState(() {
        _mensajes = list;
        _cargando = false;
      });
    } catch (e) {
      setState(() {
        _cargando = false;
        _error = e.toString().replaceFirst('Exception: ', '');
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_cargando) {
      return const Center(child: CircularProgressIndicator(color: AppColors.primary));
    }

    if (_error != null) {
      return ErrorRetryView(mensaje: _error!, onRetry: _cargarHistorial);
    }

    if (_mensajes.isEmpty) {
      return const EmptyStateView(
        icon: Icons.chat_bubble_outline_rounded,
        title: 'Sin conversaciones registradas',
        description: 'Aún no hay interacción entre los desarrolladores y la IA en este proyecto.',
      );
    }

    return Column(
      children: [
        // Lista de mensajes (modo lectura)
        Expanded(
          child: ListView.separated(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            itemCount: _mensajes.length,
            separatorBuilder: (_, _) => const SizedBox(height: 12),
            itemBuilder: (context, index) {
              final m = _mensajes[index];
              return _burbujaMensaje(m);
            },
          ),
        ),

        // Barra inferior informativa de Solo Lectura (sin input de texto)
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          decoration: const BoxDecoration(
            color: AppColors.surface,
            border: Border(top: BorderSide(color: AppColors.border)),
          ),
          child: SafeArea(
            top: false,
            child: Row(
              children: [
                const Icon(Icons.lock_outline_rounded, size: 18, color: AppColors.readOnlyPrimary),
                const SizedBox(width: 10),
                Expanded(
                  child: Text(
                    'Historial de solo lectura. Para interactuar con la IA, ingresa desde la plataforma web.',
                    style: AppTypography.bodySmall.copyWith(color: AppColors.textSecondary),
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _burbujaMensaje(ChatMessageModel m) {
    final bool esIa = m.esIa;

    return Row(
      mainAxisAlignment: esIa ? MainAxisAlignment.start : MainAxisAlignment.end,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (esIa) ...[
          Container(
            width: 32,
            height: 32,
            decoration: BoxDecoration(
              gradient: AppColors.brandGradient,
              borderRadius: BorderRadius.circular(10),
            ),
            child: const Icon(Icons.smart_toy_rounded, size: 18, color: Colors.white),
          ),
          const SizedBox(width: 8),
        ],
        Flexible(
          child: Container(
            constraints: const BoxConstraints(maxWidth: 320),
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: esIa ? AppColors.card : AppColors.primary.withAlpha(45),
              borderRadius: BorderRadius.circular(14),
              border: Border.all(
                color: esIa ? AppColors.border : AppColors.primaryLight.withAlpha(80),
                width: 1,
              ),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Cabecera del mensaje
                Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      m.nombreEmisor,
                      style: AppTypography.label.copyWith(
                        color: esIa ? AppColors.primaryLight : AppColors.textPrimary,
                      ),
                    ),
                    const SizedBox(width: 6),
                    Text(
                      m.fechaFormateada,
                      style: AppTypography.bodySmall.copyWith(fontSize: 9.5),
                    ),
                  ],
                ),
                if (esIa && m.modoIa.isNotEmpty && m.modoIa != 'GENERAL') ...[
                  const SizedBox(height: 4),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                    decoration: BoxDecoration(
                      color: AppColors.secondary.withAlpha(30),
                      borderRadius: BorderRadius.circular(4),
                    ),
                    child: Text(
                      'Modo: ${m.modoIa}',
                      style: AppTypography.code.copyWith(fontSize: 9.5, color: AppColors.secondary),
                    ),
                  ),
                ],
                const SizedBox(height: 8),

                // Contenido del texto formateado con Markdown
                MarkdownBody(
                  data: m.contenido,
                  selectable: true,
                  styleSheet: MarkdownStyleSheet.fromTheme(Theme.of(context)).copyWith(
                    p: AppTypography.bodyLarge.copyWith(fontSize: 13.5, height: 1.45),
                    code: AppTypography.code.copyWith(
                      fontSize: 12,
                      backgroundColor: AppColors.background.withAlpha(180),
                    ),
                    codeblockDecoration: BoxDecoration(
                      color: AppColors.background,
                      borderRadius: BorderRadius.circular(6),
                      border: Border.all(color: AppColors.border),
                    ),
                  ),
                ),

                // Si tiene propuesta asociada
                if (m.tienePropuesta) ...[
                  const SizedBox(height: 10),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: AppColors.accent.withAlpha(20),
                      borderRadius: BorderRadius.circular(6),
                      border: Border.all(color: AppColors.accent.withAlpha(70)),
                    ),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        const Icon(Icons.auto_awesome_rounded, size: 14, color: AppColors.accent),
                        const SizedBox(width: 5),
                        Text(
                          'Propuesta #${m.idPropuestaAsociada} generada',
                          style: AppTypography.bodySmall.copyWith(
                            color: AppColors.accent,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ],
            ),
          ),
        ),
        if (!esIa) ...[
          const SizedBox(width: 8),
          Container(
            width: 32,
            height: 32,
            decoration: BoxDecoration(
              color: AppColors.surface,
              borderRadius: BorderRadius.circular(10),
              border: Border.all(color: AppColors.border),
            ),
            child: const Icon(Icons.person_outline_rounded, size: 18, color: AppColors.textSecondary),
          ),
        ],
      ],
    );
  }
}
