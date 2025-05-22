package com.example.demo.Controller;

import java.util.ArrayList;
import java.util.List;

public class WithdrawalForm {

    private List<Long> productIds = new ArrayList<>();
    private List<Integer> quantities = new ArrayList<>();
    private String transactionReason;
    private Long destinataireId;
    private String newDestinataireName;
    private String newDestinataireEmail;
    private String newDestinataireAddress;
    private String destinataireOption;

    // Getters and Setters
    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }

    public List<Integer> getQuantities() {
        return quantities;
    }

    public void setQuantities(List<Integer> quantities) {
        this.quantities = quantities;
    }

    public String getTransactionReason() {
        return transactionReason;
    }

    public void setTransactionReason(String transactionReason) {
        this.transactionReason = transactionReason;
    }

    public Long getDestinataireId() {
        return destinataireId;
    }

    public void setDestinataireId(Long destinataireId) {
        this.destinataireId = destinataireId;
    }

    public String getNewDestinataireName() {
        return newDestinataireName;
    }

    public void setNewDestinataireName(String newDestinataireName) {
        this.newDestinataireName = newDestinataireName;
    }

    public String getNewDestinataireEmail() {
        return newDestinataireEmail;
    }

    public void setNewDestinataireEmail(String newDestinataireEmail) {
        this.newDestinataireEmail = newDestinataireEmail;
    }

    public String getNewDestinataireAddress() {
        return newDestinataireAddress;
    }

    public void setNewDestinataireAddress(String newDestinataireAddress) {
        this.newDestinataireAddress = newDestinataireAddress;
    }

    public String getDestinataireOption() {
        return destinataireOption;
    }

    public void setDestinataireOption(String destinataireOption) {
        this.destinataireOption = destinataireOption;
    }
}