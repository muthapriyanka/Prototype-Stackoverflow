# AWS EC2 Deployment with Terraform

This Terraform module creates a small multi-instance learning deployment:

- An Application Load Balancer exposed on HTTP port 80
- Two `t3.small` application nodes running the React frontend and Spring Boot API
- One `c7i-flex.large` infrastructure node running MySQL, Redis, Kafka, and Elasticsearch
- Two public subnets in separate availability zones
- Security groups that allow infrastructure ports only from the application nodes
- AWS Systems Manager Session Manager access, so SSH does not need to be public
- Encrypted EBS root volumes and generated database passwords

The infrastructure services are shared. Adding another application node does
not create another database, cache, Kafka broker, or Elasticsearch instance.

This deployment creates billable AWS resources, including an Application Load
Balancer. Check AWS Billing and Cost Explorer, and run `terraform destroy` when
you finish learning.

## Architecture

```text
Internet
   |
Application Load Balancer
   |----------------------|
app-1 (t3.small)     app-2 (t3.small)
   |----------------------|
       private VPC traffic
                |
infrastructure (c7i-flex.large)
MySQL | Redis | Kafka | Elasticsearch
```

## Deploy

```bash
cd infra/aws
cp terraform.tfvars.example terraform.tfvars
AWS_PROFILE=prototype-new terraform init
terraform fmt -check
terraform validate
AWS_PROFILE=prototype-new terraform plan
AWS_PROFILE=prototype-new terraform apply
```

Terraform prints the load-balanced application URL and all three instance IDs.
First boot can take several minutes while Docker images are built.

## Verify

```bash
APP_URL=$(AWS_PROFILE=prototype-new terraform output -raw application_url)
curl -s "$APP_URL/api/actuator/health"

AWS_PROFILE=prototype-new aws elbv2 describe-target-health \
  --region us-east-2 \
  --target-group-arn $(AWS_PROFILE=prototype-new aws elbv2 describe-target-groups \
    --region us-east-2 \
    --names prototype-stackoverflow-app \
    --query 'TargetGroups[0].TargetGroupArn' \
    --output text)
```

Open a shell without exposing SSH:

```bash
AWS_PROFILE=prototype-new terraform output -json session_manager_commands
```

On the infrastructure node:

```bash
cd /opt/stackoverflow
sudo docker compose --env-file .env.infrastructure \
  -f docker-compose.infrastructure.yml ps
```

On either application node:

```bash
cd /opt/stackoverflow/app
sudo docker compose --env-file .env.app -f docker-compose.app.yml ps
```

## Instance Sizing

The two application nodes have 2 GiB RAM and a 2 GiB swap file. The shared
infrastructure node has 4 GiB RAM. These sizes are suitable for this learning
project, not a production workload with real traffic or high availability.

## Existing All-in-One Instance

During migration, Terraform preserves the previous all-in-one instance as
`prototype-stackoverflow-legacy-all-in-one`. Remove it only after deciding
whether its MySQL data must be migrated to the new shared infrastructure node.

## Destroy

```bash
cd infra/aws
AWS_PROFILE=prototype-new terraform destroy
```

This removes resources managed by this Terraform state. The current deployment
uses HTTP only; a public production system should add a domain, ACM certificate,
HTTPS listener, backups, private subnets/NAT strategy, and managed data services.
