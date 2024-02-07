#!/bin/bash
set -e

CONFIG_PATH=/data/options.json
export POSTGRES_PRISMA_URL=$(jq --raw-output '.postgres_url // empty' $CONFIG_PATH)
export POSTGRES_URL_NON_POOLING=$(jq --raw-output '.postgres_url_non_pooling // empty' $CONFIG_PATH)
export NEXT_PUBLIC_BASE_URL=https://localhost
export CERTFILE=$(jq --raw-output '.certfile // empty' $CONFIG_PATH)
export KEYFILE=$(jq --raw-output '.keyfile // empty' $CONFIG_PATH)

prisma migrate deploy
# npm run start
NODE_ENV=production node ./server.js