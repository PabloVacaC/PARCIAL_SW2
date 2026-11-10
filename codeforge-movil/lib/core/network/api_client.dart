import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:http/http.dart' as http;
import '../config/api_config.dart';
import '../storage/offline_cache_service.dart';
import '../storage/secure_storage_service.dart';
import 'api_exception.dart';
import 'offline_state.dart';

class ApiClient {
  final SecureStorageService _storageService;
  final OfflineCacheService _cacheService;
  final OfflineState _offlineState;
  final http.Client _httpClient;

  ApiClient({
    SecureStorageService? storageService,
    OfflineCacheService? cacheService,
    OfflineState? offlineState,
    http.Client? httpClient,
  })  : _storageService = storageService ?? SecureStorageService(),
        _cacheService = cacheService ?? OfflineCacheService.instance,
        _offlineState = offlineState ?? OfflineState.instance,
        _httpClient = httpClient ?? http.Client();

  Future<Map<String, String>> _headersAutenticados({bool jsonContent = true}) async {
    final headers = <String, String>{
      'Accept': 'application/json',
    };
    if (jsonContent) {
      headers['Content-Type'] = 'application/json; charset=UTF-8';
    }

    final token = await _storageService.obtenerToken();
    if (token != null && token.isNotEmpty) {
      headers['Authorization'] = 'Bearer $token';
    }

    return headers;
  }

  // ============================================================
  // PETICIONES GET (100% SOLO LECTURA + CACHÉ OFFLINE AUTOMÁTICA)
  // ============================================================

  Future<dynamic> get(String url) async {
    final headers = await _headersAutenticados();

    try {
      final uri = Uri.parse(url);

      final response = await _httpClient
          .get(uri, headers: headers)
          .timeout(const Duration(seconds: 6));

      final data = _procesarRespuesta(response);
      _offlineState.setOffline(false);
      _cacheService.guardar(url, data);
      return data;
    } on SocketException catch (_) {
      // Intentar candidatos de red alternativos en Android (USB vs Wi-Fi)
      if (!kIsWeb && Platform.isAndroid) {
        final uri = Uri.tryParse(url);
        final currentHost = uri?.host ?? '127.0.0.1';

        for (final candidate in ApiConfig.candidateHosts) {
          if (candidate == currentHost) continue;
          try {
            final String baseCandidate = candidate == '3.85.57.219' ? 'http://$candidate/api' : 'http://$candidate:8080/api';
            final String pathAndQuery = url.substring(url.indexOf('/api') + 4);
            final fallbackUrl = '$baseCandidate$pathAndQuery';
            final resFallback = await _httpClient
                .get(Uri.parse(fallbackUrl), headers: headers)
                .timeout(const Duration(seconds: 3));

            if (resFallback.statusCode >= 200 && resFallback.statusCode < 300) {
              final data = _procesarRespuesta(resFallback);
              ApiConfig.setBaseUrl(baseCandidate);
              await _storageService.guardarBaseUrl(baseCandidate);
              _offlineState.setOffline(false);
              _cacheService.guardar(url, data);
              return data;
            }
          } catch (_) {}
        }
      }

      return await _recuperarDeCacheOError(url);
    } on TimeoutException catch (_) {
      return await _recuperarDeCacheOError(url);
    } on HttpException catch (_) {
      return await _recuperarDeCacheOError(url);
    } on ApiException catch (e) {
      if (e.codigoEstado != null && e.codigoEstado! >= 500) {
        return await _recuperarDeCacheOError(url);
      }
      rethrow;
    } catch (e) {
      return await _recuperarDeCacheOError(url);
    }
  }

  Future<dynamic> _recuperarDeCacheOError(String url) async {
    _offlineState.setOffline(true);
    final cached = await _cacheService.obtener(url);
    if (cached != null) {
      return cached;
    }

    throw ApiException(
      'Modo sin conexión: no hay datos guardados localmente para esta sección aún. Conéctate a internet para sincronizar.',
    );
  }

  // ============================================================
  // PETICIONES POST (AUTENTICACIÓN + FALLBACK INTELIGENTE MULTI-HOST)
  // ============================================================

  Future<dynamic> post(String url, {Map<String, dynamic>? body}) async {
    final headers = await _headersAutenticados();
    final bodyJson = body != null ? jsonEncode(body) : null;

    try {
      final uri = Uri.parse(url);
      final response = await _httpClient
          .post(uri, headers: headers, body: bodyJson)
          .timeout(const Duration(seconds: 6));

      final data = _procesarRespuesta(response);
      _offlineState.setOffline(false);
      return data;
    } on SocketException catch (e) {
      // Fallback automático inteligente entre USB (127.0.0.1) y Wi-Fi (192.168.0.20 / 192.168.0.19)
      if (!kIsWeb && Platform.isAndroid) {
        final uri = Uri.tryParse(url);
        final currentHost = uri?.host ?? '127.0.0.1';

        for (final candidate in ApiConfig.candidateHosts) {
          if (candidate == currentHost) continue;
          try {
            final String baseCandidate = candidate == '3.85.57.219' ? 'http://$candidate/api' : 'http://$candidate:8080/api';
            final String pathAndQuery = url.substring(url.indexOf('/api') + 4);
            final fallbackUrl = '$baseCandidate$pathAndQuery';
            final resFallback = await _httpClient
                .post(Uri.parse(fallbackUrl), headers: headers, body: bodyJson)
                .timeout(const Duration(seconds: 4));

            if (resFallback.statusCode >= 200 && resFallback.statusCode < 500) {
              final data = _procesarRespuesta(resFallback);
              ApiConfig.setBaseUrl(baseCandidate);
              await _storageService.guardarBaseUrl(baseCandidate);
              _offlineState.setOffline(false);
              return data;
            }
          } catch (_) {
            // Probar siguiente host candidato
          }
        }
      }

      _offlineState.setOffline(true);
      throw ApiException(
        'No se pudo conectar a $url ($e).\n\n• Si estás por cable USB: ejecuta "adb reverse tcp:8080 tcp:8080".\n• Si estás por Wi-Fi: pulsa "Configurar servidor" y selecciona Wi-Fi (${ApiConfig.defaultHostIp}).',
      );
    } on TimeoutException {
      _offlineState.setOffline(true);
      throw ApiException(
        'Tiempo de espera agotado al conectar al servidor ($url). Verifica que el backend esté ejecutándose.',
      );
    } on ApiException {
      rethrow;
    } catch (e) {
      throw ApiException('Error al procesar la solicitud: $e');
    }
  }

  dynamic _procesarRespuesta(http.Response response) {
    final statusCode = response.statusCode;
    final bodyString = utf8.decode(response.bodyBytes);

    if (statusCode >= 200 && statusCode < 300) {
      if (bodyString.isEmpty) return null;
      try {
        return jsonDecode(bodyString);
      } catch (_) {
        return bodyString;
      }
    }

    String errorMsg = 'Error en el servidor ($statusCode)';
    try {
      final errorJson = jsonDecode(bodyString);
      if (errorJson is Map) {
        errorMsg = errorJson['mensaje'] ??
            errorJson['message'] ??
            errorJson['error'] ??
            errorMsg;
      }
    } catch (_) {
      if (bodyString.isNotEmpty) {
        errorMsg = bodyString;
      }
    }

    if (statusCode == 401) {
      throw ApiException(
        errorMsg.isNotEmpty && !errorMsg.startsWith('Error en el servidor')
            ? errorMsg
            : 'Usuario o contraseña incorrectos.',
        codigoEstado: 401,
      );
    } else if (statusCode == 403) {
      throw ApiException(
        'No tienes permisos de acceso a este recurso del proyecto.',
        codigoEstado: 403,
      );
    } else if (statusCode == 404) {
      throw ApiException(
        'El recurso o proyecto solicitado no fue encontrado.',
        codigoEstado: 404,
      );
    }

    throw ApiException(errorMsg, codigoEstado: statusCode);
  }
}
