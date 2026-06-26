import json
from pathlib import Path

from lambda_function import lambda_handler


def main():
    event_path = Path(__file__).with_name("test_event.json")
    with event_path.open("r", encoding="utf-8") as file:
        event = json.load(file)

    result = lambda_handler(event, None)
    print(json.dumps(result, indent=2))


if __name__ == "__main__":
    main()
