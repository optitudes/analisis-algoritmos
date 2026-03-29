%% Diagrama de mermaid.
flowchart TD
    A([Inicio: fetchHistoricalData]) --> B[Buscar activo por símbolo]
    B --> C{¿Activo encontrado?}
    C -- No --> D([Retornar error: activo no encontrado])
    C -- Sí --> E[Eliminar datos previos del activo]
    E --> F[Definir rango de fechas\n2019-01-01 → 2025-01-01]
    F --> G[Construir URL de Yahoo Finance\ncon hostname y símbolo codificado]
    G --> H[fetchWithRetry: llamar a la API]

    subgraph Reintentos [fetchWithRetry - hasta 5 intentos]
        H1[Ejecutar GET con headers HTTP] --> H2{¿Respuesta exitosa?}
        H2 -- Sí --> H3([Retornar respuesta])
        H2 -- Error 429 --> H4[Marcar rate limited\nEsperar tiempo exponencial]
        H2 -- Otro error --> H5[Esperar tiempo exponencial]
        H4 --> H6{¿Quedan intentos?}
        H5 --> H6
        H6 -- Sí --> H1
        H6 -- No --> H7{¿Fue rate limited?}
        H7 -- Sí --> H8[Esperar 30 segundos]
        H7 -- No --> H9([Retornar null])
        H8 --> H9
    end

    H --> I{¿Respuesta vacía o null?}
    I -- Sí --> J([Retornar error: respuesta vacía])
    I -- No --> K[Parsear JSON\nchart → result]
    K --> L{¿Hay resultados?}
    L -- No --> M([Retornar error de Yahoo Finance])
    L -- Sí --> N[Extraer timestamps,\nquote y adjclose]
    N --> O{¿Hay timestamps?}
    O -- No --> P([Retornar error: símbolo inválido])
    O -- Sí --> Q[Iterar sobre cada dato]

    subgraph Iteracion [Por cada punto de datos]
        Q1{¿Índice fuera de rango\nen algún campo?} -- Sí --> Q2[Saltar registro]
        Q1 -- No --> Q3[Convertir timestamp a fecha\nzona horaria New York]
        Q3 --> Q4[Leer open, high, low,\nclose, volume]
        Q4 --> Q5{¿Usar adjclose?}
        Q5 -- Sí --> Q6[Asignar adjclose como close]
        Q5 -- No --> Q7[Usar close directo]
        Q6 --> Q8{¿Todos los valores\nson cero?}
        Q7 --> Q8
        Q8 -- Sí --> Q2
        Q8 -- No --> Q9[Crear objeto PriceData\ny agregar a lista]
    end

    Q --> Q1
    Q9 --> R{¿Lista con datos?}
    R -- Sí --> S[Guardar todos los registros\ncon saveAll + flush]
    R -- No --> T[Log: sin registros]
    S --> U([Fin: retornar lista de errores vacía])
    T --> U
