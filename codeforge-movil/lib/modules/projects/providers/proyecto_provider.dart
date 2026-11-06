import 'package:flutter/foundation.dart';
import '../models/proyecto_model.dart';
import '../services/proyecto_service.dart';

class ProyectoProvider extends ChangeNotifier {
  final ProyectoService _proyectoService;

  List<ProyectoModel> _proyectos = [];
  List<ProyectoModel> _proyectosFiltrados = [];
  bool _cargando = false;
  String? _error;
  String _filtro = '';

  ProyectoProvider({ProyectoService? proyectoService})
      : _proyectoService = proyectoService ?? ProyectoService();

  List<ProyectoModel> get proyectos => _proyectosFiltrados;
  bool get cargando => _cargando;
  String? get error => _error;
  String get filtro => _filtro;
  int get totalProyectos => _proyectos.length;

  Future<void> cargarProyectos() async {
    _cargando = true;
    _error = null;
    notifyListeners();

    try {
      final list = await _proyectoService.obtenerMisProyectos();
      _proyectos = list;
      _aplicarFiltro();
      _cargando = false;
      notifyListeners();

      // Cargar cantidad de integrantes de fondo sin bloquear la UI
      for (final p in _proyectos) {
        _proyectoService.obtenerColaboradores(p.idProyecto).then((colabs) {
          p.cantidadIntegrantes = colabs.length + 1; // +1 propietario
          notifyListeners();
        });
      }
    } catch (e) {
      _cargando = false;
      _error = e.toString().replaceFirst('Exception: ', '');
      notifyListeners();
    }
  }

  void filtrar(String texto) {
    _filtro = texto;
    _aplicarFiltro();
    notifyListeners();
  }

  void _aplicarFiltro() {
    if (_filtro.trim().isEmpty) {
      _proyectosFiltrados = List.from(_proyectos);
    } else {
      final q = _filtro.toLowerCase();
      _proyectosFiltrados = _proyectos.where((p) {
        return p.nombre.toLowerCase().contains(q) ||
            p.descripcion.toLowerCase().contains(q) ||
            p.nombrePropietario.toLowerCase().contains(q) ||
            p.idProyecto.toString().contains(q);
      }).toList();
    }
  }
}
