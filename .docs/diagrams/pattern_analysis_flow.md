%% Diagrama de mermaid.
flowchart TD
    A([Inicio: analyzePatterns]) --> B[Buscar activo por ID]
    B --> C{¿Activo encontrado?}
    C -- No --> D([Retornar lista vacía])
    C -- Sí --> E[Obtener PriceData\nordenado por fecha ASC]
    E --> F{¿Datos suficientes\npara la ventana?}
    F -- No --> D
    F -- Sí --> G[Extraer closePrices\ny volumes a arreglos]

    G --> H[Patrón 1: Bullish Streak]
    G --> I[Patrón 2: Volume Spike Reversal]

    subgraph SlidingWindow1 ["Sliding Window — Bullish Streak O(n×w)"]
        H1[/"Para i = 0 hasta n - w"/] --> H2["Ventana = close[i..i+w-1]"]
        H2 --> H3[Contar días consecutivos al alza]
        H3 --> H4{¿Existe racha\n≥ 3 días donde\nclose[j+1] > close[j]?}
        H4 -- Sí --> H5[Registrar ocurrencia\nen posición i]
        H4 -- No --> H6[Avanzar ventana]
        H5 --> H6
        H6 --> H7{¿Quedan\nventanas?}
        H7 -- Sí --> H1
        H7 -- No --> H8([Retornar lista de ocurrencias])
    end

    subgraph SlidingWindow2 ["Sliding Window — Volume Spike Reversal O(n×w)"]
        I1[/"Para i = 0 hasta n - w"/] --> I2["Ventana = data[i..i+w-1]"]
        I2 --> I3["Calcular μ_vol =\npromedio volumen ventana"]
        I3 --> I4[/"Para j interior\nde la ventana"/]
        I4 --> I5{"¿volume[j] ≥ 2 × μ_vol?"}
        I5 -- No --> I6[Siguiente j]
        I5 -- Sí --> I7{"¿signo(Δprecio_antes)\n≠ signo(Δprecio_después)?"}
        I7 -- No --> I6
        I7 -- Sí --> I8[Registrar ocurrencia\nen posición i]
        I6 --> I9{¿Quedan j?}
        I9 -- Sí --> I4
        I9 -- No --> I10[Avanzar ventana]
        I8 --> I10
        I10 --> I11{¿Quedan\nventanas?}
        I11 -- Sí --> I1
        I11 -- No --> I12([Retornar lista de ocurrencias])
    end

    H --> H1
    I --> I1

    H8 --> J[Construir PatternResult\nBullish Streak]
    I12 --> K[Construir PatternResult\nVolume Spike Reversal]

    J --> L["Calcular frecuencia(%) =\n(ocurrencias / totalVentanas) × 100"]
    K --> L

    L --> M([Retornar lista de PatternResult])
