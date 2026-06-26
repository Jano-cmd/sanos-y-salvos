import json


def lambda_handler(event, context):
    records = event.get("Records", [])

    for record in records:
        body = record.get("body", "{}")
        payload = json.loads(body)

        report_id = payload.get("reportId")
        pet_name = payload.get("petName")
        location = payload.get("location")
        message = payload.get("message")

        print(f"Processing reportId={report_id}")
        print(f"Pet name: {pet_name}")
        print(f"Location: {location}")
        print(f"Message: {message}")

    return {
        "statusCode": 200,
        "body": json.dumps({"message": "Notification messages processed successfully"}),
    }
