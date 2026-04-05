package com.grepp.backend5.product.presentation.controller;

import com.grepp.backend5.product.application.usecase.ProductUseCase;
import com.grepp.backend5.product.domain.model.Product;
import com.grepp.backend5.product.presentation.dto.request.CreateProductRequest;
import com.grepp.backend5.product.presentation.dto.request.UpdateProductRequest;
import com.grepp.backend5.product.presentation.dto.response.ProductEmbeddingRefreshResponse;
import com.grepp.backend5.product.presentation.dto.response.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product", description = "상품 CRUD API")
public class ProductController {

    private final ProductUseCase productUseCase;

    public ProductController(ProductUseCase productUseCase) {
        this.productUseCase = productUseCase;
    }

    @PostMapping
    @Operation(summary = "상품 생성", description = "신규 상품을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 값 오류")
    })
    public ResponseEntity<ProductResponse> create(
            @Parameter(description = "요청자 UUID")
            @RequestHeader("X-Actor-Id") UUID actorId,
            @Valid @RequestBody CreateProductRequest request
    ) {
        Product created = productUseCase.create(request, actorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.from(created));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "상품 단건 조회", description = "상품 ID로 상품 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "상품 없음")
    })
    public ProductResponse getById(@Parameter(description = "상품 UUID") @PathVariable UUID productId) {
        return ProductResponse.from(productUseCase.getById(productId));
    }

    @GetMapping
    @Operation(summary = "상품 목록 조회", description = "전체 상품 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public List<ProductResponse> getAll() {
        return productUseCase.getAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    @GetMapping("/semantic-search")
    @Operation(summary = "상품 벡터 검색", description = "질문을 벡터로 바꿔 유사한 상품을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public List<ProductResponse> semanticSearch(
            @Parameter(description = "검색 문장", example = "영상 편집용 노트북")
            @RequestParam String query,
            @Parameter(description = "반환 개수", example = "5")
            @RequestParam(defaultValue = "5") int size
    ) {
        return productUseCase.searchBySemantic(query, size).stream()
                .map(ProductResponse::from)
                .toList();
    }

    @PostMapping("/embeddings/refresh")
    @Operation(summary = "전체 상품 임베딩 재생성", description = "현재 저장된 모든 상품의 임베딩을 다시 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재생성 성공",
                    content = @Content(schema = @Schema(implementation = ProductEmbeddingRefreshResponse.class)))
    })
    public ProductEmbeddingRefreshResponse refreshEmbeddings() {
        return productUseCase.refreshAllEmbeddings();
    }

    @PutMapping("/{productId}")
    @Operation(summary = "상품 수정", description = "상품 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "상품 없음")
    })
    public ProductResponse update(
            @Parameter(description = "상품 UUID") @PathVariable UUID productId,
            @Parameter(description = "요청자 UUID") @RequestHeader("X-Actor-Id") UUID actorId,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        Product updated = productUseCase.update(productId, request, actorId);
        return ProductResponse.from(updated);
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "상품 없음")
    })
    public ResponseEntity<Void> delete(@Parameter(description = "상품 UUID") @PathVariable UUID productId) {
        productUseCase.delete(productId);
        return ResponseEntity.noContent().build();
    }
}
