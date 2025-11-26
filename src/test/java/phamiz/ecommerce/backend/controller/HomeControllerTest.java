package phamiz.ecommerce.backend.controller;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import phamiz.ecommerce.backend.dto.Product.ProductDTO;
import phamiz.ecommerce.backend.service.IProductService;

@WebMvcTest(controllers = HomeController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class
})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
public class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IProductService productService;

    @MockBean
    private DataSource dataSource;

    @Test
    @DisplayName("GET /home/get-new - Should return list of 6 newest products")
    void shouldReturnNewestProducts() throws Exception {
        ProductDTO product1 = new ProductDTO();
        product1.setId(1L);
        product1.setProductName("Newest Product 1");

        ProductDTO product2 = new ProductDTO();
        product2.setId(2L);
        product2.setProductName("Newest Product 2");

        ProductDTO product3 = new ProductDTO();
        product3.setId(3L);
        product3.setProductName("Newest Product 3");

        List<ProductDTO> newProducts = Arrays.asList(product1, product2, product3);

        when(productService.getNewProduct()).thenReturn(newProducts);

        mockMvc.perform(get("/home/get-new")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].productName").value("Newest Product 1"))
                .andExpect(jsonPath("$[1].productName").value("Newest Product 2"))
                .andExpect(jsonPath("$[2].productName").value("Newest Product 3"));
    }

    @Test
    @DisplayName("GET /home/get-new - Should return empty list when no products")
    void shouldReturnEmptyListWhenNoProducts() throws Exception {
        when(productService.getNewProduct()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/home/get-new")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /home/get-random - Should return list of random products")
    void shouldReturnRandomProducts() throws Exception {
        ProductDTO product1 = new ProductDTO();
        product1.setId(10L);
        product1.setProductName("Random Product 1");

        List<ProductDTO> randomProducts = Collections.singletonList(product1);

        when(productService.getRandomProduct()).thenReturn(randomProducts);

        mockMvc.perform(get("/home/get-random")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productName").value("Random Product 1"));
    }
}
