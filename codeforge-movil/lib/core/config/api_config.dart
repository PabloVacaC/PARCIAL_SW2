import 'dart:io' show Platform;
import 'package:flutter/foundation.dart' show kIsWeb;

class ApiConfig {
  static const String defaultHostIp = '192.168.0.20';
  static const List<String> candidateHosts = [
    '127.0.0.1',
    '192.168.0.20',
    '192.168.0.19',
    '10.0.2.2',
  ];

  static String _customBaseUrl = '';

  static void setBaseUrl(String url) {
    String cleanUrl = url.endsWith('/') ? url.substring(0, url.length - 1) : url;
    _customBaseUrl = cleanUrl;
  }

  static String get baseUrl {
    if (_customBaseUrl.isNotEmpty) {
      return _customBaseUrl;
    }

    if (kIsWeb) {
      return 'http://localhost:8080/api';
    }

    try {
      if (Platform.isAndroid || Platform.isIOS) {
        // Por defecto usamos 127.0.0.1 si adb reverse está activo, o la IP de red
        return 'http://127.0.0.1:8080/api';
      }
    } catch (_) {}

    return 'http://127.0.0.1:8080/api';
  }

  // Endpoints de solo lectura y autenticación autorizada
  static String get loginUrl => '$baseUrl/auth/login';
  static String get misProyectosUrl => '$baseUrl/proyectos/mis-proyectos';
  static String proyectoUrl(int id) => '$baseUrl/proyectos/$id';
  static String colaboradoresUrl(int id) => '$baseUrl/proyectos/$id/colaboradores';
  static String documentacionUrl(int id) => '$baseUrl/proyectos/$id/documentacion';
  static String modelosProyectoUrl(int id) => '$baseUrl/modelos/proyecto/$id';
  static String modeloDetalleUrl(int id) => '$baseUrl/modelos/$id';
  static String archivosGeneradosUrl(int idModelo) => '$baseUrl/generador/modelos/$idModelo/archivos';
  static String iaConversacionUrl(int idProyecto) => '$baseUrl/ia/colaborativa/proyecto/$idProyecto/conversacion';
  static String iaPropuestasUrl(int idProyecto) => '$baseUrl/ia/colaborativa/proyecto/$idProyecto/propuestas';
  static String iaPropuestaDetalleUrl(int idProyecto, int idPropuesta) =>
      '$baseUrl/ia/colaborativa/proyecto/$idProyecto/propuestas/$idPropuesta';
}
