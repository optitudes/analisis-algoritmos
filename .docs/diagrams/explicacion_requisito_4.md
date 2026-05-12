# Requerimiento 4: Explicación de Endpoints (Dashboard Bursátil)

A continuación se detalla la funcionalidad del endpoint implementado para proporcionar los datos visuales requeridos en el Requerimiento 4, incluyendo los parámetros que recibe y la estructura del JSON que retorna al frontend.

---

### 1. Datos para Gráfico de Velas (Candlestick) y Media Móvil Simple (SMA)
**Endpoint:** `GET /api/visualization/candlestick/{activeId}`

**Descripción:** Recibe el ID de un activo y devuelve su serie de tiempo histórica completa formateada específicamente para renderizar un gráfico de velas (Apertura, Máximo, Mínimo, Cierre). Además, calcula de forma algorítmica la **Media Móvil Simple (SMA)** para el período indicado, devolviéndola junto con cada punto de datos.

**Requiere:**
- **Path Variable `activeId`** (Long): El ID del activo en la base de datos (ej. `1`).
- **Query Parameter `smaPeriod`** (int, Opcional): El tamaño del período en días para calcular la Media Móvil Simple. Por defecto es `20`.
  - *Nota:* Si hay menos días de datos que el `smaPeriod`, o durante los primeros `smaPeriod - 1` días de la serie, el valor de `sma` retornará `null`.

**Ejemplo de Petición:**
`GET /api/visualization/candlestick/1?smaPeriod=20`

**Formato JSON de Respuesta:**
```json
{
  "activeId": 1,
  "activeSymbol": "CIB",
  "activeName": "Bancolombia - ADR",
  "smaPeriod": 20,
  "data": [
    {
      "date": "2020-01-02",
      "open": 53.25,
      "high": 54.10,
      "low": 53.00,
      "close": 53.80,
      "volume": 1250000,
      "sma": null
    },
    {
      "date": "2020-01-03",
      "open": 53.80,
      "high": 54.50,
      "low": 53.20,
      "close": 54.10,
      "volume": 1340000,
      "sma": null
    },
    "... (más días donde la SMA es null hasta llegar al día 20) ...",
    {
      "date": "2020-01-30",
      "open": 55.00,
      "high": 55.80,
      "low": 54.90,
      "close": 55.60,
      "volume": 1450000,
      "sma": 54.35
    },
    {
      "date": "2020-01-31",
      "open": 55.60,
      "high": 56.10,
      "low": 55.20,
      "close": 55.90,
      "volume": 1600000,
      "sma": 54.45
    }
  ]
}
```
