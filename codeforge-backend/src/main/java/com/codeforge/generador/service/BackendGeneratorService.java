package com.codeforge.generador.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.codeforge.generador.generator.ApplicationGenerator;
import com.codeforge.generador.generator.ApplicationYmlGenerator;
import com.codeforge.generador.generator.ControllerGenerator;
import com.codeforge.generador.generator.DtoGenerator;
import com.codeforge.generador.generator.EntityGenerator;
import com.codeforge.generador.generator.EnvExampleGenerator;
import com.codeforge.generador.generator.MavenWrapperGenerator;
import com.codeforge.generador.generator.PomGenerator;
import com.codeforge.generador.generator.PostmanGenerator;
import com.codeforge.generador.generator.ReadmeGenerator;
import com.codeforge.generador.generator.RepositoryGenerator;
import com.codeforge.generador.generator.SchemaSqlGenerator;
import com.codeforge.generador.generator.ServiceGenerator;
import com.codeforge.generador.generator.ZipGenerator;
import com.codeforge.generador.generator.flutter.FlutterAiToolsGenerator;
import com.codeforge.generador.generator.flutter.FlutterApiGenerator;
import com.codeforge.generador.generator.flutter.FlutterExecutorGenerator;
import com.codeforge.generador.generator.flutter.FlutterLocalAiEngineGenerator;
import com.codeforge.generador.generator.flutter.FlutterModelGenerator;
import com.codeforge.generador.generator.flutter.FlutterProjectGenerator;
import com.codeforge.generador.generator.flutter.FlutterScreenGenerator;
import com.codeforge.modelo.entity.Modelo;
import com.codeforge.modelo.repository.ModeloRepository;

@Service
public class BackendGeneratorService {

    private final ModeloRepository modeloRepository;

    private final PomGenerator pomGenerator;

    private final ApplicationGenerator applicationGenerator;

    private final ApplicationYmlGenerator applicationYmlGenerator;

    private final EnvExampleGenerator envExampleGenerator;

    private final ReadmeGenerator readmeGenerator;

    private final PostmanGenerator postmanGenerator;

    private final EntityGenerator entityGenerator;

    private final RepositoryGenerator repositoryGenerator;

    private final ServiceGenerator serviceGenerator;

    private final ControllerGenerator controllerGenerator;

    private final DtoGenerator dtoGenerator;

    private final MavenWrapperGenerator mavenWrapperGenerator;

    private final SchemaSqlGenerator schemaSqlGenerator;

    private final ZipGenerator zipGenerator;

    /*
     * ============================================================
     * GENERADORES FLUTTER
     * ============================================================
     */

    private final FlutterProjectGenerator flutterProjectGenerator;

    private final FlutterModelGenerator flutterModelGenerator;

    private final FlutterApiGenerator flutterApiGenerator;

    private final FlutterAiToolsGenerator flutterAiToolsGenerator;

    private final FlutterExecutorGenerator flutterExecutorGenerator;

    private final FlutterLocalAiEngineGenerator flutterLocalAiEngineGenerator;

    private final FlutterScreenGenerator flutterScreenGenerator;

    public BackendGeneratorService(
            ModeloRepository modeloRepository,
            PomGenerator pomGenerator,
            ApplicationGenerator applicationGenerator,
            ApplicationYmlGenerator applicationYmlGenerator,
            EnvExampleGenerator envExampleGenerator,
            ReadmeGenerator readmeGenerator,
            PostmanGenerator postmanGenerator,
            EntityGenerator entityGenerator,
            RepositoryGenerator repositoryGenerator,
            ServiceGenerator serviceGenerator,
            ControllerGenerator controllerGenerator,
            DtoGenerator dtoGenerator,
            MavenWrapperGenerator mavenWrapperGenerator,
            SchemaSqlGenerator schemaSqlGenerator,
            ZipGenerator zipGenerator,
            FlutterProjectGenerator flutterProjectGenerator,
            FlutterModelGenerator flutterModelGenerator,
            FlutterApiGenerator flutterApiGenerator,
            FlutterAiToolsGenerator flutterAiToolsGenerator,
            FlutterExecutorGenerator flutterExecutorGenerator,
            FlutterLocalAiEngineGenerator flutterLocalAiEngineGenerator,
            FlutterScreenGenerator flutterScreenGenerator
    ) {

        this.modeloRepository =
                modeloRepository;

        this.pomGenerator =
                pomGenerator;

        this.applicationGenerator =
                applicationGenerator;

        this.applicationYmlGenerator =
                applicationYmlGenerator;

        this.envExampleGenerator =
                envExampleGenerator;

        this.readmeGenerator =
                readmeGenerator;

        this.postmanGenerator =
                postmanGenerator;

        this.entityGenerator =
                entityGenerator;

        this.repositoryGenerator =
                repositoryGenerator;

        this.serviceGenerator =
                serviceGenerator;

        this.controllerGenerator =
                controllerGenerator;

        this.dtoGenerator =
                dtoGenerator;

        this.mavenWrapperGenerator =
                mavenWrapperGenerator;

        this.schemaSqlGenerator =
                schemaSqlGenerator;

        this.zipGenerator =
                zipGenerator;

        /*
         * ========================================================
         * INYECCIÓN DE GENERADORES FLUTTER
         * ========================================================
         */

        this.flutterProjectGenerator =
                flutterProjectGenerator;

        this.flutterModelGenerator =
                flutterModelGenerator;

        this.flutterApiGenerator =
                flutterApiGenerator;

        this.flutterAiToolsGenerator =
                flutterAiToolsGenerator;

        this.flutterExecutorGenerator =
                flutterExecutorGenerator;

        this.flutterLocalAiEngineGenerator =
                flutterLocalAiEngineGenerator;

        this.flutterScreenGenerator =
                flutterScreenGenerator;
    }

    public Path generarBackend(
            Long idModelo
    ) {

        Modelo modelo =
                modeloRepository
                        .findById(idModelo)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "El modelo no existe."
                                )
                        );

        Map<String, Object> contenido =
                modelo.getContenidoJson();

        if (
                contenido == null ||
                contenido.isEmpty()
        ) {

            throw new RuntimeException(
                    "El modelo no contiene información UML."
            );
        }

        try {

            Path carpetaTemporal =
                    Files.createTempDirectory(
                            "codeforge-backend-"
                    );

            System.out.println(
                    "GENERADOR: modelo " +
                            idModelo +
                            " cargado correctamente."
            );

            System.out.println(
                    "GENERADOR: carpeta temporal = " +
                            carpetaTemporal
            );

            /*
             * ============================================================
             * POM
             * ============================================================
             */

            pomGenerator.generar(
                    carpetaTemporal
            );

            /*
             * ============================================================
             * APLICACIÓN SPRING BOOT
             * ============================================================
             */

            applicationGenerator.generar(
                    carpetaTemporal,
                    "CodeForgeGenerated"
            );

            /*
             * ============================================================
             * APPLICATION.YML
             * ============================================================
             */

            applicationYmlGenerator.generar(
                    carpetaTemporal
            );

            /*
             * ============================================================
             * .ENV.EXAMPLE
             * ============================================================
             */

            envExampleGenerator.generar(
                    carpetaTemporal
            );

            /*
             * ============================================================
             * README
             * ============================================================
             */

            readmeGenerator.generar(
                    carpetaTemporal
            );

            /*
             * ============================================================
             * POSTMAN
             * ============================================================
             */

            postmanGenerator.generar(
                    carpetaTemporal,
                    contenido
            );

            /*
             * ============================================================
             * ENTIDADES JPA
             * ============================================================
             */

            entityGenerator.generar(
                    carpetaTemporal,
                    contenido
            );

            /*
             * ============================================================
             * REPOSITORIOS JPA
             * ============================================================
             */

            repositoryGenerator.generar(
                    carpetaTemporal,
                    contenido
            );

            /*
             * ============================================================
             * SERVICIOS
             * ============================================================
             */

            serviceGenerator.generar(
                    carpetaTemporal,
                    contenido
            );

            /*
             * ============================================================
             * CONTROLADORES REST
             * ============================================================
             */

            controllerGenerator.generar(
                    carpetaTemporal,
                    contenido
            );

            /*
             * ============================================================
             * DTOs
             * ============================================================
             */

            dtoGenerator.generar(
                    carpetaTemporal,
                    contenido
            );

            /*
             * ============================================================
             * SCHEMA SQL
             * ============================================================
             */

            schemaSqlGenerator.generar(
                    carpetaTemporal,
                    contenido
            );

            /*
             * ============================================================
             * MAVEN WRAPPER
             * ============================================================
             */

            Path proyectoOrigen = Path.of("").toAbsolutePath();
            if (!Files.exists(proyectoOrigen.resolve(".mvn/wrapper/maven-wrapper.properties"))) {
                Path altPath = Path.of("C:\\Users\\Juan Pablo\\Desktop\\PARCIAL_SW1\\codeforge-backend");
                if (Files.exists(altPath.resolve(".mvn/wrapper/maven-wrapper.properties"))) {
                    proyectoOrigen = altPath;
                }
            }

            if (Files.exists(proyectoOrigen.resolve(".mvn/wrapper/maven-wrapper.properties"))) {
                mavenWrapperGenerator.generar(
                        carpetaTemporal,
                        proyectoOrigen
                );
            }

            /*
             * ============================================================
             * FLUTTER
             * ============================================================
             *
             * La carpeta Flutter se genera DENTRO de la misma carpeta
             * temporal que después será comprimida en el ZIP.
             *
             * No modificamos el backend Spring existente.
             * Simplemente agregamos una carpeta:
             *
             * flutter_client/
             *
             * ============================================================
             */

            System.out.println(
                    "GENERADOR: iniciando generación Flutter..."
            );

            /*
             * Crear proyecto Flutter
             */
            Path flutterRoot =
                    flutterProjectGenerator.generar(
                            carpetaTemporal
                    );

            /*
             * Obtener elementos UML
             */
            List<Map<String, Object>> elementos =
                    obtenerElementosUML(
                            contenido
                    );

            System.out.println(
                    "GENERADOR: elementos UML para Flutter = " +
                            elementos.size()
            );

            /*
             * Modelos Dart
             */
            flutterModelGenerator.generar(
                    flutterRoot,
                    elementos
            );

            /*
             * APIs REST Flutter
             */
            flutterApiGenerator.generar(
                    flutterRoot,
                    elementos
            );

            /*
             * Herramientas de IA
             */
            flutterAiToolsGenerator.generar(
                    flutterRoot,
                    elementos
            );

            /*
             * Ejecutor de herramientas IA
             */
            flutterExecutorGenerator.generar(
                    flutterRoot,
                    elementos
            );

            /*
             * Motor de IA Semántica Local Offline
             */
            flutterLocalAiEngineGenerator.generar(
                    flutterRoot,
                    elementos
            );

            /*
             * Pantalla Móvil Asistente de Voz IA
             */
            flutterScreenGenerator.generar(
                    flutterRoot,
                    elementos
            );

            System.out.println(
                    "GENERADOR: Flutter generado correctamente (Models + APIs + AI Engine + Voice Screen)."
            );

            /*
             * ============================================================
             * ZIP
             * ============================================================
             */

            Path archivoZip =
                    zipGenerator.generar(
                            carpetaTemporal
                    );

            System.out.println(
                    "GENERADOR: proyecto completo generado: " +
                            "POM + Application + application.yml + " +
                            ".env.example + README + Postman + " +
                            "entidades + repositorios + servicios + " +
                            "controladores + DTOs + schema.sql + " +
                            "Maven Wrapper + Flutter."
            );

            System.out.println(
                    "GENERADOR: ZIP generado = " +
                            archivoZip
            );

            /*
             * Conservamos la respuesta actual:
             * el método sigue devolviendo la carpeta.
             *
             * El Controller utilizará después el ZIP.
             */

            return carpetaTemporal;

        } catch (
                IOException e
        ) {

            throw new RuntimeException(
                    "No se pudo crear la carpeta temporal " +
                            "del backend generado.",
                    e
            );
        }
    }

    /*
     * ============================================================
     * OBTENER ELEMENTOS UML
     * ============================================================
     *
     * El JSON del modelo contiene:
     *
     * {
     *   "elementos": [...],
     *   "relaciones": [...]
     * }
     *
     * Los generadores Flutter solamente necesitan
     * la lista de elementos.
     * ============================================================
     */

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> obtenerElementosUML(
            Map<String, Object> contenido
    ) {

        Object elementos =
                contenido.get("elementos");

        if (!(elementos instanceof List<?>)) {
            return List.of();
        }

        return (List<Map<String, Object>>) elementos;
    }

    public List<Map<String, String>> listarArchivosGenerados(Long idModelo) {
        Path carpeta = generarBackend(idModelo);
        List<Map<String, String>> archivos = new java.util.ArrayList<>();
        try (var stream = Files.walk(carpeta)) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                String relativePath = carpeta.relativize(path).toString().replace("\\", "/");
                if (!relativePath.endsWith(".zip") && !relativePath.contains(".mvn") && !relativePath.contains("mvnw")) {
                    try {
                        String contenido = Files.readString(path);
                        String nombre = path.getFileName().toString();
                        String tipo = "TEXT";
                        if (nombre.endsWith(".java")) tipo = "JAVA";
                        else if (nombre.endsWith(".dart")) tipo = "DART";
                        else if (nombre.endsWith(".xml")) tipo = "XML";
                        else if (nombre.endsWith(".sql")) tipo = "SQL";
                        else if (nombre.endsWith(".yml") || nombre.endsWith(".yaml")) tipo = "YAML";
                        else if (nombre.endsWith(".json")) tipo = "JSON";
                        else if (nombre.endsWith(".md")) tipo = "MARKDOWN";

                        java.util.Map<String, String> item = new java.util.LinkedHashMap<>();
                        item.put("ruta", relativePath);
                        item.put("nombre", nombre);
                        item.put("tipo", tipo);
                        item.put("contenido", contenido);
                        archivos.add(item);
                    } catch (Exception ignored) {}
                }
            });
        } catch (Exception e) {
            System.err.println("Error al listar archivos generados: " + e.getMessage());
        }
        return archivos;
    }
}