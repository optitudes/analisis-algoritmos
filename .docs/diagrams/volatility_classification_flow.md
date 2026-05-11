%% Diagrama de mermaid.
flowchart TD
    A([Inicio: getRiskRanking]) --> B[Obtener todos los activos\ndel portafolio]
    B --> C[/"Para cada activo"/]

    subgraph Volatilidad ["Cálculo de volatilidad por activo"]
        C1[Obtener PriceData\nordenado por fecha ASC] --> C2{¿Tiene al menos\n2 registros?}
        C2 -- No --> C3[Saltar activo]
        C2 -- Sí --> C4["Extraer closePrices[]"]
        C4 --> C5["Calcular retornos logarítmicos\nr_i = ln(close_i / close_{i-1})"]
        C5 --> C6["Calcular media\nμ = (1/n) × Σ r_i"]
        C6 --> C7["Calcular desviación estándar\nσ = √((1/(n-1)) × Σ(r_i - μ)²)\n(corrección de Bessel)"]
        C7 --> C8["Anualizar volatilidad\nσ_anual = σ_diaria × √252"]
        C8 --> C9{Clasificar riesgo}
        C9 --> C10["σ_anual < 0.20\n→ CONSERVADOR"]
        C9 --> C11["0.20 ≤ σ_anual < 0.40\n→ MODERADO"]
        C9 --> C12["σ_anual ≥ 0.40\n→ AGRESIVO"]
        C10 --> C13[Crear VolatilityResult]
        C11 --> C13
        C12 --> C13
    end

    C --> C1
    C13 --> D{¿Quedan activos?}
    D -- Sí --> C
    D -- No --> E["Arreglo de VolatilityResult[]"]

    subgraph Ordenamiento ["QuickSort por volatilidad O(n log n)"]
        E1["Seleccionar pivote\n(último elemento)"] --> E2["Particionar: volatilidad < pivote\na la izquierda"]
        E2 --> E3["Colocar pivote\nen posición final"]
        E3 --> E4["Aplicar recursivamente\na ambas mitades"]
        E4 --> E5{¿Partición\nde tamaño > 1?}
        E5 -- Sí --> E1
        E5 -- No --> E6([Arreglo ordenado])
    end

    E --> E1

    E6 --> F["Contabilizar distribución\npor categoría"]
    F --> G["Construir respuesta:\n- ranking ordenado\n- totalActives\n- categoryDistribution"]
    G --> H([Retornar VolatilityRankingResponse])
