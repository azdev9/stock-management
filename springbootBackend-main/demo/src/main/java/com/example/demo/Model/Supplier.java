package com.example.demo.Model;

import jakarta.persistence.*;

@Entity
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String contact;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        ACTIVE, INACTIVE
    }

    // Constructors
    public Supplier() {
    }

    public Supplier(String name, String contact, Status status) {
        this.name = name;
        this.contact = contact;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}