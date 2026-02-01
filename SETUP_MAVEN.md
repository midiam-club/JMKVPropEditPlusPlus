# Guía de Configuración de Maven

VS Code no puede encontrar Maven en tu sistema. Esto es necesario para compilar el proyecto migrado.

## Pasos para solucionar el error

### Opción 1: Descargar e Instalar Maven (Recomendado)
Para que funcione todo (VS Code y el script `run.bat`), necesitas Maven en tu sistema.

1. Descarga **Apache Maven** (Binary zip archive) desde: https://maven.apache.org/download.cgi
2. Descomprime el archivo en una carpeta segura, por ejemplo `C:\Program Files\Maven`.
3. Añade la carpeta `bin` de Maven a tus **Variables de Entorno**:
   - Pulsa `Win + R`, escribe `sysdm.cpl` y Enter.
   - Pestaña "Opciones avanzadas" -> "Variables de entorno".
   - En "Variables del sistema", busca `Path` y dale a "Editar".
   - Añade la ruta completa a la carpeta bin (ej. `C:\Program Files\Maven\apache-maven-3.9.6\bin`).
   - Acepta todo.
4. Reinicia VS Code.

### Opción 2: Configurar solo VS Code
Si ya tienes Maven descargado pero no en el PATH:

1. En VS Code, abre `File` -> `Preferences` -> `Settings` (o `Ctrl + ,`).
2. Busca `maven.executable.path`.
3. Introduce la ruta completa al ejecutable `mvn.cmd` o `mvn.bat`.

### Verificación
Abre una nueva terminal en VS Code y escribe:
```cmd
mvn -version
```
Si ves la versión de Maven, ¡estás listo! Ahora `run.bat` funcionará.
