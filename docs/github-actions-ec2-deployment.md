# GitHub Actions EC2 Deployment

## Que hace este pipeline

El workflow `.github/workflows/aws-ec2-deploy.yml` automatiza el despliegue hacia la instancia EC2 cuando hay un `push` a la rama `implementacion-aws` o cuando se ejecuta manualmente desde GitHub Actions.

El flujo tiene dos jobs:

1. `test-and-build`: compila y valida `api-gateway`, `auth-service`, `user-service`, `pet-service`, `report-service`, `match-service` y `notification-service` con `mvn -B clean package` usando Java 17 y cache de Maven.
2. `deploy-to-ec2`: se conecta por SSH a la EC2, actualiza el repositorio, construye las imagenes Docker una por una con `docker-compose.ec2.yml`, levanta los contenedores y valida que los 7 endpoints `/actuator/health` respondan con estado `UP`.

El workflow falla en cuanto detecta un secreto faltante, una compilacion rota, un problema de Docker, un error de despliegue o un servicio que no llega a estado saludable.

## Secrets que debes crear en GitHub

Crear estos secrets en el repositorio, en `Settings > Secrets and variables > Actions`:

- `EC2_HOST`: hostname o IP publica de la instancia EC2.
- `EC2_USER`: usuario SSH de la instancia. Para este entorno debe ser el usuario configurado en la maquina.
- `EC2_SSH_KEY`: contenido completo de `labsuser.pem` o de la clave privada equivalente que permite entrar por SSH a la EC2.

Importante:

- `EC2_SSH_KEY` debe contener el archivo completo, incluyendo las lineas `-----BEGIN ...-----` y `-----END ...-----`.
- No se usan credenciales AWS en el workflow de GitHub Actions.
- Las variables AWS reales permanecen solo en `/home/ubuntu/sanos-y-salvos/.env.ec2` dentro de la EC2.

## Como ejecutar el pipeline manualmente

1. Ir a la pestana `Actions` del repositorio.
2. Abrir el workflow `AWS EC2 Deploy`.
3. Hacer clic en `Run workflow`.
4. Elegir la rama `implementacion-aws`.
5. Ejecutar el workflow.

## Como verifica el despliegue

Despues de `docker compose --env-file .env.ec2 -f docker-compose.ec2.yml up -d`, el pipeline revisa con reintentos estos endpoints dentro de la EC2:

- `http://localhost:8080/actuator/health`
- `http://localhost:8081/actuator/health`
- `http://localhost:8082/actuator/health`
- `http://localhost:8083/actuator/health`
- `http://localhost:8084/actuator/health`
- `http://localhost:8085/actuator/health`
- `http://localhost:8086/actuator/health`

Cada endpoint debe devolver un JSON con `"status":"UP"`.

Si alguno falla, el workflow imprime:

- `docker compose ps`
- las ultimas lineas de `docker compose logs`

## Por que el build en EC2 es secuencial

La instancia es pequena y usa 2 GB de swap. Construir los 7 servicios al mismo tiempo puede agotar memoria y degradar el despliegue. Por eso el workflow ejecuta:

```bash
docker compose --env-file .env.ec2 -f docker-compose.ec2.yml build api-gateway
docker compose --env-file .env.ec2 -f docker-compose.ec2.yml build auth-service
docker compose --env-file .env.ec2 -f docker-compose.ec2.yml build user-service
docker compose --env-file .env.ec2 -f docker-compose.ec2.yml build pet-service
docker compose --env-file .env.ec2 -f docker-compose.ec2.yml build report-service
docker compose --env-file .env.ec2 -f docker-compose.ec2.yml build match-service
docker compose --env-file .env.ec2 -f docker-compose.ec2.yml build notification-service
```

Este enfoque reduce picos de consumo y hace el proceso mas estable en AWS Academy.

## Consideraciones sobre `.env.ec2`

- `.env.ec2` debe existir en `/home/ubuntu/sanos-y-salvos/.env.ec2` antes de ejecutar el deploy.
- El workflow valida su existencia y aborta con un mensaje claro si falta.
- El archivo esta fuera de Git y no debe sobrescribirse ni eliminarse durante el despliegue.
- Las credenciales temporales AWS de `.env.ec2` deben renovarse cada vez que AWS Academy reinicia o expira la sesion del laboratorio.

## Como verificar el resultado final

Cuando todo sale bien, el workflow muestra estos mensajes:

```text
Deployment completed successfully
All 7 services are healthy
```

Ademas, desde la EC2 puedes revisar manualmente:

```bash
docker compose --env-file .env.ec2 -f docker-compose.ec2.yml ps
docker compose --env-file .env.ec2 -f docker-compose.ec2.yml logs --tail=200
```
