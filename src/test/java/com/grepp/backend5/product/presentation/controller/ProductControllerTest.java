package com.grepp.backend5.product.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grepp.backend5.common.exception.GlobalExceptionHandler;
import com.grepp.backend5.product.application.exception.ProductNotFoundException;
import com.grepp.backend5.product.application.exception.SellerNotFoundException;
import com.grepp.backend5.product.application.usecase.ProductUseCase;
import com.grepp.backend5.product.domain.model.Product;
import com.grepp.backend5.product.presentation.dto.request.CreateProductRequest;
import com.grepp.backend5.product.presentation.dto.response.ProductEmbeddingRefreshResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private ProductUseCase productUseCase;

    @BeforeEach
    void setUp() {
        productUseCase = mock(ProductUseCase.class);

        ProductController productController = new ProductController(productUseCase);

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createReturnsCreatedProduct() throws Exception {
        UUID actorId = UUID.randomUUID();

        CreateProductRequest request = new CreateProductRequest(
                UUID.randomUUID(),
                "Macbook Pro 14",
                "M3 chip",
                new BigDecimal("2590000.00"),
                10,
                "ACTIVE"
        );

        Product created = Product.create(
                request.sellerId(),
                request.name(),
                request.description(),
                request.price(),
                request.stock(),
                request.status(),
                actorId
        );
        created.setRegDt(LocalDateTime.now());
        created.setModifyDt(LocalDateTime.now());

        when(productUseCase.create(any(CreateProductRequest.class), any(UUID.class))).thenReturn(created);

        mockMvc.perform(post("/api/products")
                        .header("X-Actor-Id", actorId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(created.getId().toString()))
                .andExpect(jsonPath("$.name").value("Macbook Pro 14"))
                .andExpect(jsonPath("$.regId").value(actorId.toString()))
                .andExpect(jsonPath("$.modifyId").value(actorId.toString()));
    }

    @Test
    void createReturnsBadRequestWhenActorHeaderMissing() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                UUID.randomUUID(),
                "Macbook Pro 14",
                "M3 chip",
                new BigDecimal("2590000.00"),
                10,
                "ACTIVE"
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("X-Actor-Id")));
    }

    @Test
    void createReturnsBadRequestWhenActorHeaderInvalidUuid() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                UUID.randomUUID(),
                "Macbook Pro 14",
                "M3 chip",
                new BigDecimal("2590000.00"),
                10,
                "ACTIVE"
        );

        mockMvc.perform(post("/api/products")
                        .header("X-Actor-Id", "invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createReturnsBadRequestWhenValidationFails() throws Exception {
        String invalidRequest = """
                {
                  "sellerId":"%s",
                  "name":"",
                  "description":"M3 chip",
                  "price":2590000.00,
                  "stock":10,
                  "status":"ACTIVE"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/products")
                        .header("X-Actor-Id", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getByIdReturnsNotFoundWithStandardErrorBody() throws Exception {
        UUID productId = UUID.randomUUID();
        when(productUseCase.getById(productId)).thenThrow(new ProductNotFoundException(productId));

        mockMvc.perform(get("/api/products/{productId}", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", containsString(productId.toString())))
                .andExpect(jsonPath("$.path").value("/api/products/" + productId));
    }

    @Test
    void semanticSearchReturnsProducts() throws Exception {
        Product product = Product.create(
                UUID.randomUUID(),
                "Macbook Pro 14",
                "M3 chip",
                new BigDecimal("2590000.00"),
                10,
                "ACTIVE",
                UUID.randomUUID()
        );
        product.setRegDt(LocalDateTime.now());
        product.setModifyDt(LocalDateTime.now());

        when(productUseCase.searchBySemantic("영상 편집용 노트북", 5))
                .thenReturn(java.util.List.of(product));

        mockMvc.perform(get("/api/products/semantic-search")
                        .param("query", "영상 편집용 노트북")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Macbook Pro 14"));
    }

    @Test
    void refreshEmbeddingsReturnsCounts() throws Exception {
        when(productUseCase.refreshAllEmbeddings())
                .thenReturn(new ProductEmbeddingRefreshResponse(10, 8));

        mockMvc.perform(post("/api/products/embeddings/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(10))
                .andExpect(jsonPath("$.updatedCount").value(8));
    }

    @Test
    void createReturnsNotFoundWhenSellerDoesNotExist() throws Exception {
        UUID actorId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        CreateProductRequest request = new CreateProductRequest(
                sellerId,
                "Macbook Pro 14",
                "M3 chip",
                new BigDecimal("2590000.00"),
                10,
                "ACTIVE"
        );
        when(productUseCase.create(any(CreateProductRequest.class), any(UUID.class)))
                .thenThrow(new SellerNotFoundException(sellerId));

        mockMvc.perform(post("/api/products")
                        .header("X-Actor-Id", actorId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString(sellerId.toString())));
    }
}
