import '../../../core/config/api_config.dart';
import '../../../core/network/api_client.dart';
import '../models/colaborador_model.dart';
import '../models/proyecto_model.dart';

class ProyectoService {
  final ApiClient _apiClient;

  ProyectoService({ApiClient? apiClient}) : _apiClient = apiClient ?? ApiClient();

  Future<List<ProyectoModel>> obtenerMisProyectos() async {
    final response = await _apiClient.get(ApiConfig.misProyectosUrl);
    if (response is List) {
      return response.map((item) => ProyectoModel.fromJson(item as Map<String, dynamic>)).toList();
    }
    return [];
  }

  Future<ProyectoModel> obtenerProyecto(int idProyecto) async {
    final response = await _apiClient.get(ApiConfig.proyectoUrl(idProyecto));
    return ProyectoModel.fromJson(response as Map<String, dynamic>);
  }

  Future<List<ColaboradorModel>> obtenerColaboradores(int idProyecto) async {
    try {
      final response = await _apiClient.get(ApiConfig.colaboradoresUrl(idProyecto));
      if (response is List) {
        return response
            .map((item) => ColaboradorModel.fromJson(item as Map<String, dynamic>))
            .toList();
      }
    } catch (_) {}
    return [];
  }

  Future<Map<String, dynamic>> obtenerDocumentacion(int idProyecto) async {
    try {
      final response = await _apiClient.get(ApiConfig.documentacionUrl(idProyecto));
      if (response is Map<String, dynamic>) {
        return response;
      }
    } catch (_) {}
    return {};
  }
}
