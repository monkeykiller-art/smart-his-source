# Kubernetes secrets

Secret manifests are intentionally excluded from version control. Create the secrets from environment variables in your shell or CI/CD secret store before deploying.

```bash
kubectl create secret generic his-app-secret --namespace his-app \\
  --from-literal=DB_PASSWORD="$DB_PASSWORD" \\
  --from-literal=NACOS_PASSWORD="$NACOS_PASSWORD" \\
  --from-literal=REDIS_PASSWORD="$REDIS_PASSWORD" \\
  --from-literal=HIS_JWT_SECRET="$HIS_JWT_SECRET"

kubectl create secret generic his-infra-secret --namespace his-infra \\
  --from-literal=POSTGRES_PASSWORD="$POSTGRES_PASSWORD" \\
  --from-literal=DB_PASSWORD="$DB_PASSWORD" \\
  --from-literal=NACOS_AUTH_TOKEN="$NACOS_AUTH_TOKEN" \\
  --from-literal=NACOS_AUTH_IDENTITY_KEY="$NACOS_AUTH_IDENTITY_KEY" \\
  --from-literal=NACOS_AUTH_IDENTITY_VALUE="$NACOS_AUTH_IDENTITY_VALUE"
```
