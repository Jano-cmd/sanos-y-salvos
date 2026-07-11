# EC2 Deployment

## Preparar variables de entorno

1. Crear el archivo local para EC2 a partir del ejemplo:

```bash
cp .env.ec2.example .env.ec2
```

2. Editar `.env.ec2` y completar las variables reales de AWS para `report-service`:

```env
AWS_REGION=us-east-1
AWS_SQS_QUEUE_URL=
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
AWS_SESSION_TOKEN=
```

`report-service` toma estas variables desde el entorno del contenedor. No hay credenciales hardcodeadas en el repositorio.

## Construir imagenes

```bash
docker compose --env-file .env.ec2 -f docker-compose.ec2.yml build
```

## Levantar el entorno

```bash
docker compose --env-file .env.ec2 -f docker-compose.ec2.yml up -d
```

## Revisar estado y logs

```bash
docker compose -f docker-compose.ec2.yml ps
docker compose -f docker-compose.ec2.yml logs
```

Para seguir logs de un servicio puntual:

```bash
docker compose -f docker-compose.ec2.yml logs -f api-gateway
```

## Probar endpoints con curl

Probar healthchecks:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8085/actuator/health
curl http://localhost:8086/actuator/health
```

Probar rutas a traves del gateway:

```bash
curl http://localhost:8080/auth/actuator/health
curl http://localhost:8080/users/actuator/health
curl http://localhost:8080/pets/actuator/health
curl http://localhost:8080/reports/actuator/health
curl http://localhost:8080/matches/actuator/health
curl http://localhost:8080/notifications/actuator/health
```

## Detener el entorno

```bash
docker compose -f docker-compose.ec2.yml down
```

## Notas de configuracion

- `api-gateway` usa el perfil `docker` dentro de contenedores para resolver `auth-service`, `user-service`, `pet-service`, `report-service`, `match-service` y `notification-service` por nombre.
- La configuracion base del gateway mantiene `localhost` para no romper la ejecucion local fuera de Docker.
- `docker-compose.yml` y `docker-stack.yml` se mantienen; solo se ajusto el gateway para que siga funcionando en entornos containerizados.
- La base de datos sigue siendo H2 en memoria para simplificar la entrega en EC2.
