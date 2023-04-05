package ru.itmo.ad.parser.java.modifiers;

public record Modifiers(
        boolean isFinal,
        boolean isStatic,
        Privacy privacy,
        boolean isDefault,
        ClassType classType,
        boolean isSealed
) {

    public enum ClassType {
        RECORD("record"),
        CLASS("class"),
        INTERFACE("interface"),
        ENUM("enum"),
        ;
        private final String value;

        ClassType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum Privacy {
        PRIVATE("private"),
        PROTECTED("protected"),
        PUBLIC("public"),
        DEFAULT(""),
        ;
        private final String value;

        Privacy(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(privacy.value);
        if (!sb.isEmpty()) {
            sb.append(" ");
        }
        if (isFinal) {
            sb.append("final ");
        }
        if (isStatic) {
            sb.append("static ");
        }
        if (classType != null) {
            sb.append(classType.value);
        }
        return sb.toString();
    }

    public boolean isClass() {
        return classType != null;
    }
}
