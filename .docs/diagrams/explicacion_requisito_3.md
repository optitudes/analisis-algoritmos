# Requerimiento 3: Explicación de Endpoints

A continuación se detalla la funcionalidad de cada endpoint implementado para el Requerimiento 3, incluyendo los parámetros que requieren y la estructura del JSON que retornan al frontend.

---

### 1. Análisis de patrones por activo
**Endpoint:** `GET /api/analysis/patterns/{activeId}`

**Descripción:** Recibe el ID de un activo y recorre todo su historial de precios con una "ventana" que se desliza día a día. Dentro de cada ventana busca dos patrones:
- **Bullish Streak**: ¿Hubo al menos 3 días seguidos donde el precio de cierre subió respecto al día anterior?
- **Volume Spike Reversal**: ¿Hubo un día con volumen anormalmente alto (≥ 2× el promedio de la ventana) donde además el precio cambió de dirección?

**Requiere:**
- **Path Variable `activeId`** (Long): El ID del activo en la base de datos (ej. `1`).
- **Query Parameter `windowSize`** (int, Opcional): El tamaño de la ventana deslizante. Por defecto es `10`. Valores típicos: `5` (semanal), `10` (bisemanal), `20` (mensual).

**Ejemplo de Petición:**
`GET /api/analysis/patterns/1?windowSize=10`

**Formato JSON de Respuesta:**
```json
[
  {
    "activeSymbol": "CIB",
    "activeName": "Bancolombia - ADR",
    "patternName": "Bullish Streak",
    "patternDescription": "Racha alcista: subsecuencia de al menos 3 días consecutivos donde close[j+1] > close[j]...",
    "windowSize": 10,
    "totalWindows": 1250,
    "patternOccurrences": 45,
    "frequencyPercentage": 3.6,
    "occurrenceDates": [
      "2020-05-12",
      "2021-08-23"
    ]
  },
  {
    "activeSymbol": "CIB",
    "activeName": "Bancolombia - ADR",
    "patternName": "Volume Spike Reversal",
    "patternDescription": "Pico de volumen con reversión...",
    "windowSize": 10,
    "totalWindows": 1250,
    "patternOccurrences": 12,
    "frequencyPercentage": 0.96,
    "occurrenceDates": [
      "2020-03-15",
      "2022-11-02"
    ]
  }
]
```

---

### 2. Análisis de patrones para todos los activos
**Endpoint:** `GET /api/analysis/patterns`

**Descripción:** Ejecuta la detección de los dos patrones mencionados (Bullish Streak y Volume Spike Reversal) para todos los activos del portafolio.

**Requiere:**
- **Query Parameter `windowSize`** (int, Opcional): El tamaño de la ventana deslizante. Por defecto es `10`.

**Ejemplo de Petición:**
`GET /api/analysis/patterns?windowSize=15`

**Formato JSON de Respuesta:**
```json
{
  "CIB": [
    {
      "activeSymbol": "CIB",
      "activeName": "Bancolombia - ADR",
      "patternName": "Bullish Streak",
      "windowSize": 15,
      "totalWindows": 1245,
      "patternOccurrences": 38,
      "frequencyPercentage": 3.05,
      "occurrenceDates": ["2020-05-12"]
    },
    {
      "activeSymbol": "CIB",
      "patternName": "Volume Spike Reversal",
      "patternOccurrences": 8,
      "frequencyPercentage": 0.64,
      "occurrenceDates": ["2020-03-15"]
    }
  ],
  "EC": [
    {
      "activeSymbol": "EC",
      "activeName": "Ecopetrol - ADR",
      "patternName": "Bullish Streak",
      "patternOccurrences": 52,
      "frequencyPercentage": 4.17,
      "occurrenceDates": ["2019-12-01"]
    }
  ]
}
```

---

### 3. Volatilidad y clasificación de riesgo por activo
**Endpoint:** `GET /api/analysis/volatility/{activeId}`

**Descripción:** Calcula qué tan "riesgoso" es un activo basándose en cuánto fluctúa su precio históricamente (desviación estándar de retornos diarios) y lo clasifica como:
- **CONSERVADOR** (< 20%)
- **MODERADO** (20%-40%)
- **AGRESIVO** (≥ 40%)

**Requiere:**
- **Path Variable `activeId`** (Long): El ID del activo en la base de datos (ej. `2`).

**Ejemplo de Petición:**
`GET /api/analysis/volatility/2`

**Formato JSON de Respuesta:**
```json
{
  "activeSymbol": "EC",
  "activeName": "Ecopetrol - ADR",
  "meanDailyReturn": 0.00015,
  "standardDeviation": 0.0245,
  "historicalVolatility": 0.3889,
  "riskCategory": "MODERADO",
  "dataPointsUsed": 1259
}
```
*(Nota: `historicalVolatility` está en formato decimal, 0.3889 representa 38.89%)*

---

### 4. Ranking completo de riesgo del portafolio
**Endpoint:** `GET /api/analysis/risk-ranking`

**Descripción:** Calcula la volatilidad de todos los activos, los ordena de menor a mayor riesgo utilizando QuickSort y proporciona una distribución por categorías.

**Requiere:**
- No requiere parámetros.

**Ejemplo de Petición:**
`GET /api/analysis/risk-ranking`

**Formato JSON de Respuesta:**
```json
{
  "ranking": [
    {
      "activeSymbol": "KOF",
      "activeName": "Coca-Cola FEMSA",
      "meanDailyReturn": 0.0001,
      "standardDeviation": 0.011,
      "historicalVolatility": 0.1746,
      "riskCategory": "CONSERVADOR",
      "dataPointsUsed": 1259
    },
    {
      "activeSymbol": "EC",
      "activeName": "Ecopetrol - ADR",
      "meanDailyReturn": 0.00015,
      "standardDeviation": 0.0245,
      "historicalVolatility": 0.3889,
      "riskCategory": "MODERADO",
      "dataPointsUsed": 1259
    },
    {
      "activeSymbol": "PBR",
      "activeName": "Petrobras - ADR",
      "meanDailyReturn": 0.0002,
      "standardDeviation": 0.035,
      "historicalVolatility": 0.5556,
      "riskCategory": "AGRESIVO",
      "dataPointsUsed": 1259
    }
  ],
  "totalActives": 20,
  "categoryDistribution": {
    "CONSERVADOR": 5,
    "MODERADO": 11,
    "AGRESIVO": 4
  }
}
```
