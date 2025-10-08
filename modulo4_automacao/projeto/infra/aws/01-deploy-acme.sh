#!/usr/bin/env bash
set -euo pipefail

REGION="${AWS_REGION:-us-east-1}"
ENVIRONMENT="${ENVIRONMENT:-dev}"
STACK_NAME="cartorio-mod4-acme-${ENVIRONMENT}"

aws cloudformation deploy \
  --region "${REGION}" \
  --stack-name "${STACK_NAME}" \
  --capabilities CAPABILITY_NAMED_IAM \
  --template-body '{
    "AWSTemplateFormatVersion": "2010-09-09",
    "Description": "ACME automation",
    "Resources": {
      "AcmeOrders": {
        "Type": "AWS::DynamoDB::Table",
        "Properties": {
          "BillingMode": "PAY_PER_REQUEST",
          "AttributeDefinitions": [
            {"AttributeName": "orderId", "AttributeType": "S"}
          ],
          "KeySchema": [
            {"AttributeName": "orderId", "KeyType": "HASH"}
          ],
          "TableName": { "Fn::Sub": "cartorio-${ENVIRONMENT}-acme-orders" }
        }
      },
      "ChallengeQueue": {
        "Type": "AWS::SQS::Queue",
        "Properties": {
          "QueueName": { "Fn::Sub": "cartorio-${ENVIRONMENT}-acme-challenges" },
          "VisibilityTimeout": 60
        }
      },
      "AcmeRole": {
        "Type": "AWS::IAM::Role",
        "Properties": {
          "AssumeRolePolicyDocument": {
            "Version": "2012-10-17",
            "Statement": [
              {"Effect":"Allow","Principal":{"Service":"states.amazonaws.com"},"Action":"sts:AssumeRole"}
            ]
          },
          "Policies": [
            {
              "PolicyName": "AcmeStateMachineAccess",
              "PolicyDocument": {
                "Version": "2012-10-17",
                "Statement": [
                  {
                    "Effect": "Allow",
                    "Action": ["dynamodb:PutItem","dynamodb:UpdateItem","dynamodb:GetItem"],
                    "Resource": {"Fn::GetAtt":["AcmeOrders","Arn"]}
                  },
                  {
                    "Effect": "Allow",
                    "Action": ["sqs:SendMessage"],
                    "Resource": {"Fn::GetAtt":["ChallengeQueue","Arn"]}
                  }
                ]
              }
            }
          ]
        }
      },
      "AcmeStateMachine": {
        "Type": "AWS::StepFunctions::StateMachine",
        "Properties": {
          "StateMachineName": { "Fn::Sub": "cartorio-${ENVIRONMENT}-acme" },
          "RoleArn": { "Fn::GetAtt":["AcmeRole","Arn"] },
          "StateMachineType": "STANDARD",
          "DefinitionString": {
            "Fn::Sub": "{\"StartAt\":\"PersistOrder\",\"States\":{\"PersistOrder\":{\"Type\":\"Task\",\"Resource\":\"arn:aws:states:::dynamodb:putItem\",\"Parameters\":{\"TableName\":\"cartorio-${ENVIRONMENT}-acme-orders\",\"Item\":{\"orderId\":{\"S.$\":\"$.orderId\"},\"domain\":{\"S.$\":\"$.domain\"},\"status\":{\"S\":\"pending\"},\"token\":{\"S.$\":\"$.token\"}}},\"Next\":\"NotifyChallenge\"},\"NotifyChallenge\":{\"Type\":\"Task\",\"Resource\":\"arn:aws:states:::sqs:sendMessage\",\"Parameters\":{\"QueueUrl\":\"${ChallengeQueue}\",\"MessageBody\":{\"orderId.$\":\"$.orderId\",\"token.$\":\"$.token\",\"domain.$\":\"$.domain\"}}},\"End\":true}}"
          }
        }
      }
    },
    "Outputs": {
      "OrdersTable": { "Value": { "Ref": "AcmeOrders" } },
      "ChallengeQueueUrl": { "Value": { "Ref": "ChallengeQueue" } },
      "StateMachineArn": { "Value": { "Ref": "AcmeStateMachine" } }
    }
  }'
