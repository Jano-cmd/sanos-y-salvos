# Serverless Notification Function

## Proposito

Esta funcion simula una AWS Lambda que recibe mensajes desde una cola cloud y procesa la informacion necesaria para generar o preparar una notificacion.

## Relacion con la cola

- Productor: `report-service`
- Cola: `sanos-report-queue`
- Consumidor: esta funcion serverless
- Resultado: procesamiento de una notificacion basada en el reporte recibido

## Ejemplo de evento de prueba

```json
{
  "Records": [
    {
      "body": "{\"reportId\":101,\"petName\":\"Luna\",\"location\":\"Bogota - Chapinero\",\"message\":\"Se reporto una mascota perdida cerca del parque principal\"}"
    }
  ]
}
```

## Funcionamiento

La funcion recorre `event["Records"]`, toma cada `record["body"]`, lo interpreta como JSON y muestra en logs los datos del reporte para simular el procesamiento de la notificacion.

## Despliegue conceptual

- AWS Lambda: se puede vincular a Amazon SQS para ejecucion automatica al llegar mensajes.
- Azure Functions: se puede conectar a Azure Queue Storage o Service Bus.
- Google Cloud Functions: se puede activar desde Pub/Sub.

## Uso academico

Este ejemplo no usa credenciales reales ni servicios cloud activos. Su objetivo es demostrar el patron cola-consumidor-serverless solicitado en la entrega.
