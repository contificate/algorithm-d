import java.util.*;

/**
 * Trie data structure used for Aho-Corasick automaton construction.
 */
public class Trie<T> {
    public static class Node<X> {
        private final Map<X, Node<X>> arrows = new HashMap<>();
        private final Node<X> parent;
        private Node<X> suffix;
        private Node<X> output;
        private Optional<List<X>> pattern = Optional.empty();

        public Node(Node<X> parent) {
            this.parent = parent;
        }

        public void add(X symbol, Node<X> target) {
            arrows.put(symbol, target);
        }

        /**
         * Compute state/node reached when transitioning on a symbol, x.
         * This can be precomputed as the delta/"next move" function, as in
         * Aho and Corasick's paper.
         */
        public Node<X> gotoOn(X symbol) {
            var node = this;
            while (!node.arrows.containsKey(symbol)) {
                node = node.suffix;
                if (node.parent == null) break;
            }

            return node.arrows.getOrDefault(symbol, node);
        }

        /**
         * Follow output links.
         * As with gotoOn, this too can be statically resolved for a
         * more efficient implementation.
         */
        public List<List<X>> getOutputs() {
            List<List<X>> outputs = new ArrayList<>();
            pattern.ifPresent(outputs::add);

            var link = this.output;
            while (link != null) {
                link.pattern.ifPresent(outputs::add);
                link = link.output;
            }

            return outputs;
        }
    }

    private final Node<T> root = new Node<>(null);

    public Node<T> getRoot() {
        return root;
    }

    private Trie() {
    }

    /**
     * Add a pattern to the trie.
     */
    public void add(List<T> pattern) {
        var node = root;

        for (T s : pattern) {
            var next = node.arrows.get(s);
            if (next == null) {
                next = new Node<>(node);
                node.arrows.put(s, next);
            }

            node = next;
        }

        node.pattern = Optional.of(new ArrayList<>(pattern));
    }

    /**
     * Compute suffix and output links using a breadth-first traversal.
     */
    public void computeAutomatonLinks() {
        Queue<Pair<T, Node<T>>> q = new LinkedList<>();

        // root and its children have root as their suffix link
        root.suffix = root;
        root.arrows.forEach((initial, child) -> {
            child.suffix = root;

            // enqueue root's grandchildren first
            child.arrows.forEach((symbol, grandchild) -> q.add(new Pair<>(symbol, grandchild)));
        });

        while (!q.isEmpty()) {
            var pair = q.poll();
            var symbol = pair.first();
            var node = pair.second();

            // compute suffix link by examining parent's suffix link
            // i.e. the suffix for node's pattern's prefix, excluding current symbol
            var suffix = node.parent.suffix;
            while (!suffix.arrows.containsKey(symbol)) {
                // chase up another suffix link
                suffix = suffix.suffix;

                // avoid endless loop
                if (suffix == root) break;
            }

            // could be the case that we broke on root
            node.suffix = suffix.arrows.getOrDefault(symbol, root);

            // compute output link for node
            node.output = node.suffix.pattern.isPresent() ? node.suffix : node.suffix.output;

            // queue children
            node.arrows.forEach((sym, child) -> q.add(new Pair<>(sym, child)));
        }
    }

    /**
     * Builder abstraction to avoid direct construction of a trie
     * without precomputed suffix and output links.
     */
    public static class TrieBuilder<X> {
        private final Trie<X> root = new Trie<>();

        public TrieBuilder<X> add(List<X> pattern) {
            root.add(pattern);
            return this;
        }

        public Trie<X> build() {
            root.computeAutomatonLinks();
            return root;
        }
    }

}

