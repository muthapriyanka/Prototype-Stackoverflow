output "application_url" {
  description = "Public load-balanced URL for the application."
  value       = "http://${aws_lb.app.dns_name}"
}

output "application_instance_ids" {
  description = "Application EC2 instance IDs."
  value       = aws_instance.app_nodes[*].id
}

output "infrastructure_instance_id" {
  description = "Shared infrastructure EC2 instance ID."
  value       = aws_instance.infrastructure.id
}

output "session_manager_commands" {
  description = "Commands for opening shells without exposing SSH."
  value = concat(
    ["aws ssm start-session --target ${aws_instance.infrastructure.id} --region ${var.aws_region} --profile prototype-new"],
    [for instance in aws_instance.app_nodes : "aws ssm start-session --target ${instance.id} --region ${var.aws_region} --profile prototype-new"]
  )
}
