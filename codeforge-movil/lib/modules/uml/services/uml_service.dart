import '../../../core/config/api_config.dart';
import '../../../core/network/api_client.dart';
import '../models/uml_model.dart';

class UmlService {
  final ApiClient _apiClient;

  UmlService({ApiClient? apiClient}) : _apiClient = apiClient ?? ApiClient();

  Future<List<UmlDiagramaModel>> obtenerModelosProyecto(int idProyecto) async {
    final response = await _apiClient.get(ApiConfig.modelosProyectoUrl(idProyecto));
    if (response is List) {
      return response
          .map((item) => UmlDiagramaModel.fromBackendJson(item as Map<String, dynamic>))
          .toList();
    }
    return [];
  }

  Future<UmlDiagramaModel> obtenerDetalleModelo(int idModelo) async {
    final response = await _apiClient.get(ApiConfig.modeloDetalleUrl(idModelo));
    return UmlDiagramaModel.fromBackendJson(response as Map<String, dynamic>);
  }
}
