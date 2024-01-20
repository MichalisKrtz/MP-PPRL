package mp_pprl.optimization;

import mp_pprl.graph.ArrayIndex2D;
import mp_pprl.graph.Edge;
import mp_pprl.graph.Vertex;
import mp_pprl.protocols.SimilarityCalculator;
import mp_pprl.db.Record;

import java.util.*;

public class HungarianAlgorithm {
    // This method uses the Hungarian algorithm to select the optimal edges based on their similarity.
    public static Set<Edge> findOptimalEdges(Set<Edge> edges) {
        List<Vertex> uniqueVertices = getUniqueVerticesFromEdges(edges);
        List<Record> uniqueRecords = getUniqueRecordsFromEdges(edges);

        int n = findSimilarityMatrixSize(uniqueVertices, uniqueRecords);

        if (n == 0) {
            return new HashSet<>();
        }

        double[][] similarityMatrix = initializeSimilarityMatrix(n, edges, uniqueVertices, uniqueRecords);

        double[][] modifiedSimilarityMatrix = convertToMinimizationProblem(similarityMatrix);

        modifiedSimilarityMatrix = reduceRows(modifiedSimilarityMatrix);

        modifiedSimilarityMatrix = reduceColumns(modifiedSimilarityMatrix);

        // The two lists are populated inside the checkForOptimalAssignment() method.
        Set<Integer> crossedRows = new HashSet<>();
        Set<Integer> crossedCols = new HashSet<>();

        while (!checkForOptimalAssignment(modifiedSimilarityMatrix, crossedRows, crossedCols)) {
            modifiedSimilarityMatrix = shiftZeros(modifiedSimilarityMatrix, crossedRows, crossedCols);
            crossedRows.clear();
            crossedCols.clear();
        }

        List<ArrayIndex2D> indices = chooseFinalAssignments(modifiedSimilarityMatrix);

        return getEdgesFromFinalAssignments(indices, uniqueVertices, uniqueRecords);
    }

    // --- Only for debugging ---
    private static void printMatrix(double[][] modifiedSimilarityMatrix) {
        System.out.println("{");
        for (double[] similarityMatrix : modifiedSimilarityMatrix) {
            System.out.print("{");
            for (int j = 0; j < modifiedSimilarityMatrix.length; j++) {
                System.out.printf("%.2f", similarityMatrix[j]);
                System.out.print(", ");
            }
            System.out.println("},");
        }
        System.out.println("}");
    }

    private static Set<Edge> getEdgesFromFinalAssignments(List<ArrayIndex2D> indices, List<Vertex> uniqueVertices, List<Record> uniqueRecords) {
        List<Integer> dummyRows = new ArrayList<>();
        List<Integer> dummyColumns = new ArrayList<>();
        if (uniqueVertices.size() > uniqueRecords.size()) {
            for (int i = uniqueRecords.size(); i < uniqueVertices.size(); i++) {
                dummyColumns.add(i);
            }
        } else if (uniqueVertices.size() < uniqueRecords.size()) {
            for (int i = uniqueVertices.size(); i < uniqueRecords.size(); i++) {
                dummyRows.add(i);
            }
        }

        Set<Edge> optimalEdges = new HashSet<>();
        for (ArrayIndex2D index : indices) {
            if (dummyRows.contains(index.row())) {
                continue;
            }
            if (dummyColumns.contains(index.col())) {
                continue;
            }
            optimalEdges.add(new Edge(uniqueVertices.get(index.row()), uniqueRecords.get(index.col())));
        }

        return optimalEdges;
    }


    /* Returns the indices of the final Assignments.
    There might be more than one way to choose n zeros (i.e. optimal assignments).
    All Cases will have the same total similarity. */
    private static List<ArrayIndex2D> chooseFinalAssignments(double[][] matrix) {
        List<ArrayIndex2D> indices = new ArrayList<>();

        Set<Integer> availableRows = new HashSet<>();
        Set<Integer> availableCols = new HashSet<>();
        for (int i = 0; i < matrix.length; i++) {
            availableRows.add(i);
            availableCols.add(i);
        }
        while (!availableRows.isEmpty()) {
            Map<Integer, Integer> rowZeroesMap = new HashMap<>();
            for (int i = 0; i < matrix.length; i++) {
                if (!availableRows.contains(i)) {
                    continue;
                }
                int zeroesCount = 0;
                for (int j = 0; j < matrix.length; j++) {
                    if (!availableCols.contains(j)) {
                        continue;
                    }
                    if (matrix[i][j] != 0) {
                        continue;
                    }
                    zeroesCount++;
                }
                rowZeroesMap.put(i, zeroesCount);
            }

            int leastZeroesRow = Collections.min(rowZeroesMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            for (int j = 0; j < matrix.length; j++) {
                if (matrix[leastZeroesRow][j] == 0 && availableCols.contains(j)) {
                    indices.add(new ArrayIndex2D(leastZeroesRow, j));
                    availableRows.remove(leastZeroesRow);
                    availableCols.remove(j);
                    break;
                }
            }
        }

        return indices;
    }

    private static double[][] shiftZeros(double[][] matrix, Set<Integer> crossedRows, Set<Integer> crossedCols) {
        double minNumber = getMinUncrossedNumber(matrix, crossedRows, crossedCols);
        double[][] modifiedMatrix = new double[matrix.length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                if (crossedRows.contains(i) && crossedCols.contains(j)) {
                    modifiedMatrix[i][j] = matrix[i][j] + minNumber;
                } else if (crossedRows.contains(i) || crossedCols.contains(j)) {
                    modifiedMatrix[i][j] = matrix[i][j];
                } else {
                    modifiedMatrix[i][j] = matrix[i][j] - minNumber;
                }
            }
        }

        return modifiedMatrix;
    }

    private static double getMinUncrossedNumber(double[][] matrix, Set<Integer> crossedRows, Set<Integer> crossedCols) {
        double minNumber = 1;
        for (int i = 0; i < matrix.length; i++) {
            if (crossedRows.contains(i)) {
                continue;
            }
            for (int j = 0; j < matrix.length; j++) {
                if (crossedCols.contains(j)) {
                    continue;
                }
                if (minNumber > matrix[i][j]) {
                    minNumber = matrix[i][j];
                }
            }
        }

        return minNumber;
    }

    //  The lists crossedRows and crossedCols are modified in order to be used in the shiftZeros method.
    private static boolean checkForOptimalAssignment(double[][] matrix, Set<Integer> crossedRows, Set<Integer> crossedCols) {
        int minLines = 0;

        while (true) {
            //  Initialize counters
            Map<Integer, Integer> rowZerosCountMap = new HashMap<>();
            Map<Integer, Integer> colZerosCountMap = new HashMap<>();
            for (int i = 0; i < matrix.length; i++) {
                rowZerosCountMap.put(i, 0);
                colZerosCountMap.put(i, 0);
            }
            //  Count zeros for each available row and column
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j <matrix.length; j++) {
                    if (matrix[i][j] != 0 || crossedRows.contains(i) || crossedCols.contains(j)) {
                        continue;
                    }
                    rowZerosCountMap.replace(i, rowZerosCountMap.get(i) + 1);
                    colZerosCountMap.replace(j, colZerosCountMap.get(j) + 1);
                }
            }

            //  Break condition
            //  Check if both the row and column with most zeros have 0 zeros, which means all zeros have been crossed.
            int mostZerosRow = Collections.max(rowZerosCountMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            int mostZerosCol = Collections.max(colZerosCountMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            if (rowZerosCountMap.get(mostZerosRow) == 0 && colZerosCountMap.get(mostZerosCol) == 0) {
                break;
            }

            //  Calculate gains for each row and column and cross the one with most gain.
            Map<Integer, Integer> rowGainsMap = calculateGainsForEachRow(matrix, rowZerosCountMap, colZerosCountMap, crossedRows, crossedCols);
            Map<Integer, Integer> colGainsMap = calculateGainsForEachColumn(matrix, rowZerosCountMap, colZerosCountMap, crossedRows, crossedCols);
            int maxGainRow = Collections.max(rowGainsMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            int maxGainCol = Collections.max(colGainsMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            if (rowGainsMap.get(maxGainRow) >= colGainsMap.get(maxGainCol)) {
                int maxGainWithMostZerosRow = getMaxGainWithMostZerosRow(maxGainRow, rowGainsMap, rowZerosCountMap);
                crossedRows.add(maxGainWithMostZerosRow);
            } else {
                int maxGainWithMostZerosCol = getMaxGainWithMostZerosCol(maxGainCol, colGainsMap, colZerosCountMap);
                crossedCols.add(maxGainWithMostZerosCol);
            }

            minLines++;
        }

        return minLines == matrix.length;
    }

    private static int getMaxGainWithMostZerosCol(int maxGainCol, Map<Integer, Integer> colGainsMap, Map<Integer, Integer> colZerosCountMap) {
        int maxGainWithMostZerosCol = maxGainCol;
        for (Map.Entry<Integer, Integer> colGainsEntry : colGainsMap.entrySet()) {
            if (!Objects.equals(colGainsEntry.getValue(), colGainsMap.get(maxGainCol))) {
                continue;
            }
            if (colZerosCountMap.get(colGainsEntry.getKey()) <= colZerosCountMap.get(maxGainCol)) {
                continue;
            }
            maxGainWithMostZerosCol = colGainsEntry.getKey();
        }
        return maxGainWithMostZerosCol;
    }

    private static int getMaxGainWithMostZerosRow(int maxGainRow, Map<Integer, Integer> rowGainsMap, Map<Integer, Integer> rowZerosCountMap) {
        int maxGainWithMostZerosRow = maxGainRow;
        for (Map.Entry<Integer, Integer> rowGainsEntry : rowGainsMap.entrySet()) {
            if (!Objects.equals(rowGainsEntry.getValue(), rowGainsMap.get(maxGainRow))) {
                continue;
            }
            if (rowZerosCountMap.get(rowGainsEntry.getKey()) <= rowZerosCountMap.get(maxGainRow)) {
                continue;
            }
            maxGainWithMostZerosRow = rowGainsEntry.getKey();
        }
        return maxGainWithMostZerosRow;
    }

    private static Map<Integer, Integer> calculateGainsForEachRow(double[][] matrix, Map<Integer, Integer> rowZerosCountMap, Map<Integer, Integer> colZerosCountMap, Set<Integer> crossedRows, Set<Integer> crossedCols) {
        Map<Integer, Integer> rowGainsMap = new HashMap<>();
        for (int i = 0; i < matrix.length; i++) {
            if (rowZerosCountMap.get(i) == 0 || crossedRows.contains(i)) {
                continue;
            }
            rowGainsMap.put(i, rowZerosCountMap.get(i));
            for (int j = 0; j < matrix.length; j++) {
                if (matrix[i][j] != 0 || crossedCols.contains(j)) {
                    continue;
                }
                if (colZerosCountMap.get(j) > 1) {
                    rowGainsMap.replace(i, rowGainsMap.get(i) - 1);
                }
            }
        }

        return rowGainsMap;
    }

    private static Map<Integer, Integer> calculateGainsForEachColumn(double[][] matrix, Map<Integer, Integer> rowZerosCountMap, Map<Integer, Integer> colZerosCountMap, Set<Integer> crossedRows, Set<Integer> crossedCols) {
        Map<Integer, Integer> colGainsMap = new HashMap<>();
        for (int j = 0; j < matrix.length; j++) {
            if (colZerosCountMap.get(j) == 0 || crossedCols.contains(j)) {
                continue;
            }
            colGainsMap.put(j, colZerosCountMap.get(j));
            for (int i = 0; i < matrix.length; i++) {
                if (matrix[i][j] != 0 || crossedRows.contains(i)) {
                    continue;
                }
                if (rowZerosCountMap.get(i) > 1) {
                    colGainsMap.replace(j, colGainsMap.get(j) - 1);
                }
            }
        }

        return colGainsMap;
    }

    private static double[][] reduceColumns(double[][] matrix) {
        double[][] modifiedMatrix = new double[matrix.length][matrix.length];
        for (int j = 0; j < matrix.length; j++) {
            double columnMin = matrix[0][j];
            for (int i = 1; i < matrix.length; i++) {
                if (columnMin > matrix[i][j]) {
                    columnMin = matrix[i][j];
                }
            }
            for (int i = 0; i < matrix.length; i++) {
                modifiedMatrix[i][j] = matrix[i][j] - columnMin;
            }
        }

        return modifiedMatrix;
    }

    private static double[][] reduceRows(double[][] matrix) {
        double[][] modifiedMatrix = new double[matrix.length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            double rowMin = matrix[i][0];
            for (int j = 1; j < matrix.length; j++) {
                if (rowMin > matrix[i][j]) {
                    rowMin = matrix[i][j];
                }
            }
            for (int j = 0; j < matrix.length; j++) {
                modifiedMatrix[i][j] = matrix[i][j] - rowMin;
            }
        }

        return modifiedMatrix;
    }

    private static double[][] convertToMinimizationProblem(double[][] matrix) {
        double[][] modifiedMatrix = new double[matrix.length][matrix.length];
        double maxElement = 0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                if (maxElement < matrix[i][j]) {
                    maxElement = matrix[i][j];
                }
            }
        }
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                modifiedMatrix[i][j] = maxElement - matrix[i][j];
            }
        }

        return modifiedMatrix;
    }

    private static double[][] initializeSimilarityMatrix(int matrixSize, Set<Edge> edges, List<Vertex> uniqueVertices, List<Record> uniqueRecords) {
        double[][] similarityMatrix = new double[matrixSize][matrixSize];
        for (int i = 0; i < uniqueVertices.size(); i++) {
            for (int j = 0; j < uniqueRecords.size(); j++) {
                if (edges.contains(new Edge(uniqueVertices.get(i), uniqueRecords.get(j)))) {
                    similarityMatrix[i][j] = SimilarityCalculator.calculateAverageSimilarity(uniqueVertices.get(i), uniqueRecords.get(j));
                }
            }
        }

        return similarityMatrix;
    }

    private static int findSimilarityMatrixSize(List<Vertex> vertices, List<Record> records) {
        return Math.max(vertices.size(), records.size());
    }

    private static List<Vertex> getUniqueVerticesFromEdges(Set<Edge> edges) {
        List<Vertex> uniqueVertices = new ArrayList<>();
        for (Edge e : edges) {
            if (!uniqueVertices.contains(e.vertex())) {
                uniqueVertices.add(e.vertex());
            }
        }

        return uniqueVertices;
    }

    private static List<Record> getUniqueRecordsFromEdges(Set<Edge> edges) {
        List<Record> uniqueRecords = new ArrayList<>();
        for (Edge e : edges) {
            if (!uniqueRecords.contains(e.record())) {
                uniqueRecords.add(e.record());
            }
        }

        return uniqueRecords;
    }
}
