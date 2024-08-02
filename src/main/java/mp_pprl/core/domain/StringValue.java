package mp_pprl.core.domain;

public class StringValue implements DynamicValue {
    private final String value;

    public StringValue(String value) {
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
