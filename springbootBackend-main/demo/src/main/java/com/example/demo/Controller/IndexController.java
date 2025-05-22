package com.example.demo.Controller;

import com.example.demo.Model.Product;
import com.example.demo.Model.ProductService;
import com.example.demo.Model.StockTransaction;
import com.example.demo.Model.StockTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class IndexController {
    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private ProductService productService;
    @Autowired
    private StockTransactionService stockTransactionService;

    @GetMapping({"/", "/index"})
    public String getDashboard(Model model, Authentication authentication) {
        try {
            // Fetch products
            var products = productService.getAllProducts();
            model.addAttribute("products", products != null ? products : Collections.emptyList());
            logger.debug("Fetched products: {}", products != null ? products.size() : 0);

            // Calculate totalProducts
            model.addAttribute("totalProducts", products != null ? products.size() : 0);

            // Calculate totalStockValue
            model.addAttribute("totalStockValue", products != null ?
                    products.stream().mapToDouble(p -> (p.getQuantity() != null ? p.getQuantity() : 0) *
                            (p.getPrice() != null ? p.getPrice() : 0)).sum() : 0.0);

            // Calculate lowStockCount
            model.addAttribute("lowStockCount", products != null ?
                    products.stream().filter(p -> p.getQuantity() != null && p.getQuantity() < 10).count() : 0);

            // Fetch recent transactions only for ADMIN
            List<StockTransaction> transactions = Collections.emptyList();
            if (authentication != null && authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                try {
                    transactions = stockTransactionService.getAllTransactions();
                    logger.debug("User: {}, Roles: {}", authentication.getName(), authentication.getAuthorities());
                } catch (Exception e) {
                    logger.error("Error fetching transactions", e);
                }
            } else {
                logger.debug("User: {}, Roles: {}, Skipping transactions fetch",
                        authentication != null ? authentication.getName() : "none",
                        authentication != null ? authentication.getAuthorities() : "none");
            }

            if (transactions != null && !transactions.isEmpty()) {
                logger.debug("Fetched transactions: {}", transactions.size());
                Map<Long, String> productMap = products != null ?
                        products.stream()
                                .filter(p -> p.getId() != null && p.getProductName() != null)
                                .collect(Collectors.toMap(Product::getId, Product::getProductName)) :
                        Collections.emptyMap();
                logger.debug("Product map size: {}", productMap.size());

                List<StockTransaction> enrichedTransactions = transactions.stream()
                        .filter(t -> t.getTransactionDate() != null)
                        .map(t -> {
                            t.setProductName(productMap.getOrDefault(t.getProductId(), "Unknown Product"));
                            logger.debug("Transaction productId: {}, set productName: {}", t.getProductId(), t.getProductName());
                            return t;
                        })
                        .sorted((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate()))
                        .limit(5)
                        .collect(Collectors.toList());
                model.addAttribute("recentTransactions", enrichedTransactions);
                logger.debug("Enriched transactions size: {}", enrichedTransactions.size());
            } else {
                model.addAttribute("recentTransactions", Collections.emptyList());
                logger.debug("No transactions found or user is not ADMIN");
            }
        } catch (Exception e) {
            logger.error("Error processing dashboard", e);
            model.addAttribute("errorMessage", "An error occurred while loading the dashboard. Please try again.");
            model.addAttribute("recentTransactions", Collections.emptyList());
        }

        return "index";
    }
}