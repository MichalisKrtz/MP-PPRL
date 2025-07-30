package mp_pprl.core.optimization;

//public class FloatMatrix {
//    private final float[] data;
//    private final int rows;
//    private final int cols;
//
//    public FloatMatrix(int rows, int cols) {
//        this.rows = rows;
//        this.cols = cols;
//        this.data = new float[rows * cols];
//    }
//
//    public float get(int row, int col) {
//        return data[row * cols + col];
//    }
//
//    public void set(int row, int col, float value) {
//        data[row * cols + col] = value;
//    }
//
//    public int length() {
//        return rows;
//    }
//
//    public int rowsCount() {
//        return rows;
//    }
//
//    public int colsCount() {
//        return cols;
//    }
//}

//import org.ejml.data.DMatrixSparseCSC;
//
//public class FloatMatrix {
//    private final DMatrixSparseCSC matrix;
//
//    public FloatMatrix(int rows, int cols, int initialNonZeros) {
//        this.matrix = new DMatrixSparseCSC(rows, cols, initialNonZeros);
//    }
//
//    public float get(int row, int col) {
//        return (float) matrix.get(row, col);
//    }
//
//    public void set(int row, int col, float value) {
//        if (matrix.get(row, col) == 0.0f && value == 0.0f)
//        {
//            return;
//        }
//        matrix.set(row, col, value);
//    }
//
//    public int length() {
//        return matrix.getNumRows();
//    }
//
//    public int rowsCount() {
//        return matrix.getNumRows();
//    }
//
//    public int colsCount() {
//        return matrix.getNumCols();
//    }
//
//    public DMatrixSparseCSC getRawMatrix() {
//        return matrix;
//    }
//}


public class FloatMatrix {
    private final float[][] matrix;

    public FloatMatrix(int rows, int cols) {
        matrix = new float[rows][cols];
    }

    public float get(int row, int col) {
        return matrix[row][col];
    }

    public void set(int row, int col, float value) {
        matrix[row][col] = value;
    }

    public int length() {
        return matrix.length;
    }

    public int rowsCount() {
        return matrix[0].length;
    }

    public int colsCount() {
        return matrix.length;
    }

}
