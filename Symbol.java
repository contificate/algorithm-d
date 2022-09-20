import java.util.Objects;

/**
 * Symbol as input for our Aho-Corasick automaton.
 */
public abstract class Symbol {
    /**
     * An index symbol represents following a child subtree's index.
     * The path strings contain indices to differentiate between subtrees when matching.
     */
    public static class Index extends Symbol {
        private final int value;

        public Index(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Index index = (Index) o;
            return value == index.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return Integer.toString(value);
        }
    }

    /**
     * A label symbol represents following a tree node labelled with the
     * corresponding label.
     */
    public static class Label extends Symbol {
        private final String value;

        public Label(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Label label = (Label) o;
            return Objects.equals(value, label.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
