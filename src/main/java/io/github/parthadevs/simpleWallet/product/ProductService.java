package io.github.parthadevs.simpleWallet.product;

import io.github.parthadevs.simpleWallet.category.Category;
import io.github.parthadevs.simpleWallet.category.CategoryRepository;
import io.github.parthadevs.simpleWallet.common.exception.ResourceNotFoundException;
import io.github.parthadevs.simpleWallet.product.dto.CreateProductRequest;
import io.github.parthadevs.simpleWallet.product.dto.ProductResponse;
import io.github.parthadevs.simpleWallet.product.dto.ProductStockRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductStockRepository productStockRepository;

    public Page<ProductResponse> listProducts(Long categoryId, String search, boolean isActive, int page, int perPage) {
        int pageNumber = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(pageNumber, perPage, Sort.by("name").ascending());

        Page<Product> productPage;
        if (search != null && !search.isBlank()) {
            productPage = productRepository.searchByNameOrSlug(search, pageable);
        } else if (categoryId != null) {
            productPage = productRepository.findByIsActiveAndCategoryId(isActive, categoryId, pageable);
        } else {
            productPage = productRepository.findByIsActive(isActive, pageable);
        }

        List<ProductResponse> content = productPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return new org.springframework.data.domain.PageImpl<>(content, pageable, productPage.getTotalElements());
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToResponse(product);
    }

    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
        return mapToResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Product with slug '" + request.getSlug() + "' already exists.");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        Product product = Product.builder()
                .category(category)
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .price(request.getPrice())
                .currency(request.getCurrency())
                .availableQuantity(request.getAvailableQuantity() != null ? request.getAvailableQuantity() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Product saved = productRepository.save(product);

        // Add initial stock if payloads provided
        if (request.getStockItems() != null && !request.getStockItems().isEmpty()) {
            addStock(saved.getId(), new ProductStockRequest(request.getStockItems()));
        }

        return mapToResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, CreateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (request.getName() != null && !request.getName().isBlank()) {
            product.setName(request.getName());
        }
        if (request.getSlug() != null && !request.getSlug().isBlank()
                && !request.getSlug().equals(product.getSlug())
                && productRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Product with slug '" + request.getSlug() + "' already exists.");
        }
        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            product.setSlug(request.getSlug());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getCurrency() != null && !request.getCurrency().isBlank()) {
            product.setCurrency(request.getCurrency());
        }
        if (request.getAvailableQuantity() != null) {
            product.setAvailableQuantity(request.getAvailableQuantity());
        }
        if (request.getIsActive() != null) {
            product.setActive(request.getIsActive());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public void deactivateProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public void addStock(Long productId, ProductStockRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        int addedCount = 0;
        for (var item : request.getItems()) {
            ProductStock stock = ProductStock.builder()
                    .product(product)
                    .payload(item.getPayload())
                    .isSold(false)
                    .build();
            productStockRepository.save(stock);
            addedCount++;
        }

        product.setAvailableQuantity(product.getAvailableQuantity() + addedCount);
        productRepository.save(product);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .categorySlug(product.getCategory().getSlug())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .currency(product.getCurrency())
                .availableQuantity(product.getAvailableQuantity())
                .isActive(product.isActive())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
