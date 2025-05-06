package mp_pprl.core.optimization;

public class FloatMatrix {
    private final float[] data;
    private final int rows;
    private final int cols;

    public FloatMatrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.data = new float[rows * cols];
    }

    public float get(int row, int col) {
        return data[row * cols + col];
    }

    public void set(int row, int col, float value) {
        data[row * cols + col] = value;
    }

    public int length() {
        return rows;
    }

    public int rowsCount() {
        return rows;
    }

    public int colsCount() {
        return cols;
    }
}
