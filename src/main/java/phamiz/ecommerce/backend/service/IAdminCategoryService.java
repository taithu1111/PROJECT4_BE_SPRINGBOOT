package phamiz.ecommerce.backend.service;

import phamiz.ecommerce.backend.dto.Category.CategoryDTO;
import phamiz.ecommerce.backend.dto.Category.CreateCategoryRequest;

import java.util.List;

public interface IAdminCategoryService {
    List<CategoryDTO> getAllCategories();

    CategoryDTO createCategory(CreateCategoryRequest request);

    CategoryDTO updateCategory(Long id, CreateCategoryRequest request);

    void deleteCategory(Long id);
}
