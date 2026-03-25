package com.example.demo.util;

import com.example.demo.entidades.PriceData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

/**
 * Implementación de los 12 algoritmos de ordenamiento adaptados para objetos PriceData.
 * Criterio 1: Fecha de cotización del activo (Ascendente).
 * Criterio 2: Precio de cierre (Ascendente).
 */
public class PriceDataSortingAlgorithms {

    /**
     * Compara dos PriceData basado en las instrucciones:
     * 1. Fecha de cotización
     * 2. Precio de cierre si las fechas son iguales
     * Devuelve negativo si a < b, 0 si son iguales, positivo si a > b.
     */
    public static int compare(PriceData a, PriceData b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;

        int dateCmp = a.getDate().compareTo(b.getDate());
        if (dateCmp != 0) {
            return dateCmp;
        }
        return a.getClose().compareTo(b.getClose());
    }

    // ==========================================
    // 1. TimSort
    // ==========================================
    private static final int RUN = 32;

    public static void timSort(PriceData[] arr) {
        int n = arr.length;
        for (int i = 0; i < n; i += RUN) {
            insertionSortForTimSort(arr, i, Math.min((i + RUN - 1), (n - 1)));
        }
        for (int size = RUN; size < n; size = 2 * size) {
            for (int left = 0; left < n; left += 2 * size) {
                int mid = left + size - 1;
                int right = Math.min((left + 2 * size - 1), (n - 1));
                if (mid < right) {
                    mergeForTimSort(arr, left, mid, right);
                }
            }
        }
    }

    private static void insertionSortForTimSort(PriceData[] arr, int left, int right) {
        for (int i = left + 1; i <= right; i++) {
            PriceData temp = arr[i];
            int j = i - 1;
            while (j >= left && compare(arr[j], temp) > 0) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = temp;
        }
    }

    private static void mergeForTimSort(PriceData[] arr, int l, int m, int r) {
        int len1 = m - l + 1, len2 = r - m;
        PriceData[] left = new PriceData[len1];
        PriceData[] right = new PriceData[len2];
        System.arraycopy(arr, l, left, 0, len1);
        System.arraycopy(arr, m + 1, right, 0, len2);

        int i = 0, j = 0, k = l;
        while (i < len1 && j < len2) {
            if (compare(left[i], right[j]) <= 0) {
                arr[k++] = left[i++];
            } else {
                arr[k++] = right[j++];
            }
        }
        while (i < len1) arr[k++] = left[i++];
        while (j < len2) arr[k++] = right[j++];
    }

    // ==========================================
    // 2. Comb Sort
    // ==========================================
    public static void combSort(PriceData[] arr) {
        int n = arr.length;
        int gap = n;
        boolean swapped = true;
        while (gap != 1 || swapped) {
            gap = (gap * 10) / 13;
            if (gap < 1) gap = 1;
            swapped = false;
            for (int i = 0; i < n - gap; i++) {
                if (compare(arr[i], arr[i + gap]) > 0) {
                    PriceData temp = arr[i];
                    arr[i] = arr[i + gap];
                    arr[i + gap] = temp;
                    swapped = true;
                }
            }
        }
    }

    // ==========================================
    // 3. Selection Sort
    // ==========================================
    public static void selectionSort(PriceData[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (compare(arr[j], arr[minIdx]) < 0) {
                    minIdx = j;
                }
            }
            PriceData temp = arr[minIdx];
            arr[minIdx] = arr[i];
            arr[i] = temp;
        }
    }

    // ==========================================
    // 4. Tree Sort
    // ==========================================
    static class Node {
        PriceData key;
        Node left, right;
        public Node(PriceData item) { key = item; }
    }
    private static Node insertRec(Node root, PriceData key) {
        if (root == null) {
            return new Node(key);
        }
        if (compare(key, root.key) < 0) {
            root.left = insertRec(root.left, key);
        } else {
            root.right = insertRec(root.right, key);
        }
        return root;
    }
    private static void storeInOrder(Node root, PriceData[] arr, int[] index) {
        if (root != null) {
            storeInOrder(root.left, arr, index);
            arr[index[0]++] = root.key;
            storeInOrder(root.right, arr, index);
        }
    }
    public static void treeSort(PriceData[] arr) {
        Node root = null;
        for (PriceData j : arr) {
            root = insertRec(root, j);
        }
        storeInOrder(root, arr, new int[]{0});
    }

    // ==========================================
    // 5. Pigeonhole Sort
    // Adaptado: Se aplica sobre los "Epoch Days" de las fechas. 
    // Internamente se guardan listas en cada hueco para permitir
    // múltiples elementos en la misma fecha y luego ordenar por precio de cierre.
    // ==========================================
    public static void pigeonholeSort(PriceData[] arr) {
        if (arr.length <= 1) return;
        long min = arr[0].getDate().toEpochDay();
        long max = arr[0].getDate().toEpochDay();
        for (PriceData a : arr) {
            long epoch = a.getDate().toEpochDay();
            if (epoch > max) max = epoch;
            if (epoch < min) min = epoch;
        }
        // Si el rango de fechas es demasiado gigantesco, PigeonHole Memory no será suficiente.
        // Pero para series temporales suele estar entre miles de días (años) que es pasable.
        int range = (int) (max - min + 1);
        if (range > 1000000) {
            // Protección contra OOM
            Arrays.sort(arr, PriceDataSortingAlgorithms::compare);
            return;
        }

        List<PriceData>[] holes = new List[range];
        for (PriceData a : arr) {
            int holeIndex = (int) (a.getDate().toEpochDay() - min);
            if (holes[holeIndex] == null) {
                holes[holeIndex] = new ArrayList<>();
            }
            holes[holeIndex].add(a);
        }
        
        int index = 0;
        for (int i = 0; i < range; i++) {
            if (holes[i] != null) {
                // Ordenar por precio de cierre dentro del mismo día usando una clase comparadora auxiliar simple
                holes[i].sort((a, b) -> a.getClose().compareTo(b.getClose()));
                for (PriceData p : holes[i]) {
                    arr[index++] = p;
                }
            }
        }
    }

    // ==========================================
    // 6. Bucket Sort
    // Adaptado usando los precios de cierre para formar las cubetas,
    // garantizando que cada cubeta se ordene al final con la lógica custom (fecha y cierre).
    // ==========================================
    public static void bucketSort(PriceData[] arr) {
        if (arr.length <= 1) return;
        double maxPrice = arr[0].getClose();
        for (int i = 1; i < arr.length; i++) {
            if (arr[i].getClose() > maxPrice) maxPrice = arr[i].getClose();
        }
        
        int bucketCount = arr.length;
        List<List<PriceData>> buckets = new ArrayList<>(bucketCount);
        for (int i = 0; i < bucketCount; i++) {
            buckets.add(new ArrayList<>());
        }
        for (PriceData val : arr) {
            // Distribuir en las cubetas según precio de cierre para fraccionamiento
            int bucketIndex = (int) ((val.getClose() / (maxPrice + 1)) * bucketCount);
            buckets.get(bucketIndex).add(val);
        }
        int index = 0;
        for (List<PriceData> bucket : buckets) {
            bucket.sort(PriceDataSortingAlgorithms::compare); // Fallback a merge estandar para mantener la lógica custom
            for (PriceData val : bucket) {
                arr[index++] = val;
            }
        }
    }

    // ==========================================
    // 7. QuickSort
    // ==========================================
    public static void quickSort(PriceData[] arr) {
        quickSortRec(arr, 0, arr.length - 1);
    }
    private static void quickSortRec(PriceData[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSortRec(arr, low, pi - 1);
            quickSortRec(arr, pi + 1, high);
        }
    }
    private static int partition(PriceData[] arr, int low, int high) {
        PriceData pivot = arr[high];
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            if (compare(arr[j], pivot) < 0) {
                i++;
                PriceData temp = arr[i]; arr[i] = arr[j]; arr[j] = temp;
            }
        }
        PriceData temp = arr[i + 1]; arr[i + 1] = arr[high]; arr[high] = temp;
        return i + 1;
    }

    // ==========================================
    // 8. HeapSort
    // ==========================================
    public static void heapSort(PriceData[] arr) {
        int n = arr.length;
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i);
        }
        for (int i = n - 1; i > 0; i--) {
            PriceData temp = arr[0]; arr[0] = arr[i]; arr[i] = temp;
            heapify(arr, i, 0);
        }
    }
    private static void heapify(PriceData[] arr, int n, int i) {
        int largest = i;
        int l = 2 * i + 1;
        int r = 2 * i + 2;
        if (l < n && compare(arr[l], arr[largest]) > 0) largest = l;
        if (r < n && compare(arr[r], arr[largest]) > 0) largest = r;
        if (largest != i) {
            PriceData swap = arr[i]; arr[i] = arr[largest]; arr[largest] = swap;
            heapify(arr, n, largest);
        }
    }

    // ==========================================
    // 9. Bitonic Sort
    // ==========================================
    public static void bitonicSort(PriceData[] arr) {
        int n = arr.length;
        if(n <= 1) return;
        int nextPowerOfTwo = 1;
        while(nextPowerOfTwo < n) nextPowerOfTwo <<= 1;
        
        PriceData[] padded = new PriceData[nextPowerOfTwo];
        System.arraycopy(arr, 0, padded, 0, n);
        
        // Fill padding with maximum possible synthetic data to pull them to the top
        PriceData maxPadInfo = new PriceData();
        maxPadInfo.setDate(java.time.LocalDate.of(9999, 12, 31));
        maxPadInfo.setClose(Double.MAX_VALUE);
        
        for (int i = n; i < nextPowerOfTwo; i++) padded[i] = maxPadInfo;
        
        bitonicSortRec(padded, 0, padded.length, 1);
        System.arraycopy(padded, 0, arr, 0, n);
    }
    private static void bitonicSortRec(PriceData[] a, int low, int cnt, int dir) {
        if (cnt > 1) {
            int k = cnt / 2;
            bitonicSortRec(a, low, k, 1);
            bitonicSortRec(a, low + k, k, 0);
            bitonicMerge(a, low, cnt, dir);
        }
    }
    private static void bitonicMerge(PriceData[] a, int low, int cnt, int dir) {
        if (cnt > 1) {
            int k = cnt / 2;
            for (int i = low; i < low + k; i++) {
                if (dir == (compare(a[i], a[i + k]) > 0 ? 1 : 0)) {
                    PriceData temp = a[i]; a[i] = a[i + k]; a[i + k] = temp;
                }
            }
            bitonicMerge(a, low, k, dir);
            bitonicMerge(a, low + k, k, dir);
        }
    }

    // ==========================================
    // 10. Gnome Sort
    // ==========================================
    public static void gnomeSort(PriceData[] arr) {
        int index = 0;
        int n = arr.length;
        while (index < n) {
            if (index == 0) index++;
            if (compare(arr[index], arr[index - 1]) >= 0) {
                index++;
            } else {
                PriceData temp = arr[index]; arr[index] = arr[index - 1]; arr[index - 1] = temp;
                index--;
            }
        }
    }

    // ==========================================
    // 11. Binary Insertion Sort
    // ==========================================
    public static void binaryInsertionSort(PriceData[] arr) {
        for (int i = 1; i < arr.length; i++) {
            PriceData x = arr[i];
            
            // Binary search para PriceData usando un fallback manual estándar
            int low = 0;
            int high = i - 1;
            while (low <= high) {
                int mid = (low + high) / 2;
                if (compare(arr[mid], x) < 0) {
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }
            
            int j = low;
            System.arraycopy(arr, j, arr, j + 1, i - j);
            arr[j] = x;
        }
    }

    // ==========================================
    // 12. Radix Sort
    // Adaptado transformando la fecha y precio en una sola llave de 64-bits (Long)
    // Ordenando byte por byte base-256 (Paso de 8 bits).
    // ==========================================
    public static void radixSort(PriceData[] arr) {
        if (arr.length <= 1) return;
        
        // Generar una llave compacta para cada registro para evitar la conversión per paso
        long[] keys = new long[arr.length];
        long minEpochDay = arr[0].getDate().toEpochDay();
        for (int i = 0; i < arr.length; i++) {
           long d = arr[i].getDate().toEpochDay();
           if(d < minEpochDay) minEpochDay = d;
        }

        for (int i = 0; i < arr.length; i++) {
            // El Epoch normalizado por días (asegurar que es positivo restando min) a 32 bits
            long d = arr[i].getDate().toEpochDay() - minEpochDay; 
            // Cerrar precio en centavos (limitar a 32 bits para entrar en un long perfectamente)
            long c = (long)(arr[i].getClose() * 100.0) & 0xFFFFFFFFL; 
            keys[i] = (d << 32) | c;
        }

        PriceData[] output = new PriceData[arr.length];
        long[] outputKeys = new long[arr.length];

        for (int shift = 0; shift < 64; shift += 8) {
            int[] count = new int[256];
            for (int i = 0; i < arr.length; i++) {
                int byteVal = (int) ((keys[i] >>> shift) & 0xFF);
                count[byteVal]++;
            }
            for (int i = 1; i < 256; i++) {
                count[i] += count[i - 1];
            }
            for (int i = arr.length - 1; i >= 0; i--) {
                int byteVal = (int) ((keys[i] >>> shift) & 0xFF);
                output[count[byteVal] - 1] = arr[i];
                outputKeys[count[byteVal] - 1] = keys[i];
                count[byteVal]--;
            }
            System.arraycopy(output, 0, arr, 0, arr.length);
            System.arraycopy(outputKeys, 0, keys, 0, arr.length);
        }
    }
}
