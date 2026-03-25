package com.example.demo.repositorios;

import com.example.demo.entidades.PriceData;
import com.example.demo.entidades.Active;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceDataRepository extends JpaRepository<PriceData, Long> {
    List<PriceData> findByActiveOrderByDateDesc(Active active);
    List<PriceData> findByActiveAndDateBetweenOrderByDateAsc(Active active, LocalDate startDate, LocalDate endDate);
    Optional<PriceData> findByActiveAndDate(Active active, LocalDate date);
    List<PriceData> findByActive(Active active);
    List<PriceData> findByActiveIdOrderByDateAsc(Long activeId);
    void deleteByActive(Active active);
}
