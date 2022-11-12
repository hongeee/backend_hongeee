# Introduce

- 사용자별 휴가 (연차, 반차, 반반차)를 신청하고 취소할 수 있는 휴가 신청 시스템 API 서버입니다.

# Domain Entity

- [User.java](./src/main/java/com/hongeee/vacation/domain/User.java): 사용자 정보
- [Vacation.java](./src/main/java/com/hongeee/vacation/domain/Vacation.java): 휴가 신청 정보

# Requirements

- Java 1.8
- Docker

# Setup

## Docker Compose

- [compose.yml](./docker/compose.yml)로 MySQL Docker Container를 생성합니다.
- [mysql-init-files](./docker/mysql-init-files) 디렉터리 내 .sql 파일로 데이터베이스, 테이블 및 초기 데이터를 생성합니다.

```shell
$ ./gradlew composeUp
```

## Run application

- bootRun 또는 IDE에서 [RequestVacationApplication.java](./src/main/java/com/hongeee/vacation/RequestVacationApplication.java) Run을 통해 실행할 수 있습니다.

```shell
$ ./gradlew clean bootRun
```

# Scenario

- Postman Collection ([kakaostyle.postman_collection.json](./src/main/resources/kakaostyle.postman_collection.json)) 을 사용하여 호출할 수 있습니다.
- cURL을 사용하여 호출할 수 있습니다.

## 회원 가입 (Optional)

- 테스트 계정 정보가 초기 세팅되어 있어 필수 사항은 아닙니다.
  - email: hongeee@kakaostyle.com
  - password: changeit
- 시나리오
  - 회원 가입 API 호출

### 회원 가입 API 호출

```shell
curl --location --request POST 'localhost:8080/sign-up' \
--header 'Content-Type: application/json' \
--data-raw '{
    "email": "hongeee@kakaostyle.com",
    "password": "changeit",
    "name": "홍인석"
}'
```

## 로그인

- 테스트 계정 정보가 초기 세팅되어 있습니다.
    - email: hongeee@kakaostyle.com
    - password: changeit
- 시나리오
    - 로그인 API 호출

### 로그인 API 호출

```shell
curl --location --request POST 'localhost:8080/sign-in' \
--header 'Content-Type: application/json' \
--data-raw '{
    "email": "hongeee@kakaostyle.com",
    "password": "changeit"
}'
```

## 토큰 재발급 (Optional)

- 로그인 API를 호출하여 발급된 AccessToken이 만료되었을 경우 인증 정보를 다시 입력하지 않고 RefreshToken을 사용하여 AccessToken을 재발급합니다.
- 시나리오
  - 토큰 재발급 API 호출

### 토큰 재발급 API 호출

```shell
curl --location --request POST 'localhost:8080/token/refresh' \
--header 'Content-Type: application/json' \
--data-raw '{
    "accessToken": "{accessToken}",
    "refreshToken": "{refreshToken}"
}'
```

## 휴가 신청

- 휴가 시작일, 휴가 종료일, 연차/반차/반반차 정보를 지정하여 휴가를 신청합니다.
- 연차의 경우 휴가 시작일, 휴가 종료일로 공휴일을 제외한 휴가 사용 일수가 자동으로 계산됩니다.
- 반차/반반차의 경우 오전/오후를 구분하지 않았습니다.
- 아래와 같은 경우 휴가 신청을 할 수 없습니다.
  - 연차를 모두 사용한 (없는) 경우
  - 신청한 휴가 기간이 0일 일 경우
  - 신청한 휴가 기간보다 보유한 연차가 부족할 경우
  - 신청한 휴가 기간과 겹치는 휴가 신청이 이미 있을 경우
- 시나리오
  - 로그인 API 호출
  - 휴가 신청 API 호출

### 로그인 API 호출

```shell
curl --location --request POST 'localhost:8080/sign-in' \
--header 'Content-Type: application/json' \
--data-raw '{
    "email": "hongeee@kakaostyle.com",
    "password": "changeit"
}'
```

### 휴가 신청 API 호출

#### 연차

```shell
curl --location --request POST 'localhost:8080/api/vacations' \
--header 'Authorization: {accessToken}' \
--header 'Content-Type: application/json' \
--data-raw '{
    "startDate": "2022-12-01",
    "endDate": "2022-12-02",
    "comment": "휴가 신청",
    "vacationType": "DAY"
}'
```

#### 반차

```shell
curl --location --request POST 'localhost:8080/api/vacations' \
--header 'Authorization: {accessToken}' \
--header 'Content-Type: application/json' \
--data-raw '{
    "startDate": "2022-12-05",
    "comment": "반차 신청",
    "vacationType": "HALF_DAY"
}'
```

#### 반반차

```shell
curl --location --request POST 'localhost:8080/api/vacations' \
--header 'Authorization: {accessToken}' \
--header 'Content-Type: application/json' \
--data-raw '{
    "startDate": "2022-12-06",
    "comment": "반반차 신청",
    "vacationType": "QUARTER_DAY"
}'
```

## 휴가 신청 목록

- 휴가 신청 목록을 조회합니다.
- 시나리오
    - 로그인 API 호출
    - 휴가 신청 목록 API 호출

### 로그인 API 호출

```shell
curl --location --request POST 'localhost:8080/sign-in' \
--header 'Content-Type: application/json' \
--data-raw '{
    "email": "hongeee@kakaostyle.com",
    "password": "changeit"
}'
```

### 휴가 신청 목록 API 호출

```shell
curl --location --request GET 'localhost:8080/api/vacations' \
--header 'Authorization: {accessToken}'
```

## 휴가 신청 취소

- 기 신청한 휴가를 취소합니다.
- 아직 시작하지 않은 휴가만 취소할 수 있습니다.
  - 반차/반반차의 경우 오전/오후를 구분하지 않으므로 당일이 되면 시간에 상관 없이 취소가 불가합니다.
- 시나리오
    - 로그인 API 호출
    - 휴가 신청 API 호출
    - 휴가 신청 목록 API 호출
    - 휴가 신청 취소 API 호출

### 로그인 API 호출

```shell
curl --location --request POST 'localhost:8080/sign-in' \
--header 'Content-Type: application/json' \
--data-raw '{
    "email": "hongeee@kakaostyle.com",
    "password": "changeit"
}'
```

### 휴가 신청 API 호출

```shell
curl --location --request POST 'localhost:8080/api/vacations' \
--header 'Authorization: {accessToken}' \
--header 'Content-Type: application/json' \
--data-raw '{
    "startDate": "2022-12-24",
    "endDate": "2022-12-26",
    "comment": "메리 크리스마스",
    "vacationType": "DAY"
}'
```

### 휴가 신청 목록 API 호출

```shell
curl --location --request GET 'localhost:8080/api/vacations' \
--header 'Authorization: {accessToken}'
```

### 휴가 신청 취소 API 호출

```shell
curl --location --request PUT 'localhost:8080/api/vacations/{id}' \
--header 'Authorization: {accessToken}'
```