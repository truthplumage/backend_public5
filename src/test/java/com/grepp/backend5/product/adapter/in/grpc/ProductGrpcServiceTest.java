package com.grepp.backend5.product.adapter.in.grpc;

import com.grepp.backend5.product.application.port.in.ProductUseCase;
import com.grepp.backend5.product.domain.Product;
import com.grepp.backend5.product.grpc.*;
import io.grpc.*;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// DB 없이 실제 TCP gRPC 서버와 클라이언트 stub으로 통신을 검증한다.
class ProductGrpcServiceTest {
    private ProductUseCase useCase;
    private Server server;
    private ManagedChannel channel;
    private ProductServiceGrpc.ProductServiceBlockingStub client;
    private Product product;

    @BeforeEach
    void setUp() throws Exception {
        useCase = mock(ProductUseCase.class);
        product = Product.create(UUID.randomUUID(), "맥북 프로", null,
                new BigDecimal("2590000.00"), 10, "ACTIVE", UUID.randomUUID());
        server = NettyServerBuilder.forAddress(new InetSocketAddress("127.0.0.1", 0))
                .addService(new ProductGrpcService(useCase)).build().start();
        channel = NettyChannelBuilder.forAddress("127.0.0.1", server.getPort())
                .usePlaintext().build();
        client = ProductServiceGrpc.newBlockingStub(channel).withDeadlineAfter(5, TimeUnit.SECONDS);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (channel != null) channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        if (server != null) server.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void getProduct() {
        when(useCase.getById(product.getId())).thenReturn(product);
        var response = client.getProduct(GetProductRequest.newBuilder()
                .setProductId(product.getId().toString()).build());
        assertEquals(product.getId().toString(), response.getId());
        assertEquals(product.getSellerId().toString(), response.getSellerId());
        assertEquals("맥북 프로", response.getName());
        assertEquals("2590000.00", response.getPrice());
        assertEquals(10, response.getStock());
        assertEquals("ACTIVE", response.getStatus());
        assertFalse(response.hasDescription());
        verify(useCase).getById(product.getId());
        printResult("단건 조회", product.getId(), response);
    }

    @Test
    void listProducts() {
        product.setDescription("M3 칩셋");
        when(useCase.getAll()).thenReturn(List.of(product));
        var response = client.listProducts(ListProductsRequest.getDefaultInstance());
        assertEquals(1, response.getProductsCount());
        assertEquals("M3 칩셋", response.getProducts(0).getDescription());
        printResult("목록 조회", "없음", response);
    }

    @Test
    void emptyList() {
        when(useCase.getAll()).thenReturn(List.of());
        var response = client.listProducts(ListProductsRequest.getDefaultInstance());
        assertEquals(0, response.getProductsCount());
        printResult("빈 목록 조회", "없음", "상품 수: " + response.getProductsCount());
    }

    @Test
    void invalidId() {
        for (String id : List.of("", "invalid", "1-1-1-1-1")) {
            var error = assertThrows(StatusRuntimeException.class, () -> client.getProduct(
                    GetProductRequest.newBuilder().setProductId(id).build()));
            assertEquals(Status.Code.INVALID_ARGUMENT, error.getStatus().getCode());
            printResult("잘못된 UUID", "\"" + id + "\"", error.getStatus());
        }
        verifyNoInteractions(useCase);
    }

    @Test
    void missingProduct() {
        when(useCase.getById(product.getId()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));
        var error = assertThrows(StatusRuntimeException.class, () -> client.getProduct(
                GetProductRequest.newBuilder().setProductId(product.getId().toString()).build()));
        assertEquals(Status.Code.NOT_FOUND, error.getStatus().getCode());
        printResult("없는 상품", product.getId(), error.getStatus());
    }

    @Test
    void internalErrorDoesNotExposeDetails() {
        when(useCase.getAll()).thenThrow(new IllegalStateException("데이터베이스 비밀번호 비밀값"));
        var error = assertThrows(StatusRuntimeException.class,
                () -> client.listProducts(ListProductsRequest.getDefaultInstance()));
        assertEquals(Status.Code.INTERNAL, error.getStatus().getCode());
        assertEquals("상품 조회에 실패했습니다.", error.getStatus().getDescription());
        printResult("내부 오류", "없음", error.getStatus());
    }
    private CreateProductRequest.Builder createRequest() {
        return CreateProductRequest.newBuilder()
                .setSellerId(product.getSellerId().toString())
                .setCreatorId(product.getRegId().toString())
                .setName("맥북 프로").setPrice("2590000.00");
    }

    @Test
    void createProduct() {
        when(useCase.create(any(Product.class))).thenAnswer(invocation -> {
            Product input = invocation.getArgument(0);
            return Product.create(input.getSellerId(), input.getName(), input.getDescription(),
                    input.getPrice(), input.getStock(), input.getStatus(), input.getRegId());
        });
        var request = createRequest().setDescription("M3 칩셋").setStock(10).setStatus("ACTIVE").build();
        var response = client.createProduct(request);
        var captor = org.mockito.ArgumentCaptor.forClass(Product.class);
        verify(useCase).create(captor.capture());
        assertEquals(product.getRegId(), captor.getValue().getRegId());
        assertEquals(product.getRegId(), captor.getValue().getModifyId());
        assertNotEquals(captor.getValue().getId().toString(), response.getId());
        assertDoesNotThrow(() -> UUID.fromString(response.getId()));
        assertEquals(request.getSellerId(), response.getSellerId());
        assertEquals(request.getName(), response.getName());
        assertEquals(request.getDescription(), response.getDescription());
        assertEquals(request.getPrice(), response.getPrice());
        assertEquals(10, response.getStock());
        assertEquals("ACTIVE", response.getStatus());
        printResult("상품 등록", TextFormat.printer().escapingNonAscii(false).printToString(request), response);
    }

    @Test
    void createProductDefaults() {
        when(useCase.create(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var request = createRequest().setPrice("0").build();
        var response = client.createProduct(request);
        assertEquals(0, response.getStock());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals("0.00", response.getPrice());
        assertFalse(response.hasDescription());
        printResult("등록 기본값", request, response);
    }

    @Test
    void invalidCreateInput() {
        var invalidRequests = List.of(
                createRequest().setSellerId("invalid").build(),
                createRequest().setCreatorId("1-1-1-1-1").build(),
                createRequest().setName(" ").build(),
                createRequest().setName("a".repeat(101)).build(),
                createRequest().setPrice("invalid").build(),
                createRequest().setPrice("-1").build(),
                createRequest().setPrice("1.001").build(),
                createRequest().setPrice("10000000000000").build(),
                createRequest().setStock(-1).build(),
                createRequest().setStatus("").build(),
                createRequest().setStatus("a".repeat(21)).build(),
                CreateProductRequest.getDefaultInstance());
        for (var request : invalidRequests) {
            var error = assertThrows(StatusRuntimeException.class, () -> client.createProduct(request));
            assertEquals(Status.Code.INVALID_ARGUMENT, error.getStatus().getCode());
            printResult("잘못된 등록 입력", request, error.getStatus());
        }
        verifyNoInteractions(useCase);
    }

    @Test
    void createFailure() {
        when(useCase.create(any(Product.class))).thenThrow(new IllegalStateException("데이터베이스 비밀번호 비밀값"));
        var request = createRequest().build();
        var error = assertThrows(StatusRuntimeException.class, () -> client.createProduct(request));
        assertEquals(Status.Code.INTERNAL, error.getStatus().getCode());
        assertEquals("상품 등록에 실패했습니다.", error.getStatus().getDescription());
        printResult("등록 실패", request, error.getStatus());
    }

    private void printResult(String scenario, Object input, Object result) {
        Object output = result instanceof Message message
                ? TextFormat.printer().escapingNonAscii(false).printToString(message) : result;
        System.out.printf("%n[%s] 검증 통과%n입력: %s%n결과:%n%s%n", scenario, input, output);
    }
}
