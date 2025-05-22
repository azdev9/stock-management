package com.example.demo.Model;

import com.example.demo.Model.Product;
import com.example.demo.Model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySupplier(Supplier supplier);
    List<Product> findByCategory(Category category);
}