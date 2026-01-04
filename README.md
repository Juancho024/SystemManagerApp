# SystemManager 📱

Sistema de gestión integral para condominios y edificios residenciales, desarrollado en Android con arquitectura cliente-servidor.

## 📋 Descripción

SystemManager es una aplicación móvil robusta diseñada para facilitar la administración de condominios. Permite gestionar propietarios, realizar seguimiento de pagos, generar registros financieros y controlar el acceso mediante un sistema de roles (Admin/Usuario).

## ✨ Características Principales

### 🔐 Autenticación y Seguridad
- Sistema de login con validación de credenciales
- Gestión de sesiones persistentes
- Cambio de contraseña seguro
- Control de acceso basado en roles (Admin/Usuario)

### 👥 Gestión de Propietarios
- Registro y búsqueda de propietarios
- Visualización de estado de pagos (Al día/Pendiente)
- Actualización de información personal
- Eliminación de registros (solo Admin)

### 💰 Gestión Financiera
- Creación de registros de cuotas mensuales
- Seguimiento de pagos y abonos
- Cálculo automático de balances
- Actualización masiva de estados de cuenta
- Generación de reportes en PDF
- Adjuntar comprobantes de pago (hasta 4 imágenes)

### 📊 Contabilidad
- Visualización de tablas de registros por mes
- Búsqueda y filtrado de registros históricos
- Generación de informes financieros
- Exportación de datos

### 📅 Calendario
- Vista mensual de eventos
- Gestión de actividades del condominio
- Acceso rápido a funciones desde el calendario

## 🛠️ Tecnologías Utilizadas

### Frontend (Android)
- **Java** - Lenguaje principal
- **Android SDK** - Plataforma de desarrollo
- **Material Design 3** - Componentes UI modernos
- **ViewBinding** - Vinculación segura de vistas
- **LiveData & ViewModel** - Arquitectura MVVM
- **Navigation Component** - Navegación entre fragmentos
- **Retrofit 2** - Cliente HTTP para consumo de API REST
- **Gson** - Serialización/deserialización JSON
- **Glide** - Carga y caché de imágenes
- **ZXing** - Generación de códigos de barras
- **iTextG** - Generación de documentos PDF
- **Security Crypto** - Almacenamiento seguro de credenciales

### Backend
- **Spring Boot** - Framework REST API
- **PostgreSQL** - Base de datos relacional
- **BCrypt** - Encriptación de contraseñas (opcional)

## 📱 Requisitos del Sistema

- Android 7.0 (API 24) o superior
- Conexión a Internet
- Permisos de almacenamiento (para PDFs e imágenes)
- Permisos de cámara (para capturar comprobantes)

## 🚀 Instalación

### Opción 1: Desde el APK
1. Descarga el archivo `SystemManager3.0.apk`
2. Habilita "Instalar aplicaciones de fuentes desconocidas" en tu dispositivo
3. Instala el APK

### Opción 2: Compilar desde el código fuente

#### Prerrequisitos
- Android Studio Hedgehog o superior
- JDK 11
- Gradle 8.0+
- Backend Spring Boot configurado y en ejecución

#### Pasos
1. Clona el repositorio:
```bash
git clone https://github.com/tuusuario/SystemManager.git
cd SystemManager
```

2. Configura la URL del API en `local.properties`:
```properties
API_BASE_URL="http://tu-servidor:8080/"
```

3. Abre el proyecto en Android Studio

4. Sincroniza Gradle:
```bash
./gradlew build
```

5. Ejecuta la aplicación en un dispositivo o emulador

## ⚙️ Configuración

### 1. Backend
Asegúrate de que el servidor Spring Boot esté corriendo y tenga los siguientes endpoints:

```
POST   /api/usuarios/login
GET    /api/usuarios
POST   /api/usuarios
PUT    /api/usuarios/{id}
DELETE /api/usuarios/{id}
POST   /api/usuarios/{id}/password

GET    /api/propietarios
POST   /api/propietarios
PUT    /api/propietarios/{id}
DELETE /api/propietarios/{id}

GET    /api/registros-financieros
POST   /api/registros-financieros
PUT    /api/registros-financieros/{id}
DELETE /api/registros-financieros/{id}

GET    /api/mes-cuota
POST   /api/mes-cuota
DELETE /api/mes-cuota/{id}
```

### 2. Base de Datos
Estructura principal de tablas:
- `usuario` - Almacena usuarios del sistema
- `propietario` - Información de propietarios
- `registro_financiero` - Registros de cuotas mensuales
- `mes_cuota` - Catálogo de meses disponibles

### 3. Variables de Entorno
Configura en `local.properties`:
```properties
API_BASE_URL="http://192.168.1.100:8080/"
```

## 📖 Uso

### Primer Inicio
1. Abre la aplicación
2. Ingresa credenciales de administrador
3. El sistema guardará la sesión automáticamente

### Como Administrador
- **Gestionar Propietarios**: Crear, editar y eliminar propietarios
- **Registros Financieros**: Crear cuotas mensuales y actualizar balances
- **Cambiar Contraseña**: Acceso desde el menú de perfil
- **Generar Reportes**: Exportar informes en PDF

### Como Usuario
- **Consultar Propietarios**: Ver información y estado de pagos
- **Ver Registros**: Consultar histórico de pagos
- **Cambiar Contraseña**: Modificar credenciales propias

## 🏗️ Estructura del Proyecto

```
SystemManager/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/jrdev/systemmanager/
│   │   │   │   ├── controllers/          # Fragmentos y Activities
│   │   │   │   │   ├── login/           # Login, cambio password, perfil
│   │   │   │   │   ├── propietarios/    # Gestión de propietarios
│   │   │   │   │   ├── contabilidad/    # Registros financieros
│   │   │   │   │   └── calendario/      # Vista calendario
│   │   │   │   ├── DataBaseConnection/  # Capa de datos
│   │   │   │   │   ├── api/            # Interfaces Retrofit
│   │   │   │   │   ├── dao/            # Data Access Objects
│   │   │   │   │   └── repository/     # Repositorios
│   │   │   │   ├── adapters/            # RecyclerView Adapters
│   │   │   │   ├── models/              # Modelos y DTOs
│   │   │   │   └── utilities/           # Helpers y utilidades
│   │   │   ├── res/
│   │   │   │   ├── layout/             # Layouts XML
│   │   │   │   ├── navigation/         # Navigation graphs
│   │   │   │   ├── menu/               # Menús
│   │   │   │   └── drawable/           # Recursos gráficos
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle.kts
│   └── local.properties                 # Configuración local (no en Git)
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```

## 🔒 Permisos Requeridos

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.CAMERA" />
```

## 🎨 Capturas de Pantalla

*(Agrega aquí capturas de pantalla de tu aplicación)*

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Para cambios importantes:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add: nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📝 Changelog

### Versión 2.1 (Actual)
- ✅ Sistema de roles Admin/Usuario implementado
- ✅ Validaciones de permisos en eliminación de propietarios
- ✅ Validaciones de permisos en registros financieros
- ✅ Funcionalidad de cambio de contraseña
- ✅ Sesión persistente con datos encriptados
- ✅ Mejoras en la UI/UX
- ✅ Correcciones de bugs en login

### Versión 1.0
- Lanzamiento inicial
- CRUD de propietarios
- Gestión básica de registros financieros

## 📄 Licencia

Este proyecto es de código propietario. Todos los derechos reservados.

## 👨‍💻 Autor

**JRDev**
- GitHub:(https://github.com/Juancho024)
- Email: juanrijo240@gmail.com

## 🙏 Agradecimientos

- Material Design por los componentes UI
- Comunidad de Android Developers
- Contribuidores del proyecto

---

**Nota**: Este proyecto está en desarrollo activo. Si encuentras algún bug o tienes sugerencias, por favor abre un issue.

⭐ Si te gusta este proyecto, no olvides darle una estrella en GitHub!
