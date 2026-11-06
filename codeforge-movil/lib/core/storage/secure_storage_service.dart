import 'dart:convert';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../../modules/auth/models/auth_user.dart';

class SecureStorageService {
  static const String _keyToken = 'codeforge_jwt_token';
  static const String _keyUser = 'codeforge_user_profile';

  final FlutterSecureStorage _storage;

  SecureStorageService()
      : _storage = const FlutterSecureStorage(
          aOptions: AndroidOptions(),
          iOptions: IOSOptions(accessibility: KeychainAccessibility.first_unlock),
        );

  Future<void> guardarSesion({required String token, required AuthUser usuario}) async {
    await _storage.write(key: _keyToken, value: token);
    await _storage.write(key: _keyUser, value: jsonEncode(usuario.toJson()));
  }

  Future<String?> obtenerToken() async {
    return await _storage.read(key: _keyToken);
  }

  Future<AuthUser?> obtenerUsuario() async {
    final rawUser = await _storage.read(key: _keyUser);
    if (rawUser == null || rawUser.isEmpty) return null;
    try {
      final map = jsonDecode(rawUser) as Map<String, dynamic>;
      return AuthUser.fromJson(map);
    } catch (_) {
      return null;
    }
  }

  Future<void> limpiarSesion() async {
    await _storage.delete(key: _keyToken);
    await _storage.delete(key: _keyUser);
  }

  Future<void> guardarBaseUrl(String url) async {
    await _storage.write(key: 'codeforge_custom_base_url', value: url);
  }

  Future<String?> obtenerBaseUrl() async {
    return await _storage.read(key: 'codeforge_custom_base_url');
  }

  Future<bool> tieneSesionActiva() async {
    final token = await obtenerToken();
    return token != null && token.isNotEmpty;
  }
}
