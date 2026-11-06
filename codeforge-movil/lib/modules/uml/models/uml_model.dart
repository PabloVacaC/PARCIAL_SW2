import 'dart:convert' show jsonDecode;
import 'package:flutter/widgets.dart';

class UmlAtributo {
  final String nombre;
  final String tipo;
  final String visibilidad;
  final bool esEstatico;

  UmlAtributo({
    required this.nombre,
    required this.tipo,
    this.visibilidad = 'PRIVATE',
    this.esEstatico = false,
  });

  String get simboloVisibilidad {
    switch (visibilidad.toUpperCase()) {
      case 'PUBLIC':
        return '+';
      case 'PROTECTED':
        return '#';
      case 'PACKAGE':
        return '~';
      case 'PRIVATE':
      default:
        return '-';
    }
  }

  factory UmlAtributo.fromJson(Map<String, dynamic> json) {
    return UmlAtributo(
      nombre: json['nombre']?.toString() ?? 'atributo',
      tipo: json['tipo']?.toString() ?? 'String',
      visibilidad: json['visibilidad']?.toString() ?? 'PRIVATE',
      esEstatico: json['esEstatico'] == true,
    );
  }
}

class UmlOperacion {
  final String nombre;
  final String tipoRetorno;
  final String visibilidad;
  final List<String> parametros;

  UmlOperacion({
    required this.nombre,
    required this.tipoRetorno,
    this.visibilidad = 'PUBLIC',
    this.parametros = const [],
  });

  String get simboloVisibilidad {
    switch (visibilidad.toUpperCase()) {
      case 'PRIVATE':
        return '-';
      case 'PROTECTED':
        return '#';
      case 'PACKAGE':
        return '~';
      case 'PUBLIC':
      default:
        return '+';
    }
  }

  factory UmlOperacion.fromJson(Map<String, dynamic> json) {
    final paramsRaw = json['parametros'];
    List<String> params = [];
    if (paramsRaw is List) {
      for (var p in paramsRaw) {
        if (p is Map) {
          params.add('${p['nombre'] ?? 'p'}: ${p['tipo'] ?? 'Object'}');
        } else {
          params.add(p.toString());
        }
      }
    }

    return UmlOperacion(
      nombre: json['nombre']?.toString() ?? 'metodo',
      tipoRetorno: json['tipoRetorno']?.toString() ?? 'void',
      visibilidad: json['visibilidad']?.toString() ?? 'PUBLIC',
      parametros: params,
    );
  }
}

class UmlClaseElement {
  final String id;
  final String nombre;
  final String tipo;
  final double x;
  final double y;
  final double width;
  final double height;
  final List<UmlAtributo> atributos;
  final List<UmlOperacion> operaciones;

  UmlClaseElement({
    required this.id,
    required this.nombre,
    required this.tipo,
    required this.x,
    required this.y,
    required this.width,
    required this.height,
    required this.atributos,
    required this.operaciones,
  });

  factory UmlClaseElement.fromJson(Map<String, dynamic> json) {
    double parseD(dynamic v, double fallback) {
      if (v == null) return fallback;
      if (v is num) return v.toDouble();
      return double.tryParse(v.toString()) ?? fallback;
    }

    final pos = json['posicion'] as Map<String, dynamic>?;
    final tam = json['tamaño'] as Map<String, dynamic>? ??
        json['tamano'] as Map<String, dynamic>? ??
        json['size'] as Map<String, dynamic>?;

    final double x = parseD(pos?['x'] ?? json['x'], 50.0);
    final double y = parseD(pos?['y'] ?? json['y'], 50.0);
    final double w = parseD(tam?['ancho'] ?? tam?['width'] ?? pos?['width'] ?? json['width'], 220.0);
    final double h = parseD(tam?['alto'] ?? tam?['height'] ?? pos?['height'] ?? json['height'], 140.0);

    final List<UmlAtributo> attrs = [];
    if (json['atributos'] is List) {
      for (var a in (json['atributos'] as List)) {
        if (a is Map<String, dynamic>) {
          attrs.add(UmlAtributo.fromJson(a));
        }
      }
    }

    final List<UmlOperacion> ops = [];
    if (json['operaciones'] is List) {
      for (var o in (json['operaciones'] as List)) {
        if (o is Map<String, dynamic>) {
          ops.add(UmlOperacion.fromJson(o));
        }
      }
    }

    return UmlClaseElement(
      id: json['id']?.toString() ?? UniqueKey().toString(),
      nombre: json['nombre']?.toString() ?? 'Clase',
      tipo: json['tipo']?.toString() ?? 'CLASE',
      x: x,
      y: y,
      width: w < 160 ? 200 : w,
      height: h < 90 ? 120 : h,
      atributos: attrs,
      operaciones: ops,
    );
  }
}

class UmlRelacionElement {
  final String id;
  final String tipo;
  final String origenId;
  final String destinoId;
  final String? rolOrigen;
  final String? rolDestino;
  final String? multiplicidadOrigen;
  final String? multiplicidadDestino;

  UmlRelacionElement({
    required this.id,
    required this.tipo,
    required this.origenId,
    required this.destinoId,
    this.rolOrigen,
    this.rolDestino,
    this.multiplicidadOrigen,
    this.multiplicidadDestino,
  });

  factory UmlRelacionElement.fromJson(Map<String, dynamic> json) {
    String parseId(dynamic v) {
      if (v == null) return '';
      if (v is Map) {
        return v['id']?.toString() ?? v['idElemento']?.toString() ?? v['nombre']?.toString() ?? '';
      }
      return v.toString().trim();
    }

    final origen = parseId(json['origen'] ?? json['origenId'] ?? json['source'] ?? json['from']);
    final destino = parseId(json['destino'] ?? json['destinoId'] ?? json['target'] ?? json['to']);

    final multOrigen = json['multiplicidadOrigen']?.toString() ??
        json['cardinalidadOrigen']?.toString() ??
        json['multiplicidad_origen']?.toString();
    final multDestino = json['multiplicidadDestino']?.toString() ??
        json['cardinalidadDestino']?.toString() ??
        json['multiplicidad_destino']?.toString();

    final rolOrigen = json['rolOrigen']?.toString() ?? json['rol_origen']?.toString();
    final rolDestino = json['rolDestino']?.toString() ?? json['rol_destino']?.toString();

    return UmlRelacionElement(
      id: json['id']?.toString() ?? UniqueKey().toString(),
      tipo: json['tipo']?.toString() ?? 'ASOCIACION',
      origenId: origen,
      destinoId: destino,
      rolOrigen: (rolOrigen != null && rolOrigen.isNotEmpty && rolOrigen != 'null') ? rolOrigen : null,
      rolDestino: (rolDestino != null && rolDestino.isNotEmpty && rolDestino != 'null') ? rolDestino : null,
      multiplicidadOrigen: (multOrigen != null && multOrigen.isNotEmpty && multOrigen != 'null') ? multOrigen : null,
      multiplicidadDestino: (multDestino != null && multDestino.isNotEmpty && multDestino != 'null') ? multDestino : null,
    );
  }
}

class UmlDiagramaModel {
  final int idModelo;
  final int idProyecto;
  final String nombre;
  final String version;
  final List<UmlClaseElement> clases;
  final List<UmlRelacionElement> relaciones;

  UmlDiagramaModel({
    required this.idModelo,
    required this.idProyecto,
    required this.nombre,
    required this.version,
    required this.clases,
    required this.relaciones,
  });

  factory UmlDiagramaModel.fromBackendJson(Map<String, dynamic> json) {
    dynamic rawContenido = json['contenidoJson'] ?? json['contenido_json'];
    Map<String, dynamic> contenido = {};
    if (rawContenido is Map<String, dynamic>) {
      contenido = rawContenido;
    } else if (rawContenido is String && rawContenido.isNotEmpty) {
      try {
        // En caso de que venga como cadena JSON serializada
        final decoded = jsonDecode(rawContenido);
        if (decoded is Map<String, dynamic>) {
          contenido = decoded;
        }
      } catch (_) {}
    } else if (json['elementos'] is List) {
      contenido = json;
    }

    final List<UmlClaseElement> cls = [];
    if (contenido['elementos'] is List) {
      for (var e in (contenido['elementos'] as List)) {
        if (e is Map<String, dynamic>) {
          cls.add(UmlClaseElement.fromJson(e));
        }
      }
    }

    final List<UmlRelacionElement> rels = [];
    if (contenido['relaciones'] is List) {
      for (var r in (contenido['relaciones'] as List)) {
        if (r is Map<String, dynamic>) {
          rels.add(UmlRelacionElement.fromJson(r));
        }
      }
    }

    return UmlDiagramaModel(
      idModelo: json['idModelo'] is int
          ? json['idModelo']
          : int.tryParse(json['idModelo']?.toString() ?? '0') ?? 0,
      idProyecto: json['idProyecto'] is int
          ? json['idProyecto']
          : int.tryParse(json['idProyecto']?.toString() ?? '0') ?? 0,
      nombre: json['nombre'] ?? 'Diagrama de Clases',
      version: json['version']?.toString() ?? '1.0',
      clases: cls,
      relaciones: rels,
    );
  }
}
