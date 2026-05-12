package com.example.demo.servicios.interfaces;

import com.example.demo.dto.CorrelationMatrixDTO;

public interface CorrelationMatrixService {

    CorrelationMatrixDTO getCorrelationMatrix(String field);
}
