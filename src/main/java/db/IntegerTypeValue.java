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
    public String getStringValue() {
        return String.valueOf(value);
    }

    @Override
    public void printValueDescription() {
        if (value == null) {
            System.out.println("The value is null");
            return;
        }
        System.out.printf("The value %d is an Integer%n", value);
    }
}
