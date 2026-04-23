# Smart Campus Sensor & Room Management API

**Module:** 5COSC022W – Client-Server Architectures  
**Student:** Ishara Hasanjan  
**Technology:** JAX-RS (Jersey 2.39.1) + Grizzly Embedded HTTP Server  
**Base URI:** `http://localhost:8080/api/v1`

---

## API Design Overview

This project implements a fully RESTful backend for the University of Westminster's **Smart Campus** initiative. It manages two primary resources — **Rooms** and **Sensors** — along with a nested sub-resource for **Sensor Readings**. The API is built exclusively using **JAX-RS (Jersey 2.x)** with an embedded **Grizzly HTTP server**, requiring no external servlet container.

### Key Design Decisions

- **In-memory storage** using `ConcurrentHashMap` — thread-safe, no database required
- **Singleton `DataStore`** shared across all JAX-RS request-scoped resource instances
- **Sub-resource locator pattern** for `SensorReadingResource` — clean separation of concerns
- **Dedicated `ExceptionMapper` per error scenario** — API never exposes Java stack traces
- **`ErrorResponse` POJO** used for all error bodies — consistent JSON structure across all errors
- **`ApiLoggingFilter`** handles request/response logging as a cross-cutting concern

### Pre-loaded Seed Data (available immediately on startup)

| Type | ID | Details |
|------|----|---------|
| Room | `LIB-301` | Library Quiet Study, capacity 50 |
| Room | `LAB-102` | Computer Science Lab, capacity 30 |
| Sensor | `TEMP-001` | Temperature, ACTIVE, in LIB-301 |
| Sensor | `CO2-001` | CO2, ACTIVE, in LIB-301 |
| Sensor | `OCC-001` | Occupancy, **MAINTENANCE**, in LAB-102 |

---

## Complete API Endpoint Reference

```
GET    /api/v1                               → Discovery endpoint (HATEOAS metadata)

GET    /api/v1/rooms                         → List all rooms
POST   /api/v1/rooms                         → Create a new room
GET    /api/v1/rooms/{roomId}                → Get room by ID
DELETE /api/v1/rooms/{roomId}                → Delete room (blocked if sensors assigned)

GET    /api/v1/sensors                       → List all sensors (optional ?type= filter)
POST   /api/v1/sensors                       → Register a new sensor
GET    /api/v1/sensors/{sensorId}            → Get sensor by ID

GET    /api/v1/sensors/{sensorId}/readings   → Get all readings for a sensor
POST   /api/v1/sensors/{sensorId}/readings   → Add a new reading for a sensor
```

### HTTP Status Code Reference

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 | OK | Successful GET |
| 201 | Created | Successful POST — includes `Location` header |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Missing required fields in request body |
| 403 | Forbidden | POST reading to a MAINTENANCE sensor |
| 404 | Not Found | URL path references a non-existent resource |
| 409 | Conflict | Duplicate ID on create, or DELETE room with active sensors |
| 422 | Unprocessable Entity | Request body references a non-existent resource (e.g. unknown `roomId`) |
| 500 | Internal Server Error | Any unhandled exception — stack trace never exposed |

---

## Project Structure

```
smart-campus-api/
├── pom.xml
├── .gitignore
└── src/main/java/com/smartcampus/
    ├── application/
    │   ├── Main.java                          ← Grizzly server entry point (port 8080)
    │   ├── SmartCampusApplication.java        ← JAX-RS @ApplicationPath("/api/v1")
    │   └── DataStore.java                     ← Thread-safe ConcurrentHashMap singleton
    ├── model/
    │   ├── Room.java                          ← Room POJO
    │   ├── Sensor.java                        ← Sensor POJO
    │   ├── SensorReading.java                 ← SensorReading POJO (UUID auto-generated)
    │   └── ErrorResponse.java                 ← Standard JSON error body POJO
    ├── resource/
    │   ├── DiscoveryResource.java             ← GET /api/v1 (HATEOAS)
    │   ├── RoomResource.java                  ← /api/v1/rooms
    │   ├── SensorResource.java                ← /api/v1/sensors
    │   └── SensorReadingResource.java         ← Sub-resource: /sensors/{id}/readings
    ├── exception/
    │   ├── ResourceNotFoundException.java             ← URL path resource missing
    │   ├── ResourceNotFoundExceptionMapper.java       ← → HTTP 404
    │   ├── RoomNotEmptyException.java                 ← DELETE room with sensors
    │   ├── RoomNotEmptyExceptionMapper.java           ← → HTTP 409
    │   ├── LinkedResourceNotFoundException.java       ← Body references missing resource
    │   ├── LinkedResourceNotFoundExceptionMapper.java ← → HTTP 422
    │   ├── SensorUnavailableException.java            ← POST reading to MAINTENANCE
    │   ├── SensorUnavailableExceptionMapper.java      ← → HTTP 403
    │   └── GlobalExceptionMapper.java                 ← Catch-all → HTTP 500
    └── filter/
        └── ApiLoggingFilter.java              ← Logs all requests and responses
```

---

## Build and Run Instructions

### Prerequisites

- **Java 11 or higher** — check with `java -version`
- **Apache Maven 3.6+** — check with `mvn -version`
- Internet connection (first build downloads dependencies automatically)

### Step 1 — Clone the repository

```bash
git clone https://github.com/Shara-Hasanjan/Smart_Campus_API.git
cd Smart_Campus_API
```

### Step 2 — Build the project

```bash
mvn clean package
```

This compiles all source files and produces a fat executable JAR at:
```
target/smart-campus-api-1.0.0.jar
```

### Step 3 — Start the server

```bash
java -jar target/smart-campus-api-1.0.0.jar
```

You should see in the console:
```
INFO: Smart Campus API started at http://localhost:8080/api/v1
INFO: Press CTRL+C to stop.
```

### Step 4 — Verify the server is running

Open a browser and go to:
```
http://localhost:8080/api/v1
```

You should see a JSON discovery response with all available resource links.

### Step 5 — Stop the server

Press `CTRL+C` in the terminal window.

### Alternative — Run directly from NetBeans

1. Open the project in NetBeans (File → Open Project → select the folder)
2. Right-click the project → **Run**
3. The server starts automatically on port 8080

---

## Sample curl Commands

### 1. Discovery — GET /api/v1

```bash
curl -s http://localhost:8080/api/v1
```

**Expected response (200 OK):**
```json
{
  "api": "Smart Campus Sensor and Room Management API",
  "version": "1.0.0",
  "timestamp": "2026-04-23T...",
  "resources": {
    "rooms":    { "href": "/api/v1/rooms",    "methods": "GET, POST" },
    "sensors":  { "href": "/api/v1/sensors",  "methods": "GET, POST" },
    "readings": { "href": "/api/v1/sensors/{sensorId}/readings", "methods": "GET, POST" }
  }
}
```

---

### 2. Create a Room — POST /api/v1/rooms

```bash
curl -s -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"ENG-201\",\"name\":\"Engineering Lab\",\"capacity\":40}"
```

**Expected response (201 Created):**
```json
{
  "id": "ENG-201",
  "name": "Engineering Lab",
  "capacity": 40,
  "sensorIds": []
}
```

---

### 3. Get All Rooms — GET /api/v1/rooms

```bash
curl -s http://localhost:8080/api/v1/rooms
```

**Expected response (200 OK):** JSON array containing LIB-301, LAB-102, and any created rooms.

---

### 4. Delete Room with Sensors — DELETE /api/v1/rooms/LIB-301

```bash
curl -s -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

**Expected response (409 Conflict):**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Room 'LIB-301' cannot be deleted. It currently has 2 sensor(s) assigned."
}
```

---

### 5. Register Sensor with Invalid roomId — POST /api/v1/sensors

```bash
curl -s -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"TEMP-999\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":0,\"roomId\":\"GHOST-99\"}"
```

**Expected response (422 Unprocessable Entity):**
```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "roomId 'GHOST-99' does not exist. Register the room first before assigning sensors to it."
}
```

---

### 6. Register Sensor with Valid roomId — POST /api/v1/sensors

```bash
curl -s -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"TEMP-999\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":0,\"roomId\":\"ENG-201\"}"
```

**Expected response (201 Created):**
```json
{
  "id": "TEMP-999",
  "type": "Temperature",
  "status": "ACTIVE",
  "currentValue": 0.0,
  "roomId": "ENG-201"
}
```

---

### 7. Filter Sensors by Type — GET /api/v1/sensors?type=CO2

```bash
curl -s "http://localhost:8080/api/v1/sensors?type=CO2"
```

**Expected response (200 OK):** JSON array containing only CO2-001.

---

### 8. Post Reading to MAINTENANCE Sensor — POST /api/v1/sensors/OCC-001/readings

```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\": 5.0}"
```

**Expected response (403 Forbidden):**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Sensor 'OCC-001' is currently under MAINTENANCE and cannot accept new readings."
}
```

---

### 9. Post a Valid Reading — POST /api/v1/sensors/TEMP-001/readings

```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\": 24.5}"
```

**Expected response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1745438400000,
  "value": 24.5
}
```

---

### 10. Verify currentValue Updated on Parent Sensor — GET /api/v1/sensors/TEMP-001

```bash
curl -s http://localhost:8080/api/v1/sensors/TEMP-001
```

**Expected response (200 OK):** `currentValue` should now be `24.5` (updated by the POST reading above).

---

### 11. Get Reading History — GET /api/v1/sensors/TEMP-001/readings

```bash
curl -s http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

**Expected response (200 OK):** JSON array of all readings posted to TEMP-001.

---

### 12. Get Non-Existent Resource — GET /api/v1/rooms/FAKE-999

```bash
curl -s http://localhost:8080/api/v1/rooms/FAKE-999
```

**Expected response (404 Not Found):**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Room not found with ID: FAKE-999"
}
```

---

