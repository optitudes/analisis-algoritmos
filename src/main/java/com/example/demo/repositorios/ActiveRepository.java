package com.example.demo.repositorios;

import com.example.demo.entidades.Active;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ActiveRepository extends JpaRepository<Active, Long> {
    boolean existsBySymbol(String symbol);
    Optional<Active> findBySymbol(String symbol);
}
