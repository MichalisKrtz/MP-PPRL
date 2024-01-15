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
    private static List<ArrayIndex2D> chooseFinalAssignments(double[][] squareMatrix) {
        List<ArrayIndex2D> indices = new ArrayList<>();

        Set<Integer> availableRows = new HashSet<>();
        Set<Integer> availableCols = new HashSet<>();
        for (int i = 0; i < squareMatrix.length; i++) {
            availableRows.add(i);
            availableCols.add(i);
        }
        while (!availableRows.isEmpty()) {
            Map<Integer, Integer> rowZeroesMap = new HashMap<>();
            for (int i = 0; i < squareMatrix.length; i++) {
                if (!availableRows.contains(i)) {
                    continue;
                }
                int zeroesCount = 0;
                for (int j = 0; j < squareMatrix.length; j++) {
                    if (!availableCols.contains(j)) {
                        continue;
                    }
                    if (squareMatrix[i][j] != 0) {
                        continue;
                    }
                    zeroesCount++;
                }
                rowZeroesMap.put(i, zeroesCount);
            }

            int leastZeroesRow = Collections.min(rowZeroesMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            for (int j = 0; j < squareMatrix.length; j++) {
                if (squareMatrix[leastZeroesRow][j] == 0 && availableCols.contains(j)) {
                    indices.add(new ArrayIndex2D(leastZeroesRow, j));
                    availableRows.remove(leastZeroesRow);
                    availableCols.remove(j);
                    break;
                }
            }
        }

        return indices;
    }

    private static double[][] shiftZeros(double[][] squareMatrix, Set<Integer> crossedRows, Set<Integer> crossedCols) {
        double minNumber = getMinUncrossedNumber(squareMatrix, crossedRows, crossedCols);
        double[][] modifiedMatrix = new double[squareMatrix.length][squareMatrix.length];
        for (int i = 0; i < squareMatrix.length; i++) {
            for (int j = 0; j < squareMatrix.length; j++) {
                if (crossedRows.contains(i) && crossedCols.contains(j)) {
                    modifiedMatrix[i][j] = squareMatrix[i][j] + minNumber;
                } else if (crossedRows.contains(i) || crossedCols.contains(j)) {
                    modifiedMatrix[i][j] = squareMatrix[i][j];
                } else {
                    modifiedMatrix[i][j] = squareMatrix[i][j] - minNumber;
                }
            }
        }

        return modifiedMatrix;
    }

    private static double getMinUncrossedNumber(double[][] squareMatrix, Set<Integer> crossedRows, Set<Integer> crossedCols) {
        double minNumber = 1;
        for (int i = 0; i < squareMatrix.length; i++) {
            if (crossedRows.contains(i)) {
                continue;
            }
            for (int j = 0; j < squareMatrix.length; j++) {
                if (crossedCols.contains(j)) {
                    continue;
                }
                if (minNumber > squareMatrix[i][j]) {
                    minNumber = squareMatrix[i][j];
                }
            }
        }
        System.out.println("Minimum uncrossed number: " + minNumber);
        return minNumber;
    }

    //  The lists crossedRows and crossedCols are modified in order to be used in the shiftZeros method.
    private static boolean checkForOptimalAssignment(double[][] squareMatrix, Set<Integer> crossedRows, Set<Integer> crossedCols) {
        int minLines = 0;
        Map<Integer, Integer> rowZerosMap = new HashMap<>();
        Map<Integer, Integer> colZerosMap = new HashMap<>();
        while (true) {
            for (int i = 0; i < squareMatrix.length; i++) {
                rowZerosMap.put(i, 0);
                colZerosMap.put(i, 0);
            }
            for (int i = 0; i < squareMatrix.length; i++) {
                for (int j = 0; j < squareMatrix.length; j++) {
                    if (squareMatrix[i][j] == 0 && !crossedRows.contains(i) && !crossedCols.contains(j)) {
                        rowZerosMap.put(i, rowZerosMap.get(i) + 1);
                        colZerosMap.put(j, colZerosMap.get(j) + 1);
                    }
                }
            }
            int mostZerosRow = Collections.max(rowZerosMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            int mostZerosCol = Collections.max(colZerosMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            //  Check if both the row and column with most zeros have 0 zeros, which means all zeros have been crossed.
            if (rowZerosMap.get(mostZerosRow) == 0 && colZerosMap.get(mostZerosCol) == 0) {
                break;
            }
            if (rowZerosMap.get(mostZerosRow) > colZerosMap.get(mostZerosCol)) {
                crossedRows.add(mostZerosRow);
            } else if (rowZerosMap.get(mostZerosRow) < colZerosMap.get(mostZerosCol)) {
                crossedCols.add(mostZerosCol);
            } else {
                int mostZerosRowsCount = 0;
                int mostZerosColsCount = 0;
                for (Integer value : rowZerosMap.values()) {
                    if (value == 0) {
                        mostZerosRowsCount++;
                    }
                }
                for (Integer value : colZerosMap.values()) {
                    if (value == 0) {
                        mostZerosColsCount++;
                    }
                }
                if (mostZerosRowsCount >= mostZerosColsCount) {
                    crossedRows.add(mostZerosRow);
                } else {
                    crossedCols.add(mostZerosCol);
                }
            }
            minLines++;
        }

        return minLines == squareMatrix.length;
    }

    private static double[][] reduceColumns(double[][] squareMatrix) {
        double[][] modifiedMatrix = new double[squareMatrix.length][squareMatrix.length];
        for (int j = 0; j < squareMatrix.length; j++) {
            double columnMin = squareMatrix[0][j];
            for (int i = 1; i < squareMatrix.length; i++) {
                if (columnMin > squareMatrix[i][j]) {
                    columnMin = squareMatrix[i][j];
                }
            }
            for (int i = 0; i < squareMatrix.length; i++) {
                modifiedMatrix[i][j] = squareMatrix[i][j] - columnMin;
            }
        }

        return modifiedMatrix;
    }

    private static double[][] reduceRows(double[][] squareMatrix) {
        double[][] modifiedMatrix = new double[squareMatrix.length][squareMatrix.length];
        for (int i = 0; i < squareMatrix.length; i++) {
            double rowMin = squareMatrix[i][0];
            for (int j = 1; j < squareMatrix.length; j++) {
                if (rowMin > squareMatrix[i][j]) {
                    rowMin = squareMatrix[i][j];
                }
            }
            for (int j = 0; j < squareMatrix.length; j++) {
                modifiedMatrix[i][j] = squareMatrix[i][j] - rowMin;
            }
        }

        return modifiedMatrix;
    }

    private static double[][] convertToMinimizationProblem(double[][] squareMatrix) {
        double[][] modifiedMatrix = new double[squareMatrix.length][squareMatrix.length];
        double maxElement = 0;
        for (int i = 0; i < squareMatrix.length; i++) {
            for (int j = 0; j < squareMatrix.length; j++) {
                if (maxElement < squareMatrix[i][j]) {
                    maxElement = squareMatrix[i][j];
                }
            }
        }
        for (int i = 0; i < squareMatrix.length; i++) {
            for (int j = 0; j < squareMatrix.length; j++) {
                modifiedMatrix[i][j] = maxElement - squareMatrix[i][j];
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
