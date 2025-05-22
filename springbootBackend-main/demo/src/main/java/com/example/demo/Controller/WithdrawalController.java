package com.example.demo.Controller;

import com.example.demo.Model.Product;
import com.example.demo.Model.ProductService;
import com.example.demo.Model.CategoryService;
import com.example.demo.Model.StockTransaction;
import com.example.demo.Model.StockTransactionService;
import com.example.demo.Model.Destinataire;
import com.example.demo.Model.DestinataireService;
import com.example.demo.Model.User; // Import User class
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder; // Import SecurityContextHolder
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/product")
public class WithdrawalController {

    private static final Logger logger = LoggerFactory.getLogger(WithdrawalController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StockTransactionService stockTransactionService;

    @Autowired
    private DestinataireService destinataireService;

    @GetMapping("/withdrawal")
    public String showWithdrawalForm(Model model, HttpServletRequest request) {
        logger.info("Showing withdrawal form, accessed via: {}", request.getRequestURI());
        model.addAttribute("withdrawalForm", new WithdrawalForm());
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("destinataires", destinataireService.getAllDestinataires());
        return "withdrawals";
    }

    @PostMapping("/withdrawal")
    public String processWithdrawal(
            @ModelAttribute("withdrawalForm") WithdrawalForm withdrawalForm,
            Model model) {
        logger.info("Processing withdrawal with form: transactionReason={}, productIds={}, quantities={}, destinataireId={}, newDestinataireName={}",
                withdrawalForm.getTransactionReason(),
                withdrawalForm.getProductIds(),
                withdrawalForm.getQuantities(),
                withdrawalForm.getDestinataireId(),
                withdrawalForm.getNewDestinataireName());
        try {
            // Get the authenticated user
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            logger.info("Withdrawal performed by user: {}", user.getUsername());

            String transactionReason = withdrawalForm.getTransactionReason();
            List<Long> productIds = withdrawalForm.getProductIds();
            List<Integer> quantities = withdrawalForm.getQuantities();
            Long destinataireId = withdrawalForm.getDestinataireId();
            String newDestinataireName = withdrawalForm.getNewDestinataireName();
            String newDestinataireEmail = withdrawalForm.getNewDestinataireEmail();
            String newDestinataireAddress = withdrawalForm.getNewDestinataireAddress();
            String destinataireOption = withdrawalForm.getDestinataireOption();

            if (transactionReason == null || transactionReason.trim().isEmpty()) {
                throw new IllegalArgumentException("Withdrawal reason is required");
            }
            if (productIds == null || productIds.isEmpty()) {
                throw new IllegalArgumentException("At least one product must be selected");
            }
            if (quantities == null || quantities.isEmpty()) {
                throw new IllegalArgumentException("No quantities provided");
            }
            if (quantities.size() < productIds.size()) {
                logger.warn("Quantities list size ({}) is less than productIds size ({})", quantities.size(), productIds.size());
                throw new IllegalArgumentException("Quantities list does not match selected products");
            }
            if ("existing".equals(destinataireOption) && (destinataireId == null || destinataireId == 0)) {
                throw new IllegalArgumentException("Please select an existing Destinataire");
            }
            if ("new".equals(destinataireOption) && (newDestinataireName == null || newDestinataireName.trim().isEmpty())) {
                throw new IllegalArgumentException("Please enter a new Destinataire name");
            }

            Destinataire destinataire;
            if ("existing".equals(destinataireOption)) {
                destinataire = destinataireService.getDestinataireById(destinataireId)
                        .orElseThrow(() -> new IllegalArgumentException("Destinataire not found: " + destinataireId));
            } else {
                destinataire = new Destinataire(
                        newDestinataireName.trim(),
                        newDestinataireEmail != null ? newDestinataireEmail.trim() : null,
                        newDestinataireAddress != null ? newDestinataireAddress.trim() : null
                );
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

            boolean hasValidQuantity = false;
            String batchId = UUID.randomUUID().toString(); // Generate batchId for this withdrawal
            for (int i = 0; i < productIds.size(); i++) {
                Long productId = productIds.get(i);
                Integer withdrawalQuantity = quantities.get(i);
                logger.debug("Processing product {} with quantity: {}", productId, withdrawalQuantity);

                if (withdrawalQuantity == null || withdrawalQuantity <= 0) {
                    logger.debug("Skipping product {}: quantity {} is not positive", productId, withdrawalQuantity);
                    continue;
                }

                Product product = productService.getProductById(productId)
                        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

                if (withdrawalQuantity > product.getQuantity()) {
                    throw new IllegalArgumentException("Withdrawal quantity exceeds available stock for product: " + product.getProductName());
                }

                // Update product quantity
                product.setQuantity(product.getQuantity() - withdrawalQuantity);
                productService.updateProduct(product, transactionReason, destinataire, user);

                // Create stock transaction with user
                StockTransaction transaction = new StockTransaction(
                        product.getId(),
                        StockTransaction.TransactionType.WITHDRAWAL,
                        withdrawalQuantity, // Integer to int conversion
                        transactionReason,
                        destinataire,
                        batchId,
                        user
                );
                transaction.setProductName(product.getProductName());
                logger.info("Creating transaction: productId={}, productName={}, quantity={}, type={}, batchId={}, user={}",
                        transaction.getProductId(), transaction.getProductName(), transaction.getQuantityChanged(), transaction.getTransactionType(), transaction.getBatchId(), user.getUsername());
                stockTransactionService.addTransaction(transaction, user);

                hasValidQuantity = true;
            }

            if (!hasValidQuantity) {
                throw new IllegalArgumentException("At least one product must have a valid withdrawal quantity");
            }

            logger.info("Withdrawal processed successfully, batchId={}", batchId);
            return "redirect:/product/stockwithdrawals?success=Withdrawal processed successfully";
        } catch (Exception e) {
            logger.error("Error processing withdrawal: {}", e.getMessage(), e);
            model.addAttribute("withdrawalForm", withdrawalForm);
            model.addAttribute("products", productService.getAllProducts());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("destinataires", destinataireService.getAllDestinataires());
            model.addAttribute("errorMessage", e.getMessage());
            return "withdrawals";
        }
    }
}