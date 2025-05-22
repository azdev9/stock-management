package com.example.demo.Controller;

import com.example.demo.Model.Supplier;
import com.example.demo.Model.SupplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/supplier")
public class SupplierController {

    private static final Logger logger = LoggerFactory.getLogger(SupplierController.class);

    @Autowired
    private SupplierService supplierService;

    // Display supplier list
    @GetMapping
    public String getAllSuppliers(Model model,
                                  @RequestParam(value = "error", required = false) String error,
                                  @RequestParam(value = "success", required = false) String success) {
        logger.info("Fetching all suppliers");
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("supplier", new Supplier()); // For form binding
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        if (success != null) {
            model.addAttribute("success", success);
        }
        return "supplier"; // Maps to supplier.html
    }

    // Add a new supplier
    @PostMapping("/add")
    public String addSupplier(@ModelAttribute Supplier supplier) {
        logger.info("Adding new supplier: {}", supplier.getName());
        try {
            supplierService.addSupplier(supplier);
            logger.info("Supplier added successfully: {}", supplier.getName());
            return "redirect:/supplier?success=Supplier added successfully";
        } catch (Exception e) {
            logger.error("Error adding supplier: {}", e.getMessage(), e);
            String errorMessage = URLEncoder.encode("Failed to add supplier: " + e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/supplier?error=" + errorMessage;
        }
    }

    // Fetch a supplier for editing
    @GetMapping("/{id}")
    @ResponseBody
    public Supplier getSupplierById(@PathVariable("id") Long id) {
        logger.info("Fetching supplier with ID: {}", id);
        return supplierService.getSupplierById(id).orElse(null);
    }

    // Update a supplier
    @PostMapping("/update")
    public String updateSupplier(@ModelAttribute Supplier supplier) {
        logger.info("Updating supplier with ID: {}", supplier.getId());
        try {
            supplierService.updateSupplier(supplier);
            logger.info("Supplier updated successfully: ID {}", supplier.getId());
            return "redirect:/supplier?success=Supplier updated successfully";
        } catch (Exception e) {
            logger.error("Error updating supplier with ID {}: {}", supplier.getId(), e.getMessage(), e);
            String errorMessage = URLEncoder.encode("Failed to update supplier: " + e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/supplier?error=" + errorMessage;
        }
    }

    // Delete a supplier
    @PostMapping("/delete/{id}")
    public String deleteSupplier(@PathVariable("id") Long id) {
        logger.info("Attempting to delete supplier with ID: {}", id);
        try {
            supplierService.deleteSupplier(id);
            logger.info("Supplier deleted successfully: ID {}", id);
            String successMessage = URLEncoder.encode("Supplier deleted successfully. Associated products have been updated.", StandardCharsets.UTF_8);
            return "redirect:/supplier?success=" + successMessage;
        } catch (Exception e) {
            logger.error("Error deleting supplier with ID {}: {}", id, e.getMessage(), e);
            String errorMessage = URLEncoder.encode("Failed to delete supplier: " + e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/supplier?error=" + errorMessage;
        }
    }
}