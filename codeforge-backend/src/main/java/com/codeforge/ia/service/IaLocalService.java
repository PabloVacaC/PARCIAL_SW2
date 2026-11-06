package com.codeforge.ia.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servicio de Inteligencia Artificial Local (100% Offline)
 * Se conecta a motores LLM locales como Ollama (http://localhost:11434).
 * Diseñado especialmente para bajo consumo de memoria (ideal para PCs con 8 GB de RAM).
 */
@Service
public class IaLocalService {

    private static final Logger log = LoggerFactory.getLogger(IaLocalService.class);

    @Value("${codeforge.ai.local.url:${codeforge.ia.local.url:http://localhost:11434}}")
    private String localUrl;

    @Value("${codeforge.ai.local.modelo:${codeforge.ia.local.modelo:qwen2.5-coder:1.5b}}")
    private String modeloLocal;

    @Value("${codeforge.ai.local.timeout-segundos:${codeforge.ia.local.timeout-segundos:90}}")
    private int timeoutSegundos;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public IaLocalService() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public IaLocalService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Verifica de manera rápida y sin bloqueo si el servidor de IA local está activo y respondiendo.
     */
    public boolean estaDisponible() {
        try {
            String baseUrl = normalizarUrl(localUrl);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/tags"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            log.debug("IA Local no disponible en {}: {}", localUrl, e.getMessage());
            return false;
        }
    }

    /**
     * Envía una consulta técnica a la IA local y retorna la respuesta generada.
     */
    public String generarRespuesta(
            String modo,
            String systemPrompt,
            String contexto,
            String usuarioNombre,
            String mensajeUsuario
    ) throws IOException, InterruptedException {

        String baseUrl = normalizarUrl(localUrl);
        String endpoint = baseUrl + "/api/chat";

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", modeloLocal);
        requestBody.put("stream", false);

        // Opciones de inferencia controlada
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("temperature", 0.2);
        options.put("num_predict", 2048);
        requestBody.put("options", options);

        List<Map<String, String>> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(Map.of("role", "system", "content", systemPrompt));
        }

        String userContent = "Contexto actual del proyecto:\n" + (contexto != null ? contexto : "")
                + "\n\nSolicitud del desarrollador (" + (usuarioNombre != null ? usuarioNombre : "Desarrollador") + "):\n"
                + mensajeUsuario;

        messages.add(Map.of("role", "user", "content", userContent));
        requestBody.put("messages", messages);

        String jsonPayload = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(timeoutSegundos))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        log.info("CODEFORGE IA LOCAL: Enviando solicitud a Ollama ({}, modelo={})", endpoint, modeloLocal);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Error HTTP " + response.statusCode() + " del motor local: " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String respuesta = root.path("message").path("content").asText();

        if (respuesta == null || respuesta.isBlank()) {
            throw new IOException("El motor local devolvió una respuesta vacía.");
        }

        return respuesta;
    }

    private String normalizarUrl(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:11434";
        }
        String u = url.trim();
        if (u.endsWith("/")) {
            return u.substring(0, u.length() - 1);
        }
        return u;
    }

    public String getModeloLocal() {
        return modeloLocal;
    }

    public String getLocalUrl() {
        return localUrl;
    }
}
