terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    bucket = "TODO-definir-bucket-terraform-state"
    key    = "aws-kms-production/prod/terraform.tfstate"
    region = "us-east-1"
    dynamodb_table = "TODO-lock-table"
    encrypt = true
  }
}

provider "aws" {
  region = var.aws_region
}

module "networking" {
  source = "../../modules/networking"
}
