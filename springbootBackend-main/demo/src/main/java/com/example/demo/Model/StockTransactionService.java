package com.example.demo.Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StockTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(StockTransactionService.class);

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public List<StockTransaction> getAllTransactions() {
        try {
            logger.info("Fetching all stock transactions with users");
            TypedQuery<StockTransaction> query = entityManager.createQuery(
                    "SELECT st FROM StockTransaction st LEFT JOIN FETCH st.user", StockTransaction.class);
            List<StockTransaction> transactions = query.getResultList();
            logger.info("Retrieved {} transactions", transactions.size());
            transactions.forEach(t -> logger.info("Transaction ID: {}, User ID: {}, User: {}, Username: {}",
                    t.getId(),
                    t.getUser() != null ? t.getUser().getId() : "null",
                    t.getUser(),
                    t.getUser() != null && t.getUser().getUsername() != null ? t.getUser().getUsername() : "null"));
            return transactions;
        } catch (Exception e) {
            logger.error("Error fetching all transactions with JPQL: {}", e.getMessage(), e);
            logger.info("Falling back to repository findAll");
            List<StockTransaction> transactions = stockTransactionRepository.findAll();
            transactions.forEach(t -> logger.info("Fallback - Transaction ID: {}, User: {}, Username: {}",
                    t.getId(),
                    t.getUser(),
                    t.getUser() != null && t.getUser().getUsername() != null ? t.getUser().getUsername() : "null"));
            return transactions;
        }
    }

    public List<StockTransaction> getTransactionsByType(StockTransaction.TransactionType type) {
        try {
            logger.info("Fetching transactions of type: {} with users", type);
            TypedQuery<StockTransaction> query = entityManager.createQuery(
                    "SELECT st FROM StockTransaction st LEFT JOIN FETCH st.user WHERE st.transactionType = :type", StockTransaction.class);
            query.setParameter("type", type);
            List<StockTransaction> transactions = query.getResultList();
            logger.info("Retrieved {} transactions of type {}", transactions.size(), type);
            transactions.forEach(t -> logger.info("Transaction ID: {}, User ID: {}, User: {}, Username: {}",
                    t.getId(),
                    t.getUser() != null ? t.getUser().getId() : "null",
                    t.getUser(),
                    t.getUser() != null && t.getUser().getUsername() != null ? t.getUser().getUsername() : "null"));
            return transactions;
        } catch (Exception e) {
            logger.error("Error fetching transactions of type {} with JPQL: {}", type, e.getMessage(), e);
            logger.info("Falling back to repository findByTransactionType");
            List<StockTransaction> transactions = stockTransactionRepository.findByTransactionType(type);
            transactions.forEach(t -> logger.info("Fallback - Transaction ID: {}, User: {}, Username: {}",
                    t.getId(),
                    t.getUser(),
                    t.getUser() != null && t.getUser().getUsername() != null ? t.getUser().getUsername() : "null"));
            return transactions;
        }
    }

    public Map<String, List<StockTransaction>> getGroupedTransactionsByType(StockTransaction.TransactionType type) {
        try {
            logger.info("Grouping transactions by batchId for type: {}", type);
            List<StockTransaction> transactions = getTransactionsByType(type);
            Map<String, List<StockTransaction>> grouped = transactions.stream()
                    .filter(t -> t.getBatchId() != null)
                    .collect(Collectors.groupingBy(
                            StockTransaction::getBatchId,
                            Collectors.toList()
                    ));
            logger.info("Grouped {} transactions into {} batches for type {}", transactions.size(), grouped.size(), type);
            return grouped;
        } catch (Exception e) {
            logger.error("Error grouping transactions for type {}: {}", type, e.getMessage(), e);
            return Map.of();
        }
    }

    public void addTransaction(StockTransaction transaction, User user) {
        try {
            transaction.setUser(user);
            logger.info("Adding transaction for product ID: {}, type: {}, batchId: {}, user: {}",
                    transaction.getProductId(), transaction.getTransactionType(), transaction.getBatchId(), user != null ? user.getUsername() : "N/A");
            stockTransactionRepository.save(transaction);
            logger.debug("Transaction saved successfully");
        } catch (Exception e) {
            logger.error("Error saving transaction: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void flush() {
        try {
            logger.debug("Flushing entity manager");
            entityManager.flush();
        } catch (Exception e) {
            logger.error("Error flushing entity manager: {}", e.getMessage(), e);
        }
    }
}