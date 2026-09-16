// Update README or documentation to include new endpoint
// (In practice this could be added to an API spec file.)

# API – Latest Version

Endpoint: **GET** `/api/latest-version?app_id={id}`

* **Parameters**:
  - `app_id` – The application's unique identifier.

* **Response**:
  - `200 OK` – Plain‑text body containing the latest version string.
  - `400 Bad Request` – Missing or unknown `app_id`.

Example: `GET https://example.com/api/latest-version?app_id=com.benzjeremy.wetter` → `1.2`
