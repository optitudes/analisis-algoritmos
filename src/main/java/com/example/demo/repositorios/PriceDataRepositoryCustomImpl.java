package com.example.demo.repositorios;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;

import com.example.demo.dto.PriceDataProjection;
import com.example.demo.repositorios.PriceDataRepositoryCustom;

public class PriceDataRepositoryCustomImpl implements PriceDataRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    private static final List<String> ALLOWED_COLUMNS = List.of("open", "high", "low", "close", "volume");

    @Override
    public List<PriceDataProjection> fetchByActiveIdAndColumn(Long activeId, String column) {
        if (!ALLOWED_COLUMNS.contains(column.toLowerCase())) {
            throw new IllegalArgumentException("Columna no permitida: " + column);
        }

        String sql = "SELECT p.date AS date, p." + column + " AS valueObtained " +
                     "FROM price_data p WHERE p.active_id = :activeId ORDER BY p.date ASC";

        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("activeId", activeId)
                .getResultList();

        return rows.stream().map(row -> new PriceDataProjection(
            (LocalDate) row[0],
            (Number) row[1]
        )).toList();
    }
}
