package com.example.demo.Model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DestinataireService {

    @Autowired
    private DestinataireRepository repository;

    public List<Destinataire> getAllDestinataires() {
        return repository.findAll();
    }

    public Optional<Destinataire> getDestinataireById(Long id) {
        return repository.findById(id);
    }

    public Destinataire saveDestinataire(Destinataire destinataire) {
        return repository.save(destinataire);
    }
}