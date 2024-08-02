package mp_pprl.core.domain;

import java.util.Arrays;

public class ByteArrayValue implements DynamicValue {
    private final byte[] value;

    public ByteArrayValue(byte[] value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String getValueAsString() {
        return Arrays.toString(value);
    }
}
