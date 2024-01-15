package mp_pprl.db;

public class IntegerValue implements DynamicValue {
    public Integer value;

    public IntegerValue(Integer value) {
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
