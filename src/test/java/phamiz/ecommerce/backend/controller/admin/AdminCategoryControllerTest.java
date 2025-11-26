package phamiz.ecommerce.backend.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import phamiz.ecommerce.backend.dto.Category.CategoryDTO;
import phamiz.ecommerce.backend.dto.Category.CreateCategoryRequest;
import phamiz.ecommerce.backend.exception.ResourceNotFoundException;
import phamiz.ecommerce.backend.service.IAdminCategoryService;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminCategoryController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
public class AdminCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IAdminCategoryService adminCategoryService;

    @MockBean
    private DataSource dataSource;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/admin/categories - Should return list of categories")
    void shouldReturnAllCategories() throws Exception {
        CategoryDTO cat1 = new CategoryDTO(1L, "Electronics", null, 1);
        CategoryDTO cat2 = new CategoryDTO(2L, "Laptops", 1L, 2);
        List<CategoryDTO> categories = Arrays.asList(cat1, cat2);

        when(adminCategoryService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/api/admin/categories")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("Electronics"))
                .andExpect(jsonPath("$[1].name").value("Laptops"));
    }

    @Test
    @DisplayName("POST /api/admin/categories - Should create category successfully")
    void shouldCreateCategory() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest("Smartphones", 1L, 2);
        CategoryDTO createdCategory = new CategoryDTO(3L, "Smartphones", 1L, 2);

        when(adminCategoryService.createCategory(any(CreateCategoryRequest.class))).thenReturn(createdCategory);

        mockMvc.perform(post("/api/admin/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Smartphones"));
    }

    @Test
    @DisplayName("PUT /api/admin/categories/{id} - Should update category successfully")
    void shouldUpdateCategory() throws Exception {
        Long id = 3L;
        CreateCategoryRequest request = new CreateCategoryRequest("Updated Phones", 1L, 2);
        CategoryDTO updatedCategory = new CategoryDTO(id, "Updated Phones", 1L, 2);

        when(adminCategoryService.updateCategory(eq(id), any(CreateCategoryRequest.class))).thenReturn(updatedCategory);

        mockMvc.perform(put("/api/admin/categories/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Phones"));
    }

    @Test
    @DisplayName("DELETE /api/admin/categories/{id} - Should delete category successfully")
    void shouldDeleteCategory() throws Exception {
        Long id = 3L;
        doNothing().when(adminCategoryService).deleteCategory(id);

        mockMvc.perform(delete("/api/admin/categories/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/admin/categories/{id} - Should return 404 when category not found")
    void shouldReturn404WhenDeleteNotFound() throws Exception {
        Long id = 999L;
        doThrow(new ResourceNotFoundException("Category not found with id: " + id))
                .when(adminCategoryService).deleteCategory(id);

        mockMvc.perform(delete("/api/admin/categories/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
