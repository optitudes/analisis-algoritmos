package com.example.demo.controllers;

import com.example.demo.dto.CandlestickResponseDTO;
import com.example.demo.servicios.interfaces.VisualizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST que expone los endpoints necesarios para el dashboard
 * interactivo especificado en el Requerimiento 4.
 */
@RestController
@RequestMapping("/api/visualization")
public class VisualizationController {

    private final VisualizationService visualizationService;

    public VisualizationController(VisualizationService visualizationService) {
        this.visualizationService = visualizationService;
    }

    /**
     * Retorna los datos necesarios para renderizar un gráfico de velas (candlestick chart)
     * e incluye el cálculo algorítmico de la Media Móvil Simple (SMA).
     *
     * <p>Permite configurar dinámicamente el período de la SMA mediante query param,
     * lo cual es ideal para dotar de interactividad al dashboard del frontend.</p>
     *
     * @param activeId el ID del activo financiero
     * @param smaPeriod el período para la SMA (opcional, valor por defecto: 20 días)
     * @return 200 OK con los datos, o 400 Bad Request si el período es inválido,
     *         o 404 Not Found si el activo no existe.
     */
    @GetMapping("/candlestick/{activeId}")
    public ResponseEntity<?> getCandlestickData(
            @PathVariable Long activeId,
            @RequestParam(required = false, defaultValue = "20") int smaPeriod) {

        if (smaPeriod < 1) {
            return ResponseEntity.badRequest()
                    .body("El período de la SMA (smaPeriod) debe ser mayor o igual a 1");
        }

        CandlestickResponseDTO response = visualizationService.getCandlestickData(activeId, smaPeriod);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }
}
