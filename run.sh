docker build -t ps5-app . && docker run -d --restart=on-failure --env-file ./env ps5-app
