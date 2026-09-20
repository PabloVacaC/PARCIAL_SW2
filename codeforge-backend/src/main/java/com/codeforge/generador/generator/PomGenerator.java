package com.codeforge.generador.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

@Component
public class PomGenerator {

    public Path generar(
            Path proyecto
    ) {

        Path pom =
                proyecto.resolve(
                        "pom.xml"
                );

        String contenido = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="
                           http://maven.apache.org/POM/4.0.0
                           https://maven.apache.org/xsd/maven-4.0.0.xsd">

                    <modelVersion>4.0.0</modelVersion>

                    <parent>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-parent</artifactId>
                        <version>4.1.1</version>
                        <relativePath/>
                    </parent>

                    <groupId>com.codeforge.generated</groupId>
                    <artifactId>codeforge-generated-backend</artifactId>
                    <version>0.0.1-SNAPSHOT</version>

                    <name>codeforge-generated-backend</name>
                    <description>Backend generado automáticamente por CodeForge</description>

                    <properties>
                        <java.version>17</java.version>
                    </properties>

                    <dependencies>

                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-web</artifactId>
                        </dependency>

                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-data-jpa</artifactId>
                        </dependency>

                        <dependency>
                            <groupId>org.postgresql</groupId>
                            <artifactId>postgresql</artifactId>
                        </dependency>

                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-validation</artifactId>
                        </dependency>

                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-test</artifactId>
                            <scope>test</scope>
                        </dependency>

                    </dependencies>

                    <build>
                        <plugins>

                            <plugin>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-maven-plugin</artifactId>
                            </plugin>

                        </plugins>
                    </build>

                </project>
                """;

        try {

            Files.createDirectories(
                    proyecto
            );

            Files.writeString(
                    pom,
                    contenido,
                    StandardCharsets.UTF_8
            );

            System.out.println(
                    "GENERADOR: pom.xml creado en " +
                            pom
            );

            return pom;

        } catch (
                IOException e
        ) {

            throw new RuntimeException(
                    "No se pudo generar pom.xml.",
                    e
            );
        }
    }
}