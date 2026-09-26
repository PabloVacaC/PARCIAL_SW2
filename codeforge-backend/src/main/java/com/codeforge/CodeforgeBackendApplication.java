package com.codeforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@SpringBootApplication
public class CodeforgeBackendApplication {

	public static void main(String[] args) {
		cargarDotEnvSiExiste();
		SpringApplication.run(CodeforgeBackendApplication.class, args);
	}

	private static void cargarDotEnvSiExiste() {
		try {
			File envFile = new File(".env");
			if (!envFile.exists()) {
				envFile = new File("codeforge-backend/.env");
			}
			if (envFile.exists()) {
				List<String> lines = Files.readAllLines(envFile.toPath());
				for (String line : lines) {
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
						continue;
					}
					int idx = line.indexOf('=');
					String key = line.substring(0, idx).trim();
					String value = line.substring(idx + 1).trim();
					if (System.getProperty(key) == null && System.getenv(key) == null) {
						System.setProperty(key, value);
					}
				}
			}
		} catch (Exception ignored) {
		}
	}

}
