#!/bin/bash
set -e

CONFIG_PATH=/data/options.json
export POSTGRES_PRISMA_URL=$(jq --raw-output '.postgres_url // empty' $CONFIG_PATH)
export POSTGRES_URL_NON_POOLING=$(jq --raw-output '.postgres_url_non_pooling // empty' $CONFIG_PATH)

npm install --ignore-scripts
npm install -g prisma
prisma migrate deploy
prisma generate
npm run dev