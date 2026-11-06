import 'package:intl/intl.dart';

class ProposalModel {
  final int idPropuesta;
  final int idProyecto;
  final int idModelo;
  final String versionOrigen;
  final String titulo;
  final String descripcionProblema;
  final String solucionPropuesta;
  final String riesgosEfectos;
  final String operacionesJson;
  final int totalOperaciones;
  final String estado; // PENDIENTE, APROBADA, RECHAZADA, APLICADA, FALLIDA
  final String nombreSolicitante;
  final String? nombreAprobador;
  final DateTime? fechaCreacion;
  final DateTime? fechaDecision;
  final DateTime? fechaAplicacion;

  ProposalModel({
    required this.idPropuesta,
    required this.idProyecto,
    required this.idModelo,
    required this.versionOrigen,
    required this.titulo,
    required this.descripcionProblema,
    required this.solucionPropuesta,
    required this.riesgosEfectos,
    required this.operacionesJson,
    required this.totalOperaciones,
    required this.estado,
    required this.nombreSolicitante,
    this.nombreAprobador,
    this.fechaCreacion,
    this.fechaDecision,
    this.fechaAplicacion,
  });

  String get fechaCreacionFormateada {
    if (fechaCreacion == null) return '';
    return DateFormat('dd/MM/yyyy HH:mm').format(fechaCreacion!);
  }

  factory ProposalModel.fromJson(Map<String, dynamic> json) {
    DateTime? parseDate(dynamic d) {
      if (d == null) return null;
      try {
        return DateTime.parse(d.toString());
      } catch (_) {
        return null;
      }
    }

    return ProposalModel(
      idPropuesta: json['idPropuesta'] is int
          ? json['idPropuesta']
          : int.tryParse(json['idPropuesta']?.toString() ?? '0') ?? 0,
      idProyecto: json['idProyecto'] is int
          ? json['idProyecto']
          : int.tryParse(json['idProyecto']?.toString() ?? '0') ?? 0,
      idModelo: json['idModelo'] is int
          ? json['idModelo']
          : int.tryParse(json['idModelo']?.toString() ?? '0') ?? 0,
      versionOrigen: json['versionOrigen'] ?? '1.0',
      titulo: json['titulo'] ?? 'Propuesta de Cambio',
      descripcionProblema: json['descripcionProblema'] ?? '',
      solucionPropuesta: json['solucionPropuesta'] ?? '',
      riesgosEfectos: json['riesgosEfectos'] ?? '',
      operacionesJson: json['operacionesJson'] ?? '[]',
      totalOperaciones: json['totalOperaciones'] is int
          ? json['totalOperaciones']
          : int.tryParse(json['totalOperaciones']?.toString() ?? '0') ?? 0,
      estado: json['estado'] ?? 'PENDIENTE',
      nombreSolicitante: json['nombreSolicitante'] ?? 'CodeForge AI',
      nombreAprobador: json['nombreAprobador'],
      fechaCreacion: parseDate(json['fechaCreacion']),
      fechaDecision: parseDate(json['fechaDecision']),
      fechaAplicacion: parseDate(json['fechaAplicacion']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'idPropuesta': idPropuesta,
      'idProyecto': idProyecto,
      'idModelo': idModelo,
      'versionOrigen': versionOrigen,
      'titulo': titulo,
      'descripcionProblema': descripcionProblema,
      'solucionPropuesta': solucionPropuesta,
      'riesgosEfectos': riesgosEfectos,
      'operacionesJson': operacionesJson,
      'totalOperaciones': totalOperaciones,
      'estado': estado,
      'nombreSolicitante': nombreSolicitante,
      'nombreAprobador': nombreAprobador,
      'fechaCreacion': fechaCreacion?.toIso8601String(),
      'fechaDecision': fechaDecision?.toIso8601String(),
      'fechaAplicacion': fechaAplicacion?.toIso8601String(),
    };
  }
}
