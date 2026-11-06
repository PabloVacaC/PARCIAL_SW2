import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/widgets/read_only_badge.dart';
import '../models/uml_model.dart';

class UmlClassDetailSheet extends StatelessWidget {
  final UmlClaseElement clase;
  final List<UmlRelacionElement> relaciones;

  const UmlClassDetailSheet({
    super.key,
    required this.clase,
    required this.relaciones,
  });

  bool _coincide(String val, String targetId, String targetNombre) {
    if (val.isEmpty) return false;
    final v = val.toLowerCase().trim();
    return v == targetId.toLowerCase().trim() || v == targetNombre.toLowerCase().trim();
  }

  @override
  Widget build(BuildContext context) {
    final relacionesClase = relaciones.where(
      (r) => _coincide(r.origenId, clase.id, clase.nombre) ||
          _coincide(r.destinoId, clase.id, clase.nombre),
    ).toList();

    final screenHeight = MediaQuery.of(context).size.height;

    return Container(
      constraints: BoxConstraints(
        maxHeight: screenHeight * 0.88,
      ),
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
      decoration: const BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      child: SafeArea(
        top: false,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Barra de arrastre
            Center(
              child: Container(
                width: 38,
                height: 4,
                decoration: BoxDecoration(
                  color: AppColors.border,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Cabecera de la Clase
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: AppColors.primary.withAlpha(40),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: AppColors.primary.withAlpha(80)),
                  ),
                  child: const Icon(Icons.class_outlined, color: AppColors.primaryLight, size: 24),
                ),
                const SizedBox(width: 14),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(clase.nombre, style: AppTypography.h2),
                      const SizedBox(height: 2),
                      Text(
                        '«${clase.tipo}» • Pos: (${clase.x.toInt()}, ${clase.y.toInt()}) • Tam: ${clase.width.toInt()}x${clase.height.toInt()}',
                        style: AppTypography.bodySmall,
                      ),
                    ],
                  ),
                ),
                const ReadOnlyBadge(compact: true),
              ],
            ),
            const SizedBox(height: 16),

            const Divider(color: AppColors.border, height: 1),
            const SizedBox(height: 12),

            // Cuerpo desplazable
            Flexible(
              child: SingleChildScrollView(
                physics: const BouncingScrollPhysics(),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Sección 1: Atributos
                    Row(
                      children: [
                        Text('Atributos (${clase.atributos.length})', style: AppTypography.label),
                        const Spacer(),
                        if (clase.atributos.isNotEmpty)
                          Text('Visibilidad / Tipo', style: AppTypography.bodySmall.copyWith(fontSize: 10)),
                      ],
                    ),
                    const SizedBox(height: 8),
                    if (clase.atributos.isEmpty)
                      Container(
                        width: double.infinity,
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: AppColors.card,
                          borderRadius: BorderRadius.circular(8),
                          border: Border.all(color: AppColors.border),
                        ),
                        child: Text('Esta clase no posee atributos definidos', style: AppTypography.bodyMedium),
                      )
                    else
                      ...clase.atributos.map(
                        (a) => Container(
                          margin: const EdgeInsets.only(bottom: 6),
                          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 9),
                          decoration: BoxDecoration(
                            color: AppColors.card,
                            borderRadius: BorderRadius.circular(8),
                            border: Border.all(color: AppColors.border),
                          ),
                          child: Row(
                            children: [
                              Container(
                                width: 22,
                                height: 22,
                                alignment: Alignment.center,
                                decoration: BoxDecoration(
                                  color: a.visibilidad == 'PRIVATE'
                                      ? AppColors.error.withAlpha(30)
                                      : AppColors.success.withAlpha(30),
                                  borderRadius: BorderRadius.circular(6),
                                ),
                                child: Text(
                                  a.simboloVisibilidad,
                                  style: TextStyle(
                                    color: a.visibilidad == 'PRIVATE' ? AppColors.error : AppColors.success,
                                    fontWeight: FontWeight.w800,
                                    fontSize: 13,
                                  ),
                                ),
                              ),
                              const SizedBox(width: 10),
                              Expanded(
                                child: Text(
                                  a.nombre,
                                  style: AppTypography.bodyMedium.copyWith(
                                    color: AppColors.textPrimary,
                                    fontWeight: FontWeight.w600,
                                  ),
                                ),
                              ),
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                                decoration: BoxDecoration(
                                  color: AppColors.surface,
                                  borderRadius: BorderRadius.circular(6),
                                  border: Border.all(color: AppColors.border),
                                ),
                                child: Text(
                                  a.tipo,
                                  style: AppTypography.code.copyWith(fontSize: 11, color: AppColors.accent),
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),

                    const SizedBox(height: 18),

                    // Sección 2: Métodos / Operaciones
                    Row(
                      children: [
                        Text('Métodos / Operaciones (${clase.operaciones.length})', style: AppTypography.label),
                        const Spacer(),
                        if (clase.operaciones.isNotEmpty)
                          Text('Firma y retorno', style: AppTypography.bodySmall.copyWith(fontSize: 10)),
                      ],
                    ),
                    const SizedBox(height: 8),
                    if (clase.operaciones.isEmpty)
                      Container(
                        width: double.infinity,
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: AppColors.card,
                          borderRadius: BorderRadius.circular(8),
                          border: Border.all(color: AppColors.border),
                        ),
                        child: Text('Esta clase no posee métodos definidos', style: AppTypography.bodyMedium),
                      )
                    else
                      ...clase.operaciones.map(
                        (o) => Container(
                          margin: const EdgeInsets.only(bottom: 6),
                          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 9),
                          decoration: BoxDecoration(
                            color: AppColors.card,
                            borderRadius: BorderRadius.circular(8),
                            border: Border.all(color: AppColors.border),
                          ),
                          child: Row(
                            children: [
                              Container(
                                width: 22,
                                height: 22,
                                alignment: Alignment.center,
                                decoration: BoxDecoration(
                                  color: AppColors.primary.withAlpha(30),
                                  borderRadius: BorderRadius.circular(6),
                                ),
                                child: Text(
                                  o.simboloVisibilidad,
                                  style: const TextStyle(
                                    color: AppColors.primaryLight,
                                    fontWeight: FontWeight.w800,
                                    fontSize: 13,
                                  ),
                                ),
                              ),
                              const SizedBox(width: 10),
                              Expanded(
                                child: Text(
                                  '${o.nombre}(${o.parametros.join(', ')})',
                                  style: AppTypography.bodyMedium.copyWith(color: AppColors.textPrimary),
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                              const SizedBox(width: 8),
                              Text(
                                '→ ${o.tipoRetorno}',
                                style: AppTypography.code.copyWith(fontSize: 11, color: AppColors.secondary),
                              ),
                            ],
                          ),
                        ),
                      ),

                    const SizedBox(height: 18),

                    // Sección 3: Relaciones Conectadas y Cardinalidades
                    Text('Relaciones y Cardinalidades (${relacionesClase.length})', style: AppTypography.label),
                    const SizedBox(height: 8),
                    if (relacionesClase.isEmpty)
                      Container(
                        width: double.infinity,
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: AppColors.card,
                          borderRadius: BorderRadius.circular(8),
                          border: Border.all(color: AppColors.border),
                        ),
                        child: Text('No hay relaciones conectadas a esta clase', style: AppTypography.bodyMedium),
                      )
                    else
                      ...relacionesClase.map((r) {
                        final esOrigen = _coincide(r.origenId, clase.id, clase.nombre);
                        final miCard = esOrigen ? r.multiplicidadOrigen : r.multiplicidadDestino;
                        final otraCard = esOrigen ? r.multiplicidadDestino : r.multiplicidadOrigen;
                        final miRol = esOrigen ? r.rolOrigen : r.rolDestino;
                        final otroRol = esOrigen ? r.rolDestino : r.rolOrigen;
                        final otroId = esOrigen ? r.destinoId : r.origenId;

                        return Container(
                          margin: const EdgeInsets.only(bottom: 8),
                          padding: const EdgeInsets.all(12),
                          decoration: BoxDecoration(
                            color: AppColors.card,
                            borderRadius: BorderRadius.circular(10),
                            border: Border.all(color: AppColors.accent.withAlpha(80)),
                          ),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  Container(
                                    padding: const EdgeInsets.symmetric(horizontal: 7, vertical: 3),
                                    decoration: BoxDecoration(
                                      color: AppColors.accent.withAlpha(35),
                                      borderRadius: BorderRadius.circular(6),
                                    ),
                                    child: Text(
                                      r.tipo.replaceAll('_', ' '),
                                      style: AppTypography.code.copyWith(
                                        fontSize: 10,
                                        color: AppColors.accent,
                                        fontWeight: FontWeight.w700,
                                      ),
                                    ),
                                  ),
                                  const SizedBox(width: 8),
                                  Expanded(
                                    child: Text(
                                      esOrigen ? 'Hacia destino' : 'Desde origen',
                                      style: AppTypography.bodySmall,
                                    ),
                                  ),
                                  if (miCard != null || otraCard != null)
                                    Container(
                                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                                      decoration: BoxDecoration(
                                        color: AppColors.surface,
                                        borderRadius: BorderRadius.circular(6),
                                        border: Border.all(color: AppColors.border),
                                      ),
                                      child: Text(
                                        'Card: [${miCard ?? '?'}] ➔ [${otraCard ?? '?'}]',
                                        style: AppTypography.code.copyWith(
                                          fontSize: 11,
                                          color: AppColors.primaryLight,
                                          fontWeight: FontWeight.w700,
                                        ),
                                      ),
                                    ),
                                ],
                              ),
                              const SizedBox(height: 6),
                              Row(
                                children: [
                                  const Icon(Icons.arrow_right_alt_rounded, size: 18, color: AppColors.textMuted),
                                  const SizedBox(width: 4),
                                  Expanded(
                                    child: Text(
                                      'Conecta con: $otroId',
                                      style: AppTypography.bodyMedium.copyWith(color: AppColors.textPrimary),
                                      overflow: TextOverflow.ellipsis,
                                    ),
                                  ),
                                ],
                              ),
                              if (miRol != null || otroRol != null) ...[
                                const SizedBox(height: 4),
                                Text(
                                  'Rol: ${miRol != null ? '+$miRol' : ''} ${otroRol != null ? '(hacia +$otroRol)' : ''}',
                                  style: AppTypography.bodySmall.copyWith(
                                    color: AppColors.textSecondary,
                                    fontStyle: FontStyle.italic,
                                  ),
                                ),
                              ],
                            ],
                          ),
                        );
                      }),

                    const SizedBox(height: 20),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
