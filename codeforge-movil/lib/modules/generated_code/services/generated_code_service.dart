import '../../../core/config/api_config.dart';
import '../../../core/network/api_client.dart';
import '../models/code_file_model.dart';

class GeneratedCodeService {
  final ApiClient _apiClient;

  GeneratedCodeService({ApiClient? apiClient}) : _apiClient = apiClient ?? ApiClient();

  Future<List<CodeFileModel>> obtenerArchivosGenerados(int idModelo) async {
    try {
      final response = await _apiClient.get(ApiConfig.archivosGeneradosUrl(idModelo));
      if (response is List) {
        return response
            .map((item) => CodeFileModel.fromJson(item as Map<String, dynamic>))
            .toList();
      }
    } catch (_) {}

    return [];
  }
}
