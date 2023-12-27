package other;

import db.Record;
import protocols.Edge;
import protocols.Vertex;

import java.util.*;

public class HungarianAlgorithm {
    // This method uses the Hungarian algorithm to select the optimal edges based on their similarity.
    public static Set<Edge> findOptimalEdges(Set<Edge> edges) {
        Set<Edge> optimalEdges = new HashSet<>();
        List<Vertex> vertices = new ArrayList<>();
        List<Record> records = new ArrayList<>();

        for (Edge e : edges) {
            if (!vertices.contains(e.vertex())) {
                vertices.add(e.vertex());
            }
            if (!records.contains(e.record())) {
                records.add(e.record());
            }
        }

        // Similarity matrix dimensions
        int n = findSimilarityMatrixSize(vertices, records);
        // Initialize similarityMatrix
        double[][] similarityMatrix = initializeSimilarityMatrix(n, edges, vertices, records);
        System.out.println("Initialization: " + Arrays.deepToString(similarityMatrix));

        convertToMinimizationProblem(similarityMatrix);
        System.out.println("Maximization to minimization conversion: " + Arrays.deepToString(similarityMatrix));

        reduceRows(similarityMatrix);
        System.out.println("Row reduction: " + Arrays.deepToString(similarityMatrix));

        reduceColumns(similarityMatrix);
        System.out.println("Column reduction: " + Arrays.deepToString(similarityMatrix));



        return optimalEdges;
    }

    private static void testForOptimalAssignment(double[][] squareMatrix) {

    }

    private static void reduceColumns(double[][] squareMatrix) {
        int n = squareMatrix.length;
        for (int j = 0; j < n; j++) {
            double columnMin = squareMatrix[0][j];
            for (int i = 1; i < n; i++) {
                if (columnMin > squareMatrix[i][j]) {
                    columnMin = squareMatrix[i][j];
                }
            }
            for (int i = 0; i < n; i++) {
                squareMatrix[i][j] -= columnMin;
            }
        }
    }

    private static void reduceRows(double[][] squareMatrix) {
        int n = squareMatrix.length;
        for (int i = 0; i < n; i++) {
            double rowMin = squareMatrix[i][0];
            for (int j = 1; j < n; j++) {
                if (rowMin > squareMatrix[i][j]) {
                    rowMin = squareMatrix[i][j];
                }
            }
            for (int j = 0; j < n; j++) {
                squareMatrix[i][j] -= rowMin;
            }
        }
    }

    private static void convertToMinimizationProblem(double[][] squareMatrix) {
        int n = squareMatrix.length;
        double maxElement = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (maxElement < squareMatrix[i][j]) {
                    maxElement = squareMatrix[i][j];
                }
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                squareMatrix[i][j] = maxElement - squareMatrix[i][j];
            }
        }
    }

    private static double[][] initializeSimilarityMatrix(int matrixSize, Set<Edge> edges, List<Vertex> vertices, List<Record> records) {
        double[][] similarityMatrix = new double[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (edges.contains(new Edge(vertices.get(i), records.get(j)))) {
                    similarityMatrix[i][j] = SimilarityCalculator.calculateAverageSimilarity(vertices.get(i), records.get(j));
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
