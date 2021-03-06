## Architecture

![micro_architecture](https://user-images.githubusercontent.com/50051656/124487479-bd6ba900-dde9-11eb-99b1-aef19980ddb9.JPG)

## API

![api](https://user-images.githubusercontent.com/50051656/124787563-84673c00-df83-11eb-86b9-305cfeb4ad29.JPG)

### 1. 사용자 등록

#### Request

``` http
POST /USER-SERVICE/users HTTP/1.1
```

``` json
{
  "email" : <이메일>,
  "name" : <이름>,
  "pwd" : <비밀번호>
}
```

#### Response

``` http
HTTP/1.1 201 Created
```

``` json
{
  "email" : "이메일",
  "name" : "이름",
  "pwd" : "비밀번호"
}
```

### 2. 사용자 조회

#### Request

``` http
GET /USER-SERVICE/users HTTP/1.1
Authorization: Bearer {ACCESS_TOKEN}
```

#### Response

``` http
HTTP/1.1 200 OK
```

``` json
[
  {
    "email" : "이메일",
    "name" : "이름",
    "userId" : "아이디"
  },
]
```

### 3. 상품 조회

#### Request

``` http
GET /CATALOG-SERVICE/catalogs HTTP/1.1
Authorization: Bearer {ACCESS_TOKEN}
```

#### Response

``` http
HTTP/1.1 200 OK
```

``` json
[
  {
    "productId" : "상품 ID",
    "productName" : "상품 이름",
    "stock" : "재고 수량",
    "unitPrice" : "개당 가격",
    "createdAt" : "상품 등록일"
  },
]
```

### 4. 상품 주문

#### Request

``` http
POST /ORDER-SERVICE/{userId}/orders HTTP/1.1
Authorization: Bearer {ACCESS_TOKEN}
```

``` json
{
    "productId": <상품 ID>,
    "qty": <수량>,
    "unitPrice": <개당 가격>
}
```

#### Response

``` http
HTTP/1.1 201 Created
```

``` json
{
  "productID" : "상품 ID",
  "qty" : "수량",
  "unitPrice" : "개당 가격",
  "totalPrice" : "총 가격",
  "orderId" : "주문 ID"
}
```

### 5. 주문 수량 업데이트

- Kafka

### 6. 주문 확인

#### Request

``` http
GET /USER-SERVICE/users/{userId} HTTP/1.1
Authorization: Bearer {ACCESS_TOKEN}
```

#### Response

``` http
HTTP/1.1 200 OK
```

``` json
{
  "userId" : "아이디",
  "name" : "이름",
  "email" : "이메일",
  "orders" : ["주문 내역"]
}
```

### 7. 주문 조회

#### Request

``` http
GET /ORDER-SERVICE/{userId}/orders HTTP/1.1
Authorization: Bearer {ACCESS_TOKEN}
```

#### Response

``` http
HTTP/1.1 200 OK
```

``` json
[
  {
    "productID" : "상품 ID",
    "qty" : "수량",
    "unitPrice" : "개당 가격",
    "totalPrice" : "총 가격",
    "createdAt" : "주문일자",
    "orderId" : "주문 ID"
  },
]
```

## Usage

### Network

``` bash
docker network create \
--gateway 172.18.0.1 \
--subnet 172.18.0.0/16 \
ecommerce-network
```

### RabbitMQ

``` bash
docker run -d --name rabbitmq \
--network ecommerce-network \
-p 15672:15672 -p 5672:5672 -p 15671:15671 -p 5671:5671 -p 4369:4369 \
-e RABBITMQ_DEFAULT_USER=guest \
-e RABBITMQ_DEFAULT_PASS=guest \
rabbitmq:management
```

### MariaDB

``` bash
docker run -d -p 3306:3306 \
--network ecommerce-network \
--name mariadb \
-e MYSQL_ROOT_PASSWORD=test1234 \
mariadb
```

### config-service

``` bash
docker run -d -p 8888:8888 \
--network ecommerce-network \
-e "spring.rabbitmq.host=rabbitmq" \
-e "spring.rabbitmq.username=guest" \
-e "spring.rabbitmq.password=guest" \
-e "spring.profiles.active=default" \
--name config-service \
mylifeforcoding/config-service:1.0
```

### discovery-service

``` bash
docker run -d -p 8761:8761 \
--network ecommerce-network \
-e "spring.cloud.config.uri=http://config-service:8888" \
--name discovery-service \
mylifeforcoding/discovery-service:1.0
```

### apigateway-service

``` bash
docker run -d -p 8000:8000 \
--network ecommerce-network \
--ip 172.18.0.102 \
-e "spring.cloud.config.uri=http://config-service:8888" \
-e "spring.rabbitmq.host=rabbitmq" \
-e "eureka.client.service-url.defaultZone=http://discovery-service:8761/eureka" \
--name apigateway-service \
mylifeforcoding/apigateway-service:1.0
```

### [kafka](https://github.com/wurstmeister/kafka-docker)

``` bash
docker-compose -f docker-compose-single-broker.yml up -d
```

### [zipkin](https://zipkin.io/pages/quickstart.html)

``` bash
docker run -d -p 9411:9411 \
--network ecommerce-network \
--name zipkin \
openzipkin/zipkin
```

### [prometheus](https://grafana.com/grafana/download?pg=get&platform=docker&plcmt=selfmanaged-box1-cta1)

``` bash
docker run -d -p 9090:9090 \
--network ecommerce-network \
--name prometheus -v \
prometheus.yml:/etc/prometheus.yml prom/prometheus
```

### [grafana](https://grafana.com/grafana/download?pg=get&platform=docker&plcmt=selfmanaged-box1-cta1)

``` bash
docker run -d -p 3000:3000 \
--network ecommerce-network \
--name grafana \
grafana/grafana
```

### user-service

``` bash
docker run -d \
--network ecommerce-network \
--name user-service \
-e spring.cloud.config.uri=http://config-service:8888 \
-e spring.rabbitmq.host=rabbitmq \
-e spring.zipkin.base-url=http://zipkin:9411 \
-e eureka.client.service-url.defaultZone=http://discovery-service:8761/eureka \
-e logging.file=/api-logs/users-ws.log \
mylifeforcoding/user-service:1.0
```

### order-service

``` bash
docker run -d \
--network ecommerce-network \
--name order-service \
-e spring.cloud.config.uri=http://config-service:8888 \
-e spring.rabbitmq.host=rabbitmq \
-e spring.zipkin.base-url=http:zipkin:9411 \
-e eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/ \
-e logging.file=/api-logs/orders-ws.log \
-e spring.datasource.url=jdbc:mariadb://mariadb:3306/mydb \
mylifeforcoding/order-service:1.0
```

### catalog-service

``` bash
docker run -d \
--network ecommerce-network \
--name catalog-service \
-e spring.cloud.config.uri=http://config-service:8888 \
-e spring.rabbitmq.host=rabbitmq \
-e eureka.client.service-url.defaultZone=http://discovery-service:8761/eureka \
-e logging.file=/api-logs/catalogs-ws.log \
mylifeforcoding/catalog-service:1.0
```
