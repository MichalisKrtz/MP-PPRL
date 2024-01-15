package mp_pprl.db;


public class DynamicValueFactory {
    public enum DynamicValueType {
        TEXT("TEXT"),
        INTEGER("INTEGER"),
        BYTE_ARRAY("BYTE_ARRAY");

        final String type;

        DynamicValueType(String type) {
            this.type = type;
        }
    }

    public static DynamicValue createDynamicValue(String dynamicValueTypeName, Object value) {
        DynamicValueType dynamicValueType = DynamicValueType.valueOf(dynamicValueTypeName);
        switch (dynamicValueType) {
            case TEXT -> {
                return new StringValue((String) value);
            }
            case INTEGER -> {
                return new IntegerValue((Integer) value);
            }
            case BYTE_ARRAY -> {
                return new ByteArrayValue((byte[]) value);
            }
            default -> System.out.println("DATA TYPE IS NOT SUPPORTED");
        }
        return null;
    }
}
