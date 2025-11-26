package phamiz.ecommerce.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import phamiz.ecommerce.backend.dto.Product.ProductDTO;
import phamiz.ecommerce.backend.exception.ProductException;
import phamiz.ecommerce.backend.model.Product;
import phamiz.ecommerce.backend.service.IProductService;

@WebMvcTest(controllers = ProductController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class
})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IProductService productService;

    @MockBean
    private DataSource dataSource;

    @Test
    @DisplayName("GET /api/product - Should return list of products")
    void shouldReturnListOfProducts() throws Exception {
        ProductDTO product1 = new ProductDTO();
        product1.setId(1L);
        product1.setProductName("Product 1");

        ProductDTO product2 = new ProductDTO();
        product2.setId(2L);
        product2.setProductName("Product 2");

        List<ProductDTO> productList = Arrays.asList(product1, product2);

        when(productService.findAllProduct()).thenReturn(productList);

        mockMvc.perform(get("/api/product")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productName").value("Product 1"))
                .andExpect(jsonPath("$[1].productName").value("Product 2"));
    }

    @Test
    @DisplayName("GET /api/product/filter - Should return paged products")
    void shouldReturnPagedProducts() throws Exception {
        ProductDTO product1 = new ProductDTO();
        product1.setId(1L);
        product1.setProductName("Filtered Product 1");

        List<ProductDTO> productList = Collections.singletonList(product1);
        Page<ProductDTO> productPage = new PageImpl<>(productList, PageRequest.of(0, 10), 1);

        when(productService.getAllProduct(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(productPage);

        mockMvc.perform(get("/api/product/filter")
                .param("pageNumber", "0")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].productName").value("Filtered Product 1"));
    }

    @Test
    @DisplayName("GET /api/product/{id} - Should return product when found")
    void shouldReturnProductWhenFound() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setProduct_name("Test Product");
        product.setPrice(100);
        product.setImages(new java.util.ArrayList<>());
        product.setProductColors(new java.util.HashSet<>());
        product.setReviews(new java.util.ArrayList<>());
        product.setRatings(new java.util.ArrayList<>());

        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(1L);
        productDTO.setProductName("Test Product");
        productDTO.setPrice(100);

        when(productService.findProductById(1L)).thenReturn(product);
        when(productService.toDTO(product)).thenReturn(productDTO);

        mockMvc.perform(get("/api/product/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productName").value("Test Product"))
                .andExpect(jsonPath("$.price").value(100));
    }

    @Test
    @DisplayName("GET /api/product/{id} - Should throw ProductException when not found")
    void shouldThrowExceptionWhenProductNotFound() throws Exception {
        when(productService.findProductById(999L))
                .thenThrow(new ProductException("Product not found with 999"));

        mockMvc.perform(get("/api/product/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
