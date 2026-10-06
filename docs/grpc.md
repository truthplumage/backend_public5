# 상품 gRPC 사용 및 구현 가이드

## 1. 적용 범위

다른 백엔드 서비스가 이 프로젝트의 상품 정보를 조회할 수 있도록 gRPC 서버를 추가했다.
현재 제공하는 기능은 상품 단건 조회와 전체 목록 조회다. 생성·수정·삭제는 기존 REST API를 사용한다.

- 환경: Java 21, Spring Boot 4.0.3, Spring gRPC 1.0.0
- REST 포트: 기본 8080
- gRPC 포트: 기본 9090 (`GRPC_PORT`로 변경 가능)
- DB: PostgreSQL. 애플리케이션 실행에는 DB가 필요하고, 작성한 gRPC 테스트에는 필요하지 않다.

서버 간 통신에 사용하는 API지만, gRPC 자체가 외부 접근을 차단하는 것은 아니다.
현재 구현에는 인증·TLS 설정이 없으므로 운영에서는 내부 네트워크 접근 제한과 필요한 보안 설정을 별도로 적용한다.

## 2. 호출 흐름

```text
다른 서비스의 ProductServiceBlockingStub
    → TCP / HTTP2 (9090)
    → ProductGrpcService
    → ProductUseCase
    → ProductService
    → ProductPersistencePort
    → ProductPersistenceAdapter
    → PostgreSQL
```

REST의 `ProductController`와 gRPC의 `ProductGrpcService`가 동일한 `ProductUseCase`를 호출한다.
조회 비즈니스 로직을 새로 복제하지 않고, 요청·응답 형식만 gRPC 어댑터에서 변환한다.

## 3. 파일별 역할

| 파일 | 역할 |
| --- | --- |
| `build.gradle` | gRPC 의존성 및 proto → Java 자동 생성 설정 |
| `src/main/proto/product.proto` | 서비스 메서드와 요청·응답 계약 |
| `src/main/java/com/grepp/backend5/product/adapter/in/grpc/ProductGrpcService.java` | gRPC 요청 처리와 응답 변환 |
| `src/main/java/com/grepp/backend5/product/application/port/in/ProductUseCase.java` | 기존 상품 비즈니스 기능의 진입점 |
| `src/main/resources/application.yaml` | gRPC 서버 포트 설정 |
| `src/test/java/com/grepp/backend5/product/adapter/in/grpc/ProductGrpcServiceTest.java` | 실제 TCP 통신을 사용하는 호출 테스트 |

## 4. proto 계약

```protobuf
service ProductService {
  rpc GetProduct(GetProductRequest) returns (ProductResponse);
  rpc ListProducts(ListProductsRequest) returns (ListProductsResponse);
}
```

두 메서드는 요청 한 번에 응답 한 번을 반환하는 unary RPC다.

- `GetProductRequest.product_id`: 조회할 상품의 UUID 문자열
- `ListProductsRequest`: 입력 필드 없는 요청
- `ProductResponse`: id, seller_id, name, description, price, stock, status
- `ListProductsResponse.products`: 상품 응답 목록 (`repeated`)

가격은 `BigDecimal`을 `toPlainString()`으로 변환한다. 클라이언트도 `new BigDecimal(response.getPrice())`로 읽으면 소수점 정밀도를 유지할 수 있다.
`description`은 `optional`이므로 `hasDescription()`으로 미설정 상태를 구분한다.

`package product.v1`은 RPC 서비스 이름의 네임스페이스이고,
`java_package`는 생성된 Java 코드의 패키지다.
`java_multiple_files = true`는 메시지들을 각각의 Java 파일로 생성한다.
필드 뒤 숫자는 전송 계약의 식별자이므로 기존 필드 번호를 변경하거나 다른 의미로 재사용하면 안 된다.

## 5. Gradle 설정과 코드 생성

`com.google.protobuf` 플러그인이 proto 파일을 읽고 다음 코드를 생성한다.

- `GetProductRequest`, `ProductResponse` 등 메시지 클래스
- `ProductServiceGrpc.ProductServiceImplBase`: 서버 구현의 부모 클래스
- `ProductServiceGrpc.ProductServiceBlockingStub`: 동기 클라이언트

```sh
./gradlew generateProto
```

생성 결과는 `build/generated/sources/proto/main/java`와
`build/generated/sources/proto/main/grpc`에 저장된다.
생성 파일을 직접 수정하지 말고 `product.proto`를 수정한 뒤 다시 생성한다.
일반 빌드나 테스트 실행에도 코드 생성이 자동으로 포함된다.

의존성 역할:

- `spring-grpc-dependencies`: gRPC 라이브러리 버전 관리
- `spring-grpc-server-spring-boot-starter`: Spring 서비스 등록 및 서버 자동 구성
- `grpc-protobuf`: protobuf 메시지와 gRPC 연결
- `grpc-stub`: 서버·클라이언트 stub 기반 코드
- 테스트의 `grpc-netty`: 테스트 TCP 서버와 채널 생성

현재 생성 도구는 protoc 4.33.1, protoc-gen-grpc-java 1.77.0을 사용한다.

## 6. 서버 구현 방법

`ProductGrpcService`는 `@Service`로 등록하고 생성된 `ProductServiceImplBase`를 상속한다.
Spring gRPC 자동 구성이 이 서비스를 서버에 등록한다.
생성자에서 `ProductUseCase`를 주입받아 기존 상품 기능을 사용한다.

### 단건 조회: getProduct

1. 요청의 `product_id`를 UUID로 변환한다.
2. 변환 후 문자열을 다시 비교해 축약 UUID 같은 비표준 입력도 거부한다.
3. `productUseCase.getById(id)`로 상품을 조회한다.
4. `toResponse(product)`로 도메인 객체를 protobuf 응답으로 변환한다.
5. `observer.onNext(response)`로 응답을 보내고 `onCompleted()`로 종료한다.

### 목록 조회: listProducts

`productUseCase.getAll()`의 각 상품을 protobuf 응답으로 변환하고
`ListProductsResponse`에 담아 반환한다. 현재 전체 조회이며 페이징은 구현하지 않았다.

### StreamObserver와 오류

`StreamObserver`는 서버가 클라이언트에 결과를 보내는 통로다.
정상 응답은 `onNext` → `onCompleted`, 실패 응답은 `onError`로 종료한다.

| 상황 | gRPC 상태 코드 |
| --- | --- |
| 비어 있거나 잘못된 상품 UUID | `INVALID_ARGUMENT` |
| 기존 상품 서비스가 HTTP 404 예외를 반환 | `NOT_FOUND` |
| 그 외 조회·변환 오류 | `INTERNAL` |

현재 기존 서비스의 `ResponseStatusException`을 gRPC 상태로 변환한다.
내부 오류의 상세 메시지는 응답에 포함하지 않는다.

## 7. 서버 실행

프로젝트 루트(`grepp-BE5/backend5`)에서 Java 21을 사용한다.
PostgreSQL 연결 정보는 환경 변수 또는 기존 `.env` 파일로 설정한다.

```properties
DB_HOST=localhost
DB_PORT=5432
DB_NAME=backend5
DB_USERNAME=postgres
DB_PASSWORD=실제비밀번호
GRPC_PORT=9090
```

```sh
./gradlew bootRun
```

상품 단건 조회에는 DB에 저장된 실제 상품 UUID가 필요하다.
기존 REST API로 상품을 등록하거나 DB의 상품 ID를 확인한 뒤 호출한다.

## 8. 다른 Java 서비스에서 호출

클라이언트 프로젝트에도 동일한 `product.proto`와 protobuf 생성 설정이 필요하다.
`grpc-netty`, `grpc-protobuf`, `grpc-stub` 의존성을 추가하고 호환되는 버전을 사용한다.
다음 예제는 채널 생성부터 종료까지 보여주는 개발 환경용 호출 코드다.
`main`의 첫 번째 인자로 실제 상품 UUID를 전달한다.

```java
import com.grepp.backend5.product.grpc.GetProductRequest;
import com.grepp.backend5.product.grpc.ListProductsRequest;
import com.grepp.backend5.product.grpc.ProductServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;

public class ProductGrpcClientExample {
    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        try {
            var stub = ProductServiceGrpc.newBlockingStub(channel);
            var product = stub.withDeadlineAfter(3, TimeUnit.SECONDS)
                    .getProduct(GetProductRequest.newBuilder()
                            .setProductId(args[0])
                            .build());
            System.out.println(product.getName() + " / " + product.getPrice());

            var products = stub.withDeadlineAfter(3, TimeUnit.SECONDS)
                    .listProducts(ListProductsRequest.getDefaultInstance());
            System.out.println("상품 수: " + products.getProductsCount());
        } catch (StatusRuntimeException e) {
            System.err.println("gRPC 오류: " + e.getStatus().getCode());
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
```

`localhost`는 호출하는 프로세스 기준이다. 다른 서버나 컨테이너에서 호출하면
상품 서비스의 내부 호스트 이름과 포트로 변경한다.
`withDeadlineAfter`는 호출마다 최대 대기 시간을 정한다.
실제 서비스에서는 채널을 재사용하고 서비스 종료 시 정리한다.

## 9. 작성한 테스트 실행 및 구조

```sh
./gradlew test --tests '*ProductGrpcServiceTest'
```

macOS에서 기본 Java가 17인 경우:

```sh
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew test --tests '*ProductGrpcServiceTest'
```

테스트 준비 과정:

1. `ProductUseCase`를 Mockito mock으로 생성한다.
2. `127.0.0.1`의 임의 포트(`0`)로 실제 Netty gRPC 서버를 실행한다.
3. 서버에 `ProductGrpcService`를 등록한다.
4. 해당 포트로 클라이언트 채널과 blocking stub을 생성한다.
5. stub 호출 결과와 상태 코드를 확인한다.
6. 각 테스트 종료 후 채널과 서버를 종료한다.

| 테스트 | 확인 내용 |
| --- | --- |
| `getProduct` | ID, 판매자, 상품명, 가격 정밀도, 재고, 상태, 미설정 설명 |
| `listProducts` | 목록 개수와 설명 변환 |
| `emptyList` | 상품이 없으면 빈 목록 반환 |
| `invalidId` | 잘못된 UUID 거부 및 비즈니스 로직 미호출 |
| `missingProduct` | 없는 상품의 `NOT_FOUND` 응답 |
| `internalErrorDoesNotExposeDetails` | `INTERNAL` 응답 및 내부 오류 내용 비노출 |

작성 후 위 6개 테스트가 통과했다.
이 테스트는 직렬화·TCP 통신·서버 어댑터·클라이언트 호출을 검증한다.
Spring Boot 전체 실행이나 실제 PostgreSQL 조회는 검증 범위에 포함하지 않는다.

## 10. 자주 만나는 문제

| 증상 | 확인할 내용 |
| --- | --- |
| 생성된 메시지 클래스를 찾지 못함 | `./gradlew generateProto` 실행 후 IDE Gradle 새로고침 |
| `UNAVAILABLE` | 서버 실행 여부, 호스트·9090 포트, 네트워크 연결 |
| `INVALID_ARGUMENT` | 상품 ID가 표준 UUID인지 확인 |
| `NOT_FOUND` | 해당 상품이 DB에 저장되어 있는지 확인 |
| `DEADLINE_EXCEEDED` | 서버 처리 시간과 클라이언트 제한 시간 확인 |
| 서버 시작 시 DB 연결 실패 | PostgreSQL 실행 및 DB 환경 변수 확인 |

기존 `ProductUseCase.create`의 미정의 `ProductReq` 타입은 현재 서비스와 컨트롤러에 맞춰
`Product`로 수정했다. gRPC에 상품 생성 기능을 추가한 것은 아니다.
