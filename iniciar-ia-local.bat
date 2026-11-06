@echo off
chcp 65001 >nul
echo ===================================================
echo     CODEFORGE - INICIANDO MOTOR DE IA LOCAL 8GB
echo ===================================================
echo.

where ollama >nul 2>nul
if %errorlevel% neq 0 (
    set "PATH=%LOCALAPPDATA%\Programs\Ollama;%PATH%"
)

where ollama >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Ollama no se encuentra instalado en el sistema.
    echo Por favor instale Ollama desde https://ollama.com/download
    exit /b 1
)

echo [1/3] Verificando servicio Ollama en segundo plano...
curl -s http://127.0.0.1:11434/api/tags >nul 2>nul
if %errorlevel% neq 0 (
    echo Iniciando servidor Ollama en segundo plano...
    start "" /b ollama serve
    timeout /t 3 /nobreak >nul
) else (
    echo Servidor Ollama ya esta en ejecucion.
)

echo [2/3] Verificando modelo qwen2.5-coder:1.5b...
ollama list | findstr "qwen2.5-coder:1.5b" >nul
if %errorlevel% neq 0 (
    echo Descargando modelo qwen2.5-coder:1.5b...
    ollama pull qwen2.5-coder:1.5b
) else (
    echo Modelo qwen2.5-coder:1.5b listo para usar.
)

echo.
echo ===================================================
echo [OK] IA Local activa en http://127.0.0.1:11434
echo Consumo de RAM: aprox 1.2 GB - Optimizado para 8 GB
echo Modo offline: 100 por ciento Funcional
echo ===================================================
echo.
