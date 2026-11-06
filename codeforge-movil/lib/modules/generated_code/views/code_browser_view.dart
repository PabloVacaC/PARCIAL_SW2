import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/widgets/empty_state_view.dart';
import '../../../core/widgets/error_retry_view.dart';
import '../../../core/widgets/read_only_badge.dart';
import '../../uml/services/uml_service.dart';
import '../models/code_file_model.dart';
import '../services/generated_code_service.dart';
import 'code_viewer_view.dart';

class CodeBrowserView extends StatefulWidget {
  final int idProyecto;

  const CodeBrowserView({super.key, required this.idProyecto});

  @override
  State<CodeBrowserView> createState() => _CodeBrowserViewState();
}

class _CodeBrowserViewState extends State<CodeBrowserView> {
  final _codeService = GeneratedCodeService();
  final _umlService = UmlService();

  List<CodeFileModel> _archivos = [];
  bool _cargando = true;
  String? _error;
  String _filtro = '';

  @override
  void initState() {
    super.initState();
    _cargarCodigo();
  }

  void _cargarCodigo() async {
    setState(() {
      _cargando = true;
      _error = null;
    });

    try {
      final modelos = await _umlService.obtenerModelosProyecto(widget.idProyecto);
      if (modelos.isEmpty) {
        setState(() {
          _archivos = [];
          _cargando = false;
        });
        return;
      }

      final idModelo = modelos.first.idModelo;
      final lista = await _codeService.obtenerArchivosGenerados(idModelo);
      setState(() {
        _archivos = lista;
        _cargando = false;
      });
    } catch (e) {
      setState(() {
        _cargando = false;
        _error = e.toString().replaceFirst('Exception: ', '');
      });
    }
  }

  IconData _iconoTipo(String tipo) {
    switch (tipo.toUpperCase()) {
      case 'JAVA':
        return Icons.coffee_rounded;
      case 'DART':
        return Icons.flutter_dash_rounded;
      case 'SQL':
        return Icons.storage_rounded;
      case 'YAML':
      case 'JSON':
        return Icons.settings_rounded;
      default:
        return Icons.description_outlined;
    }
  }

  Color _colorTipo(String tipo) {
    switch (tipo.toUpperCase()) {
      case 'JAVA':
        return const Color(0xFFF97316); // Naranja Java
      case 'DART':
        return AppColors.accent; // Cyan Flutter
      case 'SQL':
        return AppColors.primaryLight;
      case 'YAML':
      case 'JSON':
        return AppColors.warning;
      default:
        return AppColors.textMuted;
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_cargando) {
      return const Center(child: CircularProgressIndicator(color: AppColors.primary));
    }

    if (_error != null) {
      return ErrorRetryView(mensaje: _error!, onRetry: _cargarCodigo);
    }

    if (_archivos.isEmpty) {
      return const EmptyStateView(
        icon: Icons.code_off_rounded,
        title: 'Sin código generado aún',
        description:
            'Aún no se ha generado código Spring Boot o Flutter para este modelo UML. Genéralo desde la plataforma web.',
      );
    }

    final filtrados = _archivos.where((f) {
      return f.nombre.toLowerCase().contains(_filtro.toLowerCase()) ||
          f.ruta.toLowerCase().contains(_filtro.toLowerCase());
    }).toList();

    return Column(
      children: [
        // Buscador de archivos
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          child: TextField(
            onChanged: (v) => setState(() => _filtro = v),
            style: AppTypography.bodyMedium,
            decoration: InputDecoration(
              hintText: 'Buscar clases, interfaces, controllers...',
              prefixIcon: const Icon(Icons.search_rounded, color: AppColors.textMuted, size: 20),
              suffixIcon: _filtro.isNotEmpty
                  ? IconButton(
                      icon: const Icon(Icons.close_rounded, size: 18),
                      onPressed: () => setState(() => _filtro = ''),
                    )
                  : null,
            ),
          ),
        ),

        // Contador de archivos y badge
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
          child: Row(
            children: [
              Text(
                '${filtrados.length} archivos disponibles',
                style: AppTypography.bodySmall,
              ),
              const Spacer(),
              const ReadOnlyBadge(compact: true),
            ],
          ),
        ),
        const SizedBox(height: 6),

        // Lista de archivos
        Expanded(
          child: ListView.separated(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            itemCount: filtrados.length,
            separatorBuilder: (_, _) => const SizedBox(height: 8),
            itemBuilder: (context, index) {
              final archivo = filtrados[index];
              return Container(
                decoration: BoxDecoration(
                  color: AppColors.card,
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: AppColors.border),
                ),
                child: ListTile(
                  contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 4),
                  leading: Container(
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: AppColors.surface,
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Icon(_iconoTipo(archivo.tipo), color: _colorTipo(archivo.tipo), size: 20),
                  ),
                  title: Text(archivo.nombre, style: AppTypography.h3),
                  subtitle: Text(
                    archivo.ruta,
                    style: AppTypography.bodySmall,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                  trailing: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                    decoration: BoxDecoration(
                      color: AppColors.surface,
                      borderRadius: BorderRadius.circular(4),
                    ),
                    child: Text(
                      archivo.tipo,
                      style: AppTypography.code.copyWith(fontSize: 10, color: _colorTipo(archivo.tipo)),
                    ),
                  ),
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => CodeViewerView(archivo: archivo),
                      ),
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
