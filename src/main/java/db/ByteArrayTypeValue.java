package db;

import java.util.Arrays;

public class ByteArrayTypeValue implements DynamicTypeValue {
    public byte[] value;

    public ByteArrayTypeValue(byte[] value) {
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
