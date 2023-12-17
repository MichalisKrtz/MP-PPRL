package db;

public class IntegerTypeValue implements DynamicTypeValue {
    public Integer value;

    public IntegerTypeValue(Integer value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String getValueAsString() {
        return String.valueOf(value);
    }
}
