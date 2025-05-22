package com.example.demo.Controller;

import com.example.demo.Model.StockTransaction;
import com.example.demo.Model.Product;
import com.example.demo.Model.StockTransactionService;
import com.example.demo.Model.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/product/stockhistory")
public class StockHistoryController {

    private static final Logger logger = LoggerFactory.getLogger(StockHistoryController.class);

    @Autowired
    private StockTransactionService stockTransactionService;

    @Autowired
    private ProductService productService;

    // Temporarily disabled stock history endpoint
    /*
    @GetMapping
    public String getStockHistory(Model model, @RequestParam(value = "type", required = false) String type) {
        logger.info("Fetching stock history with type: {}", type);
        Map<String, List<StockTransaction>> groupedTransactions;
        String selectedType = type;

        try {
            if (type != null && !type.isEmpty()) {
                StockTransaction.TransactionType transactionType = StockTransaction.TransactionType.valueOf(type);
                groupedTransactions = stockTransactionService.getGroupedTransactionsByType(transactionType);
            } else {
                // Combine both entry and withdrawal transactions
                Map<String, List<StockTransaction>> entryTransactions = stockTransactionService.getGroupedTransactionsByType(StockTransaction.TransactionType.ENTRY);
                Map<String, List<StockTransaction>> withdrawalTransactions = stockTransactionService.getGroupedTransactionsByType(StockTransaction.TransactionType.WITHDRAWAL);
                groupedTransactions = new HashMap<>();
                groupedTransactions.putAll(entryTransactions);
                groupedTransactions.putAll(withdrawalTransactions);
                selectedType = null;
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid transaction type: {}", type);
            model.addAttribute("errorMessage", "Invalid transaction type: " + type);
            groupedTransactions = Collections.emptyMap();
            selectedType = null;
        } catch (Exception e) {
            logger.error("Error fetching transactions: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Failed to load stock history: " + e.getMessage());
            groupedTransactions = Collections.emptyMap();
            selectedType = null;
        }

        // Fetch products for transactions
        List<Long> productIds = groupedTransactions.values().stream()
                .flatMap(List::stream)
                .map(StockTransaction::getProductId)
                .filter(productId -> productId != null)
                .distinct()
                .collect(Collectors.toList());
        List<Product> products = productService.getAllProducts().stream()
                .filter(product -> productIds.contains(product.getId()))
                .collect(Collectors.toList());
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        model.addAttribute("groupedTransactions", groupedTransactions);
        model.addAttribute("productMap", productMap);
        model.addAttribute("selectedType", selectedType);
        return "stockhistory";
    }
    */
}