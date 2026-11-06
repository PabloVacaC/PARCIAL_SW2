import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/theme/app_typography.dart';
import '../../models/proyecto_model.dart';

class ProyectoCard extends StatelessWidget {
  final ProyectoModel proyecto;
  final VoidCallback onTap;

  const ProyectoCard({
    super.key,
    required this.proyecto,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final bool esActivo = proyecto.estado.toUpperCase() == 'ACTIVO';

    return GestureDetector(
      onTap: onTap,
      child: Container(
        decoration: BoxDecoration(
          color: AppColors.card,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: AppColors.border, width: 1),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withAlpha(40),
              blurRadius: 10,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Fila superior: Icono representativo, código, estado
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Container(
                    width: 44,
                    height: 44,
                    decoration: BoxDecoration(
                      gradient: AppColors.cardGlow,
                      color: AppColors.surface,
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: AppColors.primary.withAlpha(80), width: 1),
                    ),
                    child: const Icon(
                      Icons.folder_special_rounded,
                      color: AppColors.primaryLight,
                      size: 24,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          proyecto.nombre,
                          style: AppTypography.h3,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                        const SizedBox(height: 2),
                        Text(
                          'ID: #PRJ-${proyecto.idProyecto.toString().padLeft(4, '0')} • Por ${proyecto.nombrePropietario.isNotEmpty ? proyecto.nombrePropietario : proyecto.usernamePropietario}',
                          style: AppTypography.bodySmall,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ],
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: esActivo ? AppColors.successBg : AppColors.warningBg,
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(
                        color: esActivo ? AppColors.success.withAlpha(80) : AppColors.warning.withAlpha(80),
                        width: 1,
                      ),
                    ),
                    child: Text(
                      proyecto.estado.toUpperCase(),
                      style: AppTypography.bodySmall.copyWith(
                        color: esActivo ? AppColors.success : AppColors.warning,
                        fontWeight: FontWeight.w700,
                        fontSize: 10,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),

              // Descripción
              Text(
                proyecto.descripcion,
                style: AppTypography.bodyMedium,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
              const SizedBox(height: 16),

              // Indicador de Progreso
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text('Progreso del modelado', style: AppTypography.bodySmall),
                      Text('65%', style: AppTypography.bodySmall.copyWith(color: AppColors.primaryLight, fontWeight: FontWeight.w600)),
                    ],
                  ),
                  const SizedBox(height: 6),
                  ClipRRect(
                    borderRadius: BorderRadius.circular(4),
                    child: const LinearProgressIndicator(
                      value: 0.65,
                      minHeight: 4,
                      backgroundColor: AppColors.surface,
                      valueColor: AlwaysStoppedAnimation<Color>(AppColors.primary),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 14),

              const Divider(color: AppColors.border, height: 1),
              const SizedBox(height: 12),

              // Fila inferior: Integrantes y fecha
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Row(
                    children: [
                      const Icon(Icons.group_outlined, size: 15, color: AppColors.textSecondary),
                      const SizedBox(width: 5),
                      Text(
                        '${proyecto.cantidadIntegrantes} integrantes',
                        style: AppTypography.bodySmall.copyWith(color: AppColors.textSecondary),
                      ),
                    ],
                  ),
                  Row(
                    children: [
                      const Icon(Icons.access_time_rounded, size: 14, color: AppColors.textMuted),
                      const SizedBox(width: 4),
                      Text(
                        proyecto.fechaActualizacionFormateada,
                        style: AppTypography.bodySmall,
                      ),
                    ],
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
