docker build -t ps5-app . && docker run -d --restart=on-failure --name ps5-app --env-file ./env ps5-app
