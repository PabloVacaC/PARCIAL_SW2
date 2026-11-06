import 'package:flutter/foundation.dart';
import '../../../core/services/proposal_monitor_service.dart';
import '../../../core/storage/secure_storage_service.dart';
import '../models/auth_user.dart';
import '../services/auth_service.dart';

enum AuthStatus { inicial, noAutenticado, autenticado, cargando }

class AuthProvider extends ChangeNotifier {
  final AuthService _authService;
  final SecureStorageService _storageService;

  AuthStatus _status = AuthStatus.inicial;
  bool _cargandoLogin = false;
  AuthUser? _usuario;
  String? _error;

  AuthProvider({AuthService? authService, SecureStorageService? storageService})
      : _authService = authService ?? AuthService(),
        _storageService = storageService ?? SecureStorageService();

  AuthStatus get status => _status;
  bool get cargandoLogin => _cargandoLogin;
  AuthUser? get usuario => _usuario;
  String? get error => _error;
  bool get estaAutenticado => _status == AuthStatus.autenticado;

  Future<void> verificarSesionExistente() async {
    _status = AuthStatus.cargando;
    notifyListeners();

    try {
      final token = await _storageService.obtenerToken();
      final user = await _storageService.obtenerUsuario();

      if (token != null && token.isNotEmpty && user != null) {
        _usuario = user;
        _status = AuthStatus.autenticado;
        ProposalMonitorService.instance.iniciarMonitoreo();
      } else {
        _status = AuthStatus.noAutenticado;
      }
    } catch (_) {
      _status = AuthStatus.noAutenticado;
    }
    notifyListeners();
  }

  Future<bool> login(String username, String password) async {
    _cargandoLogin = true;
    _error = null;
    notifyListeners();

    try {
      final resp = await _authService.iniciarSesion(
        username: username,
        password: password,
      );
      _usuario = resp.usuario;
      _status = AuthStatus.autenticado;
      _cargandoLogin = false;
      ProposalMonitorService.instance.iniciarMonitoreo();
      notifyListeners();
      return true;
    } catch (e) {
      _cargandoLogin = false;
      _error = e.toString().replaceFirst('Exception: ', '');
      notifyListeners();
      return false;
    }
  }

  Future<void> logout() async {
    ProposalMonitorService.instance.reiniciar();
    await _authService.cerrarSesion();
    _usuario = null;
    _status = AuthStatus.noAutenticado;
    notifyListeners();
  }
}
