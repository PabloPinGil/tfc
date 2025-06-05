# StopSmoke - Tu compañero para dejar de fumar

AppStopSmoke es una aplicación móvil diseñada para ayudarte a reducir y finalmente dejar de fumar. Con un enfoque en el seguimiento, estadísticas y motivación, esta app te acompaña en tu camino hacia una vida libre de humo.

## Características principales

- 🚭 **Registro simple de cigarros** - Toca un botón para registrar cada cigarro fumado
- 📊 **Estadísticas personales** - Visualiza tu consumo diario y progreso a lo largo del tiempo
- 🏆 **Seguimiento de logros** - Celebra tus hitos sin fumar
- 👥 **Comparación con amigos** - Motívate comparando tu progreso con otros usuarios 

## Tecnologías utilizadas

- **Arquitectura**: MVVM (Model-View-ViewModel)
- **Lenguaje**: Java
- **Base de datos**: Firebase Firestore
- **Autenticación**: Firebase Authentication (usando autenticación anónima)
- **Librerías principales**:
  - MPAndroidChart (para gráficos)
  - Firebase SDK


## Configuración e instalación

### Requisitos previos

- Android Studio
- Dispositivo Android o emulador

### Pasos para ejecutar

1. Clona el repositorio

2. Abre el proyecto en Android Studio

3. Ejecuta la aplicación en un emulador o dispositivo físico


## Funcionalidades implementadas

### Registro de consumo
- Registro instantáneo de cada cigarro fumado
- Visualización en tiempo real del estado actual ("Llevas X días sin fumar" o "Llevas X cigarros hoy")

### Seguimiento estadístico
- Gráficos de consumo
- Cálculo de promedio diario
- Progresión histórica del consumo

### Sistema de comparación
- Añade amigos que también usen la app
- Compara tu progreso con el de otros usuarios
- Visualización comparativa en gráficos


## Arquitectura

StopSmoke sigue una arquitectura MVVM limpia y modular:

**Principios clave**:
- Separación de responsabilidades
- Uso de LiveData para comunicación reactiva
- Repositorios como únicas fuentes de datos
- Inyección de dependencias manual para mejor testabilidad


**Deja de fumar hoy mismo** - Cada cigarro que no fumas es una victoria. ¡StopSmoke te ayuda a ganar la batalla contra el tabaco!
