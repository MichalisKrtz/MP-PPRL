package db;

public class StringTypeValue implements DynamicTypeValue {
    public String value;

    public StringTypeValue(String value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String getValueAsString() {
        return value;
    }
}
