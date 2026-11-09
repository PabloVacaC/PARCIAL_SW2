package com.codeforge.generador.generator.flutter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

@Component
public class FlutterProjectGenerator {

    public Path generar(
            Path proyecto
    ) {

        Path flutterRoot =
                proyecto.resolve("flutter_client");

        try {

            /* =========================================================
             * CARPETAS PRINCIPALES
             * ========================================================= */

            Path lib =
                    flutterRoot.resolve("lib");

            Path generated =
                    lib.resolve("generated");

            Path config =
                    generated.resolve("config");

            Path models =
                    generated.resolve("models");

            Path api =
                    generated.resolve("api");

            Path ai =
                    generated.resolve("ai");

            Files.createDirectories(config);
            Files.createDirectories(models);
            Files.createDirectories(api);
            Files.createDirectories(ai);


            /* =========================================================
             * PUBSPEC.YAML
             * ========================================================= */

            Path pubspec =
                    flutterRoot.resolve("pubspec.yaml");

            String pubspecContenido = """
                    name: codeforge_generated_app
                    description: Aplicación Flutter generada por CodeForge con Asistente de Voz IA Local
                    publish_to: 'none'

                    version: 1.0.0+1

                    environment:
                      sdk: '>=3.0.0 <4.0.0'

                    dependencies:
                      flutter:
                        sdk: flutter

                      cupertino_icons: ^1.0.8
                      http: ^1.2.2
                      speech_to_text: ^6.6.1
                      flutter_tts: ^3.8.5

                    dev_dependencies:
                      flutter_test:
                        sdk: flutter

                      flutter_lints: ^4.0.0

                    flutter:
                      uses-material-design: true
                    """;

            Files.writeString(
                    pubspec,
                    pubspecContenido,
                    StandardCharsets.UTF_8
            );


            /* =========================================================
             * MAIN.DART
             * ========================================================= */

            Path main =
                    lib.resolve("main.dart");

            String mainContenido = """
                    import 'package:flutter/material.dart';
                    import 'screens/voice_assistant_screen.dart';

                    void main() {
                      WidgetsFlutterBinding.ensureInitialized();
                      runApp(
                        const CodeForgeGeneratedApp(),
                      );
                    }

                    class CodeForgeGeneratedApp extends StatelessWidget {
                      const CodeForgeGeneratedApp({
                        super.key,
                      });

                      @override
                      Widget build(
                        BuildContext context
                      ) {

                        return MaterialApp(
                          debugShowCheckedModeBanner: false,
                          title: 'CodeForge Asistente IA',

                          theme: ThemeData(
                            brightness: Brightness.dark,
                            scaffoldBackgroundColor: const Color(0xFF0B0F19),
                            colorScheme: ColorScheme.fromSeed(
                              seedColor: const Color(0xFFA855F7),
                              brightness: Brightness.dark,
                            ),
                            useMaterial3: true,
                          ),

                          home: const VoiceAssistantScreen(),
                        );
                      }
                    }
                    """;

            Files.writeString(
                    main,
                    mainContenido,
                    StandardCharsets.UTF_8
            );


            /* =========================================================
             * API CONFIG
             * ========================================================= */

            Path apiConfig =
                    config.resolve("api_config.dart");

            String apiConfigContenido = """
                    /// Configuración del backend Spring Boot y del motor Ollama para Flutter.
                    ///
                    /// Backend Spring Boot:
                    /// - Chrome / Windows / Mac Desktop / iOS Sim: 'http://localhost:8081'
                    /// - Android Emulator: 'http://10.0.2.2:8081'
                    /// - Celular Físico en la misma red Wi-Fi: 'http://TU_IP_LOCAL:8081'
                    ///
                    /// Motor de IA Local (Ollama):
                    /// - Desktop / Web / iOS Sim: 'http://localhost:11434'
                    /// - Android Emulator: 'http://10.0.2.2:11434'
                    /// - Celular Físico: 'http://TU_IP_LOCAL:11434'
                    class ApiConfig {

                      static String baseUrl = const String.fromEnvironment(
                        'CODEFORGE_API_URL',
                        defaultValue: 'http://localhost:8081',
                      );

                      static String ollamaUrl = const String.fromEnvironment(
                        'CODEFORGE_OLLAMA_URL',
                        defaultValue: 'http://localhost:11434',
                      );

                      static String ollamaModel = const String.fromEnvironment(
                        'CODEFORGE_OLLAMA_MODEL',
                        defaultValue: 'qwen2.5-coder:1.5b',
                      );

                      static bool useOllama = true;

                      static int ollamaTimeoutSegundos = 8;

                      static void setBaseUrl(String url) {
                        baseUrl = url;
                      }

                      static void setOllamaUrl(String url) {
                        ollamaUrl = url;
                      }

                      static void setOllamaModel(String model) {
                        ollamaModel = model;
                      }

                      static void setUseOllama(bool value) {
                        useOllama = value;
                      }

                    }
                    """;

            Files.writeString(
                    apiConfig,
                    apiConfigContenido,
                    StandardCharsets.UTF_8
            );


            /* =========================================================
             * README FLUTTER
             * ========================================================= */

            Path readme =
                    flutterRoot.resolve("README.md");

            String readmeContenido = """
                    # Flutter generado por CodeForge con Soporte IA Ollama

                    Este proyecto fue generado automáticamente a partir de un modelo UML de CodeForge.
                    Incluye un asistente de voz y comandos en lenguaje natural impulsado por **Ollama (qwen2.5-coder:1.5b)**
                    con **fallback automático e instantáneo** a un motor heurístico offline en caso de que Ollama no esté activo.

                    ## Ejecutar

                    ```bash
                    flutter pub get
                    flutter run
                    ```

                    ## Configuración de Entorno (Opcional)

                    Puedes pasar los endpoints en tiempo de compilación o configurarlos desde la pantalla del asistente móvil:

                    ```bash
                    flutter run \\
                      --dart-define=CODEFORGE_API_URL=http://localhost:8081 \\
                      --dart-define=CODEFORGE_OLLAMA_URL=http://localhost:11434 \\
                      --dart-define=CODEFORGE_OLLAMA_MODEL=qwen2.5-coder:1.5b
                    ```

                    *Nota para Android Emulator:*
                    - Spring Boot: `http://10.0.2.2:8081`
                    - Ollama Local: `http://10.0.2.2:11434`

                    ## Arquitectura

                    ```text
                    Flutter (Voice Assistant)
                       |
                       +---> Ollama (qwen2.5-coder:1.5b) [Tool Calling IA]
                       |        | (Fallback si está offline)
                       |        v
                       +---> Motor Heurístico Offline (Regex Semántico)
                       |
                       v
                    Spring Boot Backend (REST APIs)
                       |
                       v
                    PostgreSQL / MongoDB
                    ```

                    La carpeta `lib/generated` contiene los modelos, APIs y herramientas IA generadas.
                    """;

            Files.writeString(
                    readme,
                    readmeContenido,
                    StandardCharsets.UTF_8
            );


            System.out.println(
                    "FLUTTER GENERATOR: proyecto base creado = "
                            + flutterRoot
            );

            return flutterRoot;

        } catch (IOException e) {

            throw new RuntimeException(
                    "No se pudo generar el proyecto Flutter.",
                    e
            );
        }
    }
}