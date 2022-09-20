# Algorithm D
An implementation of Algorithm D as described in [Pattern Matching in Trees](https://www.cs.purdue.edu/homes/cmh/distribution/papers/PatternMatching/PatternMatchingInTrees.pdf) by 
Hoffmann and O'Donnell.

The algorithm works by constructing an Aho-Corasick automaton for a set of root-to-leaf "path strings"
from the pattern tree(s). Then, the subject tree is traversed in pre-order using a traversal stack.
At each output, the match length can be used to index the traversal stack to find the relevant subtree.
If, by the end, the number of path string matches equals the number of path strings, then there's a match.

# Usage
```sh
javac *.java && java Main
```
