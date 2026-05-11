# Explicación funcional — Requerimiento 3

## ¿Qué hace el código del Requerimiento 3?

El Requerimiento 3 añade al sistema la capacidad de **analizar patrones de comportamiento** en los precios históricos de los activos financieros y de **medir qué tan riesgoso es cada activo** según la variabilidad de sus precios. Todo esto se expone a través de 4 endpoints REST que el frontend puede consumir.

---

## Endpoints y qué hace cada uno

### 1. Análisis de patrones de un activo individual

**Endpoint:** `GET /api/analysis/patterns/{activeId}?windowSize=10`

**¿Qué recibe?**
- El ID del activo (por ejemplo, `1` para Bancolombia).
- Opcionalmente el tamaño de la ventana de análisis en días (por defecto 10).

**¿Qué hace?**
Toma todo el historial de precios del activo y lo recorre con una "ventana" de tamaño fijo que se desliza día a día (como una lupa que se mueve sobre los datos). En cada posición de la ventana, busca si ocurre alguno de estos dos patrones:

- **Bullish Streak (Racha alcista):** ¿Dentro de esta ventana hubo al menos 3 días seguidos donde el precio de cierre fue mayor que el del día anterior? Esto indica un impulso alcista sostenido.

- **Volume Spike Reversal (Pico de volumen con reversión):** ¿Dentro de esta ventana hubo un día con un volumen de negociación muy alto (al menos el doble del promedio de la ventana), y justo después el precio cambió de dirección? Esto puede indicar un punto de inflexión del mercado donde hubo una actividad inusual.

**¿Qué retorna?**
Para cada patrón retorna:
- El nombre y descripción del patrón.
- Cuántas ventanas se evaluaron en total.
- En cuántas de esas ventanas se detectó el patrón.
- El porcentaje de frecuencia (ej: "el patrón Bullish Streak apareció en el 35% de las ventanas").
- Las fechas exactas donde comenzó cada ventana con el patrón detectado.

---

### 2. Análisis de patrones de todos los activos

**Endpoint:** `GET /api/analysis/patterns?windowSize=10`

**¿Qué recibe?**
- Opcionalmente el tamaño de la ventana (por defecto 10).

**¿Qué hace?**
Ejecuta el mismo análisis del endpoint anterior, pero para **cada activo** registrado en la base de datos (Bancolombia, Ecopetrol, MercadoLibre, etc.).

**¿Qué retorna?**
Un mapa donde la clave es el símbolo del activo (ej: `"CIB"`, `"EC"`, `"MELI"`) y el valor es la lista con los resultados de ambos patrones para ese activo. Esto permite comparar fácilmente qué activos presentan más frecuentemente ciertos patrones.

---

### 3. Volatilidad y clasificación de riesgo de un activo

**Endpoint:** `GET /api/analysis/volatility/{activeId}`

**¿Qué recibe?**
- El ID del activo.

**¿Qué hace?**
Calcula qué tan "volátil" (variable) es el precio de ese activo a lo largo de su historial. El proceso paso a paso es:

1. **Calcula los retornos diarios:** Para cada par de días consecutivos, calcula cuánto cambió el precio en porcentaje (usando logaritmos, que es la convención financiera estándar).

2. **Calcula el retorno promedio:** El promedio de todos los retornos diarios.

3. **Calcula la desviación estándar:** Qué tan dispersos están los retornos respecto al promedio. Si la desviación es alta, el precio "se mueve mucho" de un día a otro.

4. **Anualiza la volatilidad:** Convierte la dispersión diaria a una medida anual multiplicando por la raíz cuadrada de 252 (los días hábiles de trading en un año). Esto da un número comparable entre activos.

5. **Clasifica el riesgo:** Según la volatilidad anualizada:
   - **CONSERVADOR** (menos del 20%): el precio es relativamente estable.
   - **MODERADO** (entre 20% y 40%): variabilidad normal del mercado.
   - **AGRESIVO** (40% o más): el precio fluctúa mucho, alto riesgo.

**¿Qué retorna?**
El símbolo y nombre del activo, el retorno diario promedio, la desviación estándar, la volatilidad anualizada, la categoría de riesgo, y cuántos datos de precios se usaron para el cálculo.

---

### 4. Ranking de riesgo de todo el portafolio

**Endpoint:** `GET /api/analysis/risk-ranking`

**¿Qué recibe?**
Nada, no requiere parámetros.

**¿Qué hace?**
1. Calcula la volatilidad de **todos** los activos del portafolio (usando el mismo proceso del endpoint anterior).
2. **Ordena** los activos de menor a mayor riesgo usando el algoritmo QuickSort implementado manualmente.
3. Cuenta cuántos activos cayeron en cada categoría.

**¿Qué retorna?**
- La lista completa de activos **ordenados del más conservador al más agresivo**.
- El total de activos analizados.
- La distribución por categoría, por ejemplo: `{"CONSERVADOR": 3, "MODERADO": 12, "AGRESIVO": 5}`.

Esto permite al usuario ver de un vistazo cuáles activos son los más seguros y cuáles los más riesgosos dentro de su portafolio.

---

## Resumen visual

```
Endpoints del Requerimiento 3
│
├── /api/analysis/patterns/{id}      → ¿Qué patrones se repiten en los precios de UN activo?
├── /api/analysis/patterns            → ¿Qué patrones se repiten en TODOS los activos?
├── /api/analysis/volatility/{id}    → ¿Qué tan riesgoso es UN activo?
└── /api/analysis/risk-ranking        → ¿Cómo se comparan TODOS los activos por riesgo? (ordenados)
```

## Archivos involucrados

| Archivo | Rol |
|---------|-----|
| `PatternAnalysisController.java` | Recibe las peticiones HTTP y las delega al servicio |
| `PatternAnalysisService.java` | Define qué operaciones están disponibles (interfaz) |
| `PatternAnalysisServiceImpl.java` | Conecta la base de datos con los algoritmos y arma las respuestas |
| `PatternAnalysisAlgorithms.java` | Contiene toda la lógica matemática y algorítmica (sliding window, volatilidad, QuickSort) |
| `PatternResult.java` | Estructura de datos para los resultados de patrones |
| `VolatilityResult.java` | Estructura de datos para los resultados de volatilidad |
| `VolatilityRankingResponse.java` | Estructura de datos para la respuesta del ranking completo |
