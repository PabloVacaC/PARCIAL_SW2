import 'dart:async';
import 'package:flutter/foundation.dart';

/// Gestor del estado de conectividad y modo offline de la aplicación.
class OfflineState extends ChangeNotifier {
  static final OfflineState instance = OfflineState._internal();
  factory OfflineState() => instance;
  OfflineState._internal();

  bool _isOffline = false;
  bool _mostrandoRestaurado = false;
  DateTime? _ultimoSyncExitoso;
  Timer? _restauradoTimer;

  bool get isOffline => _isOffline;
  bool get mostrandoRestaurado => _mostrandoRestaurado;
  DateTime? get ultimoSyncExitoso => _ultimoSyncExitoso;

  /// Actualiza el estado de conexión
  void setOffline(bool valor) {
    if (_isOffline == valor) {
      if (!valor) {
        _ultimoSyncExitoso = DateTime.now();
      }
      return;
    }

    final estabaOffline = _isOffline;
    _isOffline = valor;

    if (!valor) {
      _ultimoSyncExitoso = DateTime.now();
      if (estabaOffline) {
        // Mostramos el banner verde temporal de "Conexión restablecida"
        _mostrandoRestaurado = true;
        _restauradoTimer?.cancel();
        _restauradoTimer = Timer(const Duration(seconds: 4), () {
          _mostrandoRestaurado = false;
          notifyListeners();
        });
      }
    } else {
      _mostrandoRestaurado = false;
      _restauradoTimer?.cancel();
    }

    notifyListeners();
  }

  /// Fuerza el reseteo del estado
  void reset() {
    _isOffline = false;
    _mostrandoRestaurado = false;
    _restauradoTimer?.cancel();
    notifyListeners();
  }
}

