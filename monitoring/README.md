# Local Monitoring

Prometheus collects Spring Boot metrics and Grafana displays the provisioned
`Stack Overflow Prototype` dashboard.

## Start

Start the infrastructure:

```bash
docker compose up -d
```

Start the backend on the host:

```bash
cd backend
./mvnw spring-boot:run
```

Open:

- Spring metrics: http://localhost:8080/actuator/prometheus
- Prometheus: http://localhost:9090
- Prometheus target health: http://localhost:9090/targets
- Grafana: http://localhost:3000

Grafana's local default login is `admin` / `admin`. Override it before starting
Compose with `GRAFANA_ADMIN_USER` and `GRAFANA_ADMIN_PASSWORD` environment variables.

Run a JMeter test while the backend is running, then watch request rate, response
time, errors, JVM memory, CPU, and database connections on the Grafana dashboard.

Prometheus uses `host.docker.internal:8080` because the Spring Boot backend runs
on the host while Prometheus runs inside Docker.
