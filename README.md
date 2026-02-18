# 📘 Export2BD

**Export2BD** is a desktop tool that allows you to export data from Excel files (.xlsx) to PostgreSQL databases, without requiring any technical knowledge. Designed initially for managing **ISO 27001 security controls**, it lets you import and maintain your control catalog directly from a spreadsheet. Just enter your credentials and upload your file.

---

## Screenshots

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/WilsonGracia/export2bd/blob/main/settingsview.png?raw=true" alt="Settings" width="350"/>
      <br/><sub><b>Configure Credentials</b></sub>
      <br/><sub>Enter your PostgreSQL database connection details (host, port, username, password, and database name). The connection is validated automatically upon saving.</sub>
    </td>
    <td align="center">
      <img src="https://github.com/WilsonGracia/export2bd/blob/main/exportview.png?raw=true" alt="Export" width="350"/>
      <br/><sub><b>Export Excel</b></sub>
      <br/><sub>Select your <code>.xlsx</code> file and export it directly to the <code>controls</code> table in your database. Each row in the Excel file is inserted as a new record.</sub>
    </td>
  </tr>
</table>

---

## Navigation

- [Quick Start](#quick-start)
- [API Reference](#api-reference)
- [Architecture](#architecture)
- [JWT Authentication](#jwt-authentication)
- [Windows Packaging](#windows-packaging)
- [Configure Credentials](#configure-credentials)
- [Upload Excel](#upload-excel)
- [Launcher](#launcher)
- [Security](#security)
- [Common Errors](#common-errors)
- [Dynamic Connection](#dynamic-connection)
- [Roadmap](#roadmap)

---

## Quick Start

1. Download the `Export2BD_Final` folder
2. Run `launcher.bat`
3. The NestJS backend and then the JavaFX frontend will open automatically
4. Go to **Settings** and enter your PostgreSQL database credentials
5. Go to **Export** and upload your `.xlsx` file

> ✅ You don't need Java or Node.js installed — everything comes pre-packaged.

### Prerequisite: database structure
> ℹ️ The `controls` table is designed to store **ISO 27001 security controls** the set of security measures defined by the standard that organizations implement to manage information security risks.

> ⚠️ In this version, the application only works with a table named `controls`. Make sure your database has the following structure before using the app:
```sql
CREATE TABLE IF NOT EXISTS public.controls
(
    id_number   character varying(30)  NOT NULL,
    name        text,
    description text,
    type        character varying(50),
    created_at  timestamp without time zone DEFAULT now(),
    updated_at  timestamp without time zone,
    deleted_at  timestamp without time zone,
    CONSTRAINT control_pk PRIMARY KEY (id_number)
);
```

---

## API Reference

The backend exposes the following endpoints at `http://localhost:3000`:

### Auth

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/login` | Validates DB credentials and returns a JWT |
| POST | `/auth/refresh` | Renews the JWT token |
| POST | `/auth/validate` | Validates an existing token |

### Export

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/export/upload-with-credentials` | Uploads a `.xlsx` file and inserts records into the DB |
| POST | `/export/control-with-credentials` | Inserts a single record |

All export endpoints require the following header:
```
Authorization: Bearer <token>
```

### Body for /auth/login
```json
{
  "host": "localhost",
  "port": 5432,
  "username": "postgres",
  "password": "yourpassword",
  "database": "yourdatabase",
  "schema": "public"
}
```

---

## Architecture
```
┌─────────────────────┐    HTTP (localhost:3000)              ┌──────────────────────┐
│   JavaFX Frontend   │ ───────────────────────────────────►  │  NestJS Backend      │
│                     │                                       │                      │
│  - LoginView        │ POST /auth/login                      │  - AuthModule        │
│  - SettingsView     │ POST /export/upload-with-credentials  │  - ExportModule      │
│  - ExportView       │ POST /export/control-with-credentials │  - DynamicDatabase   │
└─────────────────────┘                                       └──────────┬───────────┘
                                                                         │
                                                                         ▼
                                                              ┌────────────────────────┐
                                                              │  PostgreSQL (external) │
                                                              │  User credentials      │
                                                              └────────────────────────┘
```

The JavaFX frontend makes HTTP calls to the NestJS backend running locally. The backend uses the user's credentials to dynamically connect to any external PostgreSQL instance.

---

## JWT Authentication

1. The user enters their DB credentials in the frontend
2. The frontend sends them to `/auth/login`
3. The backend validates the PostgreSQL connection using those credentials
4. If valid, it generates a JWT containing the encrypted credentials
5. The frontend stores the token and uses it in every subsequent request
6. The token expires in **1 hour** and can be renewed with `/auth/refresh`

---

## Windows Packaging

The project is distributed as a portable folder:
```
Export2BD_Final/
├── launcher.bat            ← Run this
├── export2bd-backend.exe   ← NestJS backend (pkg + embedded Node.js)
└── Export2BD/              ← JavaFX frontend (jpackage + Liberica JDK 21)
    ├── Export2BD.exe
    ├── app/
    └── runtime/
```

- The backend was packaged with **pkg** (embedded Node.js 18)
- The frontend was packaged with **jpackage** using **Liberica Full JDK 21** (embedded Java + JavaFX)

---

## Configure Credentials

1. Open the application with `launcher.bat`
2. Go to the **Settings** section
3. Enter your PostgreSQL database details:

| Field | Example | Description |
|-------|---------|-------------|
| Host | `localhost` | PostgreSQL server address |
| Port | `5432` | Default PostgreSQL port |
| Username | `postgres` | Database username |
| Password | `••••••` | User password |
| Database | `mydatabase` | Name of the database to use |

Click **Save** — the connection will be validated automatically.

---

## Upload Excel

The `.xlsx` file must have exactly these columns in the first row:

| id_number | name | type | description |
|-----------|------|------|-------------|
| 001 | Control A | preventive | Control description |

- `id_number`: unique identifier (max 30 characters)
- `name`: record name
- `type`: type (max 50 characters)
- `description`: record description

> ⚠️ If the file has extra or missing columns, the import will be rejected.

---

## Launcher

The `launcher.bat` does the following:

1. Starts `export2bd-backend.exe` in the background
2. Waits for port `3000` to become active
3. Opens `Export2BD.exe`
4. When the frontend is closed, it automatically kills the backend process

---

## Security

- DB credentials are **never stored in plain text**
- They travel encrypted inside a **JWT signed with HS256**
- The token expires in 1 hour
- Each PostgreSQL connection is created dynamically and closed after 30 minutes of inactivity
- The backend only accepts connections from `localhost`

---

## Common Errors

| Error | Cause | Solution |
|-------|-------|----------|
| `Server connection error` | The backend is not running | Use `launcher.bat` instead of opening the .exe directly |
| `Invalid database credentials` | Incorrect credentials | Verify host, port, username, and password |
| `Missing columns` | The Excel file doesn't have the correct format | Use columns: `id_number, name, type, description` |
| `Already exists` | The `id_number` already exists in the DB | The record was previously imported |
| `Failed to launch JVM` | Incorrect Java runtime | Use the version packaged with Liberica Full JDK |

---

## Dynamic Connection

The backend does not require a fixed database configuration:

- Each user provides their own credentials from the frontend
- The backend creates a dynamic PostgreSQL connection per user
- Connections are cached for **30 minutes** for better performance
- Up to **50 simultaneous connections** are supported
- When the app is closed, all connections are closed automatically

---

## Roadmap

This version is functional but does not represent the final product. The following improvements are planned for future versions:

### 🧪 Testing
- Implement unit and integration tests in the backend with **NestJS + Jest**
- Implement frontend tests with **TestFX** for JavaFX

### 🗂️ Table selection
- Currently the app only supports the `controls` table, which in this case refers to **ISO 27001 security controls**
- Allow the user to select the target table from the frontend at import time

### 📁 More file formats
- Support for `.csv`
- Support for `.xls` (legacy Excel format)
- Support for other tabular formats

### 🖨️ Audit printing
- Generate printable reports with the result of each import
- Include date, user, records processed, successful and failed
- Useful for audit and traceability purposes
