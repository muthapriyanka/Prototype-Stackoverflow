resource "aws_instance" "app" {
  ami                         = data.aws_ssm_parameter.amazon_linux_2023.value
  instance_type               = var.instance_type
  subnet_id                   = aws_subnet.public.id
  vpc_security_group_ids      = [aws_security_group.app.id]
  associate_public_ip_address = true
  iam_instance_profile        = aws_iam_instance_profile.ec2.name
  key_name                    = var.key_name

  user_data = templatefile("${path.module}/user-data.sh.tftpl", {
    docker_buildx_version  = var.docker_buildx_version
    docker_compose_version = var.docker_compose_version
    repository_branch      = var.repository_branch
    repository_url         = var.repository_url
  })

  user_data_replace_on_change = false

  lifecycle {
    ignore_changes = [user_data, user_data_replace_on_change]
  }

  metadata_options {
    http_endpoint = "enabled"
    http_tokens   = "required"
  }

  root_block_device {
    encrypted   = true
    volume_size = var.root_volume_size
    volume_type = "gp3"
  }

  depends_on = [aws_route_table_association.public]

  tags = {
    Name = "${var.project_name}-legacy-all-in-one"
  }
}

resource "aws_instance" "infrastructure" {
  ami                         = data.aws_ssm_parameter.amazon_linux_2023.value
  instance_type               = var.infrastructure_instance_type
  subnet_id                   = aws_subnet.public.id
  private_ip                  = cidrhost(aws_subnet.public.cidr_block, 10)
  vpc_security_group_ids      = [aws_security_group.infrastructure.id]
  associate_public_ip_address = true
  iam_instance_profile        = aws_iam_instance_profile.ec2.name
  key_name                    = var.key_name

  user_data = templatefile("${path.module}/user-data-infrastructure.sh.tftpl", {
    compose_file_base64    = filebase64("${path.module}/../../docker-compose.infrastructure.yml")
    docker_compose_version = var.docker_compose_version
    infrastructure_host    = cidrhost(aws_subnet.public.cidr_block, 10)
    mysql_password         = random_password.mysql.result
    mysql_root_password    = random_password.mysql_root.result
  })

  user_data_replace_on_change = false

  lifecycle {
    ignore_changes = [user_data, user_data_replace_on_change]
  }

  metadata_options {
    http_endpoint = "enabled"
    http_tokens   = "required"
  }

  root_block_device {
    encrypted   = true
    volume_size = var.infrastructure_root_volume_size
    volume_type = "gp3"
  }

  depends_on = [aws_route_table_association.public]

  tags = {
    Name = "${var.project_name}-infrastructure"
    Tier = "infrastructure"
  }
}

resource "aws_instance" "app_nodes" {
  count = var.app_instance_count

  ami                         = data.aws_ssm_parameter.amazon_linux_2023.value
  instance_type               = var.app_instance_type
  subnet_id                   = count.index % 2 == 0 ? aws_subnet.public.id : aws_subnet.public_secondary.id
  vpc_security_group_ids      = [aws_security_group.app_nodes.id]
  associate_public_ip_address = true
  iam_instance_profile        = aws_iam_instance_profile.app.name
  key_name                    = var.key_name

  user_data = templatefile("${path.module}/user-data-app.sh.tftpl", {
    app_index              = count.index + 1
    compose_file_base64    = filebase64("${path.module}/../../docker-compose.app.yml")
    demo_seed_enabled      = count.index == 0 ? "true" : "false"
    docker_buildx_version  = var.docker_buildx_version
    docker_compose_version = var.docker_compose_version
    infrastructure_host    = aws_instance.infrastructure.private_ip
    mysql_password         = random_password.mysql.result
    repository_branch      = var.repository_branch
    repository_url         = var.repository_url
  })

  user_data_replace_on_change = false

  lifecycle {
    ignore_changes = [user_data, user_data_replace_on_change]
  }

  metadata_options {
    http_endpoint = "enabled"
    http_tokens   = "required"
  }

  root_block_device {
    encrypted   = true
    volume_size = var.app_root_volume_size
    volume_type = "gp3"
  }

  depends_on = [
    aws_route_table_association.public,
    aws_route_table_association.public_secondary
  ]

  tags = {
    Name = "${var.project_name}-app-${count.index + 1}"
    Tier = "application"
  }
}
