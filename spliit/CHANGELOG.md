<!-- https://developers.home-assistant.io/docs/add-ons/presentation#keeping-a-changelog -->
## 1.0.16

- update to spliit 1.14.0
- update homeassistant builder to 2024.08.2

## 1.0.15

- Update to spliit 1.9.0
- Update to home assistant base image v3.20
- Update next.config.js to support i18n, and rename to include .mjs suffix.

## 1.0.14

- Update to spliit 1.8.1
- Update next.config.js to support custom upload endpoints

## 1.0.13

- Update to spliit 1.7.0

## 1.0.12

- Update to spliit 1.6.0

## 1.0.11

- Update to spliit 1.5.0
- Update add-on documentation

## 1.0.10

- Update to spliit 1.4.0
- Fix env variables set from config entries for expense documents upload

## 1.0.9

- use specific tag on spliit repo and update to spliit 1.2.0

## 1.0.8

- run nginx as root user, chown directories as root

## 1.0.7

- run nginx as nginx user

## 1.0.6

- chmod 777 on log dir

## 1.0.5

- chmod 766 on log dir

## 1.0.4

- Fix category extract setting
- add default values for build in .env
- create /var/lib/nginx directories in the docker file

## 1.0.3

- remove nginx log due to permission denied error

## 1.0.2

- use nginx server

## 1.0.1

- use custom server with ssl

## 1.0.0

- Initial release
