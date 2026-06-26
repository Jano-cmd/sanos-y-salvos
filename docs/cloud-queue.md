# Cloud Queue

## Cola academica

La cola cloud propuesta para esta entrega se llama `sanos-report-queue`.

Su objetivo es permitir comunicacion asincrona entre microservicios, evitando que `report-service` dependa de una respuesta inmediata de `notification-service`.

## Flujo asincrono

- Productor: `report-service`
- Cola: `sanos-report-queue`
- Consumidor: funcion serverless
- Resultado: procesamiento de notificacion

## Descripcion del flujo

1. `report-service` genera un evento cuando se registra o procesa un reporte.
2. Ese evento se publica en la cola `sanos-report-queue`.
3. Una funcion serverless consume el mensaje desde la cola.
4. La funcion interpreta el contenido del reporte.
5. Como resultado, se procesa la notificacion que luego podria ser enviada a `notification-service` o a un proveedor externo.

## Beneficios academicos y tecnicos

- Desacopla al productor del consumidor.
- Permite procesamiento asincrono.
- Facilita reintentos ante fallos temporales.
- Representa un patron comun en despliegues cloud con colas como Amazon SQS, Azure Queue Storage o Google Pub/Sub.

## Ejemplo de mensaje JSON

```json
{
  "reportId": 101,
  "petName": "Luna",
  "location": "Bogota - Chapinero",
  "message": "Se reporto una mascota perdida cerca del parque principal"
}
```

## Relacion con la funcion serverless

La funcion serverless del directorio `serverless-notification-function/` simula el consumidor de esta cola. En un entorno cloud real, el evento podria llegar desde SQS, Service Bus o Pub/Sub y disparar automaticamente la ejecucion de la funcion.
