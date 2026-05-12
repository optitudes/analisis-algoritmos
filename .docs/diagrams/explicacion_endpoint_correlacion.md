# Endpoint: Matriz de Correlación (Mapa de Calor)

### Endpoint
**`GET /api/correlation-matrix`**

**Descripción:** Calcula el coeficiente de correlación de Pearson entre todos los pares de activos del portafolio y los devuelve en una estructura de matriz 2D simétrica, diseñada para ser renderizada como un mapa de calor en el frontend.

**Requiere:**
- **Query Parameter `field`** (String, Opcional): La columna de precio sobre la cual calcular la correlación. Valores permitidos: `open`, `high`, `low`, `close`, `volume`. Por defecto es `close`.

**Ejemplo de Petición:**
`GET /api/correlation-matrix?field=close`

**Formato JSON de Respuesta:**
```json
{
  "labels": ["CIB", "EC", "AVAL", "TGLS", "GPRK"],
  "matrix": [
    [1.0,   0.85, 0.32, 0.12, 0.45],
    [0.85,  1.0,  0.28, 0.09, 0.51],
    [0.32,  0.28, 1.0,  0.63, 0.17],
    [0.12,  0.09, 0.63, 1.0,  0.04],
    [0.45,  0.51, 0.17, 0.04, 1.0 ]
  ],
  "executionTime": 2847
}
```

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `labels` | `String[]` | Símbolos de los activos en el mismo orden que las filas/columnas de la matriz |
| `matrix` | `Double[][]` | Matriz simétrica N×N. `matrix[i][j]` = correlación entre `labels[i]` y `labels[j]` |
| `executionTime` | `Long` | Tiempo total de cómputo en milisegundos |

---

## Justificación Matemática

### Coeficiente de Correlación de Pearson

La correlación de Pearson mide la **relación lineal** entre dos variables. Dados dos activos financieros representados por sus series de precio `X = {x₁, x₂, ..., xₙ}` y `Y = {y₁, y₂, ..., yₙ}` (ambas alineadas por fecha), el coeficiente se define como:

$$r_{XY} = \frac{\sum_{i=1}^{n} (x_i - \bar{x})(y_i - \bar{y})}{\sqrt{\sum_{i=1}^{n} (x_i - \bar{x})^2} \cdot \sqrt{\sum_{i=1}^{n} (y_i - \bar{y})^2}}$$

Donde:
- $\bar{x}$ y $\bar{y}$ son las medias aritméticas de cada serie
- El numerador $\sum (x_i - \bar{x})(y_i - \bar{y})$ es la **covarianza** entre X y Y
- El denominador es el producto de las **desviaciones estándar** de cada serie

### Interpretación

El coeficiente $r$ está acotado en $[-1, 1]$:

| Valor de $\|r\|$ | Interpretación |
|------------------|----------------|
| 0.0 – 0.1 | Sin correlación |
| 0.1 – 0.3 | Baja correlación |
| 0.3 – 0.5 | Media correlación |
| 0.5 – 0.7 | Alta correlación |
| 0.7 – 1.0 | Correlación muy alta |

El signo indica la dirección:
- **$r > 0$**: Cuando un activo sube, el otro tiende a subir (correlación positiva)
- **$r < 0$**: Cuando un activo sube, el otro tiende a bajar (correlación negativa)

### Propiedades de la Matriz

La matriz de correlación resultante tiene dos propiedades fundamentales:

1. **Simetría**: $r_{ij} = r_{ji}$ — la correlación entre A y B es idéntica a la de B y A, por lo que solo se calcula la triangular superior y se replica en la inferior.

2. **Diagonal unitaria**: $r_{ii} = 1$ — todo activo tiene correlación perfecta consigo mismo.

---

## Implementación Técnica

### Flujo de ejecución

```mermaid
flowchart TD
    A[Recibe GET /api/correlation-matrix?field=close] --> B[Obtener todos los activos de la BD]
    B --> C[Para cada activo, obtener series de precio del campo solicitado]
    C --> D[Alinear pares por fecha común]
    D --> E[Calcular Pearson para cada par i,j]

    E -- i == j --> F[Asignar 1.0]
    E -- j < i --> G[Reutilizar matrix[j][i] por simetría]
    E -- j > i --> H[Calcular con getPearsonCorrelation]

    H --> I[Construir matriz N×N]
    F --> I
    G --> I
    I --> J[Retornar JSON con labels + matrix]
```

### Eficiencia

- **Complejidad temporal**: $O(N^2 \cdot M)$ donde $N$ es el número de activos y $M$ la longitud de las series de precio.
- **Optimización**: Los datos de cada activo se consultan una sola vez y se cachean en un `Map<Long, List<PriceDataProjection>>`, evitando $N^2$ consultas a la base de datos.
- **Alineación por fecha**: Antes de calcular Pearson, se filtran solo las fechas que ambos activos comparten, garantizando que todas las comparaciones sean válidas.

### Uso en el Frontend (Mapa de Calor)

La estructura `labels` + `matrix` está diseñada para consumo directo por librerías de visualización. Ejemplo con Plotly.js:

```javascript
const data = [{
  z: matrix,
  x: labels,
  y: labels,
  type: 'heatmap',
  colorscale: 'RdBu',
  zmin: -1,
  zmax: 1
}];
Plotly.newPlot('correlation-chart', data);
```

La escala de color divergente (rojo para correlación negativa, azul para positiva) permite identificar visualmente clusters de activos altamente correlacionados y oportunidades de diversificación.
