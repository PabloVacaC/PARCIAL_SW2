import 'dart:math';
import 'package:flutter/material.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/widgets/empty_state_view.dart';
import '../../../core/widgets/error_retry_view.dart';
import '../models/uml_model.dart';
import '../services/uml_service.dart';
import '../widgets/uml_canvas_painter.dart';
import 'uml_class_detail_sheet.dart';

class UmlDiagramView extends StatefulWidget {
  final int idProyecto;

  const UmlDiagramView({super.key, required this.idProyecto});

  @override
  State<UmlDiagramView> createState() => _UmlDiagramViewState();
}

class _UmlDiagramViewState extends State<UmlDiagramView> {
  final _umlService = UmlService();
  final _transformationController = TransformationController();

  List<UmlDiagramaModel> _modelos = [];
  UmlDiagramaModel? _modeloSeleccionado;
  bool _cargando = true;
  String? _error;
  bool _modoLienzo = true; // true = Canvas interactivo, false = Lista estructurada
  String _filtroClases = '';
  String? _claseActivaId;
  bool _ajustadoInicial = false;

  @override
  void initState() {
    super.initState();
    _cargarModelos();
  }

  void _cargarModelos() async {
    setState(() {
      _cargando = true;
      _error = null;
      _ajustadoInicial = false;
    });

    try {
      final modelos = await _umlService.obtenerModelosProyecto(widget.idProyecto);
      if (mounted) {
        setState(() {
          _modelos = modelos;
          if (modelos.isNotEmpty) {
            _modeloSeleccionado = modelos.first;
          }
          _cargando = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _cargando = false;
          _error = e.toString().replaceFirst('Exception: ', '');
        });
      }
    }
  }

  void _abrirDetalleClase(UmlClaseElement clase) {
    if (_modeloSeleccionado == null) return;
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => UmlClassDetailSheet(
        clase: clase,
        relaciones: _modeloSeleccionado!.relaciones,
      ),
    );
  }

  void _ajustarDiagramaAPantalla(Size viewportSize, UmlDiagramaModel modelo) {
    if (modelo.clases.isEmpty || viewportSize.width <= 0 || viewportSize.height <= 0) return;

    double minX = double.infinity;
    double minY = double.infinity;
    double maxX = -double.infinity;
    double maxY = -double.infinity;

    for (final c in modelo.clases) {
      if (c.x < minX) minX = c.x;
      if (c.y < minY) minY = c.y;
      if (c.x + c.width > maxX) maxX = c.x + c.width;
      if (c.y + c.height > maxY) maxY = c.y + c.height;
    }

    if (minX.isInfinite) {
      minX = 0;
      minY = 0;
      maxX = 800;
      maxY = 800;
    }

    // Agregar margen
    const padding = 60.0;
    final contentW = (maxX - minX) + padding * 2;
    final contentH = (maxY - minY) + padding * 2;

    final scaleX = viewportSize.width / contentW;
    final scaleY = viewportSize.height / contentH;
    final scale = min(scaleX, scaleY).clamp(0.2, 1.0);

    final double targetX = (viewportSize.width - (maxX + minX) * scale) / 2.0;
    final double targetY = (viewportSize.height - (maxY + minY) * scale) / 2.0;

    final matrix = Matrix4.identity()
      ..setTranslationRaw(targetX, targetY, 0.0)
      ..multiply(Matrix4.diagonal3Values(scale, scale, 1.0));

    _transformationController.value = matrix;
  }

  void _zoom(double factor) {
    final currentMatrix = _transformationController.value;
    final currentScale = currentMatrix.getMaxScaleOnAxis();
    final newScale = (currentScale * factor).clamp(0.15, 3.5);
    final relativeScale = newScale / currentScale;

    final center = Offset(
      MediaQuery.of(context).size.width / 2,
      MediaQuery.of(context).size.height / 2,
    );

    final matrix = Matrix4.identity()
      ..setTranslationRaw(center.dx, center.dy, 0.0)
      ..multiply(Matrix4.diagonal3Values(relativeScale, relativeScale, 1.0))
      ..setTranslationRaw(-center.dx, -center.dy, 0.0)
      ..multiply(currentMatrix);

    _transformationController.value = matrix;
  }

  @override
  Widget build(BuildContext context) {
    if (_cargando) {
      return const Center(
        child: CircularProgressIndicator(color: AppColors.primary),
      );
    }

    if (_error != null) {
      return ErrorRetryView(mensaje: _error!, onRetry: _cargarModelos);
    }

    if (_modelos.isEmpty) {
      return const EmptyStateView(
        icon: Icons.schema_outlined,
        title: 'Sin diagramas UML',
        description: 'Este proyecto aún no tiene diagramas UML registrados en la plataforma web.',
      );
    }

    final modelo = _modeloSeleccionado!;
    final clasesFiltradas = modelo.clases.where(
      (c) => c.nombre.toLowerCase().contains(_filtroClases.toLowerCase()),
    ).toList();

    return Column(
      children: [
        // Barra superior de control del Diagrama UML
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
          decoration: const BoxDecoration(
            color: AppColors.surface,
            border: Border(bottom: BorderSide(color: AppColors.border)),
          ),
          child: Row(
            children: [
              // Selector de modelo / versión
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Flexible(
                          child: Text(
                            modelo.nombre,
                            style: AppTypography.h3,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                        const SizedBox(width: 6),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                          decoration: BoxDecoration(
                            color: AppColors.card,
                            borderRadius: BorderRadius.circular(6),
                            border: Border.all(color: AppColors.border),
                          ),
                          child: Text(
                            'v${modelo.version}',
                            style: AppTypography.code.copyWith(fontSize: 10, color: AppColors.primaryLight),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 2),
                    Text(
                      '${modelo.clases.length} clases • ${modelo.relaciones.length} relaciones activas',
                      style: AppTypography.bodySmall.copyWith(color: AppColors.accent),
                    ),
                  ],
                ),
              ),

              // Botón alternar vista (Lienzo vs Lista)
              SegmentedButton<bool>(
                segments: const [
                  ButtonSegment(
                    value: true,
                    icon: Icon(Icons.hub_outlined, size: 16),
                    tooltip: 'Lienzo',
                  ),
                  ButtonSegment(
                    value: false,
                    icon: Icon(Icons.format_list_bulleted_rounded, size: 16),
                    tooltip: 'Lista',
                  ),
                ],
                selected: {_modoLienzo},
                onSelectionChanged: (set) => setState(() => _modoLienzo = set.first),
                style: ButtonStyle(
                  visualDensity: VisualDensity.compact,
                  backgroundColor: WidgetStateProperty.resolveWith(
                    (s) => s.contains(WidgetState.selected) ? AppColors.primary : AppColors.card,
                  ),
                ),
              ),
            ],
          ),
        ),

        // Cuerpo: Lienzo Interactivo vs Lista
        Expanded(
          child: _modoLienzo
              ? _construirLienzoInteractivo(modelo)
              : _construirListaEstructurada(clasesFiltradas, modelo),
        ),
      ],
    );
  }

  Widget _construirLienzoInteractivo(UmlDiagramaModel modelo) {
    if (modelo.clases.isEmpty) {
      return const EmptyStateView(
        icon: Icons.bubble_chart_outlined,
        title: 'Diagrama vacío',
        description: 'No hay clases dibujadas en este diagrama.',
      );
    }

    // Calcular tamaño total del canvas con margen amplio
    double maxX = 1200;
    double maxY = 1200;
    for (var c in modelo.clases) {
      if (c.x + c.width + 300 > maxX) maxX = c.x + c.width + 300;
      if (c.y + c.height + 300 > maxY) maxY = c.y + c.height + 300;
    }

    return LayoutBuilder(
      builder: (context, constraints) {
        final viewportSize = Size(constraints.maxWidth, constraints.maxHeight);

        // Auto-ajustar vista automáticamente la primera vez que se carga el diagrama
        if (!_ajustadoInicial && viewportSize.width > 0 && viewportSize.height > 0) {
          WidgetsBinding.instance.addPostFrameCallback((_) {
            if (mounted && !_ajustadoInicial) {
              _ajustarDiagramaAPantalla(viewportSize, modelo);
              setState(() => _ajustadoInicial = true);
            }
          });
        }

        return Stack(
          children: [
            InteractiveViewer(
              transformationController: _transformationController,
              minScale: 0.15,
              maxScale: 3.5,
              boundaryMargin: const EdgeInsets.all(800),
              constrained: false,
              child: GestureDetector(
                onTapUp: (details) {
                  final pos = details.localPosition;
                  for (var c in modelo.clases) {
                    final rect = Rect.fromLTWH(c.x, c.y, c.width, c.height);
                    if (rect.contains(pos)) {
                      setState(() => _claseActivaId = c.id);
                      _abrirDetalleClase(c);
                      return;
                    }
                  }
                },
                child: CustomPaint(
                  size: Size(maxX, maxY),
                  painter: UmlCanvasPainter(
                    clases: modelo.clases,
                    relaciones: modelo.relaciones,
                    claseSeleccionadaId: _claseActivaId,
                  ),
                ),
              ),
            ),

            // Floating info badge superior izquierda
            Positioned(
              top: 12,
              left: 12,
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                decoration: BoxDecoration(
                  color: AppColors.surface.withAlpha(235),
                  borderRadius: BorderRadius.circular(10),
                  border: Border.all(color: AppColors.border),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withAlpha(60),
                      blurRadius: 6,
                    ),
                  ],
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Icon(Icons.pinch_rounded, size: 14, color: AppColors.accent),
                    const SizedBox(width: 6),
                    Text(
                      'Pellizca para zoom • Toca una clase para detalles',
                      style: AppTypography.bodySmall.copyWith(
                        fontSize: 10.5,
                        color: AppColors.textPrimary,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ],
                ),
              ),
            ),

            // Barra flotante de controles de Zoom en la esquina inferior derecha
            Positioned(
              bottom: 16,
              right: 16,
              child: Container(
                decoration: BoxDecoration(
                  color: AppColors.surface.withAlpha(235),
                  borderRadius: BorderRadius.circular(24),
                  border: Border.all(color: AppColors.border),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withAlpha(80),
                      blurRadius: 10,
                      offset: const Offset(0, 4),
                    ),
                  ],
                ),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    IconButton(
                      icon: const Icon(Icons.add_rounded, size: 20),
                      tooltip: 'Acercar',
                      color: AppColors.textPrimary,
                      onPressed: () => _zoom(1.3),
                    ),
                    const Divider(height: 1, color: AppColors.border),
                    IconButton(
                      icon: const Icon(Icons.remove_rounded, size: 20),
                      tooltip: 'Alejar',
                      color: AppColors.textPrimary,
                      onPressed: () => _zoom(0.7),
                    ),
                    const Divider(height: 1, color: AppColors.border),
                    IconButton(
                      icon: const Icon(Icons.fit_screen_rounded, size: 19),
                      tooltip: 'Ajustar a pantalla',
                      color: AppColors.accent,
                      onPressed: () => _ajustarDiagramaAPantalla(viewportSize, modelo),
                    ),
                  ],
                ),
              ),
            ),
          ],
        );
      },
    );
  }

  Widget _construirListaEstructurada(List<UmlClaseElement> clases, UmlDiagramaModel modelo) {
    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 10.0),
          child: TextField(
            onChanged: (v) => setState(() => _filtroClases = v),
            style: AppTypography.bodyMedium,
            decoration: InputDecoration(
              hintText: 'Filtrar clases por nombre...',
              prefixIcon: const Icon(Icons.search_rounded, size: 18, color: AppColors.textMuted),
              contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
            ),
          ),
        ),
        Expanded(
          child: ListView.separated(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
            itemCount: clases.length,
            separatorBuilder: (_, _) => const SizedBox(height: 10),
            itemBuilder: (context, index) {
              final c = clases[index];
              // Relaciones de esta clase
              final rels = modelo.relaciones.where((r) {
                final or = r.origenId.toLowerCase();
                final des = r.destinoId.toLowerCase();
                final id = c.id.toLowerCase();
                final nom = c.nombre.toLowerCase();
                return or == id || or == nom || des == id || des == nom;
              }).toList();

              return Container(
                decoration: BoxDecoration(
                  color: AppColors.card,
                  borderRadius: BorderRadius.circular(14),
                  border: Border.all(color: AppColors.border),
                ),
                child: InkWell(
                  borderRadius: BorderRadius.circular(14),
                  onTap: () => _abrirDetalleClase(c),
                  child: Padding(
                    padding: const EdgeInsets.all(14.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Container(
                              padding: const EdgeInsets.all(8),
                              decoration: BoxDecoration(
                                color: AppColors.primary.withAlpha(35),
                                borderRadius: BorderRadius.circular(8),
                              ),
                              child: const Icon(Icons.class_outlined, color: AppColors.primaryLight, size: 20),
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(c.nombre, style: AppTypography.h3),
                                  Text(
                                    '«${c.tipo}» • ${c.atributos.length} atributos • ${c.operaciones.length} métodos',
                                    style: AppTypography.bodySmall,
                                  ),
                                ],
                              ),
                            ),
                            const Icon(Icons.chevron_right_rounded, color: AppColors.textMuted),
                          ],
                        ),

                        // Relaciones y cardinalidades directas en la lista
                        if (rels.isNotEmpty) ...[
                          const SizedBox(height: 10),
                          const Divider(height: 1, color: AppColors.border),
                          const SizedBox(height: 8),
                          Wrap(
                            spacing: 6,
                            runSpacing: 6,
                            children: rels.map((r) {
                              final esOrigen = r.origenId.toLowerCase() == c.id.toLowerCase() ||
                                  r.origenId.toLowerCase() == c.nombre.toLowerCase();
                              final miCard = esOrigen ? r.multiplicidadOrigen : r.multiplicidadDestino;
                              final otraCard = esOrigen ? r.multiplicidadDestino : r.multiplicidadOrigen;
                              final target = esOrigen ? r.destinoId : r.origenId;

                              return Container(
                                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                                decoration: BoxDecoration(
                                  color: AppColors.surface,
                                  borderRadius: BorderRadius.circular(6),
                                  border: Border.all(color: AppColors.accent.withAlpha(80)),
                                ),
                                child: Text(
                                  '${r.tipo.replaceAll('_', ' ')}: [${miCard ?? '1'}] ➔ [${otraCard ?? '1'}] $target',
                                  style: AppTypography.code.copyWith(fontSize: 10, color: AppColors.accent),
                                ),
                              );
                            }).toList(),
                          ),
                        ],
                      ],
                    ),
                  ),
                ),
              );
            },
          ),
        ),
      ],
    );
  }
}
