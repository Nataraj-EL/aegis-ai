# API Specifications

This directory holds documentation regarding the REST APIs exposed by the Aegis AI backend, including OpenAPI specs, payload envelopes, and integration details.

## Core Rules
- All responses use the structured `ApiResponse` envelope:
  ```json
  {
    "success": true,
    "message": "Operation completed successfully",
    "data": { ... },
    "error": null
  }
  ```
- Endpoints return appropriate HTTP status codes (200 OK, 201 Created, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 500 Internal Server Error).
