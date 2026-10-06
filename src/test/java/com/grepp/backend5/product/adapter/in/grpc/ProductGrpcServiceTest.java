package com.grepp.backend5.product.adapter.in.grpc;

import com.grepp.backend5.product.application.port.in.ProductUseCase;
import com.grepp.backend5.product.domain.Product;
import com.grepp.backend5.product.grpc.*;
import io.grpc.*;
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
    }

    @Test
    void listProducts() {
        product.setDescription("M3 칩셋");
        when(useCase.getAll()).thenReturn(List.of(product));
        var response = client.listProducts(ListProductsRequest.getDefaultInstance());
        assertEquals(1, response.getProductsCount());
        assertEquals("M3 칩셋", response.getProducts(0).getDescription());
    }

    @Test
    void emptyList() {
        when(useCase.getAll()).thenReturn(List.of());
        assertEquals(0, client.listProducts(ListProductsRequest.getDefaultInstance()).getProductsCount());
    }

    @Test
    void invalidId() {
        for (String id : List.of("", "invalid", "1-1-1-1-1")) {
            var error = assertThrows(StatusRuntimeException.class, () -> client.getProduct(
                    GetProductRequest.newBuilder().setProductId(id).build()));
            assertEquals(Status.Code.INVALID_ARGUMENT, error.getStatus().getCode());
        }
        verifyNoInteractions(useCase);
    }

    @Test
    void missingProduct() {
        when(useCase.getById(product.getId()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        var error = assertThrows(StatusRuntimeException.class, () -> client.getProduct(
                GetProductRequest.newBuilder().setProductId(product.getId().toString()).build()));
        assertEquals(Status.Code.NOT_FOUND, error.getStatus().getCode());
    }

    @Test
    void internalErrorDoesNotExposeDetails() {
        when(useCase.getAll()).thenThrow(new IllegalStateException("DB password secret"));
        var error = assertThrows(StatusRuntimeException.class,
                () -> client.listProducts(ListProductsRequest.getDefaultInstance()));
        assertEquals(Status.Code.INTERNAL, error.getStatus().getCode());
        assertEquals("Failed to query products", error.getStatus().getDescription());
    }
}
