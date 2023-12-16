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
    public String getStringValue() {
        return value;
    }

    @Override
    public void printValueDescription() {
        if (value == null) {
            System.out.println("The value is null");
            return;
        }
        System.out.printf("The value %s is a String%n", value);
    }
}
