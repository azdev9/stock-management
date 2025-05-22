package com.example.demo.Controller;

import com.example.demo.Model.*;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/product")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private DestinataireService destinataireService;

    @Autowired
    private StockTransactionService stockTransactionService;

    // Custom binder for Category and Supplier
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Category.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text == null || text.isEmpty()) {
                    setValue(null);
                } else {
                    try {
                        Long id = Long.parseLong(text);
                        categoryService.getCategoryById(id)
                                .ifPresentOrElse(
                                        category -> setValue(category),
                                        () -> {
                                            throw new IllegalArgumentException("Invalid category ID: " + id);
                                        }
                                );
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid category ID format: " + text);
                    }
                }
            }
        });

        binder.registerCustomEditor(Supplier.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text == null || text.isEmpty()) {
                    setValue(null);
                } else {
                    try {
                        Long id = Long.parseLong(text);
                        supplierService.getSupplierById(id)
                                .ifPresentOrElse(
                                        supplier -> setValue(supplier),
                                        () -> {
                                            throw new IllegalArgumentException("Invalid supplier ID: " + id);
                                        }
                                );
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid supplier ID format: " + text);
                    }
                }
            }
        });
    }

    // Display product list
    @GetMapping
    public String getAllProducts(Model model, @RequestParam(value = "error", required = false) String error) {
        logger.info("Fetching all products");
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products != null ? products : Collections.emptyList());
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("suppliers", supplierService.getAllSuppliers());
        model.addAttribute("destinataires", destinataireService.getAllDestinataires());
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        return "product";
    }

    // Show add product form
    @GetMapping("/addproduct")
    public String showAddProductForm(Model model) {
        logger.info("Showing add product form");
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("suppliers", supplierService.getAllSuppliers());
        return "addproduct";
    }

    // Add a new product
    @PostMapping("/add")
    public String addProduct(@Valid @ModelAttribute Product product, BindingResult result, Model model) {
        logger.info("Adding new product: {}", product.getProductName());
        if (result.hasErrors()) {
            logger.warn("Validation errors while adding product: {}", result.getAllErrors());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("suppliers", supplierService.getAllSuppliers());
            model.addAttribute("errorMessage", "Please correct the errors in the form");
            return "addproduct";
        }
        try {
            // Get the authenticated user
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            productService.addProduct(product, user);
            logger.info("Product added successfully: {}", product.getProductName());
            return "redirect:/product?success=Product added successfully";
        } catch (Exception e) {
            logger.error("Error adding product: {}", e.getMessage(), e);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("suppliers", supplierService.getAllSuppliers());
            model.addAttribute("errorMessage", "Failed to add product: " + e.getMessage());
            return "addproduct";
        }
    }

    // Fetch a product for editing
    @GetMapping("/{id:\\d+}")
    @ResponseBody
    public Product getProductById(@PathVariable("id") Long id) {
        logger.info("Fetching product with ID: {}", id);
        return productService.getProductById(id).orElse(null);
    }

    // Update a product
    @PostMapping("/update")
    public String updateProduct(@Valid @ModelAttribute Product product, BindingResult result,
                                @RequestParam(value = "transactionReason", required = false) String transactionReason,
                                @RequestParam(value = "destinataireId", required = false) Long destinataireId,
                                @RequestParam(value = "destinataireName", required = false) String destinataireName,
                                @RequestParam(value = "destinataireEmail", required = false) String destinataireEmail,
                                @RequestParam(value = "destinataireAddress", required = false) String destinataireAddress,
                                Model model) {
        logger.info("Updating product with ID: {}", product.getId());
        if (result.hasErrors()) {
            logger.warn("Validation errors while updating product: {}", result.getAllErrors());
            model.addAttribute("products", productService.getAllProducts());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("suppliers", supplierService.getAllSuppliers());
            model.addAttribute("destinataires", destinataireService.getAllDestinataires());
            model.addAttribute("errorMessage", "Please correct the errors in the form");
            return "product";
        }
        try {
            // Get the authenticated user
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Destinataire destinataire = null;
            if (destinataireId != null) {
                destinataire = destinataireService.getAllDestinataires().stream()
                        .filter(d -> d.getId().equals(destinataireId))
                        .findFirst()
                        .orElse(null);
            } else if (destinataireName != null && !destinataireName.isBlank()) {
                destinataire = new Destinataire(destinataireName, destinataireEmail, destinataireAddress);
                jakarta.validation.ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory();
                jakarta.validation.Validator validator = factory.getValidator();
                var violations = validator.validate(destinataire);
                if (!violations.isEmpty()) {
                    StringBuilder errorMessage = new StringBuilder("Destinataire validation errors: ");
                    violations.forEach(v -> errorMessage.append(v.getMessage()).append("; "));
                    throw new IllegalArgumentException(errorMessage.toString());
                }
                destinataire = destinataireService.saveDestinataire(destinataire);
            }
            productService.updateProduct(product, transactionReason, destinataire, user);
            logger.info("Product updated successfully: ID {}", product.getId());
            return "redirect:/product?success=Product updated successfully";
        } catch (Exception e) {
            logger.error("Error updating product with ID {}: {}", product.getId(), e.getMessage(), e);
            String errorMessage = URLEncoder.encode("Failed to update product: " + e.getMessage(), StandardCharsets.UTF_8);
            model.addAttribute("products", productService.getAllProducts());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("suppliers", supplierService.getAllSuppliers());
            model.addAttribute("destinataires", destinataireService.getAllDestinataires());
            model.addAttribute("errorMessage", "Failed to update product: " + e.getMessage());
            return "product";
        }
    }

    // Delete a product
    @PostMapping("/delete/{id:\\d+}")
    public String deleteProduct(@PathVariable("id") Long id) {
        logger.info("Attempting to delete product with ID: {}", id);
        try {
            productService.deleteProduct(id);
            logger.info("Product deleted successfully: ID {}", id);
            return "redirect:/product?success=Product deleted successfully";
        } catch (Exception e) {
            logger.error("Error deleting product with ID {}: {}", id, e.getMessage(), e);
            String errorMessage = URLEncoder.encode("Failed to delete product: " + e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/product?error=" + errorMessage;
        }
    }

    // Display stock entry history
    @GetMapping("/stockentries")
    @Transactional(readOnly = true)
    public String getStockEntries(Model model) {
        logger.info("Fetching stock entry transactions");
        Map<String, List<StockTransaction>> groupedTransactions = Collections.emptyMap();
        try {
            groupedTransactions = stockTransactionService.getGroupedTransactionsByType(StockTransaction.TransactionType.ENTRY);
            List<Product> products = productService.getAllProducts();
            Map<Long, Product> productMap = products != null ?
                    products.stream()
                            .filter(p -> p.getId() != null)
                            .collect(Collectors.toMap(Product::getId, p -> p)) :
                    Collections.emptyMap();
            model.addAttribute("groupedTransactions", groupedTransactions);
            model.addAttribute("productMap", productMap);
            // Debug transactions
            groupedTransactions.forEach((batchId, transactions) ->
                    transactions.forEach(t -> logger.info("StockEntries - Batch: {}, Transaction ID: {}, User: {}, Username: {}",
                            batchId, t.getId(), t.getUser(), t.getUser() != null ? t.getUser().getUsername() : "null")));
            logger.info("Rendering stockentries with {} batches", groupedTransactions.size());
            return "stockentries";
        } catch (Exception e) {
            logger.error("Error fetching stock entries: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Failed to load stock entries: " + e.getMessage());
            model.addAttribute("groupedTransactions", groupedTransactions);
            model.addAttribute("productMap", Collections.emptyMap());
            return "stockentries";
        }
    }

    // Display stock withdrawal history
    @GetMapping("/stockwithdrawals")
    @Transactional(readOnly = true)
    public String getStockWithdrawals(Model model) {
        logger.info("Fetching stock withdrawal transactions");
        Map<String, List<StockTransaction>> groupedTransactions = Collections.emptyMap();
        try {
            groupedTransactions = stockTransactionService.getGroupedTransactionsByType(StockTransaction.TransactionType.WITHDRAWAL);
            List<Product> products = productService.getAllProducts();
            Map<Long, Product> productMap = products != null ?
                    products.stream()
                            .filter(p -> p.getId() != null)
                            .collect(Collectors.toMap(Product::getId, p -> p)) :
                    Collections.emptyMap();
            model.addAttribute("groupedTransactions", groupedTransactions);
            model.addAttribute("productMap", productMap);
            // Debug transactions
            groupedTransactions.forEach((batchId, transactions) ->
                    transactions.forEach(t -> logger.info("StockWithdrawals - Batch: {}, Transaction ID: {}, User: {}, Username: {}",
                            batchId, t.getId(), t.getUser(), t.getUser() != null ? t.getUser().getUsername() : "null")));
            logger.info("Rendering stockwithdrawals with {} batches", groupedTransactions.size());
            return "stockwithdrawals";
        } catch (Exception e) {
            logger.error("Error fetching stock withdrawals: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Failed to load stock withdrawals: " + e.getMessage());
            model.addAttribute("groupedTransactions", groupedTransactions);
            model.addAttribute("productMap", Collections.emptyMap());
            return "stockwithdrawals";
        }
    }

    // Generate PDF for stock entry batch
    @GetMapping("/stockentries/batch/{batchId}/print")
    public ResponseEntity<ByteArrayResource> printStockEntryBatch(@PathVariable("batchId") String batchId) {
        logger.info("Generating PDF for stock entry batch ID: {}", batchId);
        try {
            List<StockTransaction> transactions = stockTransactionService.getAllTransactions().stream()
                    .filter(t -> t.getBatchId().equals(batchId) && t.getTransactionType() == StockTransaction.TransactionType.ENTRY)
                    .collect(Collectors.toList());
            if (transactions.isEmpty()) {
                throw new IllegalArgumentException("No transactions found for batch ID: " + batchId);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Stock Entry Batch Receipt")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("StockApp")
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(""));
            document.add(new Paragraph("Batch ID: " + batchId));

            for (StockTransaction transaction : transactions) {
                Product product = transaction.getProductId() != null ?
                        productService.getProductById(transaction.getProductId()).orElse(null) : null;

                document.add(new Paragraph("Transaction ID: " + transaction.getId())
                        .setFontSize(14)
                        .setBold());
                Table table = new Table(new float[]{150, 300});
                table.addHeaderCell(new Cell().add(new Paragraph("Field").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Value").setBold()));

                table.addCell(new Cell().add(new Paragraph("Product Name")));
                table.addCell(new Cell().add(new Paragraph(product != null ? product.getProductName() : "Product Deleted")));

                table.addCell(new Cell().add(new Paragraph("Quantity Changed")));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(transaction.getQuantityChanged()))));

                table.addCell(new Cell().add(new Paragraph("Supplier")));
                table.addCell(new Cell().add(new Paragraph(transaction.getSupplier() != null ? transaction.getSupplier().getName() : "N/A")));

                table.addCell(new Cell().add(new Paragraph("Date")));
                table.addCell(new Cell().add(new Paragraph(transaction.getTransactionDate() != null ?
                        transaction.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A")));

                table.addCell(new Cell().add(new Paragraph("Reason")));
                table.addCell(new Cell().add(new Paragraph(transaction.getReason() != null ? transaction.getReason() : "N/A")));

                table.addCell(new Cell().add(new Paragraph("Performed By")));
                table.addCell(new Cell().add(new Paragraph(transaction.getUser() != null ? transaction.getUser().getUsername() : "Unknown")));

                table.addCell(new Cell().add(new Paragraph("Role")));
                table.addCell(new Cell().add(new Paragraph(transaction.getUser() != null ? transaction.getUser().getRole() : "N/A")));

                document.add(table);
                document.add(new Paragraph(""));
            }

            document.close();

            byte[] pdfBytes = baos.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=stock_entry_batch_" + batchId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error generating PDF for batch {}: {}", batchId, e.getMessage(), e);
            throw new IllegalArgumentException("Failed to generate PDF: " + e.getMessage());
        }
    }

    // Generate PDF for stock withdrawal batch
    @GetMapping("/stockwithdrawals/batch/{batchId}/print")
    public ResponseEntity<ByteArrayResource> printStockWithdrawalBatch(@PathVariable("batchId") String batchId) {
        logger.info("Generating PDF for stock withdrawal batch ID: {}", batchId);
        try {
            List<StockTransaction> transactions = stockTransactionService.getAllTransactions().stream()
                    .filter(t -> t.getBatchId().equals(batchId) && t.getTransactionType() == StockTransaction.TransactionType.WITHDRAWAL)
                    .collect(Collectors.toList());
            if (transactions.isEmpty()) {
                throw new IllegalArgumentException("No transactions found for batch ID: " + batchId);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Stock Withdrawal Batch Receipt")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("StockApp")
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(""));
            document.add(new Paragraph("Batch ID: " + batchId));

            for (StockTransaction transaction : transactions) {
                Product product = transaction.getProductId() != null ?
                        productService.getProductById(transaction.getProductId()).orElse(null) : null;

                document.add(new Paragraph("Transaction ID: " + transaction.getId())
                        .setFontSize(14)
                        .setBold());
                Table table = new Table(new float[]{150, 300});
                table.addHeaderCell(new Cell().add(new Paragraph("Field").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Value").setBold()));

                table.addCell(new Cell().add(new Paragraph("Product Name")));
                table.addCell(new Cell().add(new Paragraph(product != null ? product.getProductName() : "Product Deleted")));

                table.addCell(new Cell().add(new Paragraph("Quantity Changed")));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(transaction.getQuantityChanged()))));

                table.addCell(new Cell().add(new Paragraph("Recipient")));
                table.addCell(new Cell().add(new Paragraph(transaction.getDestinataire() != null ? transaction.getDestinataire().getName() : "N/A")));

                table.addCell(new Cell().add(new Paragraph("Date")));
                table.addCell(new Cell().add(new Paragraph(transaction.getTransactionDate() != null ?
                        transaction.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A")));

                table.addCell(new Cell().add(new Paragraph("Reason")));
                table.addCell(new Cell().add(new Paragraph(transaction.getReason() != null ? transaction.getReason() : "N/A")));

                table.addCell(new Cell().add(new Paragraph("Performed By")));
                table.addCell(new Cell().add(new Paragraph(transaction.getUser() != null ? transaction.getUser().getUsername() : "Unknown")));

                table.addCell(new Cell().add(new Paragraph("Role")));
                table.addCell(new Cell().add(new Paragraph(transaction.getUser() != null ? transaction.getUser().getRole() : "N/A")));

                document.add(table);
                document.add(new Paragraph(""));
            }

            document.close();

            byte[] pdfBytes = baos.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=stock_withdrawal_batch_" + batchId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error generating PDF for batch {}: {}", batchId, e.getMessage(), e);
            throw new IllegalArgumentException("Failed to generate PDF: " + e.getMessage());
        }
    }

    // Generate PDF for all stock entry transactions
    @GetMapping("/stockentries/printall")
    public ResponseEntity<ByteArrayResource> printAllStockEntries() {
        logger.info("Generating PDF for all stock entry transactions");
        try {
            List<StockTransaction> transactions = stockTransactionService.getAllTransactions().stream()
                    .filter(t -> t.getTransactionType() == StockTransaction.TransactionType.ENTRY)
                    .collect(Collectors.toList());
            if (transactions.isEmpty()) {
                throw new IllegalArgumentException("No entry transactions found.");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("All Stock Entry Transactions")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("StockApp")
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(""));

            for (StockTransaction transaction : transactions) {
                Product product = transaction.getProductId() != null ?
                        productService.getProductById(transaction.getProductId()).orElse(null) : null;

                document.add(new Paragraph("Transaction ID: " + transaction.getId())
                        .setFontSize(14)
                        .setBold());
                Table table = new Table(new float[]{150, 300});
                table.addHeaderCell(new Cell().add(new Paragraph("Field").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Value").setBold()));

                table.addCell(new Cell().add(new Paragraph("Product Name")));
                table.addCell(new Cell().add(new Paragraph(product != null ? product.getProductName() : "Product Deleted")));

                table.addCell(new Cell().add(new Paragraph("Quantity Changed")));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(transaction.getQuantityChanged()))));

                table.addCell(new Cell().add(new Paragraph("Supplier")));
                table.addCell(new Cell().add(new Paragraph(transaction.getSupplier() != null ? transaction.getSupplier().getName() : "N/A")));

                table.addCell(new Cell().add(new Paragraph("Date")));
                table.addCell(new Cell().add(new Paragraph(transaction.getTransactionDate() != null ?
                        transaction.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A")));

                table.addCell(new Cell().add(new Paragraph("Reason")));
                table.addCell(new Cell().add(new Paragraph(transaction.getReason() != null ? transaction.getReason() : "N/A")));

                table.addCell(new Cell().add(new Paragraph("Performed By")));
                table.addCell(new Cell().add(new Paragraph(transaction.getUser() != null ? transaction.getUser().getUsername() : "Unknown")));

                table.addCell(new Cell().add(new Paragraph("Role")));
                table.addCell(new Cell().add(new Paragraph(transaction.getUser() != null ? transaction.getUser().getRole() : "N/A")));

                document.add(table);
                document.add(new Paragraph(""));
            }

            document.close();

            byte[] pdfBytes = baos.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=all_stock_entries.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error generating PDF for all stock entries: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to generate PDF: " + e.getMessage());
        }
    }

    // Generate PDF for all stock withdrawal transactions
    @GetMapping("/stockwithdrawals/printall")
    public ResponseEntity<ByteArrayResource> printAllStockWithdrawals() {
        logger.info("Generating PDF for all stock withdrawal transactions");
        try {
            List<StockTransaction> transactions = stockTransactionService.getAllTransactions().stream()
                    .filter(t -> t.getTransactionType() == StockTransaction.TransactionType.WITHDRAWAL)
                    .collect(Collectors.toList());
            if (transactions.isEmpty()) {
                throw new IllegalArgumentException("No withdrawal transactions found.");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("All Stock Withdrawal Transactions")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("StockApp")
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(""));

            for (StockTransaction transaction : transactions) {
                Product product = transaction.getProductId() != null ?
                        productService.getProductById(transaction.getProductId()).orElse(null) : null;

                document.add(new Paragraph("Transaction ID: " + transaction.getId())
                        .setFontSize(14)
                        .setBold());
                Table table = new Table(new float[]{150, 300});
                table.addHeaderCell(new Cell().add(new Paragraph("Field").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Value").setBold()));

                table.addCell(new Cell().add(new Paragraph("Product Name")));
                table.addCell(new Cell().add(new Paragraph(product != null ? product.getProductName() : "Product Deleted")));

                table.addCell(new Cell().add(new Paragraph("Quantity Changed")));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(transaction.getQuantityChanged()))));

                table.addCell(new Cell().add(new Paragraph("Recipient")));
                table.addCell(new Cell().add(new Paragraph(transaction.getDestinataire() != null ? transaction.getDestinataire().getName() : "N/A")));

                table.addCell(new Cell().add(new Paragraph("Date")));
                table.addCell(new Cell().add(new Paragraph(transaction.getTransactionDate() != null ?
                        transaction.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A")));

                table.addCell(new Cell().add(new Paragraph("Reason")));
                table.addCell(new Cell().add(new Paragraph(transaction.getReason() != null ? transaction.getReason() : "N/A")));

                table.addCell(new Cell().add(new Paragraph("Performed By")));
                table.addCell(new Cell().add(new Paragraph(transaction.getUser() != null ? transaction.getUser().getUsername() : "Unknown")));

                table.addCell(new Cell().add(new Paragraph("Role")));
                table.addCell(new Cell().add(new Paragraph(transaction.getUser() != null ? transaction.getUser().getRole() : "N/A")));

                document.add(table);
                document.add(new Paragraph(""));
            }

            document.close();

            byte[] pdfBytes = baos.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=all_stock_withdrawals.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error generating PDF for all stock withdrawals: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to generate PDF: " + e.getMessage());
        }
    }
}