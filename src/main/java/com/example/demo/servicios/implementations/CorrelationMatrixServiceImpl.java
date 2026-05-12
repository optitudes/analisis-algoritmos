package com.example.demo.servicios.implementations;

import com.example.demo.dto.CorrelationMatrixDTO;
import com.example.demo.dto.PearsonCorrelationResoultDTO;
import com.example.demo.dto.PriceDataProjection;
import com.example.demo.entidades.Active;
import com.example.demo.repositorios.ActiveRepository;
import com.example.demo.repositorios.PriceDataRepository;
import com.example.demo.servicios.interfaces.CorrelationMatrixService;
import com.example.demo.servicios.interfaces.TimeSeriesSimilarityService;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CorrelationMatrixServiceImpl implements CorrelationMatrixService {

    private final ActiveRepository activeRepository;
    private final PriceDataRepository priceDataRepository;
    private final TimeSeriesSimilarityService timeSeriesSimilarityService;

    public CorrelationMatrixServiceImpl(
            ActiveRepository activeRepository,
            PriceDataRepository priceDataRepository,
            TimeSeriesSimilarityService timeSeriesSimilarityService) {
        this.activeRepository = activeRepository;
        this.priceDataRepository = priceDataRepository;
        this.timeSeriesSimilarityService = timeSeriesSimilarityService;
    }

    @Override
    public CorrelationMatrixDTO getCorrelationMatrix(String field) {
        long startTime = System.currentTimeMillis();

        List<Active> actives = activeRepository.findAll();
        List<String> labels = actives.stream().map(Active::getSymbol).toList();
        int n = actives.size();

        Map<Long, List<PriceDataProjection>> dataMap = new HashMap<>();
        for (Active active : actives) {
            dataMap.put(active.getId(),
                priceDataRepository.fetchByActiveIdAndColumn(active.getId(), field));
        }

        List<List<Double>> matrix = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            List<Double> row = new ArrayList<>(n);
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    row.add(1.0);
                } else if (j < i) {
                    row.add(matrix.get(j).get(i));
                } else {
                    List<PriceDataProjection> dataI = dataMap.get(actives.get(i).getId());
                    List<PriceDataProjection> dataJ = dataMap.get(actives.get(j).getId());

                    List<PriceDataProjection> alignedI = alignByDate(dataI, dataJ);
                    List<PriceDataProjection> alignedJ = alignByDate(dataJ, dataI);

                    if (alignedI.size() < 2) {
                        row.add(null);
                    } else {
                        try {
                            PearsonCorrelationResoultDTO pearson = timeSeriesSimilarityService
                                .getPearsonCorrelation(alignedI, alignedJ);
                            row.add(pearson.coefficient());
                        } catch (Exception e) {
                            row.add(null);
                        }
                    }
                }
            }
            matrix.add(row);
        }

        long executionTime = System.currentTimeMillis() - startTime;
        return new CorrelationMatrixDTO(labels, matrix, executionTime);
    }

    private List<PriceDataProjection> alignByDate(
            List<PriceDataProjection> data,
            List<PriceDataProjection> reference) {
        Set<LocalDate> referenceDates = reference.stream()
            .map(PriceDataProjection::date)
            .collect(Collectors.toSet());
        return data.stream()
            .filter(p -> referenceDates.contains(p.date()))
            .toList();
    }
}
