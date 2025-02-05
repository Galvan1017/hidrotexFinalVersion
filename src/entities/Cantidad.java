

public record Cantidad(Number value) {
    public boolean isInteger() {
        return value instanceof Integer;
    }

    public boolean isDouble() {
        return value instanceof Double;
    }

    public int asInteger() {
        return value.intValue();
    }

    public double asDouble() {
        return value.doubleValue();
    }

    @Override
    public String toString() {
        return ""+value;
    }


}
