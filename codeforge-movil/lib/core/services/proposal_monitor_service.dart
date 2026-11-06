import 'dart:async';
import 'package:flutter/foundation.dart';
import '../../modules/projects/services/proyecto_service.dart';
import '../../modules/proposals/models/proposal_model.dart';
import '../../modules/proposals/services/proposal_service.dart';
import 'proposal_notification_service.dart';

/// Servicio de fondo que monitorea la llegada de nuevas propuestas de IA Colaborativa.
/// Cuando se detecta una propuesta generada desde la web o el móvil, dispara
/// la notificación push en pantalla con vibración háptica.
class ProposalMonitorService {
  static final ProposalMonitorService instance = ProposalMonitorService._internal();
  factory ProposalMonitorService() => instance;
  ProposalMonitorService._internal();

  final ProposalService _proposalService = ProposalService();
  final ProyectoService _proyectoService = ProyectoService();
  final Set<int> _propuestasConocidas = {};
  final Set<int> _proyectosObservados = {};

  Timer? _timer;
  bool _primeraPasada = true;
  bool _consultando = false;

  /// Inicia el monitoreo recurrente cada 3.5 segundos
  void iniciarMonitoreo({List<int>? idsProyectos}) {
    if (idsProyectos != null && idsProyectos.isNotEmpty) {
      _proyectosObservados.addAll(idsProyectos);
    }

    _timer?.cancel();
    _timer = Timer.periodic(const Duration(milliseconds: 3500), (_) => _verificarNuevasPropuestas());

    // Ejecución inmediata inicial
    _verificarNuevasPropuestas();
  }

  /// Agrega un proyecto específico al monitor
  void agregarProyecto(int idProyecto) {
    _proyectosObservados.add(idProyecto);
    _verificarNuevasPropuestas();
  }

  /// Detiene el monitoreo
  void detenerMonitoreo() {
    _timer?.cancel();
    _timer = null;
  }

  /// Limpia el historial de propuestas conocidas (ej. al cerrar sesión)
  void reiniciar() {
    detenerMonitoreo();
    _propuestasConocidas.clear();
    _proyectosObservados.clear();
    _primeraPasada = true;
  }

  Future<void> _verificarNuevasPropuestas() async {
    if (_consultando) return;
    _consultando = true;

    try {
      // Si aún no hay proyectos observados, autodescubrirlos del usuario
      if (_proyectosObservados.isEmpty) {
        final misProys = await _proyectoService.obtenerMisProyectos();
        for (final p in misProys) {
          _proyectosObservados.add(p.idProyecto);
        }
      }

      if (_proyectosObservados.isEmpty) {
        _consultando = false;
        return;
      }

      for (final idProyecto in _proyectosObservados.toList()) {
        final propuestas = await _proposalService.listarPropuestas(idProyecto);

        if (_primeraPasada) {
          // Primera pasada al iniciar: memorizar propuestas previas
          for (final p in propuestas) {
            _propuestasConocidas.add(p.idPropuesta);
          }
        } else {
          // Pasadas periódicas: cualquier propuesta con id desconocido es NUEVA
          for (final p in propuestas) {
            if (!_propuestasConocidas.contains(p.idPropuesta)) {
              _propuestasConocidas.add(p.idPropuesta);

              debugPrint('[ProposalMonitor] ¡NUEVA PROPUESTA DETECTADA! #${p.idPropuesta} en proyecto #$idProyecto');

              // Disparar la Notificación Push Heads-Up
              ProposalNotificationService.instance.mostrarNotificacionPropuesta(p);
            }
          }
        }
      }

      if (_primeraPasada) {
        debugPrint('[ProposalMonitor] Inicializado con ${_propuestasConocidas.length} propuestas conocidas en ${_proyectosObservados.length} proyectos.');
        _primeraPasada = false;
      }
    } catch (e) {
      debugPrint('[ProposalMonitor] Error en verificación: $e');
    } finally {
      _consultando = false;
    }
  }

  /// Permite simular una notificación de prueba para verificación instantánea
  void simularNotificacionPrueba({int idProyecto = 1}) {
    final demo = ProposalModel(
      idPropuesta: (DateTime.now().millisecondsSinceEpoch % 900) + 100,
      idProyecto: idProyecto,
      idModelo: 1,
      versionOrigen: 'v1.0.0',
      titulo: 'Nueva propuesta generada por IA Colaborativa',
      descripcionProblema: 'Optimización de relaciones y entidades detectada.',
      solucionPropuesta: 'Se añadieron atributos y relaciones necesarias.',
      riesgosEfectos: 'Ninguno detectado.',
      operacionesJson: '[]',
      totalOperaciones: 2,
      estado: 'PENDIENTE',
      nombreSolicitante: 'IA CodeForge',
      fechaCreacion: DateTime.now(),
    );

    ProposalNotificationService.instance.mostrarNotificacionPropuesta(demo);
  }
}
