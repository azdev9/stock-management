package com.example.demo.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.ORDINAL)
    private TransactionType transactionType;

    private int quantityChanged;
    private String reason;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @ManyToOne
    @JoinColumn(name = "destinataire_id")
    private Destinataire destinataire;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "batch_id")
    private String batchId;

    @Transient
    private String productName;

    public enum TransactionType {
        ENTRY, WITHDRAWAL
    }

    // No-arg constructor for JPA
    public StockTransaction() {
    }

    // Constructor for withdrawals
    public StockTransaction(Long productId, TransactionType transactionType, int quantityChanged, String reason, Destinataire destinataire, String batchId, User user) {
        this.productId = productId;
        this.transactionType = transactionType;
        this.quantityChanged = quantityChanged;
        this.reason = reason;
        this.destinataire = destinataire;
        this.supplier = null;
        this.batchId = batchId;
        this.transactionDate = LocalDateTime.now();
        this.user = user;
    }

    // Constructor for entries
    public StockTransaction(Long productId, TransactionType transactionType, int quantityChanged, String reason, Supplier supplier, String batchId, User user) {
        this.productId = productId;
        this.transactionType = transactionType;
        this.quantityChanged = quantityChanged;
        this.reason = reason;
        this.destinataire = null;
        this.supplier = supplier;
        this.batchId = batchId;
        this.transactionDate = LocalDateTime.now();
        this.user = user;
    }

    // Unified constructor for both entries and withdrawals
    public StockTransaction(Long productId, TransactionType transactionType, int quantityChanged, String reason, Destinataire destinataire, Supplier supplier, String batchId, User user) {
        this.productId = productId;
        this.transactionType = transactionType;
        this.quantityChanged = quantityChanged;
        this.reason = reason;
        this.destinataire = destinataire;
        this.supplier = supplier;
        this.batchId = batchId;
        this.transactionDate = LocalDateTime.now();
        this.user = user;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }
    public int getQuantityChanged() { return quantityChanged; }
    public void setQuantityChanged(int quantityChanged) { this.quantityChanged = quantityChanged; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
    public Destinataire getDestinataire() { return destinataire; }
    public void setDestinataire(Destinataire destinataire) { this.destinataire = destinataire; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Long getUserId() { return user != null ? user.getId() : null; }
}