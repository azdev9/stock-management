package com.example.demo.Model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public Optional<Supplier> getSupplierById(Long id) {
        return supplierRepository.findById(id);
    }

    public void addSupplier(Supplier supplier) {
        supplierRepository.save(supplier);
    }

    public void updateSupplier(Supplier supplier) {
        supplierRepository.save(supplier);
    }

    @Transactional
    public void deleteSupplier(Long id) {
        Optional<Supplier> supplierOpt = supplierRepository.findById(id);
        if (supplierOpt.isPresent()) {
            Supplier supplier = supplierOpt.get();
            // Find all products associated with this supplier
            List<Product> products = productRepository.findBySupplier(supplier);
            // Set the supplier to null for all associated products
            for (Product product : products) {
                product.setSupplier(null);
                productRepository.save(product);
            }
            // Now delete the supplier
            supplierRepository.deleteById(id);
        }
    }
}