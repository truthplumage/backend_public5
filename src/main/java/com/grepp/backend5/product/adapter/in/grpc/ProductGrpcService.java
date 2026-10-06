package com.grepp.backend5.product.adapter.in.grpc;

import com.grepp.backend5.product.application.port.in.ProductUseCase;
import com.grepp.backend5.product.domain.Product;
import com.grepp.backend5.product.grpc.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;
import java.math.BigDecimal;

@Service
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {
    private final ProductUseCase productUseCase;

    public ProductGrpcService(ProductUseCase productUseCase) {
        this.productUseCase = productUseCase;
    }

    @Override
    public void createProduct(CreateProductRequest request, StreamObserver<ProductResponse> observer) {
        final Product input;
        try {
            UUID sellerId = parseUuid(request.getSellerId(), "seller_id");
            UUID creatorId = parseUuid(request.getCreatorId(), "creator_id");
            String name = request.getName();
            if (name.isBlank() || name.length() > 100) {
                throw new IllegalArgumentException("상품명은 공백이 아닌 값을 포함하고 100자 이하여야 합니다.");
            }
            BigDecimal price;
            try {
                price = new BigDecimal(request.getPrice()).setScale(2, java.math.RoundingMode.UNNECESSARY);
            } catch (ArithmeticException | NumberFormatException e) {
                throw new IllegalArgumentException("가격은 소수점 이하 최대 2자리의 숫자여야 합니다.");
            }
            if (price.signum() < 0 || price.precision() > 15) {
                throw new IllegalArgumentException("가격은 0 이상 9999999999999.99 이하여야 합니다.");
            }
            int stock = request.hasStock() ? request.getStock() : 0;
            if (stock < 0) throw new IllegalArgumentException("재고는 0 이상이어야 합니다.");
            String status = request.hasStatus() ? request.getStatus() : "ACTIVE";
            if (status.isBlank() || status.length() > 20) {
                throw new IllegalArgumentException("상태는 공백이 아닌 값을 포함하고 20자 이하여야 합니다.");
            }
            input = Product.create(sellerId, name,
                    request.hasDescription() ? request.getDescription() : null,
                    price, stock, status, creatorId);
        } catch (IllegalArgumentException e) {
            observer.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
            return;
        }
        try {
            observer.onNext(toResponse(productUseCase.create(input)));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(Status.INTERNAL.withDescription("상품 등록에 실패했습니다.").asRuntimeException());
        }
    }

    private UUID parseUuid(String value, String field) {
        try {
            UUID id = UUID.fromString(value);
            if (!id.toString().equalsIgnoreCase(value)) throw new IllegalArgumentException();
            return id;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(field + "는 올바른 UUID 형식이어야 합니다.");
        }
    }

    @Override
    public void getProduct(GetProductRequest request, StreamObserver<ProductResponse> observer) {
        final UUID id;
        try {
            id = UUID.fromString(request.getProductId());
            if (!id.toString().equalsIgnoreCase(request.getProductId())) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            observer.onError(Status.INVALID_ARGUMENT.withDescription("product_id는 올바른 UUID 형식이어야 합니다.").asRuntimeException());
            return;
        }
        try {
            observer.onNext(toResponse(productUseCase.getById(id)));
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(errorStatus(e).asRuntimeException());
        }
    }

    @Override
    public void listProducts(ListProductsRequest request, StreamObserver<ListProductsResponse> observer) {
        try {
            var response = ListProductsResponse.newBuilder();
            productUseCase.getAll().forEach(product -> response.addProducts(toResponse(product)));
            observer.onNext(response.build());
            observer.onCompleted();
        } catch (Exception e) {
            observer.onError(errorStatus(e).asRuntimeException());
        }
    }

    private Status errorStatus(Exception e) {
        if (e instanceof ResponseStatusException response && response.getStatusCode().value() == 404) {
            return Status.NOT_FOUND.withDescription("상품을 찾을 수 없습니다.");
        }
        return Status.INTERNAL.withDescription("상품 조회에 실패했습니다.");
    }

    private ProductResponse toResponse(Product product) {
        var response = ProductResponse.newBuilder()
                .setId(product.getId().toString())
                .setSellerId(product.getSellerId().toString())
                .setName(product.getName())
                .setPrice(product.getPrice().toPlainString())
                .setStock(product.getStock())
                .setStatus(product.getStatus());
        if (product.getDescription() != null) response.setDescription(product.getDescription());
        return response.build();
    }
}
