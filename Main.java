import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) {
        var reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        while (true) {
            try {
                var pattern = reader.readLine();
                var subject = reader.readLine();
                System.out.print("> ");
                algorithmD(pattern, subject);
            } catch (Exception ignored) {
                break;
            }
        }

    }

    public static void algorithmD(String patternTree, String subjectTree) {
        // parse pattern and subject trees into trees for matching
        var pattern = Parser.parse(patternTree);
        var subject = Parser.parse(subjectTree);

        // pattern must be rooted at a labelled node for initial transition
        assert pattern instanceof Tree.Node;
        assert subject instanceof Tree.Node;

        // construct Aho-Corasick automaton from pattern tree
        var paths = Tree.rootToLeafPaths(pattern);
        var builder = new Trie.TrieBuilder<Symbol>();
        paths.forEach(builder::add);

        // construct Aho-Corasick automaton
        var trie = builder.build();

        // algorithm D stack entry, for pre-order book-keeping
        class Entry {
            public final Tree.Node node;
            public final Trie.Node<Symbol> state;
            public int visited;

            public Entry(Tree.Node node, Trie.Node<Symbol> state, int visited) {
                this.node = node;
                this.state = state;
                this.visited = visited;
            }
        }

        var stack = new Stack<Entry>();

        // tabulate update counters and registers matches
        Function<Trie.Node<Symbol>, Void> tabulate = (state) -> {
            for (List<Symbol> output : state.getOutputs()) {
                // inefficient, should be precomputed and stored as match length
                var match = output.stream().filter(p -> p instanceof Symbol.Label).toList();
                var entry = stack.get(stack.size() - match.size());
                var node = entry.node;
                node.match = (++node.count == paths.size());
            }

            return null;
        };

        // populate stack with initial transition
        var subjectRoot = (Tree.Node) subject;
        var next = trie.getRoot().gotoOn(new Symbol.Label(subjectRoot.getLabel()));
        stack.push(new Entry(subjectRoot, next, -1));
        tabulate.apply(next);

        // process all subtrees
        while (!stack.empty()) {
            var top = stack.peek();
            var thisNode = top.node;
            var thisState = top.state;
            var visited = top.visited;

            // visited all children
            if (visited >= thisNode.getChildren().size() - 1) {
                stack.pop();
                continue;
            }

            // increase visitation index, initially -1 for all entries
            top.visited = ++visited;

            // follow child subtree's index symbol
            var intState = thisState.gotoOn(new Symbol.Index(visited));
            tabulate.apply(intState);

            // follow child subtree, pushing it to the stack
            var nextNode = thisNode.getChildren().get(visited);
            var nextState = intState.gotoOn(new Symbol.Label(((Tree.Node) nextNode).getLabel()));
            stack.push(new Entry((Tree.Node) nextNode, nextState, -1));
            tabulate.apply(nextState);
        }

        // print nested matches by cycling through colours
        final var colours = List.of("\u001B[31m", "\u001B[33m", "\u001B[32m", "\u001B[33m", "\u001B[36m", "\u001B[35m");
        int depth = -1;
        for (char c : subject.toString().toCharArray()) {
            switch (c) {
                case '[' -> depth++;
                case ']' -> depth--;
                default -> System.out.printf("%s%c", (depth >= 0) ? colours.get(depth % colours.size()) : "\033[0m", c);
            }
        }
        System.out.println("\u001B[0m");
    }
}
