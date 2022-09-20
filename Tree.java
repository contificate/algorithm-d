import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a tree such as a(b(_), _).
 * Both pattern and subject trees use the same representation.
 */
public abstract class Tree {
    public static class Node extends Tree {
        private final String label;
        private final List<Tree> children;

        // in-place match bookkeeping for simplicity
        public int count = 0;
        public boolean match = false;

        public Node(String label, List<Tree> children) {
            this.label = label;
            this.children = children;
        }

        public String getLabel() {
            return label;
        }

        public List<Tree> getChildren() {
            return children;
        }

        @Override
        public String toString() {
            if (children.isEmpty())
                return label;

            final var subtrees = children.stream().map(Object::toString).collect(Collectors.joining(","));
            final var node = String.format("%s(%s)", label, subtrees);
            return match ? String.format("[%s]", node) : node;
        }
    }

    public static class Wildcard extends Tree {
        @Override
        public String toString() {
            return "_";
        }
    }

    /**
     * Messy path string collection algorithm.
     *
     * The trie can be constructed directly from the pattern tree, but then it becomes
     * somewhat messy in how you deal with multiple patterns. For multiple patterns,
     * one can tag each path string w/ the pattern tree it came from and then maintain separate
     * match counters for each pattern.
     */
    public static void rootToLeaf(Tree root, LinkedList<Symbol> acc, List<List<Symbol>> paths) {
        if (root instanceof Tree.Node node) {
            // if the node is a leaf, collect it
            if (node.getChildren().isEmpty()) {
                var path = new LinkedList<>(acc);
                path.push(new Symbol.Label(node.getLabel()));
                Collections.reverse(path);
                paths.add(path);
                return;
            }

            // collect paths down children depth-first
            for (int i = 0; i < node.getChildren().size(); i++) {
                acc.push(new Symbol.Label(node.getLabel()));
                acc.push(new Symbol.Index(i));
                rootToLeaf(node.getChildren().get(i), acc, paths);
                acc.pop();
                acc.pop();
            }

        } else if (root instanceof Tree.Wildcard) {
            // wildcards are leafs, collect up to them
            var path = new ArrayList<>(acc);
            Collections.reverse(path);
            paths.add(new ArrayList<>(path));
        }
    }

    public static List<List<Symbol>> rootToLeafPaths(Tree root) {
        var paths = new ArrayList<List<Symbol>>();
        rootToLeaf(root, new LinkedList<>(), paths);
        return paths;
    }

}

