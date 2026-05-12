package com.example.demo.servicios.implementations;

import com.example.demo.dto.CandlestickDataDTO;
import com.example.demo.dto.CandlestickResponseDTO;
import com.example.demo.entidades.Active;
import com.example.demo.entidades.PriceData;
import com.example.demo.repositorios.ActiveRepository;
import com.example.demo.repositorios.PriceDataRepository;
import com.example.demo.servicios.interfaces.VisualizationService;
import com.example.demo.util.VisualizationAlgorithms;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de visualizaciones para el Requerimiento 4.
 */
@Service
public class VisualizationServiceImpl implements VisualizationService {

    private final ActiveRepository activeRepository;
    private final PriceDataRepository priceDataRepository;

    public VisualizationServiceImpl(ActiveRepository activeRepository, PriceDataRepository priceDataRepository) {
        this.activeRepository = activeRepository;
        this.priceDataRepository = priceDataRepository;
    }

    @Override
    public CandlestickResponseDTO getCandlestickData(Long activeId, int smaPeriod) {
        // 1. Buscar el activo
        Optional<Active> activeOpt = activeRepository.findById(activeId);
        if (activeOpt.isEmpty()) {
            return null; // Activo no encontrado
        }
        Active active = activeOpt.get();

        // 2. Obtener historial de precios ordenado por fecha ascendente
        List<PriceData> priceDataList = priceDataRepository.findByActiveIdOrderByDateAsc(activeId);
        
        if (priceDataList.isEmpty()) {
            // Retorna respuesta vacía pero válida si no hay datos
            return new CandlestickResponseDTO(
                    active.getId(), active.getSymbol(), active.getName(), smaPeriod, new ArrayList<>()
            );
        }

        // 3. Extraer solo los precios de cierre para calcular la SMA eficientemente
        double[] closePrices = new double[priceDataList.size()];
        for (int i = 0; i < priceDataList.size(); i++) {
            closePrices[i] = priceDataList.get(i).getClose();
        }

        // 4. Calcular la Media Móvil Simple de forma algorítmica
        Double[] smaValues = VisualizationAlgorithms.calculateSMA(closePrices, smaPeriod);

        // 5. Ensamblar la lista de DTOs mapeando Precio + SMA
        List<CandlestickDataDTO> dataList = new ArrayList<>(priceDataList.size());
        for (int i = 0; i < priceDataList.size(); i++) {
            PriceData pd = priceDataList.get(i);
            dataList.add(new CandlestickDataDTO(
                    pd.getDate().toString(),
                    pd.getOpen(),
                    pd.getHigh(),
                    pd.getLow(),
                    pd.getClose(),
                    pd.getVolume(),
                    smaValues[i]
            ));
        }

        // 6. Retornar envoltorio final
        return new CandlestickResponseDTO(
                active.getId(),
                active.getSymbol(),
                active.getName(),
                smaPeriod,
                dataList
        );
    }
}
