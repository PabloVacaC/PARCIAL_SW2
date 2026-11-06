# 🧠 Guía de IA Local Offline (Optimizada para 8 GB de RAM) - CodeForge

Este documento explica cómo funciona la Inteligencia Artificial Local integrada en CodeForge y cómo desplegarla o correrla en cualquier otra PC (incluso con especificaciones estándar de 8 GB de RAM).

---

## 🚀 Estado Actual en tu Computadora

¡Todo está **100% instalado, configurado y validado**!

1. **Motor de IA Instalado**: [Ollama](https://ollama.com) v0.40.10 instalado en el sistema.
2. **Modelo Descargado**: `qwen2.5-coder:1.5b` (~986 MB en disco, ~1.2 GB de RAM en ejecución).
3. **Backend Conectado**:
   - `IaLocalService.java`: Cliente HTTP nativo que se comunica directamente con `http://localhost:11434`.
   - `IaColaborativaService.java`: Estrategia **híbrida inteligente**. Si hay conexión a internet y clave de Gemini, la usa; si no hay internet o falla, **conmuta automáticamente a la IA local offline sin interrumpir al usuario**.
   - Propuestas UML (`CREAR_CLASE`, `CREAR_RELACION`, etc.) se generan y procesan en el canvas JointJS tanto online como offline.
4. **Pruebas de Validación**:
   - Tests unitarios de backend ejecutados exitosamente (`BUILD SUCCESS`).
   - Inferencia local comprobada en menos de 2 segundos.

---

## 💻 ¿Cómo correrlo en otra PC (Paso a Paso)?

Si deseas llevar este proyecto a otra PC (por ejemplo, para defender tu proyecto o exponerlo sin depender de internet):

### Paso 1: Instalar Ollama en la nueva PC
- Descarga el instalador oficial desde [ollama.com/download](https://ollama.com/download) y ejecútalo (instalación en 1 clic).

### Paso 2: Descargar el modelo ligero
Abre una terminal (PowerShell o CMD) en la nueva PC y escribe:
```bash
ollama pull qwen2.5-coder:1.5b
```
*(Solo tarda 1 minuto, pesa apenas 986 MB).*

### Paso 3: Iniciar el proyecto
En la raíz de este proyecto tienes el archivo ejecutable:
- Haz doble clic en `iniciar-ia-local.bat`.
- Luego inicia el backend de Spring Boot (`mvn spring-boot:run`) y el frontend (`npm start`).

---

## ⚙️ Modos de Configuración en `application.yml`

En `codeforge-backend/src/main/resources/application.yml` puedes alternar el comportamiento con la propiedad:

```yaml
codeforge:
  ai:
    proveedor: HIBRIDO # Opciones: HIBRIDO, LOCAL, GEMINI
    gemini:
      api-key: ${GEMINI_API_KEY:}
      model: gemini-3.5-flash
    local:
      url: http://localhost:11434
      modelo: qwen2.5-coder:1.5b
      timeout-segundos: 90
```

- **`HIBRIDO`** (Recomendado por defecto): Usa la nube cuando hay internet; si se cae la red o no hay clave, activa la IA local automáticamente.
- **`LOCAL`**: 100% offline. Nunca toca internet ni requiere ninguna clave API.
- **`GEMINI`**: Solo nube.
