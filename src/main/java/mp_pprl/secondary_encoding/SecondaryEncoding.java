package mp_pprl.secondary_encoding;

import java.util.Objects;

/* -----------------------
 * Secondary encoding (X = sum absolute zero-based positions, Y = count ones)
 * ----------------------- */
public class SecondaryEncoding {
    final long X;
    final int Y;
    public SecondaryEncoding(long X, int Y) { this.X = X; this.Y = Y; }
    @Override public boolean equals(Object o) {
        if (!(o instanceof SecondaryEncoding)) return false;
        SecondaryEncoding s = (SecondaryEncoding)o;
        return this.X == s.X && this.Y == s.Y;
    }
    @Override public int hashCode() { return Objects.hash(X, Y); }
}
