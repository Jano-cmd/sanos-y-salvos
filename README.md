# Sanos y Salvos

## Descripcion general

Sanos y Salvos es un sistema academico basado en microservicios con Spring Boot orientado a la gestion de reportes y notificaciones relacionadas con mascotas. El proyecto fue preparado para demostrar una arquitectura distribuida con despliegue local en Docker Compose, despliegue orquestado con Docker Swarm, automatizacion CI/CD con GitHub Actions, un API Gateway para acceso centralizado y un flujo cloud asincrono con cola y funcion serverless.

## Arquitectura de microservicios

El sistema esta compuesto por 7 servicios independientes:

- `api-gateway`: punto de entrada externo al sistema
- `auth-service`: autenticacion
- `user-service`: gestion de usuarios
- `pet-service`: gestion de mascotas
- `report-service`: gestion de reportes
- `match-service`: logica de coincidencias
- `notification-service`: procesamiento de notificaciones

Arquitectura general:

```text
Cliente externo
      |
      v
API Gateway (8080)
      |
      +--> auth-service (8081)
      +--> user-service (8082)
      +--> pet-service (8083)
      +--> report-service (8084) --> sanos-report-queue --> funcion serverless --> notificacion procesada
      +--> match-service (8085)
      +--> notification-service (8086)
```

## Servicios y puertos

| Servicio | Puerto |
|---|---:|
| `api-gateway` | 8080 |
| `auth-service` | 8081 |
| `user-service` | 8082 |
| `pet-service` | 8083 |
| `report-service` | 8084 |
| `match-service` | 8085 |
| `notification-service` | 8086 |

## API Gateway

El API Gateway centraliza el acceso externo al sistema y evita exponer directamente cada microservicio al cliente. Su funcion es enrutar las peticiones entrantes hacia el servicio correspondiente.

Rutas configuradas:

- `/auth/**` -> `auth-service:8081`
- `/users/**` -> `user-service:8082`
- `/pets/**` -> `pet-service:8083`
- `/reports/**` -> `report-service:8084`
- `/matches/**` -> `match-service:8085`
- `/notifications/**` -> `notification-service:8086`

Archivo principal del gateway:

- `api-gateway/src/main/java/com/example/apigateway/config/GatewayConfig.java`

## Docker local

### 1. Construir las imagenes

```powershell
docker compose build
```

### 2. Levantar el entorno local

```powershell
docker compose up
```

### 3. Ver contenedores en ejecucion

```powershell
docker ps
```

### 4. Verificar salud de los servicios

```powershell
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8085/actuator/health
curl http://localhost:8086/actuator/health
```

### 5. Detener el entorno local

```powershell
docker compose down
```

Archivo principal:

- `docker-compose.yml`

## Docker Swarm

### 1. Iniciar Swarm

```powershell
docker swarm init
```

O con script:

```powershell
.\scripts\swarm-init.ps1
```

### 2. Desplegar el stack

Antes de desplegar, construir imagenes locales:

```powershell
docker compose build
docker stack deploy -c docker-stack.yml sanos
```

O con script:

```powershell
.\scripts\swarm-deploy.ps1
```

### 3. Ver servicios desplegados

```powershell
docker service ls
```

Opcionalmente:

```powershell
docker stack services sanos
```

### 4. Escalar replicas

Escalar `report-service` a 4 replicas:

```powershell
docker service scale sanos_report-service=4
```

Volver a 2 replicas:

```powershell
docker service scale sanos_report-service=2
```

O con script:

```powershell
.\scripts\swarm-scale.ps1
```

### 5. Eliminar el stack

```powershell
docker stack rm sanos
```

O con script:

```powershell
.\scripts\swarm-remove.ps1
```

Archivos relacionados:

- `docker-stack.yml`
- `scripts/swarm-init.ps1`
- `scripts/swarm-deploy.ps1`
- `scripts/swarm-scale.ps1`
- `scripts/swarm-remove.ps1`

## CI/CD con GitHub Actions

El pipeline CI/CD se encuentra en:

- `.github/workflows/ci-cd.yml`

Se ejecuta automaticamente en:

- `push` hacia `main` y `dev`
- `pull_request` hacia `main` y `dev`

El workflow realiza:

- pruebas Maven por microservicio
- build Maven por microservicio
- construccion de imagen Docker por microservicio
- etapa `deploy-simulation` para simular despliegue academico

La simulacion de despliegue incluye mensajes sobre:

- configuracion de API Gateway
- configuracion de Docker Swarm
- configuracion de cola cloud
- configuracion de funcion serverless
- despliegue de microservicios

Para revisar el pipeline:

- abrir el repositorio en GitHub
- entrar en la pestana `Actions`
- seleccionar el workflow `CI-CD`

## Cola cloud

Para la parte cloud de la entrega se documenta una cola llamada `sanos-report-queue`.

- Productor: `report-service`
- Consumidor: funcion serverless
- Proposito: comunicacion asincrona entre servicios

La idea es que `report-service` publique eventos en la cola y una funcion serverless procese esos mensajes para desencadenar la logica de notificacion sin acoplar directamente ambos componentes.

Documentacion relacionada:

- `docs/cloud-queue.md`

## Funcion serverless

La funcion serverless de ejemplo se encuentra en:

- `serverless-notification-function/lambda_function.py`

Su responsabilidad es procesar mensajes provenientes de la cola, leer `event["Records"]`, interpretar cada `record["body"]` como JSON y registrar en logs los datos del reporte.

Esta implementacion es una simulacion academica y documentada. No usa credenciales reales ni despliega de verdad a AWS, Azure o GCP.

Documentacion relacionada:

- `serverless-notification-function/README.md`

## Flujo end-to-end

Flujo general del sistema:

1. Un cliente externo envia una peticion al sistema.
2. El `api-gateway` recibe y enruta la solicitud.
3. La solicitud llega a `report-service`.
4. `report-service` genera un evento asincrono.
5. El evento se publica en `sanos-report-queue`.
6. La funcion serverless consume el mensaje desde la cola.
7. La funcion procesa la informacion y deja la notificacion procesada.

Este flujo demuestra integracion entre entrada centralizada, microservicios, mensajeria asincrona y procesamiento serverless.

## Decisiones tecnicas

- **Escalabilidad**: Docker Swarm permite aumentar replicas de servicios como `report-service` para soportar mas carga.
- **Disponibilidad**: el uso de replicas y politicas de reinicio mejora tolerancia a fallos en el entorno orquestado.
- **Mantenibilidad**: cada microservicio tiene responsabilidades separadas y puede evolucionar de forma independiente.
- **Separacion de responsabilidades**: el gateway centraliza acceso, los microservicios encapsulan logica de dominio y la funcion serverless maneja el procesamiento asincrono.

## Limitaciones

- La cola cloud y la funcion serverless estan implementadas como ejemplo academico y documentado, sin conexion real a AWS, Azure o GCP.
- No se incluyen credenciales reales por seguridad.
- La etapa `deploy-simulation` de GitHub Actions simula despliegue y no ejecuta despliegues reales a nube.

## Archivos principales

- `docker-compose.yml`: entorno local con Docker Compose
- `docker-stack.yml`: despliegue con Docker Swarm
- `.github/workflows/ci-cd.yml`: pipeline CI/CD academico
- `docs/cloud-queue.md`: documentacion de la cola cloud
- `serverless-notification-function/lambda_function.py`: funcion serverless de ejemplo
- `serverless-notification-function/README.md`: documentacion de la funcion serverless
- `scripts/swarm-init.ps1`: inicializa Docker Swarm
- `scripts/swarm-deploy.ps1`: despliega el stack
- `scripts/swarm-scale.ps1`: escala `report-service`
- `scripts/swarm-remove.ps1`: elimina el stack
