package com.codeforge.generador.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class RepositoryGenerator {

    public List<Path> generar(
            Path proyecto,
            Map<String, Object> contenido
    ) {

        List<Path> archivosGenerados = new ArrayList<>();

        Object elementosObjeto = contenido.get("elementos");

        if (!(elementosObjeto instanceof List<?> elementos)) {
            return archivosGenerados;
        }

        Path carpetaRepositories = proyecto.resolve(
                "src/main/java/com/codeforge/generated/repository"
        );

        try {
            Files.createDirectories(carpetaRepositories);

            for (Object elementoObjeto : elementos) {

                if (!(elementoObjeto instanceof Map<?, ?> elemento)) {
                    continue;
                }

                String tipo = texto(elemento.get("tipo"));

                if (!"CLASE".equalsIgnoreCase(tipo)) {
                    continue;
                }

                String nombreClase = convertirNombreClase(
                        texto(elemento.get("nombre"))
                );

                if (nombreClase == null || nombreClase.isBlank()) {
                    continue;
                }

                List<Map<String, Object>> atributos =
                        convertirListaMapas(
                                elemento.get("atributos")
                        );

                String tipoId = obtenerTipoId(atributos);

                Path archivo = generarRepository(
                        carpetaRepositories,
                        nombreClase,
                        tipoId
                );

                archivosGenerados.add(archivo);
            }

            System.out.println(
                    "GENERADOR: repositorios generados = "
                            + archivosGenerados.size()
            );

            return archivosGenerados;

        } catch (IOException e) {
            throw new RuntimeException(
                    "No se pudieron generar los repositorios.",
                    e
            );
        }
    }

    private Path generarRepository(
            Path carpeta,
            String nombreClase,
            String tipoId
    ) throws IOException {

        String nombreRepository =
                nombreClase + "Repository";

        StringBuilder codigo =
                new StringBuilder();

        codigo.append(
                "package com.codeforge.generated.repository;\n\n"
        );

        codigo.append(
                "import org.springframework.data.jpa.repository.JpaRepository;\n"
        );

        codigo.append(
                "import com.codeforge.generated.entity."
        );

        codigo.append(nombreClase);

        codigo.append(";\n\n");

        codigo.append("public interface ");

        codigo.append(nombreRepository);

        codigo.append(" extends JpaRepository<");

        codigo.append(nombreClase);

        codigo.append(", ");

        codigo.append(tipoId);

        codigo.append("> {\n\n");

        codigo.append("}\n");

        Path archivo =
                carpeta.resolve(
                        nombreRepository + ".java"
                );

        Files.writeString(
                archivo,
                codigo.toString(),
                StandardCharsets.UTF_8
        );

        System.out.println(
                "GENERADOR: repositorio creado: " + archivo
        );

        return archivo;
    }

    private String obtenerTipoId(
            List<Map<String, Object>> atributos
    ) {

        if (atributos.isEmpty()) {
            return "Long";
        }

        Map<String, Object> primerAtributo =
                atributos.get(0);

        String tipo =
                texto(primerAtributo.get("tipo"));

        return convertirTipoJava(tipo);
    }

    private String texto(Object valor) {

        if (valor == null) {
            return "";
        }

        return String.valueOf(valor).trim();
    }

    private List<Map<String, Object>> convertirListaMapas(
            Object valor
    ) {

        List<Map<String, Object>> resultado =
                new ArrayList<>();

        if (!(valor instanceof List<?> lista)) {
            return resultado;
        }

        for (Object elemento : lista) {

            if (elemento instanceof Map<?, ?> mapa) {

                @SuppressWarnings("unchecked")
                Map<String, Object> mapaConvertido =
                        (Map<String, Object>) mapa;

                resultado.add(
                        mapaConvertido
                );
            }
        }

        return resultado;
    }

private String convertirNombreClase(
        String nombre
) {

    if (
            nombre == null ||
            nombre.isBlank()
    ) {
        return null;
    }

    String[] partes =
            nombre.trim()
                    .split(
                            "[^a-zA-Z0-9]+"
                    );

    StringBuilder resultado =
            new StringBuilder();

    for (String parte : partes) {

        if (parte.isBlank()) {
            continue;
        }

        resultado.append(
                Character.toUpperCase(
                        parte.charAt(0)
                )
        );

        if (parte.length() > 1) {

            resultado.append(
                    parte.substring(1)
                            .toLowerCase()
            );
        }
    }

    return resultado.toString();
}

    private String convertirTipoJava(
            String tipo
    ) {

        if (
                tipo == null ||
                tipo.isBlank()
        ) {
            return "Long";
        }

        String tipoNormalizado =
                tipo.trim().toLowerCase();

        return switch (tipoNormalizado) {

            case "string" ->
                    "String";

            case "integer", "int" ->
                    "Integer";

            case "long" ->
                    "Long";

            case "double" ->
                    "Double";

            case "float" ->
                    "Float";

            case "boolean", "bool" ->
                    "Boolean";

            case "date", "localdate" ->
                    "LocalDate";

            case "datetime", "localdatetime" ->
                    "LocalDateTime";

            case "time", "localtime" ->
                    "LocalTime";

            default ->
                    convertirNombreClase(tipo);
        };
    }
}