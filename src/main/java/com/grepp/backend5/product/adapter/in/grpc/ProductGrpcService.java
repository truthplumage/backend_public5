package com.grepp.backend5.product.adapter.in.grpc;

import com.grepp.backend5.product.application.port.in.ProductUseCase;
import com.grepp.backend5.product.domain.Product;
import com.grepp.backend5.product.grpc.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@Service
public class ProductGrpcService extends ProductServiceGrpc.ProductServiceImplBase {
    private final ProductUseCase productUseCase;

    public ProductGrpcService(ProductUseCase productUseCase) {
        this.productUseCase = productUseCase;
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
            observer.onError(Status.INVALID_ARGUMENT.withDescription("product_id must be a UUID").asRuntimeException());
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
            return Status.NOT_FOUND.withDescription("Product not found");
        }
        return Status.INTERNAL.withDescription("Failed to query products");
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
