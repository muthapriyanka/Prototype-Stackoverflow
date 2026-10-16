variable "admin_ssh_cidr" {
  description = "Optional CIDR allowed to connect over SSH, such as 203.0.113.10/32. Leave null to use AWS Session Manager only."
  type        = string
  default     = null
  nullable    = true

  validation {
    condition     = var.admin_ssh_cidr == null || can(cidrnetmask(var.admin_ssh_cidr))
    error_message = "admin_ssh_cidr must be null or a valid CIDR block."
  }
}

variable "aws_account_id" {
  description = "AWS account allowed to run this deployment. Terraform refuses any other account."
  type        = string
  default     = "448895342297"

  validation {
    condition     = can(regex("^[0-9]{12}$", var.aws_account_id))
    error_message = "aws_account_id must contain exactly 12 digits."
  }
}

variable "aws_region" {
  description = "AWS region where the stack will be created."
  type        = string
  default     = "us-east-2"
}

variable "docker_compose_version" {
  description = "Docker Compose release installed on the EC2 instance."
  type        = string
  default     = "v5.6.0"
}

variable "docker_buildx_version" {
  description = "Docker Buildx release installed on the EC2 instance."
  type        = string
  default     = "v0.37.2"
}

variable "http_cidrs" {
  description = "CIDR blocks allowed to access the web application over HTTP."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "app_instance_count" {
  description = "Number of application instances registered with the load balancer."
  type        = number
  default     = 2

  validation {
    condition     = var.app_instance_count == 2
    error_message = "This learning topology currently expects exactly two application instances."
  }
}

variable "app_instance_type" {
  description = "EC2 type for each frontend/backend application node."
  type        = string
  default     = "t3.small"
}

variable "app_root_volume_size" {
  description = "Root volume size in GiB for each application node."
  type        = number
  default     = 20
}

variable "infrastructure_instance_type" {
  description = "EC2 type for the shared MySQL, Redis, Kafka, and Elasticsearch node."
  type        = string
  default     = "c7i-flex.large"
}

variable "infrastructure_root_volume_size" {
  description = "Root volume size in GiB for the shared infrastructure node."
  type        = number
  default     = 30
}

variable "instance_type" {
  description = "EC2 instance type. The full Kafka/Elasticsearch/MySQL stack needs substantially more memory than a small micro instance."
  type        = string
  default     = "m7i-flex.large"
}

variable "key_name" {
  description = "Optional existing EC2 key pair name. This is only needed when admin_ssh_cidr is set."
  type        = string
  default     = null
  nullable    = true
}

variable "project_name" {
  description = "Name prefix used for AWS resources and tags."
  type        = string
  default     = "prototype-stackoverflow"
}

variable "repository_branch" {
  description = "Git branch cloned by the EC2 bootstrap script."
  type        = string
  default     = "main"
}

variable "repository_url" {
  description = "Public Git repository containing the application."
  type        = string
  default     = "https://github.com/muthapriyanka/Prototype-Stackoverflow.git"

  validation {
    condition     = can(regex("^https://", var.repository_url))
    error_message = "repository_url must be an HTTPS Git URL that the EC2 instance can clone."
  }
}

variable "root_volume_size" {
  description = "Size in GiB of the encrypted EC2 root volume."
  type        = number
  default     = 40

  validation {
    condition     = var.root_volume_size >= 30
    error_message = "root_volume_size must be at least 30 GiB for Docker images and application data."
  }
}

variable "vpc_cidr" {
  description = "CIDR block for the application VPC."
  type        = string
  default     = "10.20.0.0/16"
}
