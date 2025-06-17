package com.example.demo.Controller;

import com.example.demo.Model.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class ContentController {

    @GetMapping("/req/login")
    public String showLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", logout);
        }
        return "login";
    }
    @GetMapping("batch-receipts")
    public String receipts() {
        return "batch-receipts";
    }

    @GetMapping("/req/add-user")
    public String signup() {
        return "add-user";
    }





    @GetMapping("/addproduct")
    public String showAddProductPage(Model model) {
        model.addAttribute("page", "addproduct");
        return "addproduct"; // Return the corresponding view for the category
    }
    @Autowired
    private ProductService productService;
    @GetMapping("/updateproduct")
    public String showUpdateProductPage(Model model) {
        model.addAttribute("page", "updateproduct");
        model.addAttribute("products", productService.getAllProducts());
        return "updateproduct";
    }


    @GetMapping("/stats")
    public String showStats() {
        return "stats";
    }

}
