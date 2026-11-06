import '../../../core/config/api_config.dart';
import '../../../core/network/api_client.dart';
import '../models/chat_message_model.dart';

class IaHistoryService {
  final ApiClient _apiClient;

  IaHistoryService({ApiClient? apiClient}) : _apiClient = apiClient ?? ApiClient();

  Future<List<ChatMessageModel>> obtenerHistorialConversacion(int idProyecto) async {
    final response = await _apiClient.get(ApiConfig.iaConversacionUrl(idProyecto));
    if (response is Map<String, dynamic>) {
      final mensajesRaw = response['mensajes'];
      if (mensajesRaw is List) {
        return mensajesRaw
            .map((item) => ChatMessageModel.fromJson(item as Map<String, dynamic>))
            .toList();
      }
    }
    return [];
  }
}
