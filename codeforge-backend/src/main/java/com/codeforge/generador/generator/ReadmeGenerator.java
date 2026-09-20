package com.codeforge.generador.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

@Component
public class ReadmeGenerator {

    public Path generar(
            Path proyecto
    ) {

        try {

            Path archivo =
                    proyecto.resolve(
                            "README.md"
                    );

            String contenido = """
                    # CodeForge Generated Backend

                    Backend generado automáticamente por **CodeForge** a partir de un modelo UML.

                    ## Tecnologías

                    - Java 17
                    - Spring Boot 4.1.1
                    - Spring Data JPA
                    - Hibernate
                    - PostgreSQL
                    - Maven

                    ## Estructura del proyecto

                    ```text
                    src/
                    └── main/
                        ├── java/
                        │   └── com/codeforge/generated/
                        │       ├── controller/
                        │       ├── dto/
                        │       ├── entity/
                        │       ├── repository/
                        │       └── service/
                        └── resources/
                            ├── application.yml
                            └── database/
                                └── schema.sql
                    ```

                    ## Configuración de PostgreSQL

                    Cree una base de datos llamada:

                    ```text
                    codeforge_generated
                    ```

                    Configure las variables de entorno:

                    ```text
                    DB_URL=jdbc:postgresql://localhost:5432/codeforge_generated
                    DB_USERNAME=postgres
                    DB_PASSWORD=su_contraseña
                    ```

                    También se incluye el archivo:

                    ```text
                    .env.example
                    ```

                    como referencia para la configuración.

                    ## Ejecución

                    En Windows:

                    ```powershell
                    .\\mvnw.cmd spring-boot:run
                    ```

                    En Linux o macOS:

                    ```bash
                    ./mvnw spring-boot:run
                    ```

                    El backend se ejecuta por defecto en:

                    ```text
                    http://localhost:8081
                    ```

                    ## Compilación

                    Para compilar:

                    ```powershell
                    .\\mvnw.cmd clean package
                    ```

                    El archivo generado estará en:

                    ```text
                    target/codeforge-generated-backend-0.0.1-SNAPSHOT.jar
                    ```

                    ## Base de datos

                    El proyecto contiene:

                    ```text
                    src/main/resources/database/schema.sql
                    ```

                    Este archivo contiene el esquema SQL generado a partir del modelo UML.

                    Las entidades JPA también utilizan:

                    ```yaml
                    spring:
                      jpa:
                        hibernate:
                          ddl-auto: update
                    ```

                    por lo que Hibernate puede actualizar el esquema de la base de datos según las entidades generadas.

                    ## API REST

                    Cada clase UML de tipo `CLASE` genera automáticamente:

                    - Entity
                    - Repository
                    - Service
                    - DTO
                    - Controller

                    Los endpoints CRUD se generan siguiendo esta estructura:

                    ```text
                    POST   /api/{recurso}
                    GET    /api/{recurso}
                    GET    /api/{recurso}/{id}
                    PUT    /api/{recurso}/{id}
                    DELETE /api/{recurso}/{id}
                    ```

                    ## Relaciones UML

                    Las asociaciones UML compatibles se transforman en relaciones JPA y claves foráneas SQL cuando corresponde.

                    Ejemplo:

                    ```text
                    Factura 1..* -------- 1 Cliente
                    ```

                    puede generar una relación JPA equivalente y una clave foránea en la tabla correspondiente.

                    ## Maven Wrapper

                    El proyecto incluye Maven Wrapper:

                    ```text
                    mvnw
                    mvnw.cmd
                    .mvn/wrapper/maven-wrapper.properties
                    ```

                    Por lo tanto no es necesario instalar Maven manualmente para ejecutar el proyecto.

                    ## Generado por CodeForge

                    Este proyecto fue generado automáticamente por la plataforma CodeForge a partir de un modelo UML.
                    """;

            Files.writeString(
                    archivo,
                    contenido,
                    StandardCharsets.UTF_8
            );

            System.out.println(
                    "GENERADOR: README.md creado: "
                            + archivo
            );

            return archivo;

        } catch (
                IOException e
        ) {

            throw new RuntimeException(
                    "No se pudo generar README.md.",
                    e
            );
        }
    }
}