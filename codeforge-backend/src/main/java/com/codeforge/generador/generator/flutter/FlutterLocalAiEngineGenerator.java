package com.codeforge.generador.generator.flutter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Generador del motor de Inteligencia Artificial para el cliente Flutter de CodeForge.
 * 
 * Implementa una arquitectura híbrida de dos niveles:
 * 1. Nivel 1 (Semántico LLM): Cliente HTTP contra el motor local Ollama (qwen2.5-coder:1.5b)
 *    para realizar Function/Tool Calling inteligente a partir de lenguaje natural libre.
 * 2. Nivel 2 (Fallback Heurístico Offline): Motor de reglas y expresiones regulares que opera
 *    al 100% en el dispositivo de forma instantánea si Ollama no está en ejecución.
 */
@Component
public class FlutterLocalAiEngineGenerator {

    public void generar(
            Path flutterRoot,
            List<Map<String, Object>> elementos
    ) throws IOException {

        Path aiDir = flutterRoot
                .resolve("lib")
                .resolve("generated")
                .resolve("ai");

        Files.createDirectories(aiDir);

        String contenido = construirEngine(elementos);

        Path archivo = aiDir.resolve("local_ai_engine.dart");

        Files.writeString(archivo, contenido);

        System.out.println(
                "FLUTTER AI: Local AI Engine híbrido (Ollama + Fallback) creado: " + archivo
        );
    }

    private String construirEngine(List<Map<String, Object>> elementos) {
        StringBuilder sb = new StringBuilder();

        sb.append("/// Motor de Inteligencia Artificial Semántica Híbrido para Flutter (CodeForge).\n");
        sb.append("///\n");
        sb.append("/// Nivel 1: Inferencia local con Ollama (qwen2.5-coder:1.5b) para Tool Calling inteligente.\n");
        sb.append("/// Nivel 2: Motor heurístico offline basado en reglas para fallback instantáneo y tolerante a fallos.\n");
        sb.append("/// Conecta directamente con las herramientas generadas por CodeForge en AiToolExecutor.\n");
        sb.append("\n");
        sb.append("import 'dart:convert';\n");
        sb.append("import 'dart:async';\n");
        sb.append("import 'package:http/http.dart' as http;\n");
        sb.append("import '../config/api_config.dart';\n");
        sb.append("import 'ai_tools.dart';\n");
        sb.append("import 'ai_tool_executor.dart';\n");
        sb.append("\n");

        sb.append("enum AiActionType {\n");
        sb.append("  crear,\n");
        sb.append("  actualizar,\n");
        sb.append("  listar,\n");
        sb.append("  obtener,\n");
        sb.append("  eliminar,\n");
        sb.append("  desconocido\n");
        sb.append("}\n\n");

        sb.append("class LocalAiResponse {\n");
        sb.append("  final bool exitoso;\n");
        sb.append("  final String mensajeVoz;\n");
        sb.append("  final String comandoOriginal;\n");
        sb.append("  final String? toolEjecutada;\n");
        sb.append("  final Map<String, dynamic>? parametros;\n");
        sb.append("  final dynamic datos;\n");
        sb.append("  final String origen;\n");
        sb.append("\n");
        sb.append("  const LocalAiResponse({\n");
        sb.append("    required this.exitoso,\n");
        sb.append("    required this.mensajeVoz,\n");
        sb.append("    required this.comandoOriginal,\n");
        sb.append("    this.toolEjecutada,\n");
        sb.append("    this.parametros,\n");
        sb.append("    this.datos,\n");
        sb.append("    this.origen = 'Motor Heurístico Local Offline',\n");
        sb.append("  });\n");
        sb.append("}\n\n");

        sb.append("class LocalAiEngine {\n");
        sb.append("  /// Normaliza el texto quitando tildes y caracteres especiales\n");
        sb.append("  static String normalizar(String texto) {\n");
        sb.append("    var s = texto.toLowerCase().trim();\n");
        sb.append("    s = s.replaceAll(RegExp(r'[áàäâ]'), 'a');\n");
        sb.append("    s = s.replaceAll(RegExp(r'[éèëê]'), 'e');\n");
        sb.append("    s = s.replaceAll(RegExp(r'[íìïî]'), 'i');\n");
        sb.append("    s = s.replaceAll(RegExp(r'[óòöô]'), 'o');\n");
        sb.append("    s = s.replaceAll(RegExp(r'[úùüû]'), 'u');\n");
        sb.append("    s = s.replaceAll(RegExp(r'[ñ]'), 'n');\n");
        sb.append("    s = s.replaceAll(RegExp(r'[.,;:!?¿¡]'), ' ');\n");
        sb.append("    return s.replaceAll(RegExp(r'\\s+'), ' ').trim();\n");
        sb.append("  }\n\n");

        sb.append("  /// Procesa el comando de voz/texto del usuario.\n");
        sb.append("  /// Intenta primero ejecutar Tool Calling con Ollama (qwen2.5-coder:1.5b).\n");
        sb.append("  /// Si Ollama no está activo o falla la comunicación, conmuta automáticamente\n");
        sb.append("  /// al motor heurístico offline sin interrumpir la experiencia del usuario.\n");
        sb.append("  static Future<LocalAiResponse> procesarComando(String comando) async {\n");
        sb.append("    final textoNorm = normalizar(comando);\n");
        sb.append("    if (textoNorm.isEmpty) {\n");
        sb.append("      return LocalAiResponse(\n");
        sb.append("        exitoso: false,\n");
        sb.append("        mensajeVoz: 'No escuché ningún comando.',\n");
        sb.append("        comandoOriginal: comando,\n");
        sb.append("        origen: 'Sistema',\n");
        sb.append("      );\n");
        sb.append("    }\n\n");

        sb.append("    // 1. Inferencia semántica con Ollama Local\n");
        sb.append("    if (ApiConfig.useOllama) {\n");
        sb.append("      try {\n");
        sb.append("        final respOllama = await _procesarConOllama(comando);\n");
        sb.append("        if (respOllama != null) {\n");
        sb.append("          return respOllama;\n");
        sb.append("        }\n");
        sb.append("      } catch (e) {\n");
        sb.append("        // Fallback transparente a motor heurístico\n");
        sb.append("        // ignore: avoid_print\n");
        sb.append("        print('CODEFORGE AI: Ollama no disponible ($e). Conmutando a motor heurístico...');\n");
        sb.append("      }\n");
        sb.append("    }\n\n");

        sb.append("    // 2. Fallback de alta resiliencia: Motor heurístico offline\n");
        sb.append("    return _procesarConHeuristica(comando, textoNorm);\n");
        sb.append("  }\n\n");

        // Método Ollama
        sb.append("  /// Cliente de inferencia y Tool Calling contra Ollama Local (qwen2.5-coder:1.5b)\n");
        sb.append("  static Future<LocalAiResponse?> _procesarConOllama(String comando) async {\n");
        sb.append("    String url = ApiConfig.ollamaUrl.trim();\n");
        sb.append("    if (url.endsWith('/')) {\n");
        sb.append("      url = url.substring(0, url.length - 1);\n");
        sb.append("    }\n");
        sb.append("    final endpoint = Uri.parse('$url/api/chat');\n\n");

        sb.append("    // Construcción del resumen de herramientas disponibles\n");
        sb.append("    final StringBuffer herramientasPrompt = StringBuffer();\n");
        sb.append("    for (final tool in codeForgeAiTools) {\n");
        sb.append("      final props = (tool.parameters['properties'] as Map<String, dynamic>?)?.keys.toList() ?? [];\n");
        sb.append("      herramientasPrompt.writeln('- ${tool.name}: ${tool.description} (parámetros: $props)');\n");
        sb.append("    }\n\n");

        sb.append("    final systemPrompt = '''\n");
        sb.append("Eres el Asistente de Inteligencia Artificial de la aplicación generada por CodeForge.\n");
        sb.append("Tu tarea es interpretar la solicitud en lenguaje natural del usuario y mapearla a una de las herramientas del sistema.\n\n");
        sb.append("CATÁLOGO DE HERRAMIENTAS DISPONIBLES:\n");
        sb.append("${herramientasPrompt.toString()}\n");
        sb.append("REGLAS ESTRICTAS DE RESPUESTA:\n");
        sb.append("1. Si la solicitud corresponde a una herramienta, responde ÚNICAMENTE un JSON con:\n");
        sb.append("   {\\n");
        sb.append("     \\\"tool\\\": \\\"nombre_de_la_tool\\\",\\n");
        sb.append("     \\\"arguments\\\": { \\\"parametro\\\": valor },\\n");
        sb.append("     \\\"confirmacion\\\": \\\"Mensaje en español claro y conciso para leer por voz al usuario\\\"\\n");
        sb.append("   }\\n");
        sb.append("2. Si la solicitud es un saludo, pregunta general o no coincide con ninguna herramienta, responde:\\n");
        sb.append("   {\\n");
        sb.append("     \\\"tool\\\": null,\\n");
        sb.append("     \\\"arguments\\\": {},\\n");
        sb.append("     \\\"confirmacion\\\": \\\"Respuesta concisa y útil para el usuario\\\"\\n");
        sb.append("   }\\n");
        sb.append("3. Para parámetros numéricos (IDs, cantidades), conviértelos a números y no a cadenas de texto.\\n");
        sb.append("4. Responde EXCLUSIVAMENTE con el objeto JSON, sin formato Markdown ni texto introductorio.\n");
        sb.append("''';\n\n");

        sb.append("    final body = jsonEncode({\n");
        sb.append("      'model': ApiConfig.ollamaModel,\n");
        sb.append("      'stream': false,\n");
        sb.append("      'format': 'json',\n");
        sb.append("      'options': {\n");
        sb.append("        'temperature': 0.1,\n");
        sb.append("        'num_predict': 512,\n");
        sb.append("      },\n");
        sb.append("      'messages': [\n");
        sb.append("        {'role': 'system', 'content': systemPrompt},\n");
        sb.append("        {'role': 'user', 'content': comando},\n");
        sb.append("      ],\n");
        sb.append("    });\n\n");

        sb.append("    final resp = await http.post(\n");
        sb.append("      endpoint,\n");
        sb.append("      headers: {'Content-Type': 'application/json'},\n");
        sb.append("      body: body,\n");
        sb.append("    ).timeout(Duration(seconds: ApiConfig.ollamaTimeoutSegundos));\n\n");

        sb.append("    if (resp.statusCode != 200) {\n");
        sb.append("      throw Exception('HTTP ${resp.statusCode} desde Ollama: ${resp.body}');\n");
        sb.append("    }\n\n");

        sb.append("    final Map<String, dynamic> data = jsonDecode(utf8.decode(resp.bodyBytes));\n");
        sb.append("    final String? content = data['message']?['content'];\n");
        sb.append("    if (content == null || content.trim().isEmpty) {\n");
        sb.append("      throw Exception('Respuesta vacía de Ollama');\n");
        sb.append("    }\n\n");

        sb.append("    // Limpieza de delimitadores si el modelo incluyó texto extra\n");
        sb.append("    String cleanJson = content.trim();\n");
        sb.append("    final startIdx = cleanJson.indexOf('{');\n");
        sb.append("    final endIdx = cleanJson.lastIndexOf('}');\n");
        sb.append("    if (startIdx != -1 && endIdx != -1 && endIdx >= startIdx) {\n");
        sb.append("      cleanJson = cleanJson.substring(startIdx, endIdx + 1);\n");
        sb.append("    }\n\n");

        sb.append("    final Map<String, dynamic> parsed = jsonDecode(cleanJson);\n");
        sb.append("    final String? toolName = parsed['tool']?.toString();\n");
        sb.append("    final Map<String, dynamic> args = (parsed['arguments'] is Map)\n");
        sb.append("        ? Map<String, dynamic>.from(parsed['arguments'])\n");
        sb.append("        : <String, dynamic>{};\n");
        sb.append("    final String confirmacion = parsed['confirmacion']?.toString() ?? '';\n\n");

        sb.append("    // Caso conversacional / sin herramienta requerida\n");
        sb.append("    if (toolName == null || toolName.isEmpty || toolName.toLowerCase() == 'null') {\n");
        sb.append("      return LocalAiResponse(\n");
        sb.append("        exitoso: true,\n");
        sb.append("        mensajeVoz: confirmacion.isNotEmpty ? confirmacion : 'Entendido.',\n");
        sb.append("        comandoOriginal: comando,\n");
        sb.append("        origen: 'Ollama (${ApiConfig.ollamaModel})',\n");
        sb.append("      );\n");
        sb.append("    }\n\n");

        sb.append("    // Ejecución de la herramienta mapeada en AiToolExecutor\n");
        sb.append("    try {\n");
        sb.append("      final res = await AiToolExecutor.ejecutar(toolName, args);\n");
        sb.append("      return LocalAiResponse(\n");
        sb.append("        exitoso: true,\n");
        sb.append("        mensajeVoz: confirmacion.isNotEmpty\n");
        sb.append("            ? confirmacion\n");
        sb.append("            : 'Acción $toolName ejecutada con éxito.',\n");
        sb.append("        comandoOriginal: comando,\n");
        sb.append("        toolEjecutada: toolName,\n");
        sb.append("        parametros: args,\n");
        sb.append("        datos: res,\n");
        sb.append("        origen: 'Ollama (${ApiConfig.ollamaModel})',\n");
        sb.append("      );\n");
        sb.append("    } catch (e) {\n");
        sb.append("      return LocalAiResponse(\n");
        sb.append("        exitoso: false,\n");
        sb.append("        mensajeVoz: 'Error al ejecutar $toolName: $e',\n");
        sb.append("        comandoOriginal: comando,\n");
        sb.append("        toolEjecutada: toolName,\n");
        sb.append("        parametros: args,\n");
        sb.append("        origen: 'Ollama (${ApiConfig.ollamaModel})',\n");
        sb.append("      );\n");
        sb.append("    }\n");
        sb.append("  }\n\n");

        // Método Heurístico (Fallback)
        sb.append("  /// Motor heurístico local offline basado en reglas semánticas y expresiones regulares\n");
        sb.append("  static Future<LocalAiResponse> _procesarConHeuristica(String comando, String textoNorm) async {\n");
        sb.append("    // 1. Detectar tipo de acción\n");
        sb.append("    AiActionType accion = AiActionType.desconocido;\n");
        sb.append("    if (RegExp(r'\\b(registra|registrar|crea|crear|agrega|agregar|inserta|insertar|nuevo|nueva|anade|anadir)\\b').hasMatch(textoNorm)) {\n");
        sb.append("      accion = AiActionType.crear;\n");
        sb.append("    } else if (RegExp(r'\\b(actualiza|actualizar|modifica|modificar|edita|editar|cambia|cambiar)\\b').hasMatch(textoNorm)) {\n");
        sb.append("      accion = AiActionType.actualizar;\n");
        sb.append("    } else if (RegExp(r'\\b(elimina|eliminar|borra|borrar|quita|quitar|remueve|remover)\\b').hasMatch(textoNorm)) {\n");
        sb.append("      accion = AiActionType.eliminar;\n");
        sb.append("    } else if (RegExp(r'\\b(muestrame|muestra|mostrar|listar|lista|ver todos|ver todas|dame todos|dame todas|todos los|todas las)\\b').hasMatch(textoNorm)) {\n");
        sb.append("      accion = AiActionType.listar;\n");
        sb.append("    } else if (RegExp(r'\\b(consulta|consultar|busca|buscar|obten|obtener|ver|trae|traer|dame)\\b').hasMatch(textoNorm)) {\n");
        sb.append("      accion = AiActionType.obtener;\n");
        sb.append("    }\n\n");

        sb.append("    // 2. Extraer ID numérico si existe\n");
        sb.append("    int? idExtraido;\n");
        sb.append("    final matchId = RegExp(r'\\b(id|con id|numero|codigo|el|la)\\s+(\\d+)\\b').firstMatch(textoNorm) ??\n");
        sb.append("                    RegExp(r'\\b(\\d+)\\b').firstMatch(textoNorm);\n");
        sb.append("    if (matchId != null) {\n");
        sb.append("      final grupo = matchId.groupCount >= 2 ? matchId.group(2) : matchId.group(1);\n");
        sb.append("      if (grupo != null) {\n");
        sb.append("        idExtraido = int.tryParse(grupo);\n");
        sb.append("      }\n");
        sb.append("    }\n\n");

        sb.append("    // 3. Evaluar entidades del modelo UML\n");

        if (elementos != null) {
            for (Map<String, Object> elemento : elementos) {
                if (elemento == null) continue;
                String tipo = String.valueOf(elemento.get("tipo"));
                String nombre = String.valueOf(elemento.get("nombre"));
                if (nombre == null || nombre.isBlank() || !"CLASE".equalsIgnoreCase(tipo)) continue;

                String nombreLower = nombre.trim().toLowerCase();
                String nombrePlural = nombreLower.endsWith("s") ? nombreLower : nombreLower + "s";
                String nombreFlutter = convertirNombreFlutter(nombre);

                sb.append("    // --- Entidad: ").append(nombre).append(" ---\n");
                sb.append("    if (textoNorm.contains('").append(nombreLower).append("') || textoNorm.contains('").append(nombrePlural).append("')) {\n");
                sb.append("      // ELIMINAR\n");
                sb.append("      if (accion == AiActionType.eliminar) {\n");
                sb.append("        if (idExtraido == null) {\n");
                sb.append("          return LocalAiResponse(\n");
                sb.append("            exitoso: false,\n");
                sb.append("            mensajeVoz: 'Por favor indica el ID de ").append(nombre).append(" que deseas eliminar.',\n");
                sb.append("            comandoOriginal: comando,\n");
                sb.append("            origen: 'Motor Heurístico Local Offline',\n");
                sb.append("          );\n");
                sb.append("        }\n");
                sb.append("        try {\n");
                sb.append("          await AiToolExecutor.ejecutar('").append(nombreFlutter).append("_eliminar', {'id': idExtraido});\n");
                sb.append("          return LocalAiResponse(\n");
                sb.append("            exitoso: true,\n");
                sb.append("            mensajeVoz: '").append(nombre).append(" con ID $idExtraido eliminado correctamente.',\n");
                sb.append("            comandoOriginal: comando,\n");
                sb.append("            toolEjecutada: '").append(nombreFlutter).append("_eliminar',\n");
                sb.append("            parametros: {'id': idExtraido},\n");
                sb.append("            origen: 'Motor Heurístico Local Offline',\n");
                sb.append("          );\n");
                sb.append("        } catch (e) {\n");
                sb.append("          return LocalAiResponse(\n");
                sb.append("            exitoso: false,\n");
                sb.append("            mensajeVoz: 'Error al eliminar ").append(nombre).append(": $e',\n");
                sb.append("            comandoOriginal: comando,\n");
                sb.append("            origen: 'Motor Heurístico Local Offline',\n");
                sb.append("          );\n");
                sb.append("        }\n");
                sb.append("      }\n\n");

                sb.append("      // OBTENER POR ID\n");
                sb.append("      if (accion == AiActionType.obtener && idExtraido != null) {\n");
                sb.append("        try {\n");
                sb.append("          final res = await AiToolExecutor.ejecutar('").append(nombreFlutter).append("_obtener', {'id': idExtraido});\n");
                sb.append("          return LocalAiResponse(\n");
                sb.append("            exitoso: true,\n");
                sb.append("            mensajeVoz: 'Aquí tienes los datos de ").append(nombre).append(" $idExtraido.',\n");
                sb.append("            comandoOriginal: comando,\n");
                sb.append("            toolEjecutada: '").append(nombreFlutter).append("_obtener',\n");
                sb.append("            parametros: {'id': idExtraido},\n");
                sb.append("            datos: res,\n");
                sb.append("            origen: 'Motor Heurístico Local Offline',\n");
                sb.append("          );\n");
                sb.append("        } catch (e) {\n");
                sb.append("          return LocalAiResponse(\n");
                sb.append("            exitoso: false,\n");
                sb.append("            mensajeVoz: 'No se encontró ").append(nombre).append(" con ID $idExtraido.',\n");
                sb.append("            comandoOriginal: comando,\n");
                sb.append("            origen: 'Motor Heurístico Local Offline',\n");
                sb.append("          );\n");
                sb.append("        }\n");
                sb.append("      }\n\n");

                sb.append("      // LISTAR TODOS\n");
                sb.append("      if (accion == AiActionType.listar || (accion == AiActionType.obtener && idExtraido == null)) {\n");
                sb.append("        try {\n");
                sb.append("          final res = await AiToolExecutor.ejecutar('").append(nombreFlutter).append("_listar', {});\n");
                sb.append("          final lista = (res is List) ? res : [res];\n");
                sb.append("          return LocalAiResponse(\n");
                sb.append("            exitoso: true,\n");
                sb.append("            mensajeVoz: 'Se encontraron ${lista.length} registros de ").append(nombre).append(".',\n");
                sb.append("            comandoOriginal: comando,\n");
                sb.append("            toolEjecutada: '").append(nombreFlutter).append("_listar',\n");
                sb.append("            parametros: {},\n");
                sb.append("            datos: lista,\n");
                sb.append("            origen: 'Motor Heurístico Local Offline',\n");
                sb.append("          );\n");
                sb.append("        } catch (e) {\n");
                sb.append("          return LocalAiResponse(\n");
                sb.append("            exitoso: false,\n");
                sb.append("            mensajeVoz: 'Error al listar ").append(nombre).append(": $e',\n");
                sb.append("            comandoOriginal: comando,\n");
                sb.append("            origen: 'Motor Heurístico Local Offline',\n");
                sb.append("          );\n");
                sb.append("        }\n");
                sb.append("      }\n\n");

                sb.append("      // CREAR / REGISTRAR / ACTUALIZAR\n");
                sb.append("      if (accion == AiActionType.crear || accion == AiActionType.actualizar || accion == AiActionType.desconocido) {\n");
                sb.append("        final params = <String, dynamic>{};\n");
                sb.append("        if (idExtraido != null) params['id'] = idExtraido;\n");

                Object attrsObj = elemento.get("atributos");
                if (attrsObj instanceof List<?>) {
                    List<?> attrs = (List<?>) attrsObj;
                    for (Object aObj : attrs) {
                        if (!(aObj instanceof Map<?, ?>)) continue;
                        Map<?, ?> a = (Map<?, ?>) aObj;
                        String attrNom = String.valueOf(a.get("nombre"));
                        String attrTipo = String.valueOf(a.get("tipo"));
                        if (attrNom == null || attrNom.equalsIgnoreCase("id")) continue;

                        String attrNomLower = attrNom.toLowerCase();
                        sb.append("        // Extraer ").append(attrNom).append("\n");
                        sb.append("        final match_").append(attrNom).append(" = RegExp(r'\\b(").append(attrNomLower).append("|con ").append(attrNomLower).append("|de ").append(attrNomLower).append(")\\s+([a-zA-Z0-9@._-]+)').firstMatch(textoNorm);\n");
                        sb.append("        if (match_").append(attrNom).append(" != null && match_").append(attrNom).append("!.group(2) != null) {\n");
                        if ("Integer".equalsIgnoreCase(attrTipo) || "int".equalsIgnoreCase(attrTipo)) {
                            sb.append("          params['").append(attrNom).append("'] = int.tryParse(match_").append(attrNom).append("!.group(2)!) ?? 0;\n");
                        } else if ("Double".equalsIgnoreCase(attrTipo) || "Float".equalsIgnoreCase(attrTipo) || "BigDecimal".equalsIgnoreCase(attrTipo)) {
                            sb.append("          params['").append(attrNom).append("'] = double.tryParse(match_").append(attrNom).append("!.group(2)!) ?? 0.0;\n");
                        } else {
                            sb.append("          params['").append(attrNom).append("'] = match_").append(attrNom).append("!.group(2)!;\n");
                        }
                        sb.append("        }\n");
                    }
                }

                sb.append("        if (accion == AiActionType.actualizar && idExtraido != null) {\n");
                sb.append("          try {\n");
                sb.append("            final res = await AiToolExecutor.ejecutar('").append(nombreFlutter).append("_actualizar', params);\n");
                sb.append("            return LocalAiResponse(\n");
                sb.append("              exitoso: true,\n");
                sb.append("              mensajeVoz: '").append(nombre).append(" con ID $idExtraido actualizado correctamente.',\n");
                sb.append("              comandoOriginal: comando,\n");
                sb.append("              toolEjecutada: '").append(nombreFlutter).append("_actualizar',\n");
                sb.append("              parametros: params,\n");
                sb.append("              datos: res,\n");
                sb.append("              origen: 'Motor Heurístico Local Offline',\n");
                sb.append("            );\n");
                sb.append("          } catch (e) {\n");
                sb.append("            return LocalAiResponse(\n");
                sb.append("              exitoso: false,\n");
                sb.append("              mensajeVoz: 'Error al actualizar ").append(nombre).append(": $e',\n");
                sb.append("              comandoOriginal: comando,\n");
                sb.append("              origen: 'Motor Heurístico Local Offline',\n");
                sb.append("            );\n");
                sb.append("          }\n");
                sb.append("        } else {\n");
                sb.append("          try {\n");
                sb.append("            final res = await AiToolExecutor.ejecutar('").append(nombreFlutter).append("_crear', params);\n");
                sb.append("            return LocalAiResponse(\n");
                sb.append("              exitoso: true,\n");
                sb.append("              mensajeVoz: '").append(nombre).append(" registrado con éxito en el sistema.',\n");
                sb.append("              comandoOriginal: comando,\n");
                sb.append("              toolEjecutada: '").append(nombreFlutter).append("_crear',\n");
                sb.append("              parametros: params,\n");
                sb.append("              datos: res,\n");
                sb.append("              origen: 'Motor Heurístico Local Offline',\n");
                sb.append("            );\n");
                sb.append("          } catch (e) {\n");
                sb.append("            return LocalAiResponse(\n");
                sb.append("              exitoso: false,\n");
                sb.append("              mensajeVoz: 'No se pudo crear ").append(nombre).append(": $e',\n");
                sb.append("              comandoOriginal: comando,\n");
                sb.append("              origen: 'Motor Heurístico Local Offline',\n");
                sb.append("            );\n");
                sb.append("          }\n");
                sb.append("        }\n");
                sb.append("      }\n");
                sb.append("    }\n\n");
            }
        }

        sb.append("    // Si ninguna entidad coincidió\n");
        sb.append("    return LocalAiResponse(\n");
        sb.append("      exitoso: false,\n");
        sb.append("      mensajeVoz: 'No reconocí la entidad o acción solicitada. Prueba con: Registra un cliente, o Muéstrame todas las facturas.',\n");
        sb.append("      comandoOriginal: comando,\n");
        sb.append("      origen: 'Motor Heurístico Local Offline',\n");
        sb.append("    );\n");
        sb.append("  }\n");
        sb.append("}\n");

        return sb.toString();
    }

    private String convertirNombreFlutter(String nombre) {
        if (nombre == null || nombre.isBlank()) return "modelo";
        String limpio = nombre.trim().replaceAll("[^a-zA-Z0-9]", "_");
        limpio = limpio.replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        return limpio.toLowerCase();
    }
}
