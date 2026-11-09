package com.codeforge.generador.generator.flutter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class FlutterScreenGenerator {

    public void generar(
            Path flutterRoot,
            List<Map<String, Object>> elementos
    ) throws IOException {

        Path screensDir = flutterRoot
                .resolve("lib")
                .resolve("screens");

        Files.createDirectories(screensDir);

        String contenido = construirPantalla(elementos);

        Path archivo = screensDir.resolve("voice_assistant_screen.dart");

        Files.writeString(archivo, contenido);

        System.out.println(
                "FLUTTER UI: Voice Assistant Screen creada: " + archivo
        );
    }

    private String construirPantalla(List<Map<String, Object>> elementos) {
        StringBuilder sb = new StringBuilder();

        sb.append("import 'package:flutter/material.dart';\n");
        sb.append("import 'package:speech_to_text/speech_to_text.dart' as stt;\n");
        sb.append("import 'package:flutter_tts/flutter_tts.dart';\n");
        sb.append("import '../generated/ai/local_ai_engine.dart';\n");
        sb.append("import '../generated/config/api_config.dart';\n");
        sb.append("\n");

        sb.append("class VoiceAssistantScreen extends StatefulWidget {\n");
        sb.append("  const VoiceAssistantScreen({super.key});\n\n");
        sb.append("  @override\n");
        sb.append("  State<VoiceAssistantScreen> createState() => _VoiceAssistantScreenState();\n");
        sb.append("}\n\n");

        sb.append("class _VoiceAssistantScreenState extends State<VoiceAssistantScreen> {\n");
        sb.append("  final TextEditingController _textController = TextEditingController();\n");
        sb.append("  final ScrollController _scrollController = ScrollController();\n");
        sb.append("  final stt.SpeechToText _speech = stt.SpeechToText();\n");
        sb.append("  final FlutterTts _flutterTts = FlutterTts();\n");
        sb.append("\n");
        sb.append("  bool _isListening = false;\n");
        sb.append("  bool _isProcessing = false;\n");
        sb.append("  String _recognizedText = '';\n");
        sb.append("  LocalAiResponse? _lastResponse;\n");
        sb.append("\n");
        sb.append("  @override\n");
        sb.append("  void initState() {\n");
        sb.append("    super.initState();\n");
        sb.append("    _initTts();\n");
        sb.append("  }\n\n");
        sb.append("  void _initTts() async {\n");
        sb.append("    try {\n");
        sb.append("      await _flutterTts.setLanguage('es-ES');\n");
        sb.append("      await _flutterTts.setPitch(1.0);\n");
        sb.append("      await _flutterTts.setSpeechRate(0.5);\n");
        sb.append("    } catch (_) {}\n");
        sb.append("  }\n\n");
        sb.append("  void _speak(String text) async {\n");
        sb.append("    try {\n");
        sb.append("      await _flutterTts.speak(text);\n");
        sb.append("    } catch (_) {}\n");
        sb.append("  }\n\n");
        sb.append("  void _toggleListening() async {\n");
        sb.append("    if (!_isListening) {\n");
        sb.append("      bool available = await _speech.initialize(\n");
        sb.append("        onStatus: (status) {\n");
        sb.append("          if (status == 'done' || status == 'notListening') {\n");
        sb.append("            setState(() => _isListening = false);\n");
        sb.append("            if (_recognizedText.trim().isNotEmpty) {\n");
        sb.append("              _executeCommand(_recognizedText);\n");
        sb.append("            }\n");
        sb.append("          }\n");
        sb.append("        },\n");
        sb.append("        onError: (errorNotification) {\n");
        sb.append("          setState(() => _isListening = false);\n");
        sb.append("        },\n");
        sb.append("      );\n");
        sb.append("      if (available) {\n");
        sb.append("        setState(() {\n");
        sb.append("          _isListening = true;\n");
        sb.append("          _recognizedText = '';\n");
        sb.append("        });\n");
        sb.append("        _speech.listen(\n");
        sb.append("          localeId: 'es_ES',\n");
        sb.append("          onResult: (result) {\n");
        sb.append("            setState(() {\n");
        sb.append("              _recognizedText = result.recognizedWords;\n");
        sb.append("              _textController.text = _recognizedText;\n");
        sb.append("            });\n");
        sb.append("          },\n");
        sb.append("        );\n");
        sb.append("      }\n");
        sb.append("    } else {\n");
        sb.append("      setState(() => _isListening = false);\n");
        sb.append("      _speech.stop();\n");
        sb.append("      if (_recognizedText.trim().isNotEmpty) {\n");
        sb.append("        _executeCommand(_recognizedText);\n");
        sb.append("      }\n");
        sb.append("    }\n");
        sb.append("  }\n\n");
        sb.append("  void _executeCommand(String command) async {\n");
        sb.append("    final cmd = command.trim();\n");
        sb.append("    if (cmd.isEmpty) return;\n");
        sb.append("    setState(() => _isProcessing = true);\n");
        sb.append("    final response = await LocalAiEngine.procesarComando(cmd);\n");
        sb.append("    setState(() {\n");
        sb.append("      _lastResponse = response;\n");
        sb.append("      _isProcessing = false;\n");
        sb.append("    });\n");
        sb.append("    _speak(response.mensajeVoz);\n");
        sb.append("  }\n\n");
        sb.append("  void _showConfigDialog() {\n");
        sb.append("    final urlCtrl = TextEditingController(text: ApiConfig.baseUrl);\n");
        sb.append("    final ollamaCtrl = TextEditingController(text: ApiConfig.ollamaUrl);\n");
        sb.append("    final modelCtrl = TextEditingController(text: ApiConfig.ollamaModel);\n");
        sb.append("    bool enableOllama = ApiConfig.useOllama;\n");
        sb.append("    showDialog(\n");
        sb.append("      context: context,\n");
        sb.append("      builder: (ctx) => StatefulBuilder(\n");
        sb.append("        builder: (ctx, setDialogState) => AlertDialog(\n");
        sb.append("          backgroundColor: const Color(0xFF1E2433),\n");
        sb.append("          title: const Row(\n");
        sb.append("            children: [\n");
        sb.append("              Icon(Icons.tune_rounded, color: Color(0xFFC084FC), size: 20),\n");
        sb.append("              SizedBox(width: 8),\n");
        sb.append("              Text('Configuración CodeForge', style: TextStyle(color: Colors.white, fontSize: 16)),\n");
        sb.append("            ],\n");
        sb.append("          ),\n");
        sb.append("          content: SingleChildScrollView(\n");
        sb.append("            child: Column(\n");
        sb.append("              mainAxisSize: MainAxisSize.min,\n");
        sb.append("              crossAxisAlignment: CrossAxisAlignment.start,\n");
        sb.append("              children: [\n");
        sb.append("                const Text('URL Backend Spring Boot:', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),\n");
        sb.append("                const SizedBox(height: 6),\n");
        sb.append("                TextField(\n");
        sb.append("                  controller: urlCtrl,\n");
        sb.append("                  style: const TextStyle(color: Colors.white, fontSize: 13),\n");
        sb.append("                  decoration: InputDecoration(\n");
        sb.append("                    filled: true,\n");
        sb.append("                    fillColor: const Color(0xFF0F172A),\n");
        sb.append("                    contentPadding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),\n");
        sb.append("                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),\n");
        sb.append("                  ),\n");
        sb.append("                ),\n");
        sb.append("                const SizedBox(height: 12),\n");
        sb.append("                const Text('URL Ollama (IA Local):', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),\n");
        sb.append("                const SizedBox(height: 6),\n");
        sb.append("                TextField(\n");
        sb.append("                  controller: ollamaCtrl,\n");
        sb.append("                  style: const TextStyle(color: Colors.white, fontSize: 13),\n");
        sb.append("                  decoration: InputDecoration(\n");
        sb.append("                    filled: true,\n");
        sb.append("                    fillColor: const Color(0xFF0F172A),\n");
        sb.append("                    contentPadding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),\n");
        sb.append("                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),\n");
        sb.append("                  ),\n");
        sb.append("                ),\n");
        sb.append("                const SizedBox(height: 12),\n");
        sb.append("                const Text('Modelo Ollama:', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),\n");
        sb.append("                const SizedBox(height: 6),\n");
        sb.append("                TextField(\n");
        sb.append("                  controller: modelCtrl,\n");
        sb.append("                  style: const TextStyle(color: Colors.white, fontSize: 13),\n");
        sb.append("                  decoration: InputDecoration(\n");
        sb.append("                    filled: true,\n");
        sb.append("                    fillColor: const Color(0xFF0F172A),\n");
        sb.append("                    contentPadding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),\n");
        sb.append("                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),\n");
        sb.append("                  ),\n");
        sb.append("                ),\n");
        sb.append("                const SizedBox(height: 10),\n");
        sb.append("                Row(\n");
        sb.append("                  mainAxisAlignment: MainAxisAlignment.spaceBetween,\n");
        sb.append("                  children: [\n");
        sb.append("                    const Text('Habilitar Ollama:', style: TextStyle(color: Colors.white, fontSize: 13)),\n");
        sb.append("                    Switch(\n");
        sb.append("                      value: enableOllama,\n");
        sb.append("                      activeColor: const Color(0xFFC084FC),\n");
        sb.append("                      onChanged: (v) => setDialogState(() => enableOllama = v),\n");
        sb.append("                    ),\n");
        sb.append("                  ],\n");
        sb.append("                ),\n");
        sb.append("              ],\n");
        sb.append("            ),\n");
        sb.append("          ),\n");
        sb.append("          actions: [\n");
        sb.append("            TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancelar')),\n");
        sb.append("            ElevatedButton(\n");
        sb.append("              style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFFA855F7)),\n");
        sb.append("              onPressed: () {\n");
        sb.append("                ApiConfig.setBaseUrl(urlCtrl.text.trim());\n");
        sb.append("                ApiConfig.setOllamaUrl(ollamaCtrl.text.trim());\n");
        sb.append("                ApiConfig.setOllamaModel(modelCtrl.text.trim());\n");
        sb.append("                ApiConfig.setUseOllama(enableOllama);\n");
        sb.append("                Navigator.pop(ctx);\n");
        sb.append("              },\n");
        sb.append("              child: const Text('Guardar', style: TextStyle(color: Colors.white)),\n");
        sb.append("            ),\n");
        sb.append("          ],\n");
        sb.append("        ),\n");
        sb.append("      ),\n");
        sb.append("    );\n");
        sb.append("  }\n\n");
        sb.append("  @override\n");
        sb.append("  Widget build(BuildContext context) {\n");
        sb.append("    return Scaffold(\n");
        sb.append("      backgroundColor: const Color(0xFF0B0F19),\n");
        sb.append("      appBar: AppBar(\n");
        sb.append("        backgroundColor: const Color(0xFF0B0F19),\n");
        sb.append("        elevation: 0,\n");
        sb.append("        leading: Padding(\n");
        sb.append("          padding: const EdgeInsets.only(left: 12),\n");
        sb.append("          child: CircleAvatar(\n");
        sb.append("            backgroundColor: const Color(0xFF581C87),\n");
        sb.append("            child: const Icon(Icons.auto_awesome, color: Color(0xFFD8B4FE), size: 20),\n");
        sb.append("          ),\n");
        sb.append("        ),\n");
        sb.append("        title: Column(\n");
        sb.append("          crossAxisAlignment: CrossAxisAlignment.start,\n");
        sb.append("          children: const [\n");
        sb.append("            Text('Asistente de Voz IA', style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),\n");
        sb.append("            Text('Ollama qwen2.5-coder:1.5b + Fallback Local', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 11)),\n");
        sb.append("          ],\n");
        sb.append("        ),\n");
        sb.append("        actions: [\n");
        sb.append("          IconButton(\n");
        sb.append("            icon: const Icon(Icons.settings_outlined, color: Color(0xFF94A3B8)),\n");
        sb.append("            onPressed: _showConfigDialog,\n");
        sb.append("          ),\n");
        sb.append("          IconButton(\n");
        sb.append("            icon: const Icon(Icons.delete_sweep_outlined, color: Color(0xFF94A3B8)),\n");
        sb.append("            onPressed: () => setState(() => _lastResponse = null),\n");
        sb.append("          ),\n");
        sb.append("        ],\n");
        sb.append("      ),\n");
        sb.append("      body: Column(\n");
        sb.append("        children: [\n");
        sb.append("          Expanded(\n");
        sb.append("            child: SingleChildScrollView(\n");
        sb.append("              controller: _scrollController,\n");
        sb.append("              padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 12),\n");
        sb.append("              child: Column(\n");
        sb.append("                children: [\n");
        sb.append("                  const SizedBox(height: 12),\n");
        sb.append("                  // Hero con icono y texto\n");
        sb.append("                  Container(\n");
        sb.append("                    width: 72,\n");
        sb.append("                    height: 72,\n");
        sb.append("                    decoration: BoxDecoration(\n");
        sb.append("                      color: const Color(0xFF1E2433),\n");
        sb.append("                      shape: BoxShape.circle,\n");
        sb.append("                      border: Border.all(color: const Color(0xFF334155), width: 1.5),\n");
        sb.append("                    ),\n");
        sb.append("                    child: const Icon(Icons.record_voice_over_rounded, color: Color(0xFFC084FC), size: 36),\n");
        sb.append("                  ),\n");
        sb.append("                  const SizedBox(height: 16),\n");
        sb.append("                  const Text(\n");
        sb.append("                    '¡Controla tu sistema por Voz!',\n");
        sb.append("                    style: TextStyle(color: Colors.white, fontSize: 20, fontWeight: FontWeight.bold),\n");
        sb.append("                  ),\n");
        sb.append("                  const SizedBox(height: 6),\n");
        sb.append("                  const Text(\n");
        sb.append("                    'Presiona el micrófono y prueba diciendo:',\n");
        sb.append("                    style: TextStyle(color: Color(0xFF94A3B8), fontSize: 13),\n");
        sb.append("                  ),\n");
        sb.append("                  const SizedBox(height: 20),\n");

        sb.append("                  // Tarjetas de sugerencias generadas según el UML\n");

        List<String> chipsComandos = new ArrayList<>();

        if (elementos != null) {
            for (Map<String, Object> elemento : elementos) {
                if (elemento == null) continue;
                String tipo = String.valueOf(elemento.get("tipo"));
                String nombre = String.valueOf(elemento.get("nombre"));
                if (nombre == null || nombre.isBlank() || !"CLASE".equalsIgnoreCase(tipo)) continue;

                String nombreLower = nombre.trim().toLowerCase();
                String nombrePlural = nombreLower.endsWith("s") ? nombreLower : nombreLower + "s";

                // Atributos de muestra
                String primerAttr = "codigo 123";
                Object attrsObj = elemento.get("atributos");
                if (attrsObj instanceof List<?>) {
                    List<?> attrs = (List<?>) attrsObj;
                    for (Object aObj : attrs) {
                        if (aObj instanceof Map<?, ?>) {
                            String aNom = String.valueOf(((Map<?, ?>) aObj).get("nombre"));
                            if (aNom != null && !aNom.equalsIgnoreCase("id")) {
                                primerAttr = aNom + " " + (aNom.equalsIgnoreCase("ci") || aNom.equalsIgnoreCase("dni") ? "123456" : "A01");
                                break;
                            }
                        }
                    }
                }

                String cmdCrear = "Registra un " + nombreLower + " con " + primerAttr;
                String cmdListar = "Muéstrame todos los " + nombrePlural;
                String cmdConsultar = "Consulta el " + nombreLower + " 1";
                String cmdEliminar = "Elimina el " + nombreLower + " con ID 2";

                chipsComandos.add(cmdCrear);
                chipsComandos.add(cmdListar);

                sb.append("                  _buildSuggestionCard(\n");
                sb.append("                    icon: Icons.folder_open_rounded,\n");
                sb.append("                    title: 'Gestión de ").append(nombrePlural.substring(0, 1).toUpperCase() + nombrePlural.substring(1)).append("',\n");
                sb.append("                    examples: [\n");
                sb.append("                      '\"").append(cmdCrear).append("\"',\n");
                sb.append("                      '\"").append(cmdListar).append("\"',\n");
                sb.append("                      '\"").append(cmdConsultar).append("\"',\n");
                sb.append("                      '\"").append(cmdEliminar).append("\"',\n");
                sb.append("                    ],\n");
                sb.append("                    onTapExample: (example) {\n");
                sb.append("                      final clean = example.replaceAll('\"', '');\n");
                sb.append("                      _textController.text = clean;\n");
                sb.append("                      _executeCommand(clean);\n");
                sb.append("                    },\n");
                sb.append("                  ),\n");
                sb.append("                  const SizedBox(height: 14),\n");
            }
        }

        sb.append("                  // Panel de Respuesta de la IA\n");
        sb.append("                  if (_isProcessing) ...[\n");
        sb.append("                    const SizedBox(height: 10),\n");
        sb.append("                    const CircularProgressIndicator(color: Color(0xFFC084FC)),\n");
        sb.append("                    const SizedBox(height: 8),\n");
        sb.append("                    const Text('Procesando instrucción...', style: TextStyle(color: Color(0xFF94A3B8), fontSize: 12)),\n");
        sb.append("                  ] else if (_lastResponse != null) ...[\n");
        sb.append("                    _buildResponseCard(_lastResponse!),\n");
        sb.append("                  ],\n");
        sb.append("                  const SizedBox(height: 80),\n");
        sb.append("                ],\n");
        sb.append("              ),\n");
        sb.append("            ),\n");
        sb.append("          ),\n");

        sb.append("          // Barra de Chips de comandos rápidos\n");
        sb.append("          SingleChildScrollView(\n");
        sb.append("            scrollDirection: Axis.horizontal,\n");
        sb.append("            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 4),\n");
        sb.append("            child: Row(\n");
        sb.append("              children: [\n");

        for (String chip : chipsComandos) {
            sb.append("                Padding(\n");
            sb.append("                  padding: const EdgeInsets.only(right: 8),\n");
            sb.append("                  child: ActionChip(\n");
            sb.append("                    backgroundColor: const Color(0xFF1E2433),\n");
            sb.append("                    side: const BorderSide(color: Color(0xFF334155)),\n");
            sb.append("                    avatar: const Icon(Icons.bolt, color: Color(0xFFC084FC), size: 16),\n");
            sb.append("                    label: Text('").append(chip).append("', style: const TextStyle(color: Colors.white, fontSize: 12)),\n");
            sb.append("                    onPressed: () {\n");
            sb.append("                      _textController.text = '").append(chip).append("';\n");
            sb.append("                      _executeCommand('").append(chip).append("');\n");
            sb.append("                    },\n");
            sb.append("                  ),\n");
            sb.append("                ),\n");
        }

        sb.append("              ],\n");
        sb.append("            ),\n");
        sb.append("          ),\n");

        sb.append("          // Barra inferior de entrada (Micrófono + Input de texto)\n");
        sb.append("          Container(\n");
        sb.append("            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),\n");
        sb.append("            decoration: const BoxDecoration(\n");
        sb.append("              color: Color(0xFF0B0F19),\n");
        sb.append("              border: Border(top: BorderSide(color: Color(0xFF1E293B))),\n");
        sb.append("            ),\n");
        sb.append("            child: SafeArea(\n");
        sb.append("              child: Row(\n");
        sb.append("                children: [\n");
        sb.append("                  GestureDetector(\n");
        sb.append("                    onTap: _toggleListening,\n");
        sb.append("                    child: AnimatedContainer(\n");
        sb.append("                      duration: const Duration(milliseconds: 250),\n");
        sb.append("                      width: 48,\n");
        sb.append("                      height: 48,\n");
        sb.append("                      decoration: BoxDecoration(\n");
        sb.append("                        color: _isListening ? Colors.redAccent : const Color(0xFFE9D5FF),\n");
        sb.append("                        borderRadius: BorderRadius.circular(16),\n");
        sb.append("                        boxShadow: [\n");
        sb.append("                          if (_isListening)\n");
        sb.append("                            BoxShadow(color: Colors.red.withOpacity(0.5), blurRadius: 12, spreadRadius: 2)\n");
        sb.append("                          else\n");
        sb.append("                            BoxShadow(color: const Color(0xFFA855F7).withOpacity(0.3), blurRadius: 8),\n");
        sb.append("                        ],\n");
        sb.append("                      ),\n");
        sb.append("                      child: Icon(\n");
        sb.append("                        _isListening ? Icons.mic : Icons.mic_none_rounded,\n");
        sb.append("                        color: _isListening ? Colors.white : const Color(0xFF6B21A8),\n");
        sb.append("                        size: 26,\n");
        sb.append("                      ),\n");
        sb.append("                    ),\n");
        sb.append("                  ),\n");
        sb.append("                  const SizedBox(width: 10),\n");
        sb.append("                  Expanded(\n");
        sb.append("                    child: Container(\n");
        sb.append("                      height: 48,\n");
        sb.append("                      decoration: BoxDecoration(\n");
        sb.append("                        color: const Color(0xFF1E2433),\n");
        sb.append("                        borderRadius: BorderRadius.circular(24),\n");
        sb.append("                        border: Border.all(color: const Color(0xFF334155)),\n");
        sb.append("                      ),\n");
        sb.append("                      padding: const EdgeInsets.symmetric(horizontal: 16),\n");
        sb.append("                      child: TextField(\n");
        sb.append("                        controller: _textController,\n");
        sb.append("                        style: const TextStyle(color: Colors.white, fontSize: 14),\n");
        sb.append("                        decoration: const InputDecoration(\n");
        sb.append("                          hintText: 'O escribe tu comando...',\n");
        sb.append("                          hintStyle: TextStyle(color: Color(0xFF64748B), fontSize: 13),\n");
        sb.append("                          border: InputBorder.none,\n");
        sb.append("                        ),\n");
        sb.append("                        onSubmitted: _executeCommand,\n");
        sb.append("                      ),\n");
        sb.append("                    ),\n");
        sb.append("                  ),\n");
        sb.append("                  const SizedBox(width: 8),\n");
        sb.append("                  IconButton(\n");
        sb.append("                    icon: const Icon(Icons.send_rounded, color: Color(0xFFC084FC)),\n");
        sb.append("                    onPressed: () => _executeCommand(_textController.text),\n");
        sb.append("                  ),\n");
        sb.append("                ],\n");
        sb.append("              ),\n");
        sb.append("            ),\n");
        sb.append("          ),\n");
        sb.append("        ],\n");
        sb.append("      ),\n");
        sb.append("    );\n");
        sb.append("  }\n\n");

        sb.append("  Widget _buildSuggestionCard({\n");
        sb.append("    required IconData icon,\n");
        sb.append("    required String title,\n");
        sb.append("    required List<String> examples,\n");
        sb.append("    required ValueChanged<String> onTapExample,\n");
        sb.append("  }) {\n");
        sb.append("    return Container(\n");
        sb.append("      width: double.infinity,\n");
        sb.append("      padding: const EdgeInsets.all(16),\n");
        sb.append("      decoration: BoxDecoration(\n");
        sb.append("        color: const Color(0xFF1E2433),\n");
        sb.append("        borderRadius: BorderRadius.circular(16),\n");
        sb.append("        border: Border.all(color: const Color(0xFF334155).withOpacity(0.6)),\n");
        sb.append("      ),\n");
        sb.append("      child: Column(\n");
        sb.append("        crossAxisAlignment: CrossAxisAlignment.start,\n");
        sb.append("        children: [\n");
        sb.append("          Row(\n");
        sb.append("            children: [\n");
        sb.append("              Icon(icon, color: const Color(0xFF38BDF8), size: 18),\n");
        sb.append("              const SizedBox(width: 8),\n");
        sb.append("              Text(\n");
        sb.append("                title,\n");
        sb.append("                style: const TextStyle(color: Colors.white, fontSize: 14, fontWeight: FontWeight.bold),\n");
        sb.append("              ),\n");
        sb.append("            ],\n");
        sb.append("          ),\n");
        sb.append("          const SizedBox(height: 10),\n");
        sb.append("          ...examples.map((ex) => Padding(\n");
        sb.append("            padding: const EdgeInsets.symmetric(vertical: 4),\n");
        sb.append("            child: InkWell(\n");
        sb.append("              onTap: () => onTapExample(ex),\n");
        sb.append("              child: Text(\n");
        sb.append("                '•  $ex',\n");
        sb.append("                style: const TextStyle(color: Color(0xFFCBD5E1), fontSize: 13, height: 1.3),\n");
        sb.append("              ),\n");
        sb.append("            ),\n");
        sb.append("          )),\n");
        sb.append("        ],\n");
        sb.append("      ),\n");
        sb.append("    );\n");
        sb.append("  }\n\n");

        sb.append("  Widget _buildResponseCard(LocalAiResponse response) {\n");
        sb.append("    return Container(\n");
        sb.append("      width: double.infinity,\n");
        sb.append("      margin: const EdgeInsets.only(top: 14),\n");
        sb.append("      padding: const EdgeInsets.all(16),\n");
        sb.append("      decoration: BoxDecoration(\n");
        sb.append("        color: response.exitoso ? const Color(0xFF064E3B).withOpacity(0.3) : const Color(0xFF7F1D1D).withOpacity(0.3),\n");
        sb.append("        borderRadius: BorderRadius.circular(16),\n");
        sb.append("        border: Border.all(\n");
        sb.append("          color: response.exitoso ? const Color(0xFF10B981) : const Color(0xFFEF4444),\n");
        sb.append("          width: 1.2,\n");
        sb.append("        ),\n");
        sb.append("      ),\n");
        sb.append("      child: Column(\n");
        sb.append("        crossAxisAlignment: CrossAxisAlignment.start,\n");
        sb.append("        children: [\n");
        sb.append("          Row(\n");
        sb.append("            children: [\n");
        sb.append("              Icon(\n");
        sb.append("                response.exitoso ? Icons.check_circle_outline : Icons.error_outline,\n");
        sb.append("                color: response.exitoso ? const Color(0xFF34D399) : const Color(0xFFF87171),\n");
        sb.append("                size: 20,\n");
        sb.append("              ),\n");
        sb.append("              const SizedBox(width: 8),\n");
        sb.append("              Text(\n");
        sb.append("                response.exitoso ? 'Comando ejecutado' : 'Aviso',\n");
        sb.append("                style: TextStyle(\n");
        sb.append("                  color: response.exitoso ? const Color(0xFF34D399) : const Color(0xFFF87171),\n");
        sb.append("                  fontWeight: FontWeight.bold,\n");
        sb.append("                ),\n");
        sb.append("              ),\n");
        sb.append("              const Spacer(),\n");
        sb.append("              Container(\n");
        sb.append("                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),\n");
        sb.append("                decoration: BoxDecoration(\n");
        sb.append("                  color: response.origen.contains('Ollama') ? const Color(0xFF6366F1).withOpacity(0.25) : const Color(0xFF0284C7).withOpacity(0.25),\n");
        sb.append("                  borderRadius: BorderRadius.circular(6),\n");
        sb.append("                  border: Border.all(color: response.origen.contains('Ollama') ? const Color(0xFF818CF8).withOpacity(0.5) : const Color(0xFF38BDF8).withOpacity(0.5), width: 0.8),\n");
        sb.append("                ),\n");
        sb.append("                child: Text(\n");
        sb.append("                  response.origen,\n");
        sb.append("                  style: TextStyle(\n");
        sb.append("                    color: response.origen.contains('Ollama') ? const Color(0xFFA5B4FC) : const Color(0xFF7DD3FC),\n");
        sb.append("                    fontSize: 10,\n");
        sb.append("                    fontWeight: FontWeight.w600,\n");
        sb.append("                  ),\n");
        sb.append("                ),\n");
        sb.append("              ),\n");
        sb.append("            ],\n");
        sb.append("          ),\n");
        sb.append("          const SizedBox(height: 8),\n");
        sb.append("          Text(\n");
        sb.append("            response.mensajeVoz,\n");
        sb.append("            style: const TextStyle(color: Colors.white, fontSize: 14, height: 1.3),\n");
        sb.append("          ),\n");
        sb.append("          if (response.datos != null) ...[\n");
        sb.append("            const Divider(color: Color(0xFF334155), height: 20),\n");
        sb.append("            Text(\n");
        sb.append("              'Datos devueltos por el backend:',\n");
        sb.append("              style: const TextStyle(color: Color(0xFF94A3B8), fontSize: 11, fontWeight: FontWeight.bold),\n");
        sb.append("            ),\n");
        sb.append("            const SizedBox(height: 6),\n");
        sb.append("            Container(\n");
        sb.append("              padding: const EdgeInsets.all(10),\n");
        sb.append("              decoration: BoxDecoration(\n");
        sb.append("                color: const Color(0xFF0F172A),\n");
        sb.append("                borderRadius: BorderRadius.circular(8),\n");
        sb.append("              ),\n");
        sb.append("              child: Text(\n");
        sb.append("                '${response.datos}',\n");
        sb.append("                style: const TextStyle(color: Color(0xFFCBD5E1), fontSize: 12, fontFamily: 'monospace'),\n");
        sb.append("              ),\n");
        sb.append("            ),\n");
        sb.append("          ],\n");
        sb.append("        ],\n");
        sb.append("      ),\n");
        sb.append("    );\n");
        sb.append("  }\n");
        sb.append("}\n");

        return sb.toString();
    }
}
