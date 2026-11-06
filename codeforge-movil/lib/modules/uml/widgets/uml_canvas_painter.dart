import 'dart:math';
import 'package:flutter/material.dart';
import '../../../../core/theme/app_colors.dart';
import '../models/uml_model.dart';

class UmlCanvasPainter extends CustomPainter {
  final List<UmlClaseElement> clases;
  final List<UmlRelacionElement> relaciones;
  final String? claseSeleccionadaId;

  UmlCanvasPainter({
    required this.clases,
    required this.relaciones,
    this.claseSeleccionadaId,
  });

  @override
  void paint(Canvas canvas, Size size) {
    // 1. Dibujar cuadrícula tenue de fondo tipo blueprint
    final gridPaint = Paint()
      ..color = AppColors.border.withAlpha(20)
      ..strokeWidth = 1.0;

    const double gridSize = 32.0;
    for (double x = 0; x < size.width; x += gridSize) {
      canvas.drawLine(Offset(x, 0), Offset(x, size.height), gridPaint);
    }
    for (double y = 0; y < size.height; y += gridSize) {
      canvas.drawLine(Offset(0, y), Offset(size.width, y), gridPaint);
    }

    // Mapa para localizar cajas por ID, Nombre y variantes normalizadas
    final Map<String, Rect> boundsMap = {};
    for (final c in clases) {
      final rect = Rect.fromLTWH(c.x, c.y, c.width, c.height);
      boundsMap[c.id] = rect;
      boundsMap[c.nombre] = rect;
      boundsMap[c.nombre.toLowerCase()] = rect;
      boundsMap[c.nombre.toUpperCase()] = rect;
    }

    // 2. Dibujar conexiones de relaciones UML (Líneas, flechas y cardinalidades)
    for (final rel in relaciones) {
      final origen = boundsMap[rel.origenId] ?? boundsMap[rel.origenId.toLowerCase()];
      final destino = boundsMap[rel.destinoId] ?? boundsMap[rel.destinoId.toLowerCase()];

      if (origen != null && destino != null) {
        if (origen == destino || rel.origenId == rel.destinoId) {
          _dibujarRelacionRecursiva(canvas, origen, rel);
        } else {
          _dibujarRelacionEntreClases(canvas, origen, destino, rel);
        }
      }
    }

    // 3. Dibujar cajas de clases UML (Por encima de las líneas para bordes nítidos)
    final textPainter = TextPainter(textDirection: TextDirection.ltr);

    for (final c in clases) {
      final rect = Rect.fromLTWH(c.x, c.y, c.width, c.height);
      final bool isSelected = c.id == claseSeleccionadaId || c.nombre == claseSeleccionadaId;

      // Sombra sutil
      canvas.drawRRect(
        RRect.fromRectAndRadius(rect.shift(const Offset(0, 4)), const Radius.circular(10)),
        Paint()
          ..color = Colors.black.withAlpha(70)
          ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 8),
      );

      // Fondo de la tarjeta de la clase
      final bgPaint = Paint()
        ..color = isSelected ? AppColors.cardElevated : AppColors.card
        ..style = PaintingStyle.fill;
      canvas.drawRRect(RRect.fromRectAndRadius(rect, const Radius.circular(10)), bgPaint);

      // Borde exterior
      final borderPaint = Paint()
        ..color = isSelected ? AppColors.primaryLight : AppColors.border
        ..strokeWidth = isSelected ? 2.5 : 1.2
        ..style = PaintingStyle.stroke;
      canvas.drawRRect(RRect.fromRectAndRadius(rect, const Radius.circular(10)), borderPaint);

      // Cabecera: Nombre de la clase y estereotipo
      final double headerHeight = (c.tipo != 'CLASE') ? 42.0 : 36.0;
      final headerPaint = Paint()
        ..color = isSelected
            ? AppColors.primary.withAlpha(50)
            : AppColors.surface.withAlpha(150)
        ..style = PaintingStyle.fill;
      canvas.drawRRect(
        RRect.fromRectAndCorners(
          Rect.fromLTWH(c.x, c.y, c.width, headerHeight),
          topLeft: const Radius.circular(10),
          topRight: const Radius.circular(10),
        ),
        headerPaint,
      );

      // Texto de la cabecera
      textPainter.text = TextSpan(
        children: [
          if (c.tipo != 'CLASE')
            TextSpan(
              text: '«${c.tipo}»\n',
              style: const TextStyle(
                color: AppColors.secondary,
                fontSize: 9.5,
                fontWeight: FontWeight.w600,
              ),
            ),
          TextSpan(
            text: c.nombre,
            style: const TextStyle(
              color: AppColors.textPrimary,
              fontSize: 13.0,
              fontWeight: FontWeight.w700,
            ),
          ),
        ],
      );
      textPainter.layout(maxWidth: c.width - 16);
      textPainter.paint(canvas, Offset(c.x + 10, c.y + 7));

      // Línea divisoria de cabecera
      canvas.drawLine(
        Offset(c.x, c.y + headerHeight),
        Offset(c.x + c.width, c.y + headerHeight),
        Paint()
          ..color = AppColors.border
          ..strokeWidth = 1.0,
      );

      // Sección: Atributos
      double currentY = c.y + headerHeight + 6;
      final maxAttrs = min(c.atributos.length, 4);
      for (int i = 0; i < maxAttrs; i++) {
        final attr = c.atributos[i];
        textPainter.text = TextSpan(
          children: [
            TextSpan(
              text: '${attr.simboloVisibilidad} ',
              style: TextStyle(
                color: attr.visibilidad == 'PRIVATE' ? AppColors.error : AppColors.success,
                fontWeight: FontWeight.w700,
                fontSize: 10.5,
              ),
            ),
            TextSpan(
              text: '${attr.nombre}: ',
              style: const TextStyle(
                color: AppColors.textPrimary,
                fontSize: 10.5,
                fontWeight: FontWeight.w500,
              ),
            ),
            TextSpan(
              text: attr.tipo,
              style: const TextStyle(
                color: AppColors.accent,
                fontSize: 10.0,
              ),
            ),
          ],
        );
        textPainter.layout(maxWidth: c.width - 16);
        textPainter.paint(canvas, Offset(c.x + 10, currentY));
        currentY += 17;
      }

      if (c.atributos.length > 4) {
        textPainter.text = TextSpan(
          text: '... y ${c.atributos.length - 4} atributos más',
          style: const TextStyle(color: AppColors.textMuted, fontSize: 9.5, fontStyle: FontStyle.italic),
        );
        textPainter.layout();
        textPainter.paint(canvas, Offset(c.x + 10, currentY));
        currentY += 15;
      }

      // Línea divisoria entre atributos y métodos si hay métodos
      if (c.operaciones.isNotEmpty) {
        canvas.drawLine(
          Offset(c.x, currentY + 2),
          Offset(c.x + c.width, currentY + 2),
          Paint()
            ..color = AppColors.border.withAlpha(120)
            ..strokeWidth = 0.8,
        );
        currentY += 6;

        // Sección: Métodos / Operaciones
        final maxOps = min(c.operaciones.length, 3);
        for (int i = 0; i < maxOps; i++) {
          final op = c.operaciones[i];
          textPainter.text = TextSpan(
            children: [
              TextSpan(
                text: '${op.simboloVisibilidad} ',
                style: const TextStyle(
                  color: AppColors.primaryLight,
                  fontWeight: FontWeight.w700,
                  fontSize: 10.5,
                ),
              ),
              TextSpan(
                text: '${op.nombre}(): ',
                style: const TextStyle(
                  color: AppColors.textPrimary,
                  fontSize: 10.0,
                ),
              ),
              TextSpan(
                text: op.tipoRetorno,
                style: const TextStyle(
                  color: AppColors.secondary,
                  fontSize: 9.5,
                ),
              ),
            ],
          );
          textPainter.layout(maxWidth: c.width - 16);
          textPainter.paint(canvas, Offset(c.x + 10, currentY));
          currentY += 16;
        }
      }
    }
  }

  // =========================================================================
  // DIBUJO DE RELACIONES ENTRE DOS CLASES DISTINTAS
  // =========================================================================

  void _dibujarRelacionEntreClases(
    Canvas canvas,
    Rect origen,
    Rect destino,
    UmlRelacionElement rel,
  ) {
    // Intersección exacta de rayo con bordes rectangulares
    final pStart = _getBoxIntersection(origen, destino.center);
    final pEnd = _getBoxIntersection(destino, origen.center);

    final double dx = pEnd.dx - pStart.dx;
    final double dy = pEnd.dy - pStart.dy;
    final double dist = sqrt(dx * dx + dy * dy);

    if (dist < 10) return;

    final unit = Offset(dx / dist, dy / dist);
    final normal = Offset(-unit.dy, unit.dx);

    final String tipoUpper = rel.tipo.toUpperCase();
    final bool esDashed = tipoUpper.contains('DEPENDENCIA') ||
        tipoUpper.contains('REALIZACION') ||
        tipoUpper.contains('CLASE_ASOCIACION');

    final linePaint = Paint()
      ..color = AppColors.accent.withAlpha(220)
      ..strokeWidth = 2.0
      ..style = PaintingStyle.stroke;

    // Calcular recorte de inicio y fin para los adornos (flechas / rombos)
    Offset drawStart = pStart;
    Offset drawEnd = pEnd;

    // Ajustar por rombos en origen (Composición / Agregación)
    if (tipoUpper.contains('COMPOSICION') || tipoUpper.contains('AGREGACION')) {
      drawStart = pStart + unit * 16.0;
    }

    // Ajustar por triángulo en destino (Herencia / Realización)
    if (tipoUpper.contains('HERENCIA') ||
        tipoUpper.contains('GENERALIZACION') ||
        tipoUpper.contains('REALIZACION')) {
      drawEnd = pEnd - unit * 14.0;
    }

    // Dibujar línea (discontinua o sólida)
    if (esDashed) {
      _drawDashedLine(canvas, drawStart, drawEnd, linePaint);
    } else {
      canvas.drawLine(drawStart, drawEnd, linePaint);
    }

    // Dibujar adornos UML (rombos y flechas)
    _dibujarAdornosUml(canvas, pStart, pEnd, unit, normal, tipoUpper);

    // =======================================================================
    // DIBUJAR CARDINALIDADES / MULTIPLICIDADES Y ROLES
    // =======================================================================

    // 1. Multiplicidad y Rol de Origen
    final hasMultOrigen = rel.multiplicidadOrigen != null && rel.multiplicidadOrigen!.isNotEmpty;
    final hasRolOrigen = rel.rolOrigen != null && rel.rolOrigen!.isNotEmpty;
    if (hasMultOrigen || hasRolOrigen) {
      final posOrigen = pStart + unit * 22.0 + normal * 14.0;
      final multTexto = hasMultOrigen ? rel.multiplicidadOrigen! : '';
      final rolTexto = hasRolOrigen ? rel.rolOrigen! : '';
      _drawCardinalityBadge(
        canvas: canvas,
        pos: posOrigen,
        multiplicidad: multTexto,
        rol: rolTexto,
        colorBadge: AppColors.accent,
      );
    }

    // 2. Multiplicidad y Rol de Destino
    final hasMultDestino = rel.multiplicidadDestino != null && rel.multiplicidadDestino!.isNotEmpty;
    final hasRolDestino = rel.rolDestino != null && rel.rolDestino!.isNotEmpty;
    if (hasMultDestino || hasRolDestino) {
      final posDestino = pEnd - unit * 26.0 + normal * 14.0;
      final multTexto = hasMultDestino ? rel.multiplicidadDestino! : '';
      final rolTexto = hasRolDestino ? rel.rolDestino! : '';
      _drawCardinalityBadge(
        canvas: canvas,
        pos: posDestino,
        multiplicidad: multTexto,
        rol: rolTexto,
        colorBadge: AppColors.primaryLight,
      );
    }

    // 3. Etiqueta central de la relación (tipo o nombre)
    final mid = Offset((pStart.dx + pEnd.dx) / 2, (pStart.dy + pEnd.dy) / 2) + normal * 10.0;
    String tipoLabel = rel.tipo.replaceAll('_', ' ').toLowerCase();
    if (tipoLabel == 'asociacion' && (hasRolOrigen || hasRolDestino)) {
      // Si ya tiene rol en los extremos, no abrumar en el medio
      tipoLabel = '';
    }

    if (tipoLabel.isNotEmpty) {
      _drawPillText(
        canvas: canvas,
        pos: mid,
        texto: tipoLabel,
        bgColor: const Color(0xEE0B0F19),
        textColor: AppColors.textSecondary,
        fontSize: 9.5,
        borderColor: AppColors.border,
      );
    }
  }

  // =========================================================================
  // DIBUJO DE RELACIÓN RECURSIVA (AUTO-ASOCIACIÓN)
  // =========================================================================

  void _dibujarRelacionRecursiva(Canvas canvas, Rect box, UmlRelacionElement rel) {
    final linePaint = Paint()
      ..color = AppColors.accent.withAlpha(220)
      ..strokeWidth = 2.0
      ..style = PaintingStyle.stroke;

    final double startY = box.top + 30.0;
    final double endY = box.top + 80.0;
    final double loopRight = box.right + 45.0;

    final path = Path()
      ..moveTo(box.right, startY)
      ..lineTo(loopRight, startY)
      ..lineTo(loopRight, endY)
      ..lineTo(box.right, endY);

    canvas.drawPath(path, linePaint);

    // Flecha apuntando a la entrada
    final arrowPaint = Paint()
      ..color = AppColors.accent
      ..strokeWidth = 2.0
      ..style = PaintingStyle.stroke;
    final arrowPath = Path()
      ..moveTo(box.right + 10, endY - 6)
      ..lineTo(box.right, endY)
      ..lineTo(box.right + 10, endY + 6);
    canvas.drawPath(arrowPath, arrowPaint);

    // Multiplicidad origen arriba
    if (rel.multiplicidadOrigen != null && rel.multiplicidadOrigen!.isNotEmpty) {
      _drawCardinalityBadge(
        canvas: canvas,
        pos: Offset(box.right + 20, startY - 14),
        multiplicidad: rel.multiplicidadOrigen!,
        rol: rel.rolOrigen ?? '',
        colorBadge: AppColors.accent,
      );
    }

    // Multiplicidad destino abajo
    if (rel.multiplicidadDestino != null && rel.multiplicidadDestino!.isNotEmpty) {
      _drawCardinalityBadge(
        canvas: canvas,
        pos: Offset(box.right + 20, endY + 16),
        multiplicidad: rel.multiplicidadDestino!,
        rol: rel.rolDestino ?? '',
        colorBadge: AppColors.primaryLight,
      );
    }
  }

  // =========================================================================
  // DIBUJO DE ADORNOS UML (FLECHAS, TRIÁNGULOS, ROMBOS)
  // =========================================================================

  void _dibujarAdornosUml(
    Canvas canvas,
    Offset pStart,
    Offset pEnd,
    Offset unit,
    Offset normal,
    String tipo,
  ) {
    if (tipo.contains('HERENCIA') ||
        tipo.contains('GENERALIZACION') ||
        tipo.contains('REALIZACION')) {
      // Triángulo cerrado en destino (14x14)
      final tip = pEnd;
      final baseCenter = pEnd - unit * 14.0;
      final corner1 = baseCenter + normal * 7.5;
      final corner2 = baseCenter - normal * 7.5;

      final trianglePath = Path()
        ..moveTo(tip.dx, tip.dy)
        ..lineTo(corner1.dx, corner1.dy)
        ..lineTo(corner2.dx, corner2.dy)
        ..close();

      canvas.drawPath(
        trianglePath,
        Paint()
          ..color = AppColors.card
          ..style = PaintingStyle.fill,
      );
      canvas.drawPath(
        trianglePath,
        Paint()
          ..color = AppColors.primaryLight
          ..strokeWidth = 2.0
          ..style = PaintingStyle.stroke,
      );
    } else if (tipo.contains('COMPOSICION')) {
      // Rombo lleno en origen (longitud 16, ancho 12)
      final tip1 = pStart;
      final mid = pStart + unit * 8.0;
      final tip2 = pStart + unit * 16.0;
      final corner1 = mid + normal * 6.0;
      final corner2 = mid - normal * 6.0;

      final diamondPath = Path()
        ..moveTo(tip1.dx, tip1.dy)
        ..lineTo(corner1.dx, corner1.dy)
        ..lineTo(tip2.dx, tip2.dy)
        ..lineTo(corner2.dx, corner2.dy)
        ..close();

      canvas.drawPath(
        diamondPath,
        Paint()
          ..color = AppColors.accent
          ..style = PaintingStyle.fill,
      );
      canvas.drawPath(
        diamondPath,
        Paint()
          ..color = AppColors.accent
          ..strokeWidth = 1.5
          ..style = PaintingStyle.stroke,
      );
    } else if (tipo.contains('AGREGACION')) {
      // Rombo hueco en origen
      final tip1 = pStart;
      final mid = pStart + unit * 8.0;
      final tip2 = pStart + unit * 16.0;
      final corner1 = mid + normal * 6.0;
      final corner2 = mid - normal * 6.0;

      final diamondPath = Path()
        ..moveTo(tip1.dx, tip1.dy)
        ..lineTo(corner1.dx, corner1.dy)
        ..lineTo(tip2.dx, tip2.dy)
        ..lineTo(corner2.dx, corner2.dy)
        ..close();

      canvas.drawPath(
        diamondPath,
        Paint()
          ..color = AppColors.surface
          ..style = PaintingStyle.fill,
      );
      canvas.drawPath(
        diamondPath,
        Paint()
          ..color = AppColors.accent
          ..strokeWidth = 2.0
          ..style = PaintingStyle.stroke,
      );
    } else {
      // Flecha abierta en destino
      final wing1 = pEnd - unit * 11.0 + normal * 6.5;
      final wing2 = pEnd - unit * 11.0 - normal * 6.5;

      final arrowPath = Path()
        ..moveTo(wing1.dx, wing1.dy)
        ..lineTo(pEnd.dx, pEnd.dy)
        ..lineTo(wing2.dx, wing2.dy);

      canvas.drawPath(
        arrowPath,
        Paint()
          ..color = AppColors.accent
          ..strokeWidth = 2.0
          ..style = PaintingStyle.stroke,
      );
    }
  }

  // =========================================================================
  // BADGES RESALTADOS DE CARDINALIDAD Y ROLES
  // =========================================================================

  void _drawCardinalityBadge({
    required Canvas canvas,
    required Offset pos,
    required String multiplicidad,
    required String rol,
    required Color colorBadge,
  }) {
    final textPainter = TextPainter(textDirection: TextDirection.ltr);

    final List<TextSpan> spans = [];
    if (multiplicidad.isNotEmpty) {
      spans.add(
        TextSpan(
          text: multiplicidad,
          style: TextStyle(
            color: colorBadge,
            fontSize: 11.0,
            fontWeight: FontWeight.w800,
            letterSpacing: 0.2,
          ),
        ),
      );
    }
    if (rol.isNotEmpty) {
      if (spans.isNotEmpty) {
        spans.add(const TextSpan(text: ' '));
      }
      spans.add(
        TextSpan(
          text: '+$rol',
          style: const TextStyle(
            color: AppColors.textSecondary,
            fontSize: 9.5,
            fontStyle: FontStyle.italic,
          ),
        ),
      );
    }

    if (spans.isEmpty) return;

    textPainter.text = TextSpan(children: spans);
    textPainter.layout();

    final badgeW = textPainter.width + 10.0;
    final badgeH = textPainter.height + 6.0;
    final badgeRect = Rect.fromCenter(center: pos, width: badgeW, height: badgeH);

    // Fondo oscuro nítido
    canvas.drawRRect(
      RRect.fromRectAndRadius(badgeRect, const Radius.circular(6)),
      Paint()
        ..color = const Color(0xF2090D16)
        ..style = PaintingStyle.fill,
    );

    // Borde fino
    canvas.drawRRect(
      RRect.fromRectAndRadius(badgeRect, const Radius.circular(6)),
      Paint()
        ..color = colorBadge.withAlpha(120)
        ..strokeWidth = 1.0
        ..style = PaintingStyle.stroke,
    );

    textPainter.paint(
      canvas,
      Offset(badgeRect.left + 5.0, badgeRect.top + 3.0),
    );
  }

  void _drawPillText({
    required Canvas canvas,
    required Offset pos,
    required String texto,
    required Color bgColor,
    required Color textColor,
    required double fontSize,
    required Color borderColor,
  }) {
    final textPainter = TextPainter(
      textDirection: TextDirection.ltr,
      text: TextSpan(
        text: texto,
        style: TextStyle(
          color: textColor,
          fontSize: fontSize,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
    textPainter.layout();

    final pillW = textPainter.width + 12.0;
    final pillH = textPainter.height + 6.0;
    final pillRect = Rect.fromCenter(center: pos, width: pillW, height: pillH);

    canvas.drawRRect(
      RRect.fromRectAndRadius(pillRect, const Radius.circular(6)),
      Paint()
        ..color = bgColor
        ..style = PaintingStyle.fill,
    );
    canvas.drawRRect(
      RRect.fromRectAndRadius(pillRect, const Radius.circular(6)),
      Paint()
        ..color = borderColor
        ..strokeWidth = 0.8
        ..style = PaintingStyle.stroke,
    );

    textPainter.paint(
      canvas,
      Offset(pillRect.left + 6.0, pillRect.top + 3.0),
    );
  }

  // =========================================================================
  // UTILIDADES GEOMÉTRICAS Y MATEMÁTICAS
  // =========================================================================

  /// Calcula la intersección exacta de un rayo desde el centro del rectángulo hacia un punto objetivo
  Offset _getBoxIntersection(Rect rect, Offset target) {
    final center = rect.center;
    final dx = target.dx - center.dx;
    final dy = target.dy - center.dy;

    if (dx == 0 && dy == 0) return center;

    final halfW = rect.width / 2.0;
    final halfH = rect.height / 2.0;

    final double tx = dx != 0 ? (dx > 0 ? halfW / dx : -halfW / dx) : double.infinity;
    final double ty = dy != 0 ? (dy > 0 ? halfH / dy : -halfH / dy) : double.infinity;

    final double t = min(tx, ty);
    return Offset(center.dx + dx * t, center.dy + dy * t);
  }

  /// Dibuja líneas punteadas con alta precisión
  void _drawDashedLine(
    Canvas canvas,
    Offset p1,
    Offset p2,
    Paint paint, [
    double dashLength = 7.0,
    double spaceLength = 5.0,
  ]) {
    final double dx = p2.dx - p1.dx;
    final double dy = p2.dy - p1.dy;
    final double distance = sqrt(dx * dx + dy * dy);
    if (distance == 0) return;

    final double unitX = dx / distance;
    final double unitY = dy / distance;

    double currentDist = 0.0;
    while (currentDist < distance) {
      final double nextDist = min(currentDist + dashLength, distance);
      canvas.drawLine(
        Offset(p1.dx + unitX * currentDist, p1.dy + unitY * currentDist),
        Offset(p1.dx + unitX * nextDist, p1.dy + unitY * nextDist),
        paint,
      );
      currentDist += dashLength + spaceLength;
    }
  }

  @override
  bool shouldRepaint(covariant UmlCanvasPainter oldDelegate) {
    return oldDelegate.clases != clases ||
        oldDelegate.relaciones != relaciones ||
        oldDelegate.claseSeleccionadaId != claseSeleccionadaId;
  }
}
