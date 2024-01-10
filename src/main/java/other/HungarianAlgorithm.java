package other;

import db.Record;
import protocols.ArrayIndex2D;
import protocols.Edge;
import protocols.Vertex;

import java.util.*;

public class HungarianAlgorithm {
    // This method uses the Hungarian algorithm to select the optimal edges based on their similarity.
    public static Set<Edge> findOptimalEdges(Set<Edge> edges) {
        List<Vertex> uniqueVertices = new ArrayList<>();
        List<Record> uniqueRecords = new ArrayList<>();

        for (Edge e : edges) {
            if (!uniqueVertices.contains(e.vertex())) {
                uniqueVertices.add(e.vertex());
            }
            if (!uniqueRecords.contains(e.record())) {
                uniqueRecords.add(e.record());
            }
        }

        // Similarity matrix dimensions
        int n = findSimilarityMatrixSize(uniqueVertices, uniqueRecords);
        if (n == 0) {
            return new HashSet<>();
        }
        // Initialize similarityMatrix and keep a copy of its original state
        double[][] similarityMatrix = initializeSimilarityMatrix(n, edges, uniqueVertices, uniqueRecords);
        System.out.println("Initialization: " + Arrays.deepToString(similarityMatrix));


        double[][] modifiedSimilarityMatrix = convertToMinimizationProblem(similarityMatrix);
        System.out.println("Maximization to minimization conversion: " + Arrays.deepToString(similarityMatrix));

        modifiedSimilarityMatrix = reduceRows(modifiedSimilarityMatrix);
        System.out.println("Row reduction: " + Arrays.deepToString(modifiedSimilarityMatrix));

        modifiedSimilarityMatrix = reduceColumns(modifiedSimilarityMatrix);
        System.out.println("Column reduction: " + Arrays.deepToString(modifiedSimilarityMatrix));

        List<Integer> crossedRows = new ArrayList<>();
        List<Integer> crossedCols = new ArrayList<>();
        while (!checkForOptimalAssignment(modifiedSimilarityMatrix, crossedRows, crossedCols)) {
            System.out.println("Shifting zeros-----------------------------------------------");
            modifiedSimilarityMatrix = shiftZeros(modifiedSimilarityMatrix, crossedRows, crossedCols);
            crossedRows.clear();
            crossedCols.clear();
        }

        List<ArrayIndex2D> indices = chooseFinalAssignments(modifiedSimilarityMatrix);
        System.out.println("Indices");
        for (ArrayIndex2D index : indices) {
            System.out.println("[" + index.row() + ", " + index.col() + "]");
        }

        return getEdgesFromFinalAssignments(indices, uniqueVertices, uniqueRecords);
    }

    private static Set<Edge> getEdgesFromFinalAssignments(List<ArrayIndex2D> indices, List<Vertex> uniqueVertices, List<Record> uniqueRecords) {
        List<Integer> dummyRows = new ArrayList<>();
        List<Integer> dummyColumns = new ArrayList<>();
        if (uniqueVertices.size() > uniqueRecords.size()) {
            for (int i = uniqueRecords.size(); i < uniqueVertices.size(); i++) {
                dummyColumns.add(i);
            }
            System.out.println("Dummy Columns: " + dummyColumns);
        } else if (uniqueVertices.size() < uniqueRecords.size()) {
            for (int i = uniqueVertices.size(); i < uniqueRecords.size(); i++) {
                dummyRows.add(i);
            }
            System.out.println("Dummy Rows" + dummyRows);
        }

        Set<Edge> optimalEdges = new HashSet<>();
        for (ArrayIndex2D index : indices) {
            if(dummyRows.contains(index.row())) {
                continue;
            }
            if(dummyColumns.contains(index.col())) {
                continue;
            }
            optimalEdges.add(new Edge(uniqueVertices.get(index.row()), uniqueRecords.get(index.col())));
        }

        System.out.println("Optimal Edges: ");
        for (Edge edge : optimalEdges) {
            System.out.println("Vertex: " + edge.vertex() + ", Record: " + edge.record());
        }
        return optimalEdges;
    }


    /* Returns the indices of the final Assignments.
    There might be more than one way to choose n zeros (i.e. optimal assignments).
    All Cases will have the same total similarity. */
    private static List<ArrayIndex2D> chooseFinalAssignments(double[][] squareMatrix) {
        List<ArrayIndex2D> indices = new ArrayList<>();

        List<Integer> availableRows = new ArrayList<>();
        List<Integer> availableCols = new ArrayList<>();
        for (int i = 0; i < squareMatrix.length; i++) {
            availableRows.add(i);
            availableCols.add(i);
        }
        while (!availableRows.isEmpty()) {
            Map<Integer, Integer> rowZeroesMap = new HashMap<>();
            System.out.println(availableRows);
            for (int i = 0; i < squareMatrix.length; i++) {
                if (!availableRows.contains(i)) {
                    continue;
                }
                int zeroesCount = 0;
                for (int j = 0; j < squareMatrix.length; j++) {
                    if (!availableCols.contains(j)) {
                        continue;
                    }
                    if (squareMatrix[i][j] == 0) {
                        zeroesCount++;
                    }
                }
                rowZeroesMap.put(i, zeroesCount);
            }
            System.out.println(rowZeroesMap);

            int leastZeroesRow = Collections.min(rowZeroesMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            System.out.println("least: " + leastZeroesRow);
            for (int j = 0; j < squareMatrix.length; j++) {
                if(squareMatrix[leastZeroesRow][j] == 0 && availableCols.contains(j)) {
                    indices.add(new ArrayIndex2D(leastZeroesRow, j));
                    availableRows.remove(Integer.valueOf(leastZeroesRow));
                    availableCols.remove(Integer.valueOf(j));
                    break;
                }
            }
        }

        return indices;
    }

    private static double[][] shiftZeros(double[][] squareMatrix, List<Integer> crossedRows, List<Integer> crossedCols) {
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

    private static double getMinUncrossedNumber(double[][] squareMatrix, List<Integer> crossedRows, List<Integer> crossedCols) {
        double minNumber = -1.0;
        for (int i = 0; i < squareMatrix.length; i++) {
            if (crossedRows.contains(i)) {
                continue;
            }
            for (int j = 0; j < squareMatrix.length; j++) {
                if (crossedCols.contains(j)) {
                    continue;
                }
                if (minNumber < squareMatrix[i][j]) {
                    minNumber = squareMatrix[i][j];
                }
            }
        }
        return minNumber;
    }

    // The lists crossedRows and crossedCols are modified in order to be used in the shiftZeros method.
    private static boolean checkForOptimalAssignment(double[][] squareMatrix, List<Integer> crossedRows, List<Integer> crossedCols) {
        int minLines = 0;
        Map<Integer, Integer> rowZerosMap = new HashMap<>();
        Map<Integer, Integer> colZerosMap = new HashMap<>();

        while (true) {
            for (int i = 0; i < squareMatrix.length; i++) {
                rowZerosMap.put(i, 0);
                colZerosMap.put(i, 0);
                for (int j = 0; j < squareMatrix.length; j++) {
                    if (squareMatrix[i][j] == 0 && !crossedRows.contains(i) && !crossedCols.contains(j)) {
                        rowZerosMap.put(i, rowZerosMap.get(i) + 1);
                    }
                    if (squareMatrix[j][i] == 0 && !crossedRows.contains(j) && !crossedCols.contains(i)) {
                        colZerosMap.put(i, colZerosMap.get(i) + 1);
                    }
                }
            }
            System.out.println(rowZerosMap);
            System.out.println(colZerosMap);

            int mostZerosRow = Collections.max(rowZerosMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            int mostZerosCol = Collections.max(colZerosMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            if (rowZerosMap.get(mostZerosRow) == 0 && colZerosMap.get(mostZerosCol) == 0) {
                System.out.println("End of loop.");
                break;
            }
            if (rowZerosMap.get(mostZerosRow) >= colZerosMap.get(mostZerosCol)) {
                crossedRows.add(mostZerosRow);
            } else {
                crossedCols.add(mostZerosCol);
            }
            minLines++;

            System.out.println("mostZerosRow: " + mostZerosRow);
            System.out.println("mostZerosCol: " + mostZerosCol);
            System.out.println("crossedRows: " + crossedRows);
            System.out.println("crossedCol: " + crossedCols);
        }

        if (minLines < squareMatrix.length) {
            System.out.println("min Lines are less thant the matrix length.");
            Scanner sc = new Scanner(System.in);
            int i = sc.nextInt();
        }
//        System.out.println(minLines);

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
                    System.out.println(uniqueVertices.get(i).records());
                    System.out.println(uniqueRecords.get(j));
                    similarityMatrix[i][j] = SimilarityCalculator.calculateAverageSimilarity(uniqueVertices.get(i), uniqueRecords.get(j));
                }
            }
        }

        return similarityMatrix;
    }

    private static int findSimilarityMatrixSize(List<Vertex> vertices, List<Record> records) {
        // Vertices will probably always be more than the records
        return Math.max(vertices.size(), records.size());
    }
}
