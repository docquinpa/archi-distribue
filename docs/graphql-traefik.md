# Exposition GraphQL via Traefik

Ce document decrit une exposition simple du endpoint GraphQL du service vehicules avec Traefik.

## Prerequis

- Traefik installe dans le cluster Kubernetes
- Service `vehicules` expose en interne sur le port `8081`
- Endpoint GraphQL actif: `POST /graphql`

## Exemple Ingress (Kubernetes standard)

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: vehicules-graphql-ingress
  namespace: locare
spec:
  ingressClassName: traefik
  rules:
    - host: api.locare.local
      http:
        paths:
          - path: /graphql
            pathType: Prefix
            backend:
              service:
                name: vehicules
                port:
                  number: 8081
```

## Test rapide

```bash
curl -X POST http://api.locare.local/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ vehicles { id vin dispo } }"}'
```

## Notes

- Le schema GraphQL du service vehicules est defini dans `services/vehicules/src/main/resources/graphql/schema.graphqls`.
- Si vous utilisez Traefik CRD (`IngressRoute`), gardez la meme logique de routage vers le service `vehicules:8081`.
