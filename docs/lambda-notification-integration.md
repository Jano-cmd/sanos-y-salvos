# Lambda notification integration

## Flujo completo

1. `POST /reports/lost` o `POST /reports/found` llega a `report-service`.
2. `report-service` guarda el reporte y publica a Amazon SQS en `sanos-report-queue`.
3. La Lambda `sanos-report-processor` recibe uno o varios `Records`.
4. La Lambda transforma cada mensaje al payload esperado por `notification-service`.
5. La Lambda hace `POST` a `notification-service` en `POST /notifications`.
6. `notification-service` persiste la notificacion en H2 y responde `201 Created`.
7. La Lambda registra en CloudWatch el body SQS, la URL destino, el codigo HTTP y la respuesta del microservicio.

## Endpoint real

- Servicio directo: `POST /notifications` en `notification-service` puerto `8086`.
- Ruta disponible en gateway: `POST /notifications` via `api-gateway` puerto `8080`.
- Configuracion actual del gateway:
  - local: `${NOTIFICATION_SERVICE_URL:http://localhost:8086}` en `api-gateway/src/main/resources/application.yml`
  - docker/ec2: `http://notification-service:8086` en `api-gateway/src/main/resources/application-docker.yml`

## Payload esperado por notification-service

```json
{
  "reportId": 101,
  "type": "LOST",
  "description": "Perro perdido cerca del parque principal",
  "createdAt": "2026-07-11T12:30:00Z",
  "lat": 4.711,
  "lng": -74.0721,
  "imageUrls": [
    "https://example.com/reports/101-1.jpg"
  ]
}
```

## Variable de entorno

- Nombre: `NOTIFICATION_SERVICE_URL`
- La Lambda no hardcodea IP ni puerto.
- Valor recomendado si Lambda consume por gateway publico:

```text
http://<EC2_PUBLIC_IP>:8080/notifications
```

- Valor alternativo si expones el servicio directamente:

```text
http://<EC2_PUBLIC_IP>:8086/notifications
```

## Como actualizar NOTIFICATION_SERVICE_URL cuando cambie la IP de EC2

1. En AWS Academy identifica la nueva IP publica de la instancia EC2.
2. En AWS Console entra a `Lambda` > `sanos-report-processor`.
3. Abre `Configuration` > `Environment variables`.
4. Edita `NOTIFICATION_SERVICE_URL` con la nueva IP.
5. Guarda los cambios y prueba un nuevo mensaje en SQS.

## Como desplegar el codigo en Lambda

1. Desde `serverless-notification-function` crea un zip con `lambda_function.py`.
2. En AWS Console entra a `Lambda` > `sanos-report-processor`.
3. En `Code` elige `Upload from` > `.zip file`.
4. Sube el zip y confirma.
5. Verifica que el handler siga siendo `lambda_function.lambda_handler`.
6. Guarda y publica si usas versiones.

Ejemplo en PowerShell:

```powershell
Compress-Archive -Path .\serverless-notification-function\lambda_function.py -DestinationPath .\serverless-notification-function\lambda_function.zip -Force
```

## Como probarlo desde SQS

1. Abre la cola `sanos-report-queue`.
2. Envia un mensaje con el JSON del payload real que publica `report-service`.
3. O invoca `POST /reports/lost` para que `report-service` publique automaticamente.
4. Confirma que la Lambda procese el mensaje y que no quede visible en la cola tras un procesamiento exitoso.

## Como comprobarlo en CloudWatch

1. Abre `CloudWatch` > `Log groups`.
2. Entra al grupo de logs de `sanos-report-processor`.
3. Revisa que aparezcan:
   - `Received SQS message body:`
   - `Posting notification to URL:`
   - `notification-service HTTP status:`
   - `notification-service response body:`
   - `Failed to process messageId=...` cuando aplique

## Como comprobar que notification-service recibio la notificacion

1. Llama `GET http://<EC2_PUBLIC_IP>:8086/notifications` o `GET http://<EC2_PUBLIC_IP>:8080/notifications`.
2. Verifica que exista una entrada con `reportId`, `type`, `description`, `createdAt`, `lat`, `lng` e `imageUrls`.
3. Si necesitas una sola notificacion, usa `GET /notifications/{id}`.

## Reintentos y DLQ

- La Lambda devuelve `batchItemFailures` para que SQS solo reintente los mensajes fallidos del lote.
- Fallan y se reintentan los mensajes con:
  - JSON invalido
  - campos requeridos faltantes
  - respuesta HTTP `4xx` o `5xx`
  - timeout o error de red
  - falta de `NOTIFICATION_SERVICE_URL`
- Cuando un mensaje supera el maximo de reintentos configurado en la cola fuente, SQS lo mueve a `sanos-report-dlq`.
