import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../models/proposal_model.dart';

class ProposalDetailSheet extends StatelessWidget {
  final ProposalModel propuesta;

  const ProposalDetailSheet({super.key, required this.propuesta});

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
    final estadoColor = _colorEstado(propuesta.estado);

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
      decoration: const BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      child: SafeArea(
        top: false,
        child: SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              // Barra de arrastre
              Center(
                child: Container(
                  width: 36,
                  height: 4,
                  decoration: BoxDecoration(
                    color: AppColors.border,
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
              ),
              const SizedBox(height: 16),

              // Cabecera: Título y Estado
              Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: estadoColor.withAlpha(30),
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: Icon(Icons.auto_fix_high_rounded, color: estadoColor, size: 22),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(propuesta.titulo, style: AppTypography.h3),
                        Text(
                          'ID: #${propuesta.idPropuesta} • Solicitado por ${propuesta.nombreSolicitante}',
                          style: AppTypography.bodySmall,
                        ),
                      ],
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: estadoColor.withAlpha(30),
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(color: estadoColor.withAlpha(100)),
                    ),
                    child: Text(
                      propuesta.estado.toUpperCase(),
                      style: AppTypography.bodySmall.copyWith(
                        color: estadoColor,
                        fontWeight: FontWeight.w700,
                        fontSize: 10,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 18),

              // Problema diagnosticado
              Text('Problema / Diagnóstico', style: AppTypography.label),
              const SizedBox(height: 6),
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: AppColors.card,
                  borderRadius: BorderRadius.circular(10),
                  border: Border.all(color: AppColors.border),
                ),
                child: Text(propuesta.descripcionProblema, style: AppTypography.bodyMedium),
              ),
              const SizedBox(height: 14),

              // Solución propuesta
              Text('Solución Propuesta', style: AppTypography.label),
              const SizedBox(height: 6),
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: AppColors.card,
                  borderRadius: BorderRadius.circular(10),
                  border: Border.all(color: AppColors.border),
                ),
                child: Text(propuesta.solucionPropuesta, style: AppTypography.bodyMedium),
              ),
              const SizedBox(height: 14),

              // Riesgos e impactos
              if (propuesta.riesgosEfectos.isNotEmpty) ...[
                Text('Riesgos / Efectos Colaterales', style: AppTypography.label),
                const SizedBox(height: 6),
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: AppColors.card,
                    borderRadius: BorderRadius.circular(10),
                    border: Border.all(color: AppColors.border),
                  ),
                  child: Text(propuesta.riesgosEfectos, style: AppTypography.bodyMedium),
                ),
                const SizedBox(height: 14),
              ],

              // Operaciones UML incluidas
              Text('Operaciones UML (${propuesta.totalOperaciones})', style: AppTypography.label),
              const SizedBox(height: 6),
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: const Color(0xFF1E1E1E),
                  borderRadius: BorderRadius.circular(10),
                  border: Border.all(color: AppColors.border),
                ),
                child: Text(
                  propuesta.operacionesJson,
                  style: AppTypography.code.copyWith(fontSize: 11),
                ),
              ),
              const SizedBox(height: 20),

              // Banner de restricción de Solo Lectura
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: AppColors.readOnlyBadgeBg,
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: AppColors.readOnlyBadgeBorder),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.info_outline_rounded, size: 20, color: AppColors.readOnlyPrimary),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Text(
                        'Las decisiones de aprobación y aplicación de propuestas solo pueden realizarse desde la plataforma web.',
                        style: AppTypography.bodySmall.copyWith(color: AppColors.readOnlyPrimary),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),
            ],
          ),
        ),
      ),
    );
  }
}
