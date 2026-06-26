# Sanos y Salvos

Este repositorio contiene 7 microservicios Spring Boot y dos modos de despliegue:

- `docker-compose.yml` para pruebas locales con `docker compose up`
- `docker-stack.yml` para Docker Swarm con `docker stack deploy`

## Servicios

- `api-gateway`
- `auth-service`
- `user-service`
- `pet-service`
- `report-service`
- `match-service`
- `notification-service`

## Docker Compose local

Construir imagenes:

```powershell
docker compose build
```

Levantar entorno local:

```powershell
docker compose up
```

## Docker Swarm

### 1. Iniciar Swarm

```powershell
docker swarm init
```

O usando el script:

```powershell
.\scripts\swarm-init.ps1
```

### 2. Desplegar el stack

Antes de desplegar, asegurese de haber construido las imagenes locales con `docker compose build`.

```powershell
docker stack deploy -c docker-stack.yml sanos
```

O usando el script:

```powershell
.\scripts\swarm-deploy.ps1
```

### 3. Ver servicios desplegados

```powershell
docker service ls
```

Para ver solo los servicios del stack:

```powershell
docker stack services sanos
```

### 4. Escalar replicas

Ejemplo para escalar `report-service` a 4 replicas:

```powershell
docker service scale sanos_report-service=4
```

Volver `report-service` a 2 replicas:

```powershell
docker service scale sanos_report-service=2
```

O usando el script:

```powershell
.\scripts\swarm-scale.ps1
```

### 5. Eliminar el stack

```powershell
docker stack rm sanos
```

O usando el script:

```powershell
.\scripts\swarm-remove.ps1
```

## Archivos principales

- `docker-compose.yml`: entorno local
- `docker-stack.yml`: despliegue Docker Swarm
- `scripts/swarm-init.ps1`: inicializa Swarm
- `scripts/swarm-deploy.ps1`: despliega el stack
- `scripts/swarm-scale.ps1`: escala `report-service`
- `scripts/swarm-remove.ps1`: elimina el stack
