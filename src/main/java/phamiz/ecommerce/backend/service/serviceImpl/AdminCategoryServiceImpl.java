package phamiz.ecommerce.backend.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import phamiz.ecommerce.backend.dto.Category.CategoryDTO;
import phamiz.ecommerce.backend.dto.Category.CreateCategoryRequest;
import phamiz.ecommerce.backend.exception.ResourceNotFoundException;
import phamiz.ecommerce.backend.model.Category;
import phamiz.ecommerce.backend.repositories.ICategoryRepository;
import phamiz.ecommerce.backend.service.IAdminCategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCategoryServiceImpl implements IAdminCategoryService {

    private final ICategoryRepository categoryRepository;

    @Override
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryDTO createCategory(CreateCategoryRequest request) {
        Category category = new Category();
        category.setCategory_name(request.getName());
        category.setLevel(request.getLevel());

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent category not found with id: " + request.getParentId()));
            category.setParent_category(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Long id, CreateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setCategory_name(request.getName());
        category.setLevel(request.getLevel());

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent category not found with id: " + request.getParentId()));
            category.setParent_category(parent);
        } else {
            category.setParent_category(null);
        }

        Category updatedCategory = categoryRepository.save(category);
        return convertToDTO(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getCategory_name());
        dto.setLevel(category.getLevel());
        if (category.getParent_category() != null) {
            dto.setParentId(category.getParent_category().getId());
        }
        return dto;
    }
}
