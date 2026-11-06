# Spec 004 — Módulo de Inteligencia Artificial Colaborativa, Multimodal e Híbrida (CodeForge IA)

## Contexto y objetivo
El módulo de Inteligencia Artificial de CodeForge resuelve la necesidad de contar con asistencia inteligente, auditable y colaborativa en tiempo real durante las fases de análisis, modelado UML (estándar 2.5) y arquitectura de software. En lugar de mutar el diagrama de forma opaca y descontrolada, el sistema implementa una arquitectura basada en **propuestas transaccionales estructuradas** con control de concurrencia optimista y versionado semántico del modelo, permitiendo que equipos multidisciplinarios debatan, aprueben o rechacen los cambios asistidos por IA. Además, integra una estrategia **híbrida y resiliente** con Google Gemini (en la nube para razonamiento avanzado, análisis de imágenes y dictado por voz) y Ollama local (`qwen2.5-coder:1.5b` 100% offline y optimizado para equipos con 8 GB de RAM), garantizando continuidad operativa sin interrupciones ante pérdidas de conectividad a internet.

## Usuarios / actores
- **Modelador / Diseñador UML**: Consulta recomendaciones de diseño, solicita transformaciones a partir de diagramas en imágenes (bocetos/pizarras) o notas de voz, e interactúa con el chat colaborativo.
- **Desarrollador de Software**: Solicita soporte técnico específico según roles de ingeniería (Arquitecto, Desarrollador Spring Boot, Revisor de Código, Documentador).
- **Líder Técnico / Aprobador (Propietario o Colaborador con permisos de edición)**: Revisa las propuestas generadas por la IA, evalúa el análisis de impacto y decide su aprobación, rechazo o aplicación sobre la versión activa del modelo UML.
- **Colaborador en Modo Lectura (Web y Móvil)**: Consulta el historial de deliberaciones técnicas, diagnósticos del modelo y estado de propuestas en tiempo real sin privilegios para mutar el canvas.
- **Agente Asistente IA (CodeForge AI)**: Sistema autónomo multirrol capaz de asumir 10 especialidades técnicas y generar operaciones estructuradas de modelo (`CREAR_CLASE`, `AGREGAR_ATRIBUTO`, `AGREGAR_OPERACION`, `CREAR_RELACION`, `MODIFICAR_MULTIPLICIDAD`, `ELIMINAR_CLASE`, `ELIMINAR_RELACION`).

## Historias de usuario
- **H1 (Asistencia Técnica Multirrol en Conversación Compartida)**: Como desarrollador del proyecto, quiero interactuar con la IA seleccionando entre 10 roles técnicos especializados (Modelador UML, Arquitecto de Software, Revisor de Pruebas, Documentador, etc.) para recibir orientación precisa, contextualizada con el estado actual del modelo UML.
- **H2 (Propuestas Transaccionales Auditables de Cambio UML)**: Como equipo de desarrollo, queremos que cuando la IA proponga modificaciones al diagrama genere una propuesta formal aislada con operaciones JSON detalladas, para que el modelo nunca se modifique sin la revisión y aprobación humana.
- **H3 (Control de Concurrencia y Detección de Conflictos de Versión)**: Como líder del proyecto, quiero que el sistema valide que la versión del modelo no haya cambiado antes de aplicar una propuesta aprobada, para evitar sobreescritura accidental o colisiones entre miembros del equipo.
- **H4 (Ingesta Multimodal de Diagramas desde Imágenes)**: Como modelador, quiero subir fotografías o capturas de diagramas de clases (PNG, JPG, WEBP) para que la IA extraiga automáticamente entidades, interfaces, atributos con visibilidad, métodos y relaciones UML con sus coordenadas espaciales.
- **H5 (Ingesta Multimodal de Diagramas desde Audio / Voz)**: Como desarrollador, quiero dictar instrucciones de modelado por micrófono para que la IA interprete las reglas de negocio habladas y las traduzca a nuevos elementos y relaciones en el lienzo.
- **H6 (Resiliencia Híbrida y Operación Offline)**: Como usuario que trabaja con conectividad limitada o nula, quiero que el sistema conmute automáticamente al motor de IA local (Ollama) sin arrojar errores ni interrumpir la sesión de trabajo.
- **H7 (Diagnóstico y Auditoría Estática de Modelos UML)**: Como revisor de calidad de software, quiero solicitar un diagnóstico automático del modelo para identificar clases huérfanas, tipos de retorno no definidos, problemas de visibilidad y violaciones de convenciones UML.
- **H8 (Sincronización Colaborativa en Tiempo Real vía WebSockets)**: Como colaborador conectado a la sala de trabajo, quiero recibir de forma instantánea los mensajes, propuestas y decisiones de la IA generados por otros miembros del equipo.
- **H9 (Trazabilidad y Métricas de Inferencia)**: Como administrador de la plataforma, quiero que cada consulta a la IA registre métricas de tiempo de ejecución, modelo utilizado, tokens consumidos y tasa de éxito para fines de auditoría y control de costos.

## Requisitos funcionales (criterios de aceptación en EARS)

### Conversación y Asistencia Multirrol
- **RF-1**: CUANDO un usuario autenticado envía una consulta al endpoint `POST /api/ia/colaborativa/proyecto/{idProyecto}/mensaje`, EL SISTEMA registra el mensaje del usuario, recupera el historial de la conversación compartida y el estado del modelo UML actual, inyecta el rol especializado solicitado (`modoIa`), invoca al motor de inferencia y publica la respuesta tanto en la base de datos como en el canal WebSocket `/topic/proyecto/{idProyecto}/ia-chat`.
- **RF-2**: CUANDO el parámetro `modoIa` especifica un rol (`MODELADOR_UML`, `REQUISITOS`, `ARQUITECTO`, `DESARROLLADOR`, `REVISOR_PRUEBAS`, `DOCUMENTADOR`, `ANALISTA_ERRORES`, `IMPACTO`, `DECISIONES`, `GENERAL`), EL SISTEMA adapta el system prompt técnico y el foco analítico de acuerdo a las directrices de ingeniería configuradas para dicho rol.
- **RF-3**: CUANDO un usuario consulta `GET /api/ia/colaborativa/proyecto/{idProyecto}/conversacion`, EL SISTEMA retorna el identificador de la conversación compartida del proyecto, su título y la lista completa cronológica de mensajes intercambiados entre usuarios humanos, la IA y las notificaciones del sistema.

### Generación y Gestión de Propuestas de Cambio UML
- **RF-4**: CUANDO la respuesta de la IA contiene un bloque estructurado de operaciones (`[OPERACIONES_UML] ... [/OPERACIONES_UML]`), EL SISTEMA extrae y valida el bloque JSON, calcula el total de operaciones y persiste una nueva entidad `IaPropuesta` en estado `PENDIENTE`, vinculada a la conversación, al usuario solicitante y a la `versionOrigen` del modelo activo.
- **RF-5**: CUANDO se crea una nueva propuesta de cambio, EL SISTEMA emite automáticamente la entidad serializada en formato `IaPropuestaDTO` a través del tópico WebSocket `/topic/proyecto/{idProyecto}/ia-propuestas`.
- **RF-6**: CUANDO un usuario consulta `GET /api/ia/colaborativa/proyecto/{idProyecto}/propuestas`, EL SISTEMA retorna la colección de todas las propuestas históricas y pendientes asociadas al proyecto, ordenadas por fecha de creación descendente.
- **RF-7**: CUANDO un colaborador con permisos invoca `POST /api/ia/colaborativa/proyecto/{idProyecto}/propuestas/{idPropuesta}/decision` con la decisión `APROBAR` o `RECHAZAR`, EL SISTEMA actualiza el estado de la propuesta, registra al usuario aprobador, la marca temporal, el comentario de justificación y difunde la actualización por WebSocket.
- **RF-8**: SI la decisión enviada no corresponde a `APROBAR` o `RECHAZAR`, ENTONCES EL SISTEMA rechaza la petición con código HTTP 400 (`IllegalArgumentException`).

### Aplicación Transaccional y Control de Concurrencia Optimista
- **RF-9**: CUANDO un usuario con permisos de edición invoca `POST /api/ia/colaborativa/proyecto/{idProyecto}/propuestas/{idPropuesta}/aplicar`, EL SISTEMA verifica que la propuesta no haya sido aplicada ni rechazada previamente, ejecuta las operaciones atómicamente sobre el `contenidoJson` del modelo UML, recalcula las referencias de identificadores únicos, actualiza la versión semántica del modelo (ej. de `1.0` a `1.1`) y marca la propuesta en estado `APLICADA`.
- **RF-10**: SI al momento de aplicar una propuesta la versión actual del modelo UML en base de datos difiere de la `versionOrigen` registrada en la propuesta, ENTONCES EL SISTEMA bloquea la aplicación, transiciona el estado de la propuesta a `OBSOLETA`, publica un mensaje de advertencia de conflicto de concurrencia en el chat del proyecto y retorna un error HTTP 409/IllegalStateException.
- **RF-11**: SI se intenta aplicar una propuesta que ya posee estado `APLICADA`, ENTONCES EL SISTEMA rechaza la operación garantizando idempotencia estricta para evitar la duplicación de clases o atributos en el diagrama.

### Multimodalidad (Visión y Audio a UML)
- **RF-12**: CUANDO se envía una imagen válida al endpoint `POST /api/ia/uml/imagen` (`multipart/form-data`), EL SISTEMA valida que el archivo no esté vacío y que su tipo MIME sea compatible (`image/png`, `image/jpeg`, `image/webp`), transmite el archivo a Gemini Vision junto al esquema de salida UML 2.5 y retorna un documento JSON con elementos (clases, interfaces, enums, atributos, operaciones, visibilidades) y relaciones (asociación, herencia, agregación, composición, dependencias, relaciones recursivas y de clase de asociación).
- **RF-13**: CUANDO se envía un archivo de audio al endpoint `POST /api/ia/uml/audio`, EL SISTEMA valida los bytes, concatena el contexto opcional del diagrama y solicita a Gemini Multimodal la transcripción e inferencia de elementos UML requeridos, devolviendo el árbol JSON normalizado para el lienzo.
- **RF-14**: SI un archivo subido a los endpoints multimodales está corrupto, vacío o sin tipo MIME identificable, ENTONCES EL SISTEMA responde inmediatamente con código HTTP 400 y mensaje explicativo en formato JSON.

### Resiliencia Híbrida y Fallback Offline
- **RF-15**: MIENTRAS la configuración `codeforge.ai.proveedor` esté definida como `HIBRIDO`, EL SISTEMA intenta realizar la consulta a través de Google Gemini en la nube; ante fallos de conexión externa, agotamiento de cuota o ausencia de clave API, conmuta de forma transparente y sin error fatal al servicio `IaLocalService` (Ollama local).
- **RF-16**: MIENTRAS el servicio local Ollama se encuentre activo en `http://localhost:11434`, EL SISTEMA despacha las peticiones con el modelo local `qwen2.5-coder:1.5b` utilizando un timeout de 90 segundos y temperatura de inferencia 0.2.
- **RF-17**: SI tanto el proveedor en la nube como el servidor local de Ollama se encuentran inaccesibles, ENTONCES EL SISTEMA genera una respuesta de respaldo contextual con recomendaciones preliminares basadas en el estado del modelo para asegurar que el usuario nunca experimente una caída del servicio.

### Diagnóstico Automatizado de Modelos
- **RF-18**: CUANDO se solicita el diagnóstico mediante `GET /api/ia/colaborativa/proyecto/{idProyecto}/diagnostico`, EL SISTEMA examina el grafo del modelo UML activo y genera una lista de hallazgos (`IaUmlFindingDTO`) categorizados por severidad (`ALTA`, `MEDIA`, `BAJA`, `INFO`) y tipo (`ERROR_ESTRUCTURAL`, `INCONSISTENCIA`, `REGLA_NEGOCIO`, `RECOMENDACION_DISENO`), indicando el elemento afectado y la propuesta de solución.

### Auditoría y Métricas
- **RF-19**: EL SISTEMA registra de forma persistente en la tabla `ia_metricas` cada interacción procesada, almacenando: proyecto, usuario solicitante, nombre del modelo de IA utilizado, modo de operación, duración de la llamada en milisegundos, bandera de éxito, mensaje de error (si aplica) y cantidad de tokens de entrada, salida y totales.

## Requisitos no funcionales
- **Rendimiento**:
  - Tiempo de respuesta de la IA local en inferencias técnicas estándar menor a 3.5 segundos sobre hardware local sin tarjeta gráfica dedicada (inferencia en CPU).
  - Tiempo de timeout máximo de 90 segundos para consultas analíticas complejas o con modelos locales.
- **Consumo de Recursos (Optimización 8 GB RAM)**:
  - El motor local (Ollama + `qwen2.5-coder:1.5b`) no debe exceder 1.5 GB de memoria RAM en ejecución continua.
- **Seguridad y Control de Acceso**:
  - Toda petición a los endpoints bajo `/api/ia/**` requiere autenticación mediante token JWT en la cabecera `Authorization: Bearer <token>`.
  - Validación de membresía: El usuario autenticado debe ser propietario o colaborador registrado del proyecto consultado; en caso contrario, se arroja `AccessDeniedException` (HTTP 403).
  - Sanitización obligatoria de payloads JSON y prevención de ataques de inyección en prompts de entrada.
- **Disponibilidad y Tolerancia a Fallos**:
  - Disponibilidad ininterrumpida ante caídas de internet gracias a la arquitectura de conmutación híbrida (Gemini -> Ollama -> Respuesta de respaldo).
- **Concurrencia y Consistencia de Datos**:
  - Las operaciones de modificación y aplicación de propuestas se ejecutan bajo transacciones `@Transactional` en Spring Boot con nivel de aislamiento que previene lecturas sucias y condiciones de carrera.
- **Interoperabilidad y Estándares**:
  - Salida de diagramas compatible con el metamodelo UML 2.5 de la OMG.
  - Comunicación asíncrona mediante protocolo STOMP sobre WebSockets (`/topic/proyecto/{idProyecto}/*`).

## Casos límite
- **Proyecto sin Modelo Creado**: Si un proyecto no posee aún un modelo UML asociado, la IA genera la propuesta con la inicialización base (`versionUML: 2.5`, listas de elementos y relaciones vacías).
- **Colisión Concurrente de Aprobación/Aplicación**: Dos líderes intentando aplicar la misma propuesta simultáneamente; la primera transacción actualiza el estado y la versión, la segunda falla por validación de estado ya aplicado (idempotencia).
- **Propuesta Obsoleta por Modificación Manual**: Si un usuario edita manualmente una clase en el canvas JointJS mientras la propuesta de la IA estaba pendiente, la versión del modelo se incrementa y la propuesta queda bloqueada automáticamente como `OBSOLETA`.
- **Imagen con Notación No Estándar o Pizarra Manual**: Reconocimiento de flechas con punta abierta vs. triángulo cerrado, o rombos de agregación vs. composición según las reglas explícitas de visión artificial integradas en el servicio.
- **Relaciones Reflexivas / Recursivas**: Identificación de arcos que nacen y mueren en la misma entidad (`origen == destino`), garantizando la preservación de roles y multiplicidades en ambos extremos.
- **Audio con Ruido de Fondo o Silencio Total**: Detección de cargas vacías antes del procesamiento y captura controlada de excepciones con respuesta amigable.
- **Desconexión Súbita de WebSocket**: Los clientes desconectados recuperan el historial completo y el estado actualizado de propuestas al reconectarse mediante invocación al endpoint REST `GET /conversacion`.

## Fuera de alcance
- Entrenamiento supervisado o fine-tuning de modelos fundacionales desde el backend de Spring Boot.
- Ejecución dinámica o compilación en tiempo de ejecución de código Java generado por la IA en un sandbox de servidor (la generación de código es determinista a través del módulo `BackendGeneratorService`).
- Soporte para diagramas que no pertenezcan al estándar UML 2.5 (ej. diagramas de red física, BPMN o diagramas de flujo libres).
- Modificación directa del esquema de base de datos relacional de producción sin pasar por el flujo de propuestas y aprobación humana.

## Criterios de finalización
- **Pruebas de Integración y Unitarias en Verde**: Ejecución satisfactoria de la suite de pruebas del backend para `IaColaborativaServiceTest`, `IaLocalServiceTest` y controladores asociados con `./mvnw test`.
- **Validación del Flujo de Propuesta Transaccional**: Demostración end-to-end de envío de pregunta -> generación de `IaPropuesta` -> notificación WebSocket -> aprobación -> aplicación y cambio de versión del modelo.
- **Validación de Conflicto de Concurrencia**: Verificación de bloqueo de aplicación de propuesta cuando la versión del modelo ha cambiado (`versionOrigen != versionActual`).
- **Validación de Conmutación Offline**: Comprobación de respuesta técnica satisfactoria con la red desconectada utilizando Ollama local (`qwen2.5-coder:1.5b`) vía `iniciar-ia-local.bat`.
- **Validación Multimodal**: Demostración de generación de diagrama UML a partir de una imagen de prueba mediante `/api/ia/uml/imagen`.

## Dudas abiertas
- [NECESITA ACLARACIÓN] Evaluar si en futuras versiones se requiere soporte de streaming por Server-Sent Events (SSE) o WebSocket Chunks para que las respuestas de la IA se visualicen token por token en el frontend.
- [NECESITA ACLARACIÓN] Determinar si se habilitará la selección dinámica del modelo LLM local (ej. cambiar entre `qwen2.5-coder:1.5b`, `llama3.2:1b` o `deepseek-coder`) directamente desde el panel de configuración de la interfaz gráfica.
- [NECESITA ACLARACIÓN] Definir si el historial de conversaciones compartidas debe contar con una opción de exportación formal en formato PDF o Markdown desde el editor.
