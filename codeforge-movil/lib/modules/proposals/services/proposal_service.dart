import '../../../core/config/api_config.dart';
import '../../../core/network/api_client.dart';
import '../models/proposal_model.dart';

class ProposalService {
  final ApiClient _apiClient;

  ProposalService({ApiClient? apiClient}) : _apiClient = apiClient ?? ApiClient();

  Future<List<ProposalModel>> listarPropuestas(int idProyecto) async {
    final response = await _apiClient.get(ApiConfig.iaPropuestasUrl(idProyecto));
    if (response is List) {
      return response
          .map((item) => ProposalModel.fromJson(item as Map<String, dynamic>))
          .toList();
    }
    return [];
  }

  Future<ProposalModel> obtenerPropuesta(int idProyecto, int idPropuesta) async {
    final response = await _apiClient.get(ApiConfig.iaPropuestaDetalleUrl(idProyecto, idPropuesta));
    return ProposalModel.fromJson(response as Map<String, dynamic>);
  }
}
