package mp_pprl.incremental_clustering.optimization;

import mp_pprl.core.graph.Edge;
import mp_pprl.core.graph.Cluster;

import java.util.*;

/**
 * An implementation of the classic hungarian algorithm for the assignment problem.
 * <p>
 * Copyright 2007 Gary Baker (GPL v3)
 *
 * @author gbaker
 */
public class HungarianAlgorithm {
    public static Set<Edge> computeAssignments(Set<Edge> edges, boolean maximization) {
        List<Cluster> uniqueClustersX = getUniqueClustersX(edges);
        List<Cluster> uniqueClustersY = getUniqueClustersY(edges);

        int n = findSimilarityMatrixSize(uniqueClustersX, uniqueClustersY);

        if (n == 0) {
            return new HashSet<>();
        }

        double[][] similarityMatrix = initializeSimilarityMatrix(n, edges, uniqueClustersX, uniqueClustersY, maximization);

        if (maximization) {
            similarityMatrix = convertToMaximizationProblem(similarityMatrix);
        }

        // subtract minimum value from rows and columns to create lots of zeroes
        reduceMatrix(similarityMatrix);


        // non-negative values are the index of the starred or primed zero in the row or column
        int[] starsByRow = new int[similarityMatrix.length];
        Arrays.fill(starsByRow, -1);
        int[] starsByCol = new int[similarityMatrix[0].length];
        Arrays.fill(starsByCol, -1);
        int[] primesByRow = new int[similarityMatrix.length];
        Arrays.fill(primesByRow, -1);

        // 1s mean covered, 0s mean not covered
        int[] coveredRows = new int[similarityMatrix.length];
        int[] coveredCols = new int[similarityMatrix[0].length];

        // star any zero that has no other starred zero in the same row or column
        initStars(similarityMatrix, starsByRow, starsByCol);
        coverColumnsOfStarredZeroes(starsByCol, coveredCols);

        while (!allAreCovered(coveredCols)) {

            int[] primedZero = primeSomeUncoveredZero(similarityMatrix, primesByRow, coveredRows, coveredCols);

            while (primedZero == null) {
                // keep making more zeroes until we find something that we can prime (i.e. a zero that is uncovered)
                makeMoreZeroes(similarityMatrix, coveredRows, coveredCols);
                primedZero = primeSomeUncoveredZero(similarityMatrix, primesByRow, coveredRows, coveredCols);
            }

            // check if there is a starred zero in the primed zero's row
            int columnIndex = starsByRow[primedZero[0]];
            if (-1 == columnIndex) {

                // if not, then we need to increment the zeroes and start over
                incrementSetOfStarredZeroes(primedZero, starsByRow, starsByCol, primesByRow);
                Arrays.fill(primesByRow, -1);
                Arrays.fill(coveredRows, 0);
                Arrays.fill(coveredCols, 0);
                coverColumnsOfStarredZeroes(starsByCol, coveredCols);
            } else {

                // cover the row of the primed zero and uncover the column of the starred zero in the same row
                coveredRows[primedZero[0]] = 1;
                coveredCols[columnIndex] = 0;
            }
        }

        // ok now we should have assigned everything
        // take the starred zeroes in each column as the correct assignments

        int[][] retval = new int[similarityMatrix.length][];
        for (int i = 0; i < starsByCol.length; i++) {
            retval[i] = new int[]{starsByCol[i], i};
        }


        return getEdgesFromFinalAssignments(retval, edges, uniqueClustersX, uniqueClustersY);
    }

    private static boolean allAreCovered(int[] coveredCols) {
        for (int covered : coveredCols) {
            if (0 == covered) return false;
        }
        return true;
    }


    /**
     * the first step of the hungarian algorithm
     * is to find the smallest element in each row
     * and subtract it's values from all elements
     * in that row
     *
     * @return the next step to perform
     */
    private static void reduceMatrix(double[][] matrix) {

        for (int i = 0; i < matrix.length; i++) {

            // find the min value in the row
            double minValInRow = Double.MAX_VALUE;
            for (int j = 0; j < matrix[i].length; j++) {
                if (minValInRow > matrix[i][j]) {
                    minValInRow = matrix[i][j];
                }
            }

            // subtract it from all values in the row
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] -= minValInRow;
            }
        }

        for (int i = 0; i < matrix[0].length; i++) {
            double minValInCol = Double.MAX_VALUE;
            for (int j = 0; j < matrix.length; j++) {
                if (minValInCol > matrix[j][i]) {
                    minValInCol = matrix[j][i];
                }
            }

            for (int j = 0; j < matrix.length; j++) {
                matrix[j][i] -= minValInCol;
            }

        }

    }

    /**
     * init starred zeroes
     * <p>
     * for each column find the first zero
     * if there is no other starred zero in that row
     * then star the zero, cover the column and row and
     * go onto the next column
     *
     * @param costMatrix
     * @param starredZeroes
     * @param coveredRows
     * @param coveredCols
     * @return the next step to perform
     */
    private static void initStars(double[][] costMatrix, int[] starsByRow, int[] starsByCol) {


        int[] rowHasStarredZero = new int[costMatrix.length];
        int[] colHasStarredZero = new int[costMatrix[0].length];

        for (int i = 0; i < costMatrix.length; i++) {
            for (int j = 0; j < costMatrix[i].length; j++) {
                if (0 == costMatrix[i][j] && 0 == rowHasStarredZero[i] && 0 == colHasStarredZero[j]) {
                    starsByRow[i] = j;
                    starsByCol[j] = i;
                    rowHasStarredZero[i] = 1;
                    colHasStarredZero[j] = 1;
                    break; // move onto the next row
                }
            }
        }
    }


    /**
     * just marke the columns covered for any coluimn containing a starred zero
     *
     * @param starsByCol
     * @param coveredCols
     */
    private static void coverColumnsOfStarredZeroes(int[] starsByCol, int[] coveredCols) {
        for (int i = 0; i < starsByCol.length; i++) {
            coveredCols[i] = -1 == starsByCol[i] ? 0 : 1;
        }
    }


    /**
     * finds some uncovered zero and primes it
     *
     * @param matrix
     * @param primesByRow
     * @param coveredRows
     * @param coveredCols
     * @return
     */
    private static int[] primeSomeUncoveredZero(double matrix[][], int[] primesByRow,
                                                int[] coveredRows, int[] coveredCols) {


        // find an uncovered zero and prime it
        for (int i = 0; i < matrix.length; i++) {
            if (1 == coveredRows[i]) continue;
            for (int j = 0; j < matrix[i].length; j++) {
                // if it's a zero and the column is not covered
                if (0 == matrix[i][j] && 0 == coveredCols[j]) {

                    // ok this is an unstarred zero
                    // prime it
                    primesByRow[i] = j;
                    return new int[]{i, j};
                }
            }
        }
        return null;

    }

    /**
     * @param unpairedZeroPrime
     * @param starsByRow
     * @param starsByCol
     * @param primesByRow
     */
    private static void incrementSetOfStarredZeroes(int[] unpairedZeroPrime, int[] starsByRow, int[] starsByCol, int[] primesByRow) {

        // build the alternating zero sequence (prime, star, prime, star, etc)
        int i, j = unpairedZeroPrime[1];

        Set<int[]> zeroSequence = new LinkedHashSet<int[]>();
        zeroSequence.add(unpairedZeroPrime);
        boolean paired = false;
        do {
            i = starsByCol[j];
            paired = -1 != i && zeroSequence.add(new int[]{i, j});
            if (!paired) break;

            j = primesByRow[i];
            paired = -1 != j && zeroSequence.add(new int[]{i, j});

        } while (paired);


        // unstar each starred zero of the sequence
        // and star each primed zero of the sequence
        for (int[] zero : zeroSequence) {
            if (starsByCol[zero[1]] == zero[0]) {
                starsByCol[zero[1]] = -1;
                starsByRow[zero[0]] = -1;
            }
            if (primesByRow[zero[0]] == zero[1]) {
                starsByRow[zero[0]] = zero[1];
                starsByCol[zero[1]] = zero[0];
            }
        }

    }


    private static void makeMoreZeroes(double[][] matrix, int[] coveredRows, int[] coveredCols) {

        // find the minimum uncovered value
        double minUncoveredValue = Double.MAX_VALUE;
        for (int i = 0; i < matrix.length; i++) {
            if (0 == coveredRows[i]) {
                for (int j = 0; j < matrix[i].length; j++) {
                    if (0 == coveredCols[j] && matrix[i][j] < minUncoveredValue) {
                        minUncoveredValue = matrix[i][j];
                    }
                }
            }
        }

        // add the min value to all covered rows
        for (int i = 0; i < coveredRows.length; i++) {
            if (1 == coveredRows[i]) {
                for (int j = 0; j < matrix[i].length; j++) {
                    matrix[i][j] += minUncoveredValue;
                }
            }
        }

        // subtract the min value from all uncovered columns
        for (int i = 0; i < coveredCols.length; i++) {
            if (0 == coveredCols[i]) {
                for (int j = 0; j < matrix.length; j++) {
                    matrix[j][i] -= minUncoveredValue;
                }
            }
        }
    }


    // My methods
    private static Set<Edge> getEdgesFromFinalAssignments(int[][] indices, Set<Edge> edges,List<Cluster> uniqueClustersX, List<Cluster> uniqueClustersY) {
        List<Integer> dummyRows = new ArrayList<>();
        List<Integer> dummyColumns = new ArrayList<>();
        if (uniqueClustersX.size() > uniqueClustersY.size()) {
            for (int i = uniqueClustersY.size(); i < uniqueClustersX.size(); i++) {
                dummyColumns.add(i);
            }
        } else if (uniqueClustersX.size() < uniqueClustersY.size()) {
            for (int i = uniqueClustersX.size(); i < uniqueClustersY.size(); i++) {
                dummyRows.add(i);
            }
        }

        Set<Edge> optimalEdges = new HashSet<>();
        for (int[] index : indices) {
            if (dummyRows.contains(index[0])) {
                continue;
            }

            if (dummyColumns.contains(index[1])) {
                continue;
            }

            for (Edge edge : edges) {
                if (edge.c1().equals(uniqueClustersX.get(index[0])) && edge.c2().equals(uniqueClustersY.get(index[1]))) {
                    optimalEdges.add(edge);
                }
            }
        }

        return optimalEdges;
    }

    private static double[][] convertToMaximizationProblem(double[][] matrix) {
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

    private static double[][] initializeSimilarityMatrix(int matrixSize, Set<Edge> edges, List<Cluster> uniqueClustersX, List<Cluster> uniqueClustersY, boolean maximization) {
        double[][] similarityMatrix = new double[matrixSize][matrixSize];

        if (!maximization) {
            for(int i = 0; i < matrixSize; i++) {
                for (int j = 0; j < matrixSize; j++) {
                    similarityMatrix[i][j] = Double.MAX_VALUE;
                }
            }
        }

        for (Edge edge : edges) {
            int clusterXIndex = uniqueClustersX.indexOf(edge.c1());
            int clusterYIndex = uniqueClustersY.indexOf(edge.c2());
            similarityMatrix[clusterXIndex][clusterYIndex] = edge.metric();
        }

        return similarityMatrix;
    }

    private static int findSimilarityMatrixSize(List<Cluster> clustersX, List<Cluster> clustersY) {
        return Math.max(clustersX.size(), clustersY.size());
    }

    private static List<Cluster> getUniqueClustersX(Set<Edge> edges) {
        List<Cluster> uniqueClustersX = new ArrayList<>();
        for (Edge e : edges) {
            if (!uniqueClustersX.contains(e.c1())) {
                uniqueClustersX.add(e.c1());
            }
        }

        return uniqueClustersX;
    }

    private static List<Cluster> getUniqueClustersY(Set<Edge> edges) {
        List<Cluster> uniqueClustersY = new ArrayList<>();
        for (Edge e : edges) {
            if (!uniqueClustersY.contains(e.c2())) {
                uniqueClustersY.add(e.c2());
            }
        }

        return uniqueClustersY;
    }

}