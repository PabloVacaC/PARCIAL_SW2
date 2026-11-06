import 'dart:convert';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// Servicio de almacenamiento en caché local offline para CodeForge Mobile.
/// Permite consultar proyectos, diagramas UML, clases, código y propuestas
/// aún si el dispositivo pierde la conexión a internet o el backend se apaga.
class OfflineCacheService {
  static final OfflineCacheService instance = OfflineCacheService._internal();

  final FlutterSecureStorage _storage;
  final Map<String, dynamic> _memoriaCache = {};
  final Map<String, DateTime> _timestamps = {};

  OfflineCacheService._internal()
      : _storage = const FlutterSecureStorage(
          aOptions: AndroidOptions(),
          iOptions: IOSOptions(accessibility: KeychainAccessibility.first_unlock),
        );

  factory OfflineCacheService() => instance;

  /// Normaliza una URL eliminando el host/puerto para que la caché sea persistente
  /// aunque cambie la IP local de la computadora (ej. localhost vs 192.168.0.19).
  String normalizarClave(String url) {
    try {
      final uri = Uri.parse(url);
      final path = uri.path.isNotEmpty ? uri.path : url;
      final query = uri.hasQuery ? '?${uri.query}' : '';
      return '$path$query'.replaceAll(RegExp(r'[^a-zA-Z0-9_\-\.\?=/]'), '_');
    } catch (_) {
      return url.replaceAll(RegExp(r'[^a-zA-Z0-9_\-\.\?=/]'), '_');
    }
  }

  /// Guarda una respuesta JSON en la caché de memoria y en el almacenamiento seguro persistente.
  Future<void> guardar(String url, dynamic data) async {
    if (data == null) return;
    final clave = normalizarClave(url);
    final ahora = DateTime.now();

    _memoriaCache[clave] = data;
    _timestamps[clave] = ahora;

    try {
      final payload = jsonEncode({
        'ts': ahora.toIso8601String(),
        'url': url,
        'data': data,
      });
      await _storage.write(key: 'cf_cache_$clave', value: payload);
    } catch (_) {
      // Si el tamaño excede la cuota de un solo valor seguro, se mantiene en memoria RAM
    }
  }

  /// Obtiene los datos en caché para una URL dada. Retorna null si no existen.
  Future<dynamic> obtener(String url) async {
    final clave = normalizarClave(url);

    // 1. Verificar memoria RAM rápida
    if (_memoriaCache.containsKey(clave)) {
      return _memoriaCache[clave];
    }

    // 2. Verificar almacenamiento persistente en disco
    try {
      final raw = await _storage.read(key: 'cf_cache_$clave');
      if (raw != null && raw.isNotEmpty) {
        final decoded = jsonDecode(raw) as Map<String, dynamic>;
        final data = decoded['data'];
        if (decoded.containsKey('ts')) {
          _timestamps[clave] = DateTime.tryParse(decoded['ts'].toString()) ?? DateTime.now();
        }
        _memoriaCache[clave] = data;
        return data;
      }
    } catch (_) {}

    return null;
  }

  /// Verifica si existe caché local disponible para la URL
  Future<bool> tieneDatos(String url) async {
    final clave = normalizarClave(url);
    if (_memoriaCache.containsKey(clave)) return true;
    try {
      final raw = await _storage.read(key: 'cf_cache_$clave');
      return raw != null && raw.isNotEmpty;
    } catch (_) {
      return false;
    }
  }

  /// Retorna la fecha y hora en que se guardó la caché para esta URL
  DateTime? fechaCache(String url) {
    final clave = normalizarClave(url);
    return _timestamps[clave];
  }

  /// Limpia toda la caché guardada
  Future<void> limpiarCache() async {
    _memoriaCache.clear();
    _timestamps.clear();
    try {
      final all = await _storage.readAll();
      for (final k in all.keys) {
        if (k.startsWith('cf_cache_')) {
          await _storage.delete(key: k);
        }
      }
    } catch (_) {}
  }
}

