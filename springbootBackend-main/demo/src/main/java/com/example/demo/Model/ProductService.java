package com.example.demo.Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockTransactionService stockTransactionService;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public void addProduct(Product product, User user) {
        productRepository.save(product);
        logger.info("Recording stock transaction for new product ID: {}", product.getId());
        if (product.getQuantity() != null && product.getQuantity() > 0) {
            String batchId = UUID.randomUUID().toString();
            StockTransaction transaction = new StockTransaction(
                    product.getId(),
                    StockTransaction.TransactionType.ENTRY,
                    product.getQuantity(),
                    "Initial stock entry",
                    null, // No destinataire for entry
                    product.getSupplier(), // Use Product's Supplier for entry
                    batchId,
                    user
            );
            stockTransactionService.addTransaction(transaction, user);
            logger.info("Stock transaction recorded for product ID: {} with batchId: {} and supplier: {}", product.getId(), batchId,
                    product.getSupplier() != null ? product.getSupplier().getName() : "N/A");
        }
    }

    public void updateProduct(Product product, String transactionReason, Destinataire destinataire, User user) {
        Optional<Product> existingProductOpt = productRepository.findById(product.getId());
        if (existingProductOpt.isPresent()) {
            Product existingProduct = existingProductOpt.get();
            int oldQuantity = existingProduct.getQuantity() != null ? existingProduct.getQuantity() : 0;
            int newQuantity = product.getQuantity() != null ? product.getQuantity() : 0;

            // Update product details
            existingProduct.setProductName(product.getProductName());
            existingProduct.setCategory(product.getCategory());
            existingProduct.setQuantity(newQuantity);
            existingProduct.setSupplier(product.getSupplier());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setImage(product.getImage());
            existingProduct.setDate(product.getDate());

            productRepository.save(existingProduct);
            logger.info("Product updated: ID {}", product.getId());

            // Record stock transaction if quantity changed and no external transaction is provided
            if (oldQuantity != newQuantity && transactionReason != null) {
                int quantityChanged = Math.abs(newQuantity - oldQuantity);
                StockTransaction.TransactionType transactionType = newQuantity > oldQuantity ?
                        StockTransaction.TransactionType.ENTRY : StockTransaction.TransactionType.WITHDRAWAL;
                String batchId = UUID.randomUUID().toString();
                StockTransaction transaction = new StockTransaction(
                        product.getId(),
                        transactionType,
                        quantityChanged,
                        transactionReason != null ? transactionReason : "Quantity updated",
                        transactionType == StockTransaction.TransactionType.WITHDRAWAL ? destinataire : null,
                        transactionType == StockTransaction.TransactionType.ENTRY ? product.getSupplier() : null,
                        batchId,
                        user
                );
                stockTransactionService.addTransaction(transaction, user);
                logger.info("Stock transaction recorded for product ID: {} with batchId: {} and {}: {}", product.getId(), batchId,
                        transactionType == StockTransaction.TransactionType.WITHDRAWAL ? "destinataire" : "supplier",
                        transactionType == StockTransaction.TransactionType.WITHDRAWAL ?
                                (destinataire != null ? destinataire.getName() : "N/A") :
                                (product.getSupplier() != null ? product.getSupplier().getName() : "N/A"));
            }
        } else {
            throw new IllegalArgumentException("Product with ID " + product.getId() + " not found");
        }
    }

    public void updateProductQuantity(Long productId, int newQuantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setQuantity(newQuantity);
            productRepository.save(product);
            logger.info("Product quantity updated: ID {}, New Quantity: {}", productId, newQuantity);
        } else {
            throw new IllegalArgumentException("Product with ID " + productId + " not found");
        }
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}