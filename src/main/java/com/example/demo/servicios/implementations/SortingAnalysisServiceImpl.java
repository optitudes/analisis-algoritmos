package com.example.demo.servicios.implementations;

import com.example.demo.dto.SortingResult;
import com.example.demo.entidades.PriceData;
import com.example.demo.repositorios.PriceDataRepository;
import com.example.demo.servicios.interfaces.SortingAnalysisService;
import com.example.demo.util.PriceDataSortingAlgorithms;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Service
public class SortingAnalysisServiceImpl implements SortingAnalysisService {

    private final PriceDataRepository priceDataRepository;

    public SortingAnalysisServiceImpl(PriceDataRepository priceDataRepository) {
        this.priceDataRepository = priceDataRepository;
    }

    @Override
    public List<SortingResult> runAnalysis(int limit) {
        // 1. Obtener los archivos de la tabla "price_data" unificada
        List<PriceData> allData = priceDataRepository.findAll();

        // Opcional limitador para ver gráficas parciales sin esperar los O(n^2) mucho
        // tiempo
        if (limit > 0 && limit < allData.size()) {
            allData = allData.subList(0, limit);
        }

        PriceData[] originalArray = allData.toArray(new PriceData[0]);
        List<SortingResult> results = new ArrayList<>();

        results.add(measure("TimSort", "O(n log n)", PriceDataSortingAlgorithms::timSort, originalArray));
        results.add(measure("Comb Sort", "O(n log n)", PriceDataSortingAlgorithms::combSort, originalArray));
        results.add(measure("Selection Sort", "O(n^2)", PriceDataSortingAlgorithms::selectionSort, originalArray));
        results.add(measure("Tree Sort", "O(n log n)", PriceDataSortingAlgorithms::treeSort, originalArray));
        results.add(
                measure("Pigeonhole Sort", "O(n + Range)", PriceDataSortingAlgorithms::pigeonholeSort, originalArray));
        results.add(measure("BucketSort", "O(n + k)", PriceDataSortingAlgorithms::bucketSort, originalArray));
        results.add(measure("QuickSort", "O(n log n)", PriceDataSortingAlgorithms::quickSort, originalArray));
        results.add(measure("HeapSort", "O(n log n)", PriceDataSortingAlgorithms::heapSort, originalArray));
        results.add(measure("Bitonic Sort", "O(n log^2 n)", PriceDataSortingAlgorithms::bitonicSort, originalArray));
        results.add(measure("Gnome Sort", "O(n^2)", PriceDataSortingAlgorithms::gnomeSort, originalArray));
        results.add(measure("Binary Insertion Sort", "O(n log n)", PriceDataSortingAlgorithms::binaryInsertionSort,
                originalArray));
        results.add(measure("RadixSort", "O(d * (n + k))", PriceDataSortingAlgorithms::radixSort, originalArray));

        return results;
    }

    /**
     * Toma el método y el array base de PriceData. Clona el array y mide tiempo de
     * ejecución exacto en ms.
     */
    private SortingResult measure(String name, String complexity, Consumer<PriceData[]> sortMethod,
            PriceData[] originalArray) {
        PriceData[] copy = Arrays.copyOf(originalArray, originalArray.length);

        long startNanos = System.nanoTime();
        sortMethod.accept(copy);
        long endNanos = System.nanoTime();

        double timeInMs = (endNanos - startNanos) / 1_000_000.0;
        return new SortingResult(name, complexity, originalArray.length, timeInMs);
    }

    @Override
    public List<PriceData> getTop15DaysByVolume() {
        // Encontrar los 15 registros con mayor volumen
        List<PriceData> allData = priceDataRepository.findAll();
        allData.sort((a, b) -> b.getVolume().compareTo(a.getVolume())); // Ordenar desc por volumen

        List<PriceData> top15 = allData.subList(0, Math.min(15, allData.size()));

        // Ordenarlos ascendente por el criterio base (Fecha -> Cierre)
        PriceData[] top15Array = top15.toArray(new PriceData[0]);
        PriceDataSortingAlgorithms.quickSort(top15Array); // Usamos quickSort creado por nosotros

        return Arrays.asList(top15Array);
    }
}
