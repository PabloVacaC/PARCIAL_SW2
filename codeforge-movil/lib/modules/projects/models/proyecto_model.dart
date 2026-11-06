import 'package:intl/intl.dart';

class ProyectoModel {
  final int idProyecto;
  final String nombre;
  final String descripcion;
  final String estado;
  final int idPropietario;
  final String usernamePropietario;
  final String nombrePropietario;
  final DateTime? fechaCreacion;
  final DateTime? fechaActualizacion;
  int cantidadIntegrantes;
  double progresoEstimado;

  ProyectoModel({
    required this.idProyecto,
    required this.nombre,
    required this.descripcion,
    required this.estado,
    required this.idPropietario,
    required this.usernamePropietario,
    required this.nombrePropietario,
    this.fechaCreacion,
    this.fechaActualizacion,
    this.cantidadIntegrantes = 1,
    this.progresoEstimado = 0.65,
  });

  String get fechaCreacionFormateada {
    if (fechaCreacion == null) return 'Desconocida';
    return DateFormat('dd/MM/yyyy HH:mm').format(fechaCreacion!);
  }

  String get fechaActualizacionFormateada {
    if (fechaActualizacion == null) return 'Recientemente';
    return DateFormat('dd/MM/yyyy HH:mm').format(fechaActualizacion!);
  }

  factory ProyectoModel.fromJson(Map<String, dynamic> json) {
    DateTime? parseDate(dynamic d) {
      if (d == null) return null;
      try {
        return DateTime.parse(d.toString());
      } catch (_) {
        return null;
      }
    }

    return ProyectoModel(
      idProyecto: json['idProyecto'] is int
          ? json['idProyecto']
          : int.tryParse(json['idProyecto']?.toString() ?? '0') ?? 0,
      nombre: json['nombre'] ?? 'Sin nombre',
      descripcion: json['descripcion'] ?? 'Sin descripción',
      estado: json['estado'] ?? 'ACTIVO',
      idPropietario: json['idPropietario'] is int
          ? json['idPropietario']
          : int.tryParse(json['idPropietario']?.toString() ?? '0') ?? 0,
      usernamePropietario: json['usernamePropietario'] ?? '',
      nombrePropietario: json['nombrePropietario'] ?? '',
      fechaCreacion: parseDate(json['fechaCreacion']),
      fechaActualizacion: parseDate(json['fechaActualizacion']),
    );
  }
}
