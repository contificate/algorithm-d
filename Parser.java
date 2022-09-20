import java.util.ArrayList;
import java.util.List;

/**
 * Parser for tree expressions (ad-hoc and hacky).
 */
public class Parser {
    public static Tree parse(String input) {
        // since we have no separate lexer, strip whitespace for simplicity
        input = input.replaceAll("\\s", "");
        return parseExpression(input, 0).first();
    }

    /**
     * E -> x | _ | x ( L ).
     */
    public static Pair<Tree, Integer> parseExpression(String input, int i) {
        // exhausted input, return null
        if (i >= input.length() - 1)
            return new Pair<>(null, i);

        for (; i < input.length(); i++) {
            final char current = input.charAt(i);

            // are we parsing an identifier?
            if (Character.isAlphabetic(current)) {
                var builder = new StringBuilder();
                builder.append(current);

                // consume rest of identifier
                int j = i + 1;
                for (; j < input.length() && Character.isAlphabetic(input.charAt(j)); j++)
                    builder.append(input.charAt(j));
                i = j;

                // lookahead to see if we can expect to parse a non-empty list of expressions
                if (input.charAt(i) == '(') {
                    i++;

                    // parse a non-empty list of subtree expressions
                    final var list = parseList(input, i);
                    i = list.second();
                    if (input.charAt(i) == ')')
                        return new Pair<>(new Tree.Node(builder.toString(), list.first()), i + 1);
                }

                // no subtrees, return leaf node
                return new Pair<>(new Tree.Node(builder.toString(), new ArrayList<>()), i);
            } else if (current == '_') {
                return new Pair<>(new Tree.Wildcard(), i + 1);
            }
        }

        // shouldn't reach here
        return new Pair<>(null, i);
    }

    /**
     * L -> E , L | E.
     */
    public static Pair<List<Tree>, Integer> parseList(String input, int i) {
        var list = new ArrayList<Tree>();

        // parse first expression
        var first = parseExpression(input, i);
        list.add(first.first());

        // continue from here
        i = first.second();

        // keep parsing rest
        while (i < input.length() && input.charAt(i) == ',') {
            var next = parseExpression(input, i);
            list.add(next.first());
            i = next.second();
        }

        return new Pair<>(list, i);
    }
}
