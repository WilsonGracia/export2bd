# 📘 Export2BD

**Export2BD** es una herramienta de escritorio que permite exportar datos desde archivos Excel (.xlsx) hacia bases de datos PostgreSQL, sin necesidad de conocimientos técnicos. Solo ingresa tus credenciales y sube tu archivo.

---

## Navegación

- [Inicio Rápido](#inicio-rápido)
- [API Reference](#api-reference)
- [Arquitectura](#arquitectura)
- [Autenticación JWT](#autenticación-jwt)
- [Empaquetado Windows](#empaquetado-windows)
- [Configurar Credenciales](#configurar-credenciales)
- [Subir Excel](#subir-excel)
- [Launcher](#launcher)
- [Seguridad](#seguridad)
- [Errores Comunes](#errores-comunes)
- [Conexión Dinámica](#conexión-dinámica)

---

## Inicio Rápido

1. Descarga la carpeta `Export2BD_Final`
2. Ejecuta `launcher.bat`
3. Se abrirá el backend NestJS y luego el frontend JavaFX automáticamente
4. Ve a **Settings** e ingresa las credenciales de tu base de datos PostgreSQL
5. Ve a **Export** y sube tu archivo `.xlsx`

> ✅ No necesitas tener Java ni Node.js instalado — todo viene empaquetado.

---

## API Reference

El backend expone los siguientes endpoints en `http://localhost:3000`:

### Auth

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/auth/login` | Valida credenciales de BD y devuelve un JWT |
| POST | `/auth/refresh` | Renueva el token JWT |
| POST | `/auth/validate` | Valida un token existente |

### Export

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/export/upload-with-credentials` | Sube un `.xlsx` e inserta los registros en la BD |
| POST | `/export/control-with-credentials` | Inserta un registro individual |

Todos los endpoints de export requieren el header:

```
Authorization: Bearer <token>
```

### Body para /auth/login

```json
{
  "host": "localhost",
  "port": 5432,
  "username": "postgres",
  "password": "tupassword",
  "database": "tubasededatos",
  "schema": "public"
}
```

---

## Arquitectura

```
┌─────────────────────┐    HTTP (localhost:3000)              ┌──────────────────────┐
│   Frontend JavaFX   │ ───────────────────────────────────►  │  Backend NestJS      │
│                     │                                       │                      │
│  - LoginView        │ POST /auth/login                      │  - AuthModule        │
│  - SettingsView     │ POST /export/upload-with-credentials  │  - ExportModule      │
│  - ExportView       │ POST /export/control-with-credentials │  - DynamicDatabase   │
└─────────────────────┘                                       └──────────┬───────────┘
                                                                         │
                                                                         ▼
                                                              ┌────────────────────────┐
                                                              │  PostgreSQL (externo)  │
                                                              │  Credenciales del      │
                                                              │  usuario               │
                                                              └────────────────────────┘
```

El frontend JavaFX hace llamadas HTTP al backend NestJS que corre localmente. El backend usa las credenciales del usuario para conectarse dinámicamente a cualquier PostgreSQL externo.

---

## Autenticación JWT

1. El usuario ingresa sus credenciales de BD en el frontend
2. El frontend las envía a `/auth/login`
3. El backend valida la conexión a PostgreSQL con esas credenciales
4. Si es válida, genera un JWT que contiene las credenciales encriptadas
5. El frontend guarda el token y lo usa en cada petición posterior
6. El token expira en **1 hora** y se puede renovar con `/auth/refresh`

---

## Empaquetado Windows

El proyecto se distribuye como una carpeta portable:

```
Export2BD_Final/
├── launcher.bat            ← Ejecutar esto
├── export2bd-backend.exe   ← Backend NestJS (pkg + Node.js embebido)
└── Export2BD/              ← Frontend JavaFX (jpackage + Liberica JDK 21)
    ├── Export2BD.exe
    ├── app/
    └── runtime/
```

- El backend se empaquetó con **pkg** (Node.js 18 embebido)
- El frontend se empaquetó con **jpackage** usando **Liberica Full JDK 21** (Java + JavaFX embebido)

---

## Configurar Credenciales

1. Abre la aplicación con `launcher.bat`
2. Ve a la sección **Settings**
3. Ingresa los datos de tu base de datos PostgreSQL:

| Campo | Ejemplo | Descripción |
|-------|---------|-------------|
| Host | `localhost` | Dirección del servidor PostgreSQL |
| Puerto | `5432` | Puerto por defecto de PostgreSQL |
| Usuario | `postgres` | Nombre de usuario |
| Contraseña | `••••••` | Contraseña del usuario |
| Base de datos | `mibasededatos` | Nombre de la BD a usar |

Haz clic en **Guardar** — la conexión se validará automáticamente.

---

## Subir Excel

El archivo `.xlsx` debe tener exactamente estas columnas en la primera fila:

| id_number | name | type | description |
|-----------|------|------|-------------|
| 001 | Control A | preventivo | Descripción del control |

- `id_number`: identificador único (máx. 30 caracteres)
- `name`: nombre del registro
- `type`: tipo (máx. 50 caracteres)
- `description`: descripción del registro

> ⚠️ Si el archivo tiene columnas extra o faltantes, la importación será rechazada.

---

## Launcher

El `launcher.bat` realiza lo siguiente:

1. Inicia `export2bd-backend.exe` en segundo plano
2. Espera a que el puerto `3000` esté activo
3. Abre `Export2BD.exe`
4. Cuando se cierra el frontend, mata el proceso del backend automáticamente

---

## Seguridad

- Las credenciales de BD **nunca se almacenan en texto plano**
- Viajan encriptadas dentro de un **JWT firmado con HS256**
- El token expira en 1 hora
- Cada conexión a PostgreSQL se crea dinámicamente y se cierra tras 30 minutos de inactividad
- El backend solo acepta conexiones desde `localhost`

---

## Errores Comunes

| Error | Causa | Solución |
|-------|-------|----------|
| `Server connection error` | El backend no está corriendo | Usar `launcher.bat` en lugar de abrir el .exe directamente |
| `Invalid database credentials` | Credenciales incorrectas | Verificar host, puerto, usuario y contraseña |
| `Missing columns` | El Excel no tiene el formato correcto | Usar columnas: `id_number, name, type, description` |
| `Already exists` | El `id_number` ya existe en la BD | El registro ya fue importado previamente |
| `Failed to launch JVM` | Runtime de Java incorrecto | Usar la versión empaquetada con Liberica Full JDK |

---

## Conexión Dinámica

El backend no requiere una configuración fija de base de datos:

- Cada usuario provee sus propias credenciales desde el frontend
- El backend crea una conexión PostgreSQL dinámica por usuario
- Las conexiones se cachean por **30 minutos** para mejor rendimiento
- Se soportan hasta **50 conexiones simultáneas**
- Al cerrar la app, todas las conexiones se cierran automáticamente
