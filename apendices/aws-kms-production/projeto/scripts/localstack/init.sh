#!/usr/bin/env bash
awslocal kms create-key --description 'Mock key for tests' --key-usage SIGN_VERIFY --origin EXTERNAL >/tmp/mock-key.json

