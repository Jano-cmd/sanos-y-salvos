import json
import socket
import unittest
import urllib.error
from email.message import Message
from unittest.mock import MagicMock, patch

import lambda_function


class LambdaFunctionTests(unittest.TestCase):
    def setUp(self):
        self.valid_record = {
            "messageId": "msg-1",
            "body": json.dumps(
                {
                    "reportId": 101,
                    "type": "LOST",
                    "description": "Perro perdido cerca del parque principal",
                    "createdAt": "2026-07-11T12:30:00Z",
                    "lat": 4.711,
                    "lng": -74.0721,
                    "imageUrls": ["https://example.com/dog-1.jpg"],
                }
            ),
        }

    @patch.dict(
        "lambda_function.os.environ",
        {"NOTIFICATION_SERVICE_URL": "http://example.com/notifications"},
        clear=True,
    )
    @patch("lambda_function.urllib.request.urlopen")
    def test_valid_message_posts_expected_payload(self, mock_urlopen):
        mock_response = MagicMock()
        mock_response.read.return_value = b'{"id":1}'
        mock_response.status = 201
        mock_urlopen.return_value.__enter__.return_value = mock_response

        result = lambda_function.lambda_handler({"Records": [self.valid_record]}, None)

        self.assertEqual({"batchItemFailures": []}, result)
        request = mock_urlopen.call_args.args[0]
        self.assertEqual("http://example.com/notifications", request.full_url)
        self.assertEqual("POST", request.get_method())
        self.assertEqual(
            {
                "reportId": 101,
                "type": "LOST",
                "description": "Perro perdido cerca del parque principal",
                "createdAt": "2026-07-11T12:30:00Z",
                "lat": 4.711,
                "lng": -74.0721,
                "imageUrls": ["https://example.com/dog-1.jpg"],
            },
            json.loads(request.data.decode("utf-8")),
        )

    @patch.dict(
        "lambda_function.os.environ",
        {"NOTIFICATION_SERVICE_URL": "http://example.com/notifications"},
        clear=True,
    )
    @patch("lambda_function.urllib.request.urlopen")
    def test_notification_service_response_201_is_treated_as_success(
        self, mock_urlopen
    ):
        mock_response = MagicMock()
        mock_response.read.return_value = b'{"message":"created"}'
        mock_response.status = 201
        mock_urlopen.return_value.__enter__.return_value = mock_response

        status_code, response_body = lambda_function.post_notification(
            {
                "reportId": 101,
                "type": "LOST",
                "description": "Perro perdido",
                "createdAt": "2026-07-11T12:30:00Z",
                "lat": 4.711,
                "lng": -74.0721,
                "imageUrls": [],
            }
        )

        self.assertEqual(201, status_code)
        self.assertEqual('{"message":"created"}', response_body)

    @patch.dict(
        "lambda_function.os.environ",
        {"NOTIFICATION_SERVICE_URL": "http://example.com/notifications"},
        clear=True,
    )
    @patch("lambda_function.urllib.request.urlopen")
    def test_invalid_json_body_returns_batch_failure(self, mock_urlopen):
        result = lambda_function.lambda_handler(
            {"Records": [{"messageId": "msg-invalid-json", "body": "not-json"}]},
            None,
        )

        self.assertEqual(
            {"batchItemFailures": [{"itemIdentifier": "msg-invalid-json"}]},
            result,
        )
        mock_urlopen.assert_not_called()

    @patch.dict(
        "lambda_function.os.environ",
        {"NOTIFICATION_SERVICE_URL": "http://example.com/notifications"},
        clear=True,
    )
    @patch("lambda_function.urllib.request.urlopen")
    def test_notification_service_response_500_returns_batch_failure(
        self, mock_urlopen
    ):
        mock_urlopen.side_effect = urllib.error.HTTPError(
            url="http://example.com/notifications",
            code=500,
            msg="Internal Server Error",
            hdrs=Message(),
            fp=MagicMock(read=MagicMock(return_value=b'{"error":"boom"}')),
        )

        result = lambda_function.lambda_handler({"Records": [self.valid_record]}, None)

        self.assertEqual({"batchItemFailures": [{"itemIdentifier": "msg-1"}]}, result)

    @patch.dict(
        "lambda_function.os.environ",
        {"NOTIFICATION_SERVICE_URL": "http://example.com/notifications"},
        clear=True,
    )
    @patch("lambda_function.urllib.request.urlopen")
    def test_timeout_or_network_error_returns_batch_failure(self, mock_urlopen):
        mock_urlopen.side_effect = socket.timeout("timed out")

        result = lambda_function.lambda_handler({"Records": [self.valid_record]}, None)

        self.assertEqual({"batchItemFailures": [{"itemIdentifier": "msg-1"}]}, result)


if __name__ == "__main__":
    unittest.main()
