package com.example.demo.repositorios;

import com.example.demo.dto.PriceDataProjection;
import java.util.List;

public interface PriceDataRepositoryCustom {
    List<PriceDataProjection> fetchByActiveIdAndColumn(Long activeId, String column);
}
