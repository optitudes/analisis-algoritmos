package com.example.demo.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Utility class que contiene las implementaciones de los 12 algoritmos de
 * algoritmos de ordenamiento solicitados.
 * Todos operan sobre arreglos de enteros (int[]).
 */
public class SortingAlgorithms {

    // ==========================================
    // 1. TimSort
    // Complejidad: O(n log n)
    // ==========================================
    private static final int RUN = 32;

    /**
     * TimSort hibrida Insertion Sort y Merge Sort.
     * Divide el arreglo en bloques (runs) y los ordena con Insertion Sort,
     * luego los une usando Merge.
     */
    public static void timSort(int[] arr) {
        int n = arr.length;
        // Ordena subarreglos de tamaño RUN usando Insertion Sort
        for (int i = 0; i < n; i += RUN) {
            insertionSortForTimSort(arr, i, Math.min((i + RUN - 1), (n - 1)));
        }

        // Empieza a fusionar desde tamaño RUN. Y dobla el tamaño en cada iteración.
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

    private static void insertionSortForTimSort(int[] arr, int left, int right) {
        for (int i = left + 1; i <= right; i++) {
            int temp = arr[i];
            int j = i - 1;
            while (j >= left && arr[j] > temp) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = temp;
        }
    }

    private static void mergeForTimSort(int[] arr, int l, int m, int r) {
        int len1 = m - l + 1, len2 = r - m;
        int[] left = new int[len1];
        int[] right = new int[len2];
        System.arraycopy(arr, l, left, 0, len1);
        System.arraycopy(arr, m + 1, right, 0, len2);

        int i = 0, j = 0, k = l;
        while (i < len1 && j < len2) {
            if (left[i] <= right[j]) {
                arr[k++] = left[i++];
            } else {
                arr[k++] = right[j++];
            }
        }
        while (i < len1)
            arr[k++] = left[i++];
        while (j < len2)
            arr[k++] = right[j++];
    }

    // ==========================================
    // 2. Comb Sort
    // Complejidad: O(n^2) promedio O(n log n)
    // ==========================================
    /**
     * Comb Sort mejora a Bubble Sort utilizando un "gap" (brecha) mayor a 1
     * para eliminar valores pequeños al final del arreglo rápidamente (tortugas).
     */
    public static void combSort(int[] arr) {
        int n = arr.length;
        int gap = n;
        boolean swapped = true;

        while (gap != 1 || swapped) {
            // Factor de reducción ideal es 1.3
            gap = (gap * 10) / 13;
            if (gap < 1)
                gap = 1;

            swapped = false;
            for (int i = 0; i < n - gap; i++) {
                if (arr[i] > arr[i + gap]) {
                    // Intercambiar
                    int temp = arr[i];
                    arr[i] = arr[i + gap];
                    arr[i + gap] = temp;
                    swapped = true;
                }
            }
        }
    }

    // ==========================================
    // 3. Selection Sort
    // Complejidad: O(n^2)
    // ==========================================
    /**
     * Selection Sort busca el elemento más pequeño del arreglo no ordenado
     * y lo coloca en la posición correcta iterativamente.
     */
    public static void selectionSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (arr[j] < arr[minIdx]) {
                    minIdx = j;
                }
            }
            // Intercambiar el más pequeño encontrado
            int temp = arr[minIdx];
            arr[minIdx] = arr[i];
            arr[i] = temp;
        }
    }

    // ==========================================
    // 4. Tree Sort
    // Complejidad: O(n log n)
    // ==========================================
    static class Node {
        int key;
        Node left, right;

        public Node(int item) {
            key = item;
        }
    }

    private static Node insertRec(Node root, int key) {
        if (root == null) {
            return new Node(key);
        }
        if (key < root.key) {
            root.left = insertRec(root.left, key);
        } else {
            root.right = insertRec(root.right, key);
        }
        return root;
    }

    private static void storeInOrder(Node root, int[] arr, int[] index) {
        if (root != null) {
            storeInOrder(root.left, arr, index);
            arr[index[0]++] = root.key;
            storeInOrder(root.right, arr, index);
        }
    }

    /**
     * Tree Sort inserta todos los elementos en un Árbol Binario de Búsqueda (BST)
     * y luego hace un recorrido in-order para obtenerlos ordenados.
     */
    public static void treeSort(int[] arr) {
        Node root = null;
        for (int j : arr) {
            root = insertRec(root, j);
        }
        storeInOrder(root, arr, new int[] { 0 });
    }

    // ==========================================
    // 5. Pigeonhole Sort
    // Complejidad: O(n + Range)
    // ==========================================
    /**
     * Pigeonhole Sort funciona si el número de elementos y las llaves posibles son
     * similares.
     * Crea "agujeros" (pigeonholes) para cada rango y cuenta las frecuencias.
     */
    public static void pigeonholeSort(int[] arr) {
        if (arr.length == 0)
            return;
        int min = arr[0];
        int max = arr[0];
        for (int a : arr) {
            if (a > max)
                max = a;
            if (a < min)
                min = a;
        }
        int range = max - min + 1;
        int[] holes = new int[range];
        for (int a : arr) {
            holes[a - min]++;
        }
        int index = 0;
        for (int i = 0; i < range; i++) {
            while (holes[i]-- > 0) {
                arr[index++] = i + min;
            }
        }
    }

    // ==========================================
    // 6. Bucket Sort
    // Complejidad: O(n + k) (k buckets)
    // ==========================================
    /**
     * Bucket Sort divide elementos en "buckets" (cubetas).
     * Como tratamos con enteros, lo implementamos encontrando el max para
     * distribuirlos.
     */
    public static void bucketSort(int[] arr) {
        if (arr.length <= 0)
            return;
        int max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > max)
                max = arr[i];
        }
        // Usamos una cantidad de buckets igual a la longitud del arreglo
        int bucketCount = arr.length;
        List<List<Integer>> buckets = new ArrayList<>(bucketCount);
        for (int i = 0; i < bucketCount; i++) {
            buckets.add(new ArrayList<>());
        }
        for (int val : arr) { // Distribuir en las cubetas
            int bucketIndex = (int) ((double) val / (max + 1) * bucketCount);
            buckets.get(bucketIndex).add(val);
        }
        int index = 0;
        for (List<Integer> bucket : buckets) {
            Collections.sort(bucket); // Aquí lo ordenamos, normalmente es Insertion Sort
            for (int val : bucket) {
                arr[index++] = val;
            }
        }
    }

    // ==========================================
    // 7. QuickSort
    // Complejidad: O(n log n)
    // ==========================================
    /**
     * QuickSort usa el paradigma de Divide y Vencerás.
     * Se selecciona un pivote, y se particiona el arreglo de forma que
     * todos los menores quedan a la izquierda y los mayores a la derecha.
     */
    public static void quickSort(int[] arr) {
        quickSortRec(arr, 0, arr.length - 1);
    }

    private static void quickSortRec(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSortRec(arr, low, pi - 1);
            quickSortRec(arr, pi + 1, high);
        }
    }

    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high]; // Tomamos el último como pivote
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {
                i++;
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        return i + 1;
    }

    // ==========================================
    // 8. HeapSort
    // Complejidad: O(n log n)
    // ==========================================
    /**
     * HeapSort modela el arreglo como un árbol binario (Heap).
     * Constuye un max-heap (elemento mayor en la raíz), y lo extrae uno a uno al
     * final.
     */
    public static void heapSort(int[] arr) {
        int n = arr.length;
        // Construir max-heap
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i);
        }
        // Extraer elementos uno a uno
        for (int i = n - 1; i > 0; i--) {
            int temp = arr[0];
            arr[0] = arr[i];
            arr[i] = temp; // Mover actual a la final
            heapify(arr, i, 0); // Re-aplicar max-heapicity al heap reducido
        }
    }

    private static void heapify(int[] arr, int n, int i) {
        int largest = i;
        int l = 2 * i + 1;
        int r = 2 * i + 2;
        if (l < n && arr[l] > arr[largest])
            largest = l;
        if (r < n && arr[r] > arr[largest])
            largest = r;
        if (largest != i) {
            int swap = arr[i];
            arr[i] = arr[largest];
            arr[largest] = swap;
            heapify(arr, n, largest);
        }
    }

    // ==========================================
    // 9. Bitonic Sort
    // Complejidad: O(n log^2 n)
    // ==========================================
    /**
     * Bitonic Sort (sólo funciona bien si n es potencia de 2 en su forma pura,
     * pero para arrays que no son potencia de 2, la implementación recursiva lo
     * emula).
     * Por simplicidad se rellena hasta potencia de 2, pero para no alterar la
     * longitud
     * y poder implementarlo con cualquier N, se proporciona esta versión
     * generalizada.
     */
    public static void bitonicSort(int[] arr) {
        // En java, para arrays de cualquier tamaño, usar una adaptación suele ser raro.
        // Haremos una envoltura que ordene el subrango posible, pero para análisis,
        // padding es común.
        int n = arr.length;
        int nextPowerOfTwo = 1;
        while (nextPowerOfTwo < n)
            nextPowerOfTwo <<= 1;

        int[] padded = new int[nextPowerOfTwo];
        Arrays.fill(padded, Integer.MAX_VALUE); // Pad con max value para que queden al final
        System.arraycopy(arr, 0, padded, 0, n);

        bitonicSortRec(padded, 0, padded.length, 1);

        System.arraycopy(padded, 0, arr, 0, n); // Extraer los n originales ya ordenados
    }

    private static void bitonicSortRec(int[] a, int low, int cnt, int dir) {
        if (cnt > 1) {
            int k = cnt / 2;
            bitonicSortRec(a, low, k, 1); // ordenar mitad ascendente
            bitonicSortRec(a, low + k, k, 0); // ordenar mitad descendente
            bitonicMerge(a, low, cnt, dir);
        }
    }

    private static void bitonicMerge(int[] a, int low, int cnt, int dir) {
        if (cnt > 1) {
            int k = cnt / 2;
            for (int i = low; i < low + k; i++) {
                if (dir == (a[i] > a[i + k] ? 1 : 0)) {
                    int temp = a[i];
                    a[i] = a[i + k];
                    a[i + k] = temp;
                }
            }
            bitonicMerge(a, low, k, dir);
            bitonicMerge(a, low + k, k, dir);
        }
    }

    // ==========================================
    // 10. Gnome Sort
    // Complejidad: O(n^2)
    // ==========================================
    /**
     * Gnome Sort es un algoritmo similar al Insertion Sort,
     * basado en la idea de un gnomo de jardín moviendo elementos hacia atrás.
     */
    public static void gnomeSort(int[] arr) {
        int index = 0;
        int n = arr.length;
        while (index < n) {
            if (index == 0) {
                index++;
            }
            if (arr[index] >= arr[index - 1]) {
                index++;
            } else {
                int temp = arr[index];
                arr[index] = arr[index - 1];
                arr[index - 1] = temp;
                index--; // Mueve hacia atrás hasta que esté ordenado
            }
        }
    }

    // ==========================================
    // 11. Binary Insertion Sort
    // Complejidad: O(n^2) en swaps, O(n log n) en comparaciones
    // ==========================================
    /**
     * Binary Insertion Sort mejora al Insertion Sort tradiconal usando
     * Búsqueda Binaria para encontrar la posición correcta, reduciendo las
     * comparaciones.
     */
    public static void binaryInsertionSort(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            int x = arr[i];
            int j = Math.abs(Arrays.binarySearch(arr, 0, i, x) + 1);
            // Mover todos los elementos desde j hasta i
            System.arraycopy(arr, j, arr, j + 1, i - j);
            arr[j] = x;
        }
    }

    // ==========================================
    // 12. Radix Sort
    // Complejidad: O(d * (n + k))
    // ==========================================
    /**
     * Radix Sort ordena procesando digitó por dígito, desde los menos
     * significativos
     * hasta los más significativos, usando Counting Sort como subrutina.
     */
    public static void radixSort(int[] arr) {
        if (arr.length == 0)
            return;
        // Encontrar maximo para conocer el numero de digitos
        int max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > max)
                max = arr[i];
        }
        // Counting sort para cada digito
        for (int exp = 1; max / exp > 0; exp *= 10) {
            countSortForRadix(arr, exp);
        }
    }

    private static void countSortForRadix(int[] arr, int exp) {
        int n = arr.length;
        int[] output = new int[n];
        int[] count = new int[10];

        // Guardar ocurrencias
        for (int i = 0; i < n; i++) {
            count[(arr[i] / exp) % 10]++;
        }
        // Calcular posiciones reales
        for (int i = 1; i < 10; i++) {
            count[i] += count[i - 1];
        }
        // Construir array
        for (int i = n - 1; i >= 0; i--) {
            output[count[(arr[i] / exp) % 10] - 1] = arr[i];
            count[(arr[i] / exp) % 10]--;
        }
        // Copiar array ordenado
        System.arraycopy(output, 0, arr, 0, n);
    }
}
