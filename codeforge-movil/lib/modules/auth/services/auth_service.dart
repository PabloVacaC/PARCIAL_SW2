import '../../../core/config/api_config.dart';
import '../../../core/network/api_client.dart';
import '../../../core/storage/secure_storage_service.dart';
import '../models/login_response.dart';

class AuthService {
  final ApiClient _apiClient;
  final SecureStorageService _storageService;

  AuthService({ApiClient? apiClient, SecureStorageService? storageService})
      : _apiClient = apiClient ?? ApiClient(),
        _storageService = storageService ?? SecureStorageService();

  Future<LoginResponse> iniciarSesion({
    required String username,
    required String password,
  }) async {
    final response = await _apiClient.post(
      ApiConfig.loginUrl,
      body: {
        'username': username.trim(),
        'password': password,
      },
    );

    final loginData = LoginResponse.fromJson(response as Map<String, dynamic>);

    // Guardar token y credenciales en Keystore / Keychain
    await _storageService.guardarSesion(
      token: loginData.token,
      usuario: loginData.usuario,
    );

    return loginData;
  }

  Future<void> cerrarSesion() async {
    await _storageService.limpiarSesion();
  }
}
