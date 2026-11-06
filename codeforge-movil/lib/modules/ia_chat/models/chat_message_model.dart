import 'package:intl/intl.dart';

class ChatMessageModel {
  final int idMensaje;
  final String nombreEmisor;
  final String tipoEmisor; // USUARIO, IA, SISTEMA
  final String modoIa; // GENERAL, UML, ARQUITECTURA, CODIGO, PRUEBAS, DIAGNOSTICO
  final String contenido;
  final int? idPropuestaAsociada;
  final DateTime? fechaEnvio;

  ChatMessageModel({
    required this.idMensaje,
    required this.nombreEmisor,
    required this.tipoEmisor,
    required this.modoIa,
    required this.contenido,
    this.idPropuestaAsociada,
    this.fechaEnvio,
  });

  bool get esIa => tipoEmisor.toUpperCase() == 'IA';
  bool get tienePropuesta => idPropuestaAsociada != null;

  String get fechaFormateada {
    if (fechaEnvio == null) return '';
    return DateFormat('dd/MM/yyyy HH:mm').format(fechaEnvio!);
  }

  factory ChatMessageModel.fromJson(Map<String, dynamic> json) {
    DateTime? parseDate(dynamic d) {
      if (d == null) return null;
      try {
        return DateTime.parse(d.toString());
      } catch (_) {
        return null;
      }
    }

    return ChatMessageModel(
      idMensaje: json['idMensaje'] is int
          ? json['idMensaje']
          : int.tryParse(json['idMensaje']?.toString() ?? '0') ?? 0,
      nombreEmisor: json['nombreEmisor'] ?? 'Participante',
      tipoEmisor: json['tipoEmisor'] ?? 'USUARIO',
      modoIa: json['modoIa'] ?? 'GENERAL',
      contenido: json['contenido'] ?? '',
      idPropuestaAsociada: json['idPropuestaAsociada'] is int
          ? json['idPropuestaAsociada']
          : int.tryParse(json['idPropuestaAsociada']?.toString() ?? ''),
      fechaEnvio: parseDate(json['fechaEnvio']),
    );
  }
}
