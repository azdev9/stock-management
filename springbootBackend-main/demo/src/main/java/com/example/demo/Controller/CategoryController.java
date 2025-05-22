package com.example.demo.Controller;

import com.example.demo.Model.Category;
import com.example.demo.Model.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/category")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    // Display category list
    @GetMapping
    public String getAllCategories(Model model, @RequestParam(value = "error", required = false) String error) {
        logger.info("Fetching all categories");
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        return "category"; // Maps to category.html
    }

    // Fetch a single category by ID (for editing)
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Category> getCategoryById(@PathVariable("id") Long id) {
        logger.info("Fetching category with ID: {}", id);
        Optional<Category> category = categoryService.getCategoryById(id);
        if (category.isPresent()) {
            return ResponseEntity.ok(category.get());
        } else {
            logger.warn("Category not found with ID: {}", id);
            return ResponseEntity.status(404).body(null);
        }
    }

    // Add a new category
    @PostMapping("/add")
    public String addCategory(@ModelAttribute Category category) {
        logger.info("Adding new category: {}", category.getName());
        try {
            categoryService.addCategory(category);
            logger.info("Category added successfully: {}", category.getName());
            return "redirect:/category";
        } catch (Exception e) {
            logger.error("Error adding category: {}", e.getMessage(), e);
            return "redirect:/category?error=Failed to add category: " + e.getMessage();
        }
    }

    // Save or update a category
    @PostMapping("/save")
    public String saveCategory(@ModelAttribute Category category) {
        logger.info("Saving category with ID: {}", category.getId());
        try {
            categoryService.updateCategory(category);
            logger.info("Category updated successfully: ID {}", category.getId());
            return "redirect:/category";
        } catch (Exception e) {
            logger.error("Error updating category with ID {}: {}", category.getId(), e.getMessage(), e);
            return "redirect:/category?error=Failed to update category: " + e.getMessage();
        }
    }

    // Delete category
    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id) {
        logger.info("Attempting to delete category with ID: {}", id);
        try {
            Optional<Category> category = categoryService.getCategoryById(id);
            if (!category.isPresent()) {
                throw new IllegalArgumentException("Category not found with ID: " + id);
            }
            categoryService.deleteCategory(id);
            logger.info("Category deleted successfully: ID {}", id);
            return "redirect:/category";
        } catch (Exception e) {
            logger.error("Error deleting category with ID {}: {}", id, e.getMessage(), e);
            return "redirect:/category?error=Failed to delete category: " + e.getMessage();
        }
    }
}