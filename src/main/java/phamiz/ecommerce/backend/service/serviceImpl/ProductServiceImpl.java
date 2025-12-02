package phamiz.ecommerce.backend.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import phamiz.ecommerce.backend.dto.Product.CreateProductRequest;
import phamiz.ecommerce.backend.dto.Product.ProductDTO;
import phamiz.ecommerce.backend.dto.Product.ReviewDTO;
import phamiz.ecommerce.backend.exception.ProductException;
import phamiz.ecommerce.backend.model.*;
import phamiz.ecommerce.backend.repositories.*;
import phamiz.ecommerce.backend.service.IProductService;
import phamiz.ecommerce.backend.service.IUserService;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {
    private final IProductRepository productRepository;
    private final IUserService userService;
    private final ICategoryRepository categoryRepository;
    private final IProductImageRepository productImageRepository;
    private final IReviewRepository reviewRepository;
    private final IRatingRepository ratingRepository;

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Override
    public ProductDTO toDTO(Product product) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setProductName(product.getProduct_name());
        productDTO.setDescription(product.getDescription());
        productDTO.setQuantity(product.getQuantity());
        productDTO.setPrice(product.getPrice());
        productDTO.setBrand(product.getBrand());
        productDTO.setCategory(product.getCategory());
        productDTO.setCreatedAt(product.getCreatedAt());

        double totalRating = 0.0;
        for (Rating rating : product.getRatings()) {
            totalRating += rating.getRating();
        }
        productDTO.setRating(totalRating);
        List<ReviewDTO> listReviewDTO = new ArrayList<>();
        for (Review review : product.getReviews()) {
            ReviewDTO reviewDTO = new ReviewDTO();
            reviewDTO.setUserId(review.getUser().getId());
            reviewDTO.setComment(review.getReview());
            listReviewDTO.add(reviewDTO);
        }
        productDTO.setReviews(listReviewDTO);
        productDTO.setImages(product.getImages().stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList()));
        Set<String> colorNames = product.getProductColors().stream()
                .map(ProductColor::getColor_name)
                .collect(Collectors.toSet());
        productDTO.setProductColors(colorNames);

        return productDTO;
    }

    @Override
    @Transactional
    public List<ProductDTO> findAllProduct() {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            logger.warn("No product was found!");
        }
        logger.info("Find success");
        List<ProductDTO> listProductRespone = new ArrayList<>();
        for (Product product : products) {

            listProductRespone.add(toDTO(product));
        }
        return listProductRespone;
    }

    @Override
    public Product createProduct(CreateProductRequest request) {
        Category firstLevel = categoryRepository.findByCategoryName(request.getFirstLevelCategory());
        if (firstLevel == null) {
            Category firstLevelCategory = new Category();
            firstLevelCategory.setCategory_name(request.getFirstLevelCategory());
            firstLevelCategory.setLevel(1);
            firstLevel = categoryRepository.save(firstLevelCategory);
        }

        Category secondLevel = categoryRepository.findByNameAndParent(
                request.getSecondLevelCategory(), firstLevel.getCategory_name());
        if (secondLevel == null) {
            Category secondLevelCategory = new Category();
            secondLevelCategory.setCategory_name(request.getSecondLevelCategory());
            secondLevelCategory.setParent_category(firstLevel);
            secondLevelCategory.setLevel(2);
            secondLevel = categoryRepository.save(secondLevelCategory);
        }

        Product product = new Product();
        // Gán images và tự động set product_id cho từng image
        if (request.getImages() != null) {
            for (ProductImage image : request.getImages()) {
                product.addImage(image); // addImage đã set image.setProduct(this)
            }
        }
        product.setImages(request.getImages());

        product.setBrand(request.getBrand());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setCategory(secondLevel);
        product.setCreatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);
        logger.info("Create success product : ", savedProduct);
        return savedProduct;
    }

    @Override
    @Transactional
    public String deleteProduct(Long productId) throws ProductException {
        Product product = findProductById(productId);

        // Step 1: Delete all ProductImage records first
//        logger.info("Deleting product images for product ID: {}", productId);
//        productImageRepository.deleteByProductId(productId);
        // Clear tất cả collection

//        // Step 2: Delete all ProductColor records
        // Xóa colors thông qua Product

//        reviewRepository.deleteAllProductsReview(productId);
//        ratingRepository.deleteAllProductsRating(productId);
        // Step 3: Finally delete the product itself
        logger.info("Deleting product with ID: {}", productId);
        productRepository.deleteById(product.getId());

        logger.info("Product deleted successfully!");
        return "Product deleted success!";
    }

//    @Override
//    public Product updateProduct(Long productId, CreateProductRequest req) throws ProductException {
//        Product product = findProductById(productId);
//        product.setProduct_name(req.getTitle());
//        product.setProductColors(req.getColors());
//        product.setDescription(req.getDescription());
//        // product.setImages(req.getImages());
//        product.setBrand(req.getBrand());
//        product.setPrice(req.getPrice());
//        product.setQuantity(req.getQuantity());
//
//        product.setCategory(categoryRepository.findByCategoryName(req.getSecondLevelCategory()));
//        product.setCreatedAt(LocalDateTime.now());
//        // Gán images và tự động set product_id cho từng image
//        if (req.getImages() != null) {
//            for (ProductImage image : req.getImages()) {
//                product.addImage(image); // addImage đã set image.setProduct(this)
//            }
//        }
//
//        if (req.getQuantity() != 0) {
//            product.setQuantity(req.getQuantity());
//            logger.info("Product update success!");
//        }
//        return productRepository.save(product);
//    }

    @Override
    @Transactional
    public Product findProductById(Long id) throws ProductException {
        // Use @EntityGraph to load ALL related data in ONE query!
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product not found with id: " + id));
//
//        logger.info("Product found with full details (images, ratings, reviews, colors): {}", id);
//        return product;
        // Step 2: Fetch images riêng
        productRepository.findWithImages(id).ifPresent(p -> product.setImages(p.getImages()));

        // Step 3: Fetch ratings riêng
        productRepository.findWithRatings(id).ifPresent(p -> product.setRatings(p.getRatings()));

        // Step 4: Fetch reviews nếu cần
        // Nếu muốn, có thể tạo repository findWithReviews
        product.setReviews(reviewRepository.getAllProductsReview(id));
        // Step 5: Fetch productColors
        // Giả sử productColors được lazy load, ta có thể gọi getter để init
        product.getProductColors().size(); // force initialize

        logger.info("Product found safely with ID {} (images, ratings, reviews, colors loaded separately)", id);
        return product;
    }

    @Override
    public Page<ProductDTO> getAllProduct(String category, List<String> colors,
            Integer minPrice, Integer maxPrice,
            String sort, Integer pageNumber, Integer pageSize) throws ProductException {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<Product> products = productRepository.filterProducts(category, minPrice, maxPrice, sort);
        if (products.isEmpty()) {
            logger.error("Can not found any product");
            throw new ProductException("Can not found any product");
        }
        List<Product> listFilterProduct = new ArrayList<>();
        if (!colors.isEmpty()) {
            for (Product product : products) {
                for (ProductColor color : product.getProductColors()) {
                    if (colors.contains(color.getColor_name().toString())) {
                        listFilterProduct.add(product);
                        break;
                    }
                }
            }
        } else {
            listFilterProduct.addAll(products);
        }
        if (pageNumber < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("Invalid pageNumber or pageSize");
        }
        List<ProductDTO> listProductRespone = new ArrayList<>();
        for (Product product : listFilterProduct) {
            System.out.println(product.getProduct_name());
            listProductRespone.add(toDTO(product));
        }
        int startIndex = pageable.getPageNumber() * pageable.getPageSize();
        int endIndex = startIndex + pageable.getPageSize();
        endIndex = Math.min(endIndex, listProductRespone.size());
        List<ProductDTO> pageContent = listProductRespone.subList(startIndex, endIndex);
        Page<ProductDTO> pageResult = new PageImpl<>(pageContent, pageable, listProductRespone.size());
        return pageResult;
    }

    @Override
    public List<ProductDTO> getNewProduct() {
        List<Product> products = productRepository.findTop6ByOrderByCreatedAtDesc();
        if (products.isEmpty()) {
            logger.warn("No product was found!");
        }
        logger.info("Find success");
        List<ProductDTO> listProductRespone = new ArrayList<>();
        for (Product product : products) {

            listProductRespone.add(toDTO(product));
        }
        return listProductRespone;
    }

    @Override
    public List<ProductDTO> getRandomProduct() {
        List<Product> products = productRepository.findRandom6Products();
        if (products.isEmpty()) {
            logger.warn("No product was found!");
        }
        logger.info("Find success");
        List<ProductDTO> listProductRespone = new ArrayList<>();
        for (Product product : products) {

            listProductRespone.add(toDTO(product));
        }
        return listProductRespone;
    }

    @Override
    public void createProducts(List<CreateProductRequest> reqs) {
        for (CreateProductRequest req : reqs) {
            createProduct(req);
        }
    }
    @Override
    @Transactional
    public Product updateProduct(Long productId, CreateProductRequest req) throws ProductException {
        Product product = findProductById(productId);

        // Update cơ bản
        product.setProduct_name(req.getTitle());
        product.setDescription(req.getDescription());
        product.setBrand(req.getBrand());
        product.setPrice(req.getPrice());
        product.setQuantity(req.getQuantity());
        product.setCreatedAt(LocalDateTime.now());

        // Update colors
        if (req.getColors() != null) {
            product.setProductColors(req.getColors());
        }

        // Update images: Xóa ảnh cũ, thêm ảnh mới
        if (req.getImages() != null) {
            product.getImages().clear();
            for (ProductImage image : req.getImages()) {
                product.addImage(image); // addImage đã set product = this
            }
        }

        // Update category: xử lý parent category đúng
        Category firstLevel = categoryRepository.findByCategoryName(req.getFirstLevelCategory());
        if (firstLevel == null) {
            firstLevel = new Category();
            firstLevel.setCategory_name(req.getFirstLevelCategory());
            firstLevel.setLevel(1);
            firstLevel = categoryRepository.save(firstLevel);
        }

        Category secondLevel = categoryRepository.findByNameAndParent(req.getSecondLevelCategory(), firstLevel.getCategory_name());
        if (secondLevel == null) {
            secondLevel = new Category();
            secondLevel.setCategory_name(req.getSecondLevelCategory());
            secondLevel.setParent_category(firstLevel);
            secondLevel.setLevel(2);
            secondLevel = categoryRepository.save(secondLevel);
        }
        product.setCategory(secondLevel);

        Product updatedProduct = productRepository.save(product);
        logger.info("Product updated successfully: {}", updatedProduct.getId());
        return updatedProduct;
    }

}
