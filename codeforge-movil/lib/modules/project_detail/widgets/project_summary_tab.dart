import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/widgets/read_only_badge.dart';
import '../../projects/models/colaborador_model.dart';
import '../../projects/models/proyecto_model.dart';
import '../../projects/services/proyecto_service.dart';

class ProjectSummaryTab extends StatefulWidget {
  final ProyectoModel proyecto;

  const ProjectSummaryTab({super.key, required this.proyecto});

  @override
  State<ProjectSummaryTab> createState() => _ProjectSummaryTabState();
}

class _ProjectSummaryTabState extends State<ProjectSummaryTab> {
  final _proyectoService = ProyectoService();
  List<ColaboradorModel> _colaboradores = [];
  bool _cargandoColabs = true;

  @override
  void initState() {
    super.initState();
    _cargarColaboradores();
  }

  void _cargarColaboradores() async {
    try {
      final list = await _proyectoService.obtenerColaboradores(widget.proyecto.idProyecto);
      if (mounted) {
        setState(() {
          _colaboradores = list;
          _cargandoColabs = false;
        });
      }
    } catch (_) {
      if (mounted) {
        setState(() => _cargandoColabs = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final p = widget.proyecto;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Banner de Identidad del Proyecto
          Container(
            padding: const EdgeInsets.all(18),
            decoration: BoxDecoration(
              gradient: AppColors.brandGradient,
              borderRadius: BorderRadius.circular(20),
              boxShadow: [
                BoxShadow(
                  color: AppColors.primary.withAlpha(80),
                  blurRadius: 18,
                  offset: const Offset(0, 6),
                ),
              ],
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                      decoration: BoxDecoration(
                        color: Colors.white.withAlpha(40),
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: Text(
                        'ID: #PRJ-${p.idProyecto.toString().padLeft(4, '0')}',
                        style: AppTypography.code.copyWith(color: Colors.white, fontSize: 11),
                      ),
                    ),
                    const ReadOnlyBadge(compact: true),
                  ],
                ),
                const SizedBox(height: 14),
                Text(
                  p.nombre,
                  style: AppTypography.h1.copyWith(color: Colors.white, fontSize: 24),
                ),
                const SizedBox(height: 8),
                Text(
                  p.descripcion,
                  style: AppTypography.bodyMedium.copyWith(color: Colors.white.withAlpha(220)),
                ),
                const SizedBox(height: 16),
                Row(
                  children: [
                    const Icon(Icons.shield_outlined, color: Colors.white70, size: 16),
                    const SizedBox(width: 6),
                    Text(
                      'Propietario: ${p.nombrePropietario.isNotEmpty ? p.nombrePropietario : p.usernamePropietario}',
                      style: AppTypography.bodySmall.copyWith(color: Colors.white, fontWeight: FontWeight.w600),
                    ),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),

          // Métricas de Progreso y Estado
          Row(
            children: [
              Expanded(
                child: _tarjetaMetrica(
                  titulo: 'Estado',
                  valor: p.estado.toUpperCase(),
                  icono: Icons.flag_rounded,
                  color: AppColors.success,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: _tarjetaMetrica(
                  titulo: 'Equipo',
                  valor: '${_colaboradores.length + 1} miembros',
                  icono: Icons.groups_rounded,
                  color: AppColors.accent,
                ),
              ),
            ],
          ),
          const SizedBox(height: 20),

          // Fechas del Proyecto
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: AppColors.card,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: AppColors.border),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Trazabilidad Temporal', style: AppTypography.label),
                const SizedBox(height: 12),
                Row(
                  children: [
                    const Icon(Icons.calendar_today_rounded, size: 16, color: AppColors.textMuted),
                    const SizedBox(width: 8),
                    Text('Fecha de creación:', style: AppTypography.bodySmall),
                    const Spacer(),
                    Text(p.fechaCreacionFormateada, style: AppTypography.bodyMedium),
                  ],
                ),
                const SizedBox(height: 8),
                const Divider(color: AppColors.border),
                const SizedBox(height: 8),
                Row(
                  children: [
                    const Icon(Icons.update_rounded, size: 16, color: AppColors.textMuted),
                    const SizedBox(width: 8),
                    Text('Última actualización:', style: AppTypography.bodySmall),
                    const Spacer(),
                    Text(p.fechaActualizacionFormateada, style: AppTypography.bodyMedium),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),

          // Integrantes y Roles
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: AppColors.card,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: AppColors.border),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text('Integrantes y Roles', style: AppTypography.h3),
                    Text('${_colaboradores.length + 1} en total', style: AppTypography.bodySmall),
                  ],
                ),
                const SizedBox(height: 12),

                // Fila del Propietario
                ListTile(
                  contentPadding: EdgeInsets.zero,
                  leading: CircleAvatar(
                    backgroundColor: AppColors.primary,
                    child: Text(
                      (p.nombrePropietario.isNotEmpty ? p.nombrePropietario : 'P')[0].toUpperCase(),
                      style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                    ),
                  ),
                  title: Text(
                    p.nombrePropietario.isNotEmpty ? p.nombrePropietario : p.usernamePropietario,
                    style: AppTypography.bodyLarge.copyWith(fontWeight: FontWeight.w600),
                  ),
                  subtitle: Text('@${p.usernamePropietario}', style: AppTypography.bodySmall),
                  trailing: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: AppColors.primary.withAlpha(30),
                      borderRadius: BorderRadius.circular(6),
                      border: Border.all(color: AppColors.primary.withAlpha(80)),
                    ),
                    child: Text(
                      'PROPIETARIO',
                      style: AppTypography.code.copyWith(fontSize: 10, color: AppColors.primaryLight),
                    ),
                  ),
                ),

                // Lista de Colaboradores
                if (_cargandoColabs)
                  const Center(
                    child: Padding(
                      padding: EdgeInsets.all(12.0),
                      child: CircularProgressIndicator(strokeWidth: 2, color: AppColors.primary),
                    ),
                  )
                else
                  ..._colaboradores.map(
                    (colab) => ListTile(
                      contentPadding: EdgeInsets.zero,
                      leading: CircleAvatar(
                        backgroundColor: AppColors.surface,
                        child: Text(
                          colab.nombreCompleto.isNotEmpty ? colab.nombreCompleto[0].toUpperCase() : 'C',
                          style: const TextStyle(color: AppColors.textPrimary, fontWeight: FontWeight.bold),
                        ),
                      ),
                      title: Text(
                        colab.nombreCompleto.isNotEmpty ? colab.nombreCompleto : colab.username,
                        style: AppTypography.bodyLarge,
                      ),
                      subtitle: Text('@${colab.username}', style: AppTypography.bodySmall),
                      trailing: Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                        decoration: BoxDecoration(
                          color: AppColors.surface,
                          borderRadius: BorderRadius.circular(6),
                          border: Border.all(color: AppColors.border),
                        ),
                        child: Text(
                          colab.rol.toUpperCase(),
                          style: AppTypography.code.copyWith(fontSize: 10, color: AppColors.textSecondary),
                        ),
                      ),
                    ),
                  ),
              ],
            ),
          ),
          const SizedBox(height: 24),
        ],
      ),
    );
  }

  Widget _tarjetaMetrica({
    required String titulo,
    required String valor,
    required IconData icono,
    required Color color,
  }) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.card,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: color.withAlpha(30),
              borderRadius: BorderRadius.circular(10),
            ),
            child: Icon(icono, color: color, size: 20),
          ),
          const SizedBox(height: 12),
          Text(titulo, style: AppTypography.bodySmall),
          const SizedBox(height: 4),
          Text(valor, style: AppTypography.h3),
        ],
      ),
    );
  }
}
