#!/usr/bin/env bash
set -euo pipefail
REGION="${AWS_REGION:-us-east-1}"
ENVIRONMENT="${ENVIRONMENT:-dev}"
ARTIFACT_BUCKET="${ARTIFACT_BUCKET:-cartorio-${ENVIRONMENT}-cicd-artifacts}"
PIPELINE_NAME="${PIPELINE_NAME:-cartorio-${ENVIRONMENT}-pipeline}"

if [[ "${REGION}" == "us-east-1" ]]; then
  aws s3api create-bucket --bucket "${ARTIFACT_BUCKET}" --region "${REGION}" || true
else
  aws s3api create-bucket --bucket "${ARTIFACT_BUCKET}" --region "${REGION}" \
    --create-bucket-configuration "LocationConstraint=${REGION}" || true
fi

ROLE_ARN="$(aws iam create-role --role-name "cartorio-${ENVIRONMENT}-codepipeline" \
  --assume-role-policy-document '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":["codepipeline.amazonaws.com"]},"Action":["sts:AssumeRole"]}]}' \
  --query Role.Arn --output text 2>/dev/null || aws iam get-role --role-name "cartorio-${ENVIRONMENT}-codepipeline" --query Role.Arn --output text)"

aws iam attach-role-policy --role-name "cartorio-${ENVIRONMENT}-codepipeline" \
  --policy-arn arn:aws:iam::aws:policy/AWSCodePipelineFullAccess || true
aws iam attach-role-policy --role-name "cartorio-${ENVIRONMENT}-codepipeline" \
  --policy-arn arn:aws:iam::aws:policy/AWSCodeBuildDeveloperAccess || true
aws iam attach-role-policy --role-name "cartorio-${ENVIRONMENT}-codepipeline" \
  --policy-arn arn:aws:iam::aws:policy/AmazonS3FullAccess || true

BUILD_ROLE="$(aws iam create-role --role-name "cartorio-${ENVIRONMENT}-codebuild" \
  --assume-role-policy-document '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":["codebuild.amazonaws.com"]},"Action":["sts:AssumeRole"]}]}' \
  --query Role.Arn --output text 2>/dev/null || aws iam get-role --role-name "cartorio-${ENVIRONMENT}-codebuild" --query Role.Arn --output text)"

aws iam attach-role-policy --role-name "cartorio-${ENVIRONMENT}-codebuild" \
  --policy-arn arn:aws:iam::aws:policy/AWSCodeBuildDeveloperAccess || true
aws iam attach-role-policy --role-name "cartorio-${ENVIRONMENT}-codebuild" \
  --policy-arn arn:aws:iam::aws:policy/AmazonS3FullAccess || true

aws codebuild create-project \
  --name "cartorio-${ENVIRONMENT}-build" \
  --source type=GITHUB,location=${CODEBUILD_REPO_URL:-https://github.com/SEU_USUARIO/cartorio-digital.git} \
  --artifacts type=CODEPIPELINE \
  --environment type=LINUX_CONTAINER,image=aws/codebuild/standard:7.0,computeType=BUILD_GENERAL1_SMALL \
  --service-role "${BUILD_ROLE}" 2>/dev/null || true

aws codepipeline create-pipeline --cli-input-json "{
  \"pipeline\": {
    \"name\": \"${PIPELINE_NAME}\",\n    \"roleArn\": \"${ROLE_ARN}\",\n    \"artifactStore\": {\"type\": \"S3\", \"location\": \"${ARTIFACT_BUCKET}\"},\n    \"stages\": [\n      {\"name\": \"Source\", \"actions\": [\n        {\"name\": \"GitHubSource\",\"actionTypeId\": {\"category\": \"Source\",\"owner\": \"ThirdParty\",\"provider\": \"GitHub\",\"version\": \"1\"},\n         \"outputArtifacts\": [{\"name\": \"SourceOutput\"}],\n         \"configuration\": {\"Owner\": \"${GITHUB_OWNER:-SEU_USUARIO}\",\"Repo\": \"${GITHUB_REPO:-cartorio-digital}\",\"Branch\": \"${GITHUB_BRANCH:-main}\",\"OAuthToken\": \"${GITHUB_TOKEN:-REQUIRED}\"}}\n      ]},\n      {\"name\": \"Build\", \"actions\": [\n        {\"name\": \"CodeBuild\",\"actionTypeId\": {\"category\": \"Build\",\"owner\": \"AWS\",\"provider\": \"CodeBuild\",\"version\": \"1\"},\n         \"inputArtifacts\": [{\"name\": \"SourceOutput\"}],\n         \"outputArtifacts\": [{\"name\": \"BuildOutput\"}],\n         \"configuration\": {\"ProjectName\": \"cartorio-${ENVIRONMENT}-build\"}}\n      ]}\n    ]\n  }
}" 2>/dev/null || true
