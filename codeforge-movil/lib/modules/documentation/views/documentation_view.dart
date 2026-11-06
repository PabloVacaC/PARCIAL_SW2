import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/widgets/error_retry_view.dart';
import '../../../core/widgets/read_only_badge.dart';
import '../../projects/services/proyecto_service.dart';

class DocumentationView extends StatefulWidget {
  final int idProyecto;

  const DocumentationView({super.key, required this.idProyecto});

  @override
  State<DocumentationView> createState() => _DocumentationViewState();
}

class _DocumentationViewState extends State<DocumentationView> {
  final _proyectoService = ProyectoService();

  Map<String, dynamic> _doc = {};
  bool _cargando = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _cargarDoc();
  }

  void _cargarDoc() async {
    setState(() {
      _cargando = true;
      _error = null;
    });

    try {
      final doc = await _proyectoService.obtenerDocumentacion(widget.idProyecto);
      setState(() {
        _doc = doc;
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
      return ErrorRetryView(mensaje: _error!, onRetry: _cargarDoc);
    }

    final reqFuncionales = (_doc['requisitosFuncionales'] as List?) ?? [];
    final reqNoFuncionales = (_doc['requisitosNoFuncionales'] as List?) ?? [];
    final arquitectura = _doc['arquitectura']?.toString() ??
        'Arquitectura por capas: REST Controllers, Service Layer, Spring Data JPA, PostgreSQL.';

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Banner de documentación de solo lectura
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              gradient: AppColors.cardGlow,
              color: AppColors.card,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: AppColors.border),
            ),
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: AppColors.primary.withAlpha(40),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: const Icon(Icons.menu_book_rounded, color: AppColors.primaryLight, size: 24),
                ),
                const SizedBox(width: 14),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('Especificación Técnica', style: AppTypography.h3),
                      const SizedBox(height: 2),
                      Text(
                        'Documentación y requisitos del proyecto en modo de consulta.',
                        style: AppTypography.bodySmall,
                      ),
                    ],
                  ),
                ),
                const ReadOnlyBadge(compact: true),
              ],
            ),
          ),
          const SizedBox(height: 20),

          // Tarjeta: Decisiones de Arquitectura
          _seccionTarjeta(
            titulo: 'Decisiones de Arquitectura',
            icono: Icons.account_tree_outlined,
            contenido: Text(
              arquitectura,
              style: AppTypography.bodyMedium.copyWith(color: AppColors.textPrimary, height: 1.5),
            ),
          ),
          const SizedBox(height: 16),

          // Tarjeta: Requisitos Funcionales
          _seccionTarjeta(
            titulo: 'Requisitos Funcionales (${reqFuncionales.length})',
            icono: Icons.fact_check_outlined,
            contenido: Column(
              children: reqFuncionales.map((rf) => Padding(
                padding: const EdgeInsets.symmetric(vertical: 4),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Icon(Icons.check_circle_outline_rounded, size: 16, color: AppColors.success),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(rf.toString(), style: AppTypography.bodyMedium),
                    ),
                  ],
                ),
              )).toList(),
            ),
          ),
          const SizedBox(height: 16),

          // Tarjeta: Requisitos No Funcionales
          _seccionTarjeta(
            titulo: 'Requisitos No Funcionales (${reqNoFuncionales.length})',
            icono: Icons.security_outlined,
            contenido: Column(
              children: reqNoFuncionales.map((rnf) => Padding(
                padding: const EdgeInsets.symmetric(vertical: 4),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Icon(Icons.shield_outlined, size: 16, color: AppColors.accent),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(rnf.toString(), style: AppTypography.bodyMedium),
                    ),
                  ],
                ),
              )).toList(),
            ),
          ),
          const SizedBox(height: 24),
        ],
      ),
    );
  }

  Widget _seccionTarjeta({
    required String titulo,
    required IconData icono,
    required Widget contenido,
  }) {
    return Container(
      width: double.infinity,
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
            children: [
              Icon(icono, size: 20, color: AppColors.primaryLight),
              const SizedBox(width: 8),
              Text(titulo, style: AppTypography.h3),
            ],
          ),
          const SizedBox(height: 12),
          const Divider(color: AppColors.border),
          const SizedBox(height: 12),
          contenido,
        ],
      ),
    );
  }
}
