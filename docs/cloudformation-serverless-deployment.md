# CloudFormation serverless deployment

## Objetivo

Esta entrega agrega infraestructura como codigo para crear una cola SQS, su DLQ, una Lambda y la relacion SQS -> Lambda sin tocar los recursos manuales que ya funcionan en AWS Academy.

## Recursos manuales vs recursos CloudFormation

- Recursos manuales actuales: la cola `sanos-report-queue`, la DLQ `sanos-report-dlq`, la Lambda `sanos-report-processor` y su trigger pueden seguir existiendo sin cambios.
- Recursos CloudFormation: la plantilla crea un segundo conjunto aislado con prefijo por ambiente para evitar conflictos de nombre y evitar que CloudFormation intente adoptar recursos manuales existentes.
- La plantilla no elimina ni modifica automaticamente los recursos manuales ya creados fuera del stack.
- En AWS Academy no se crean recursos IAM desde esta plantilla porque `LabRole` suele bloquear `iam:PutRolePolicy` e `iam:DeleteRolePolicy`.

## Por que se usan nombres `student-*`

CloudFormation no puede tomar control de recursos manuales con el mismo nombre. Por eso la plantilla usa `EnvironmentName=student` por defecto y genera estos nombres:

- `student-sanos-report-dlq`
- `student-sanos-report-queue`
- `student-sanos-report-processor`

Si necesitas otro prefijo, cambia el parametro `EnvironmentName` al desplegar.

## Estructura creada

- plantilla: `infrastructure/cloudformation/serverless-stack.yml`
- script PowerShell: `scripts/package-lambda.ps1`
- script Bash: `scripts/package-lambda.sh`

## Crear un bucket S3 temporal para el ZIP

Necesitas un bucket S3 donde subir el paquete de Lambda antes de desplegar CloudFormation.

Ejemplo con AWS CLI:

```bash
aws s3 mb s3://student-sanos-lambda-artifacts --region us-east-1
```

Si el bucket ya existe y es tuyo, reutilizalo. El nombre debe ser globalmente unico en S3.

## Empaquetar la Lambda

PowerShell:

```powershell
.\scripts\package-lambda.ps1
```

Bash:

```bash
bash ./scripts/package-lambda.sh
```

Los scripts:

- crean una carpeta temporal limpia
- copian solo `lambda_function.py`
- generan `serverless-notification-function/sanos-report-processor.zip`
- excluyen `test_lambda.py`, `__pycache__`, `.pyc` y cualquier secreto
- validan que `lambda_function.py` quede en la raiz del zip

## Subir el ZIP a S3

Ejemplo:

```bash
aws s3 cp serverless-notification-function/sanos-report-processor.zip s3://student-sanos-lambda-artifacts/lambda/serverless/sanos-report-processor.zip
```

## Encontrar el ARN de LabRole

En AWS Academy normalmente puedes usar el rol existente `LabRole`.

La plantilla reutiliza ese rol sin modificarlo. No crea `AWS::IAM::Policy`, no crea `AWS::IAM::Role` y no adjunta politicas inline ni administradas desde CloudFormation.

Opciones para encontrar el ARN:

1. AWS Console -> IAM -> Roles -> `LabRole`
2. AWS CLI:

```bash
aws iam get-role --role-name LabRole --query 'Role.Arn' --output text
```

Ejemplo esperado:

```text
arn:aws:iam::123456789012:role/LabRole
```

## Crear el stack desde la consola AWS

1. Abre CloudFormation en `us-east-1`.
2. Selecciona `Create stack` -> `With new resources (standard)`.
3. Sube `infrastructure/cloudformation/serverless-stack.yml`.
4. Usa como stack name: `student-sanos-serverless`.
5. Ingresa estos parametros:
   - `EnvironmentName`: `student`
   - `NotificationServiceUrl`: `http://<EC2_PUBLIC_IP>:8080/notifications`
   - `LambdaExecutionRoleArn`: ARN de `LabRole`
   - `LambdaCodeS3Bucket`: bucket donde subiste el zip
   - `LambdaCodeS3Key`: key del zip en S3
6. Revisa el cambio y crea el stack.

## Crear o actualizar el stack con AWS CLI

Validar plantilla:

```bash
aws cloudformation validate-template --template-body file://infrastructure/cloudformation/serverless-stack.yml
```

Crear o actualizar:

```bash
aws cloudformation deploy \
  --region us-east-1 \
  --stack-name student-sanos-serverless \
  --template-file infrastructure/cloudformation/serverless-stack.yml \
  --parameter-overrides \
    EnvironmentName=student \
    NotificationServiceUrl=http://<EC2_PUBLIC_IP>:8080/notifications \
    LambdaExecutionRoleArn=arn:aws:iam::123456789012:role/LabRole \
    LambdaCodeS3Bucket=student-sanos-lambda-artifacts \
    LambdaCodeS3Key=lambda/serverless/sanos-report-processor.zip
```

## Parametros necesarios

- `EnvironmentName`
- `NotificationServiceUrl`
- `LambdaExecutionRoleArn`
- `LambdaCodeS3Bucket`
- `LambdaCodeS3Key`

## GitHub Secrets necesarios

El workflow manual `.github/workflows/aws-cloudformation-deploy.yml` requiere estos secrets:

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_SESSION_TOKEN`
- `AWS_REGION`
- `LAMBDA_EXECUTION_ROLE_ARN`
- `LAMBDA_CODE_S3_BUCKET`
- `NOTIFICATION_SERVICE_URL`

La key S3 del ZIP no se guarda como secret porque el workflow la genera automaticamente con `github.sha`.

## Obtener los Outputs del stack

```bash
aws cloudformation describe-stacks \
  --region us-east-1 \
  --stack-name student-sanos-serverless \
  --query 'Stacks[0].Outputs' \
  --output table
```

Outputs esperados:

- `ReportQueueUrl`
- `ReportQueueArn`
- `DeadLetterQueueUrl`
- `DeadLetterQueueArn`
- `LambdaFunctionArn`
- `LambdaFunctionName`

## Actualizar `AWS_SQS_QUEUE_URL` en `.env.ec2`

Despues del despliegue toma el `ReportQueueUrl` del stack y actualiza `AWS_SQS_QUEUE_URL` en el archivo `.env.ec2` del servidor EC2.

Ejemplo:

```text
AWS_SQS_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/123456789012/student-sanos-report-queue
```

No subas `.env.ec2` al repositorio ni pongas valores reales en archivos versionados.

## Recrear `report-service` en EC2

Una vez actualizado `.env.ec2`, recrea `report-service` para que tome la nueva cola:

```bash
docker compose --env-file .env.ec2 -f docker-compose.ec2.yml up -d --build report-service
```

Si quieres verificar el estado:

```bash
docker compose --env-file .env.ec2 -f docker-compose.ec2.yml ps report-service
```

## Probar el flujo completo

1. Despliega el stack CloudFormation.
2. Actualiza `AWS_SQS_QUEUE_URL` en EC2 con el output del stack.
3. Recrea `report-service`.
4. Verifica que `NOTIFICATION_SERVICE_URL` en la Lambda apunte al API Gateway de EC2, por ejemplo `http://<EC2_PUBLIC_IP>:8080/notifications`.
5. Envia un `POST /reports/lost` al sistema desplegado en EC2.
6. Verifica:
   - mensaje en la nueva cola `student-sanos-report-queue`
   - ejecucion de `student-sanos-report-processor`
   - logs en CloudWatch
   - notificacion creada en `GET http://<EC2_PUBLIC_IP>:8080/notifications`

## Eliminar el stack sin tocar recursos manuales

Como los recursos del stack usan prefijos `student-*`, puedes borrar solo los recursos creados por CloudFormation sin afectar la infraestructura manual original.

```bash
aws cloudformation delete-stack --region us-east-1 --stack-name student-sanos-serverless
```

Luego puedes esperar a que termine:

```bash
aws cloudformation wait stack-delete-complete --region us-east-1 --stack-name student-sanos-serverless
```

## Advertencias sobre AWS Academy

- `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` y `AWS_SESSION_TOKEN` expiran cada vez que reinicias Learner Lab.
- Debes actualizar esos valores en tu terminal local y en GitHub Secrets cada nueva sesion.
- No uses credenciales permanentes.
- No intentes crear usuarios IAM.
- AWS Academy suele bloquear `iam:PutRolePolicy`, por eso este stack no intenta modificar `LabRole`.
- `LabRole` debe tener previamente permisos suficientes para SQS y CloudWatch Logs para que la Lambda funcione correctamente.
- Si `LabRole` no tiene permisos suficientes, debes usar un rol existente ya autorizado por el laboratorio; CloudFormation no intentara crearlo ni ampliarlo.
