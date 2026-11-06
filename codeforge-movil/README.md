# 📱 CodeForge Mobile (Modo Solo Lectura)

Aplicación móvil desarrollada con **Flutter & Dart** para la plataforma **CodeForge**. Permite a los usuarios registrados en la web consultar sus proyectos, diagramas UML, código generado, documentación, historial de IA y propuestas de cambios desde cualquier teléfono de forma segura, rápida y con un diseño premium bajo estándares **Impeccable**.

---

## 🏛️ Arquitectura Modular del Proyecto

La aplicación está organizada bajo una arquitectura modular por características (*feature-first*):

```
lib/
├── core/                               # Núcleo y utilidades compartidas
│   ├── config/api_config.dart          # Configuración de URLs y endpoints
│   ├── network/api_client.dart         # Cliente HTTP con Bearer tokens y manejo de 401
│   ├── storage/secure_storage_service.dart # Almacenamiento seguro (Android Keystore / iOS Keychain)
│   ├── theme/                          # Sistema de diseño Impeccable (AppColors, AppTypography, AppTheme)
│   └── widgets/                        # Widgets base (ReadOnlyBadge, CustomButton, Skeletons, ErrorRetry)
│
├── modules/
│   ├── auth/                           # 🔐 Autenticación con cuentas web existentes
│   │   ├── models/ · services/ · providers/ · views/login_view.dart
│   │
│   ├── projects/                       # 📂 Lista y búsqueda de proyectos autorizados
│   │   ├── models/ · services/ · providers/ · views/proyectos_list_view.dart
│   │
│   ├── project_detail/                 # 🧭 Hub central del proyecto
│   │   ├── views/project_detail_view.dart
│   │   └── widgets/project_summary_tab.dart (Resumen, trazabilidad y roles)
│   │
│   ├── uml/                            # 📐 Diagramas UML interactivos
│   │   ├── models/ · services/ · widgets/uml_canvas_painter.dart
│   │   └── views/ (Lienzo zoomable con InteractiveViewer + explorador de clases)
│   │
│   ├── generated_code/                 # 💻 Código generado Spring Boot / Flutter
│   │   ├── models/ · services/ · views/ (Árbol de archivos + resaltado de sintaxis con flutter_highlight)
│   │
│   ├── documentation/                  # 📋 Especificación técnica y requisitos
│   │   └── views/documentation_view.dart (Arquitectura, requisitos funcionales y no funcionales)
│   │
│   ├── ia_chat/                        # 🤖 Historial de conversaciones con la IA
│   │   └── views/ia_history_view.dart (Feed de lectura con roles y badges de propuestas)
│   │
│   └── proposals/                      # ✨ Propuestas de modificación arquitectónica
│       └── views/ (Lista con chips de estado + modal bottom sheet de diagnóstico y operaciones UML)
│
└── main.dart                           # Punto de entrada con AuthGate y MultiProvider
```

---

## 🔒 Regla Fundamental: 100% Solo Lectura

- **Sin escrituras accidentales**: La aplicación móvil únicamente consume endpoints `GET` de consulta segura (salvo el inicio de sesión `POST /api/auth/login`).
- **Seguridad en Backend**: El backend de Spring Boot valida la identidad del usuario y sus permisos de acceso en cada recurso.
- **Tokens protegidos**: El JWT se almacena de forma encriptada en el enclave seguro del dispositivo (`flutter_secure_storage`).

---

## 🚀 Cómo Ejecutar la Aplicación

1. Asegúrate de tener el backend de Spring Boot iniciado en el puerto `8080`.
2. Desde la terminal en la carpeta `codeforge-movil`:
   ```bash
   cd codeforge-movil
   flutter pub get
   flutter run
   ```
3. Dispositivos compatibles:
   - Emulador Android (`http://10.0.2.2:8080/api` preconfigurado)
   - Dispositivo físico Android / iPhone (puedes ajustar la IP del servidor desde el botón "Configurar servidor" en el login)
   - Windows Desktop / Web Chrome (`flutter run -d chrome` o `flutter run -d windows`)
