import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/widgets/empty_state_view.dart';
import '../../../core/widgets/error_retry_view.dart';
import '../../../core/widgets/read_only_badge.dart';
import '../models/proposal_model.dart';
import '../services/proposal_service.dart';
import 'proposal_detail_sheet.dart';

class ProposalsListView extends StatefulWidget {
  final int idProyecto;

  const ProposalsListView({super.key, required this.idProyecto});

  @override
  State<ProposalsListView> createState() => _ProposalsListViewState();
}

class _ProposalsListViewState extends State<ProposalsListView> {
  final _proposalService = ProposalService();

  List<ProposalModel> _propuestas = [];
  bool _cargando = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _cargarPropuestas();
  }

  void _cargarPropuestas() async {
    setState(() {
      _cargando = true;
      _error = null;
    });

    try {
      final list = await _proposalService.listarPropuestas(widget.idProyecto);
      setState(() {
        _propuestas = list;
        _cargando = false;
      });
    } catch (e) {
      setState(() {
        _cargando = false;
        _error = e.toString().replaceFirst('Exception: ', '');
      });
    }
  }

  Color _colorEstado(String estado) {
    switch (estado.toUpperCase()) {
      case 'APROBADA':
      case 'APLICADA':
        return AppColors.success;
      case 'RECHAZADA':
      case 'FALLIDA':
        return AppColors.error;
      case 'PENDIENTE':
      default:
        return AppColors.warning;
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_cargando) {
      return const Center(child: CircularProgressIndicator(color: AppColors.primary));
    }

    if (_error != null) {
      return ErrorRetryView(mensaje: _error!, onRetry: _cargarPropuestas);
    }

    if (_propuestas.isEmpty) {
      return const EmptyStateView(
        icon: Icons.checklist_rtl_rounded,
        title: 'Sin propuestas de cambio',
        description:
            'Aún no hay propuestas de modificación arquitectónica registradas para este proyecto.',
      );
    }

    return Column(
      children: [
        // Cabecera de estado
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
          child: Row(
            children: [
              Text(
                '${_propuestas.length} propuestas registradas',
                style: AppTypography.bodySmall,
              ),
              const Spacer(),
              const ReadOnlyBadge(compact: true),
            ],
          ),
        ),

        // Lista de propuestas
        Expanded(
          child: ListView.separated(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
            itemCount: _propuestas.length,
            separatorBuilder: (_, _) => const SizedBox(height: 10),
            itemBuilder: (context, index) {
              final p = _propuestas[index];
              final estadoColor = _colorEstado(p.estado);

              return Container(
                decoration: BoxDecoration(
                  color: AppColors.card,
                  borderRadius: BorderRadius.circular(14),
                  border: Border.all(color: AppColors.border),
                ),
                child: ListTile(
                  contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  leading: Container(
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: estadoColor.withAlpha(25),
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: Icon(Icons.auto_awesome_rounded, color: estadoColor, size: 20),
                  ),
                  title: Row(
                    children: [
                      Expanded(
                        child: Text(
                          p.titulo,
                          style: AppTypography.h3,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 7, vertical: 2),
                        decoration: BoxDecoration(
                          color: estadoColor.withAlpha(25),
                          borderRadius: BorderRadius.circular(6),
                          border: Border.all(color: estadoColor.withAlpha(80)),
                        ),
                        child: Text(
                          p.estado.toUpperCase(),
                          style: AppTypography.bodySmall.copyWith(
                            color: estadoColor,
                            fontWeight: FontWeight.w700,
                            fontSize: 9.5,
                          ),
                        ),
                      ),
                    ],
                  ),
                  subtitle: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const SizedBox(height: 4),
                      Text(
                        p.descripcionProblema,
                        style: AppTypography.bodySmall,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                      ),
                      const SizedBox(height: 6),
                      Row(
                        children: [
                          Text('ID: #${p.idPropuesta}', style: AppTypography.code.copyWith(fontSize: 10, color: AppColors.textMuted)),
                          const SizedBox(width: 8),
                          Text('• ${p.totalOperaciones} ops UML', style: AppTypography.bodySmall.copyWith(fontSize: 10)),
                          const Spacer(),
                          Text(p.fechaCreacionFormateada, style: AppTypography.bodySmall.copyWith(fontSize: 10)),
                        ],
                      ),
                    ],
                  ),
                  onTap: () {
                    showModalBottomSheet(
                      context: context,
                      isScrollControlled: true,
                      backgroundColor: Colors.transparent,
                      builder: (_) => ProposalDetailSheet(propuesta: p),
                    );
                  },
                ),
              );
            },
          ),
        ),
      ],
    );
  }
}
