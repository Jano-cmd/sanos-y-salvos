import json
import os
import socket
import urllib.error
import urllib.request


DEFAULT_TIMEOUT_SECONDS = 5
NOTIFICATION_SERVICE_URL_ENV = "NOTIFICATION_SERVICE_URL"
REQUIRED_FIELDS = (
    "reportId",
    "type",
    "description",
    "createdAt",
    "lat",
    "lng",
    "imageUrls",
)


def lambda_handler(event, context):
    records = event.get("Records", [])
    failures = []

    for record in records:
        message_id = record.get("messageId", "unknown-message-id")

        try:
            process_record(record)
        except Exception as error:
            print(f"Failed to process messageId={message_id}: {error}")
            failures.append({"itemIdentifier": message_id})

    return {"batchItemFailures": failures}


def process_record(record):
    body = record.get("body", "{}")
    print(f"Received SQS message body: {body}")

    message = json.loads(body)
    payload = build_notification_payload(message)
    response_status, response_body = post_notification(payload)

    print(f"notification-service HTTP status: {response_status}")
    print(f"notification-service response body: {response_body}")


def build_notification_payload(message):
    missing_fields = [field for field in REQUIRED_FIELDS if field not in message]
    if missing_fields:
        raise ValueError(
            f"Missing required fields in SQS message: {', '.join(missing_fields)}"
        )

    return {
        "reportId": message["reportId"],
        "type": message["type"],
        "description": message["description"],
        "createdAt": message["createdAt"],
        "lat": message["lat"],
        "lng": message["lng"],
        "imageUrls": message["imageUrls"] or [],
    }


def post_notification(payload):
    url = get_notification_service_url()
    print(f"Posting notification to URL: {url}")

    request = urllib.request.Request(
        url=url,
        data=json.dumps(payload).encode("utf-8"),
        headers={"Content-Type": "application/json"},
        method="POST",
    )

    try:
        with urllib.request.urlopen(
            request, timeout=DEFAULT_TIMEOUT_SECONDS
        ) as response:
            response_body = response.read().decode("utf-8")
            status_code = getattr(response, "status", response.getcode())
    except urllib.error.HTTPError as error:
        error_body = error.read().decode("utf-8", errors="replace")
        print(f"notification-service HTTP error body: {error_body}")
        raise RuntimeError(
            f"notification-service returned HTTP {error.code}: {error_body}"
        ) from error
    except (urllib.error.URLError, TimeoutError, socket.timeout) as error:
        raise RuntimeError(f"notification-service request failed: {error}") from error

    if status_code < 200 or status_code >= 300:
        raise RuntimeError(
            f"notification-service returned unexpected HTTP {status_code}: {response_body}"
        )

    return status_code, response_body


def get_notification_service_url():
    url = os.environ.get(NOTIFICATION_SERVICE_URL_ENV, "").strip()
    if not url:
        raise RuntimeError(
            f"Environment variable {NOTIFICATION_SERVICE_URL_ENV} is required"
        )
    return url
