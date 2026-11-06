import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_highlight/flutter_highlight.dart';
import 'package:flutter_highlight/themes/atom-one-dark.dart';
import '../../../core/theme/app_typography.dart';
import '../../../core/widgets/read_only_badge.dart';
import '../models/code_file_model.dart';

class CodeViewerView extends StatefulWidget {
  final CodeFileModel archivo;

  const CodeViewerView({super.key, required this.archivo});

  @override
  State<CodeViewerView> createState() => _CodeViewerViewState();
}

class _CodeViewerViewState extends State<CodeViewerView> {
  double _fontSize = 12.0;

  String get _language {
    switch (widget.archivo.tipo.toUpperCase()) {
      case 'JAVA':
        return 'java';
      case 'DART':
        return 'dart';
      case 'SQL':
        return 'sql';
      case 'YAML':
        return 'yaml';
      case 'JSON':
        return 'json';
      case 'XML':
        return 'xml';
      case 'MARKDOWN':
        return 'markdown';
      default:
        return 'plaintext';
    }
  }

  void _copiarAlPortapapeles() {
    Clipboard.setData(ClipboardData(text: widget.archivo.contenido));
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Contenido copiado de ${widget.archivo.nombre}'),
        behavior: SnackBarBehavior.floating,
        duration: const Duration(seconds: 2),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(widget.archivo.nombre, style: AppTypography.h3),
            Text(widget.archivo.ruta, style: AppTypography.bodySmall, overflow: TextOverflow.ellipsis),
          ],
        ),
        actions: [
          const ReadOnlyBadge(compact: true),
          const SizedBox(width: 8),
          IconButton(
            tooltip: 'Reducir tamaño',
            icon: const Icon(Icons.text_decrease_rounded, size: 20),
            onPressed: () {
              if (_fontSize > 9) setState(() => _fontSize -= 1);
            },
          ),
          IconButton(
            tooltip: 'Aumentar tamaño',
            icon: const Icon(Icons.text_increase_rounded, size: 20),
            onPressed: () {
              if (_fontSize < 20) setState(() => _fontSize += 1);
            },
          ),
          IconButton(
            tooltip: 'Copiar código',
            icon: const Icon(Icons.copy_rounded, size: 20),
            onPressed: _copiarAlPortapapeles,
          ),
          const SizedBox(width: 8),
        ],
      ),
      body: Container(
        color: const Color(0xFF282C34), // Fondo idéntico al tema atom-one-dark
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          scrollDirection: Axis.vertical,
          child: SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: HighlightView(
              widget.archivo.contenido,
              language: _language,
              theme: atomOneDarkTheme,
              padding: const EdgeInsets.all(8),
              textStyle: AppTypography.code.copyWith(fontSize: _fontSize),
            ),
          ),
        ),
      ),
    );
  }
}
