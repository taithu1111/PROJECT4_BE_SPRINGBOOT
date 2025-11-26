package phamiz.ecommerce.backend.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import phamiz.ecommerce.backend.dto.ApiResponse;
import phamiz.ecommerce.backend.dto.Category.CategoryDTO;
import phamiz.ecommerce.backend.dto.Category.CreateCategoryRequest;
import phamiz.ecommerce.backend.service.IAdminCategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    private final IAdminCategoryService adminCategoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = adminCategoryService.getAllCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CreateCategoryRequest request) {
        CategoryDTO createdCategory = adminCategoryService.createCategory(request);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id,
            @RequestBody CreateCategoryRequest request) {
        CategoryDTO updatedCategory = adminCategoryService.updateCategory(id, request);
        return new ResponseEntity<>(updatedCategory, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Long id) {
        adminCategoryService.deleteCategory(id);
        ApiResponse response = new ApiResponse("Category deleted successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
