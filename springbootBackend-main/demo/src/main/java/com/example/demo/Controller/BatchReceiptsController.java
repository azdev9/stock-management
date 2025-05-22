package com.example.demo.Controller;

import com.example.demo.Model.Category;
import com.example.demo.Model.Product;
import com.example.demo.Model.StockTransaction;
import com.example.demo.Model.Supplier;
import com.example.demo.Model.CategoryRepository;
import com.example.demo.Model.ProductRepository;
import com.example.demo.Model.StockTransactionRepository;
import com.example.demo.Model.SupplierRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.ByteArrayOutputStream;

@Controller
public class BatchReceiptsController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    @GetMapping("/reports/batch-receipts")
    public String showBatchReceiptsPage(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("suppliers", supplierRepository.findAll());
        return "batch-receipts";
    }

    @PostMapping("/reports/batch-receipts")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateReceipt(
            @RequestParam("reportType") String reportType,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "supplierId", required = false) Long supplierId) {

        Map<String, Object> response = new HashMap<>();
        List<String> headers = new ArrayList<>();
        List<List<String>> data = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        try {
            switch (reportType) {
                case "allProducts":
                    headers.addAll(Arrays.asList("Product Name", "Category", "Quantity", "Price"));
                    List<Product> allProducts = productRepository.findAll();
                    for (Product product : allProducts) {
                        data.add(Arrays.asList(
                                product.getProductName() != null ? product.getProductName() : "N/A",
                                product.getCategory() != null ? product.getCategory().getName() : "N/A",
                                String.valueOf(product.getQuantity() != null ? product.getQuantity() : 0),
                                String.format("$%.2f", product.getPrice() != null ? product.getPrice() : 0.0)
                        ));
                    }
                    response.put("title", "All Products Receipt");
                    break;

                case "byCategory":
                    if (categoryId == null) {
                        response.put("error", "Category ID is required for this report type.");
                        return ResponseEntity.badRequest().body(response);
                    }
                    Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
                    if (!categoryOpt.isPresent()) {
                        response.put("error", "Selected category not found.");
                        return ResponseEntity.badRequest().body(response);
                    }
                    headers.addAll(Arrays.asList("Product Name", "Category", "Quantity", "Price"));
                    List<Product> productsByCategory = productRepository.findByCategory(categoryOpt.get());
                    for (Product product : productsByCategory) {
                        data.add(Arrays.asList(
                                product.getProductName() != null ? product.getProductName() : "N/A",
                                product.getCategory() != null ? product.getCategory().getName() : "N/A",
                                String.valueOf(product.getQuantity() != null ? product.getQuantity() : 0),
                                String.format("$%.2f", product.getPrice() != null ? product.getPrice() : 0.0)
                        ));
                    }
                    response.put("title", "Products by Category: " + categoryOpt.get().getName());
                    break;

                case "bySupplier":
                    if (supplierId == null) {
                        response.put("error", "Supplier ID is required for this report type.");
                        return ResponseEntity.badRequest().body(response);
                    }
                    Optional<Supplier> supplierOpt = supplierRepository.findById(supplierId);
                    if (!supplierOpt.isPresent()) {
                        response.put("error", "Selected supplier not found.");
                        return ResponseEntity.badRequest().body(response);
                    }
                    headers.addAll(Arrays.asList("Product Name", "Supplier", "Quantity", "Price"));
                    List<Product> productsBySupplier = productRepository.findBySupplier(supplierOpt.get());
                    for (Product product : productsBySupplier) {
                        data.add(Arrays.asList(
                                product.getProductName() != null ? product.getProductName() : "N/A",
                                product.getSupplier() != null ? product.getSupplier().getName() : "N/A",
                                String.valueOf(product.getQuantity() != null ? product.getQuantity() : 0),
                                String.format("$%.2f", product.getPrice() != null ? product.getPrice() : 0.0)
                        ));
                    }
                    response.put("title", "Products by Supplier: " + supplierOpt.get().getName());
                    break;

                case "allWithdrawals":
                    headers.addAll(Arrays.asList("Product Name", "Transaction Type", "Quantity Changed", "Reason", "Date"));
                    List<StockTransaction> withdrawals = stockTransactionRepository.findByTransactionType(StockTransaction.TransactionType.WITHDRAWAL);
                    for (StockTransaction transaction : withdrawals) {
                        String prodName = transaction.getProductName() != null ? transaction.getProductName() :
                                productRepository.findById(transaction.getProductId())
                                        .map(Product::getProductName)
                                        .orElse("N/A");
                        data.add(Arrays.asList(
                                prodName,
                                transaction.getTransactionType() != null ? transaction.getTransactionType().name() : "N/A",
                                String.valueOf(transaction.getQuantityChanged()),
                                transaction.getReason() != null ? transaction.getReason() : "N/A",
                                transaction.getTransactionDate() != null ? transaction.getTransactionDate().format(formatter) : "N/A"
                        ));
                    }
                    response.put("title", "All Withdrawals Receipt");
                    break;

                case "allEntries":
                    headers.addAll(Arrays.asList("Product Name", "Transaction Type", "Quantity Changed", "Supplier", "Date", "Reason"));
                    List<StockTransaction> entries = stockTransactionRepository.findByTransactionType(StockTransaction.TransactionType.ENTRY);
                    for (StockTransaction transaction : entries) {
                        String prodName = transaction.getProductName() != null ? transaction.getProductName() :
                                productRepository.findById(transaction.getProductId())
                                        .map(Product::getProductName)
                                        .orElse("N/A");
                        String supplierName = transaction.getSupplier() != null ? transaction.getSupplier().getName() : "N/A";
                        data.add(Arrays.asList(
                                prodName,
                                transaction.getTransactionType() != null ? transaction.getTransactionType().name() : "N/A",
                                String.valueOf(transaction.getQuantityChanged()),
                                supplierName,
                                transaction.getTransactionDate() != null ? transaction.getTransactionDate().format(formatter) : "N/A",
                                transaction.getReason() != null ? transaction.getReason() : "N/A"
                        ));
                    }
                    response.put("title", "All Entries Receipt");
                    break;

                default:
                    response.put("error", "Invalid report type.");
                    return ResponseEntity.badRequest().body(response);
            }

            response.put("headers", headers);
            response.put("data", data);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Failed to generate receipt: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/reports/batch-receipts/print")
    public ResponseEntity<ByteArrayResource> printBatchReceipt(
            @RequestParam("reportType") String reportType,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "supplierId", required = false) Long supplierId) {

        List<String> headers = new ArrayList<>();
        List<List<String>> data = new ArrayList<>();
        String title = "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        try {
            switch (reportType) {
                case "allProducts":
                    headers.addAll(Arrays.asList("Product Name", "Category", "Quantity", "Price"));
                    List<Product> allProducts = productRepository.findAll();
                    for (Product product : allProducts) {
                        data.add(Arrays.asList(
                                product.getProductName() != null ? product.getProductName() : "N/A",
                                product.getCategory() != null ? product.getCategory().getName() : "N/A",
                                String.valueOf(product.getQuantity() != null ? product.getQuantity() : 0),
                                String.format("$%.2f", product.getPrice() != null ? product.getPrice() : 0.0)
                        ));
                    }
                    title = "All Products Receipt";
                    break;

                case "byCategory":
                    if (categoryId == null) {
                        throw new IllegalArgumentException("Category ID is required for this report type.");
                    }
                    Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
                    if (!categoryOpt.isPresent()) {
                        throw new IllegalArgumentException("Selected category not found.");
                    }
                    headers.addAll(Arrays.asList("Product Name", "Category", "Quantity", "Price"));
                    List<Product> productsByCategory = productRepository.findByCategory(categoryOpt.get());
                    for (Product product : productsByCategory) {
                        data.add(Arrays.asList(
                                product.getProductName() != null ? product.getProductName() : "N/A",
                                product.getCategory() != null ? product.getCategory().getName() : "N/A",
                                String.valueOf(product.getQuantity() != null ? product.getQuantity() : 0),
                                String.format("$%.2f", product.getPrice() != null ? product.getPrice() : 0.0)
                        ));
                    }
                    title = "Products by Category: " + categoryOpt.get().getName();
                    break;

                case "bySupplier":
                    if (supplierId == null) {
                        throw new IllegalArgumentException("Supplier ID is required for this report type.");
                    }
                    Optional<Supplier> supplierOpt = supplierRepository.findById(supplierId);
                    if (!supplierOpt.isPresent()) {
                        throw new IllegalArgumentException("Selected supplier not found.");
                    }
                    headers.addAll(Arrays.asList("Product Name", "Supplier", "Quantity", "Price"));
                    List<Product> productsBySupplier = productRepository.findBySupplier(supplierOpt.get());
                    for (Product product : productsBySupplier) {
                        data.add(Arrays.asList(
                                product.getProductName() != null ? product.getProductName() : "N/A",
                                product.getSupplier() != null ? product.getSupplier().getName() : "N/A",
                                String.valueOf(product.getQuantity() != null ? product.getQuantity() : 0),
                                String.format("$%.2f", product.getPrice() != null ? product.getPrice() : 0.0)
                        ));
                    }
                    title = "Products by Supplier: " + supplierOpt.get().getName();
                    break;

                case "allWithdrawals":
                    headers.addAll(Arrays.asList("Product Name", "Transaction Type", "Quantity Changed", "Reason", "Date"));
                    List<StockTransaction> withdrawals = stockTransactionRepository.findByTransactionType(StockTransaction.TransactionType.WITHDRAWAL);
                    for (StockTransaction transaction : withdrawals) {
                        String prodName = transaction.getProductName() != null ? transaction.getProductName() :
                                productRepository.findById(transaction.getProductId())
                                        .map(Product::getProductName)
                                        .orElse("N/A");
                        data.add(Arrays.asList(
                                prodName,
                                transaction.getTransactionType() != null ? transaction.getTransactionType().name() : "N/A",
                                String.valueOf(transaction.getQuantityChanged()),
                                transaction.getReason() != null ? transaction.getReason() : "N/A",
                                transaction.getTransactionDate() != null ? transaction.getTransactionDate().format(formatter) : "N/A"
                        ));
                    }
                    title = "All Withdrawals Receipt";
                    break;

                case "allEntries":
                    headers.addAll(Arrays.asList("Product Name", "Transaction Type", "Quantity Changed", "Supplier", "Date", "Reason"));
                    List<StockTransaction> entries = stockTransactionRepository.findByTransactionType(StockTransaction.TransactionType.ENTRY);
                    for (StockTransaction transaction : entries) {
                        String prodName = transaction.getProductName() != null ? transaction.getProductName() :
                                productRepository.findById(transaction.getProductId())
                                        .map(Product::getProductName)
                                        .orElse("N/A");
                        String supplierName = transaction.getSupplier() != null ? transaction.getSupplier().getName() : "N/A";
                        data.add(Arrays.asList(
                                prodName,
                                transaction.getTransactionType() != null ? transaction.getTransactionType().name() : "N/A",
                                String.valueOf(transaction.getQuantityChanged()),
                                supplierName,
                                transaction.getTransactionDate() != null ? transaction.getTransactionDate().format(formatter) : "N/A",
                                transaction.getReason() != null ? transaction.getReason() : "N/A"
                        ));
                    }
                    title = "All Entries Receipt";
                    break;

                default:
                    throw new IllegalArgumentException("Invalid report type.");
            }

            // Generate PDF
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph(title)
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("StockApp")
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(""));

            // Create table with dynamic columns
            float[] columnWidths = new float[headers.size()];
            Arrays.fill(columnWidths, 100f); // Adjust column widths as needed
            Table table = new Table(columnWidths);
            for (String header : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(header).setBold()));
            }
            for (List<String> row : data) {
                for (String cell : row) {
                    table.addCell(new Cell().add(new Paragraph(cell)));
                }
            }
            document.add(table);

            document.close();

            byte[] pdfBytes = baos.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=batch_receipt_" + reportType + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(resource);

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to generate PDF: " + e.getMessage());
        }
    }
}