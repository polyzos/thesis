|PathFinding & Graph Search Algorithms|
| --- |

| Algorithm Type | What It Does| Example Uses | Spark | Neo4J |
| --- | --- | --- | --- | --- | 
| `BFS` | Traverses a tree structure by fanning out to explore the nearest neighbors and then their sub-level neighbors.| Locate neighbor nodes in GPS systems to identify nearby places of interest. | ✓ | ✘ |
| `DFS` | Traverses a tree structure by exploring as far as possible down each branch before backtracking.| Discover an optimal solution path in gaming simulations with hierarchical choices.| ✗| ✗|
| `Shortest Path - Variations: A*, Yen's` |Calculates the shortest path between a pair of nodes. | Find driving directions between two locations.| ✓| ✓|
| `All Pairs Shortest Paths` |Calculates the shortest path between all pairs of nodes in the graph. |Evaluate alternate routes around a traffic jam. | ✓| ✓|
| `Single Source Shortest Path` |Calculates the shorest path between a single root node and all other nodes. | Least cost routing of phone calls. | ✓| ✓|
| `Minimum (Weight) Spanning Tree` |Calculates the path in a connected tree structure with the smallest cost for visiting all nodes. |Optimize connected routing such as laying cable or garbage collection. | ✗| ✓|
| `Random Walk` | Returns a list of nodes along a path of specified size by randomly choosing relationships to traverse.|Augment training for machine learning or data for graph algorithms | ✗| ✓|
