#### 1. PathFinding & Graph Search Algorithms: 

| Algorithm Type | What It Does | Example Uses | Spark | Neo4J |
| --- | --- | --- | --- | --- | 
| `BFS` | Traverses a tree structure by fanning out to explore the nearest neighbors and then their sub-level neighbors.| Locate neighbor nodes in GPS systems to identify nearby places of interest. | ✓ | ✗ |
| `DFS` | Traverses a tree structure by exploring as far as possible down each branch before backtracking.| Discover an optimal solution path in gaming simulations with hierarchical choices.| ✗| ✗|
| `Shortest Path - Variations: A*, Yen's` |Calculates the shortest path between a pair of nodes. | Find driving directions between two locations.| ✓| ✓|
| `All Pairs Shortest Paths` |Calculates the shortest path between all pairs of nodes in the graph. |Evaluate alternate routes around a traffic jam. | ✓| ✓|
| `Single Source Shortest Path` |Calculates the shorest path between a single root node and all other nodes. | Least cost routing of phone calls. | ✓| ✓|
| `Minimum (Weight) Spanning Tree` |Calculates the path in a connected tree structure with the smallest cost for visiting all nodes. |Optimize connected routing such as laying cable or garbage collection. | ✗ | ✓|
| `Random Walk` | Returns a list of nodes along a path of specified size by randomly choosing relationships to traverse.|Augment training for machine learning or data for graph algorithms | ✗ | ✓|


#### 2. Centrality Algorithms:

| Algorithm Type | What It Does | Example Uses | Spark | Neo4J |
| --- | --- | --- | --- | --- | 
| `Degree Centrality`| Measures the number of relationships a node has. | Estimate a person’s popularity by looking at their in-degree and use their out-degree for gregariousness| ✓ | ✗ | 
| `Closeness Centrality Variations: Wasserman and Faust, Harmonic Centrality`| Calculates which nodes have the shortest paths to all other nodes. | Find the optimal location of new public services for maximum accessibility. | ✓| ✓| 
| `Betweenness Centrality Variation: Randomized- Approximate Brandes`| Measures the number of shortest paths that pass through a node. | Improve drug targeting by finding the control genes for specific diseases. | ✗ | ✓ | 
| `PageRank - Variation: Personalized PageRank`| Estimates a current node’s importance from its linked neighbors and their neighbors. Popularized by Google | Find the most influential features for extraction in machine learning and rank text for entity relevance in natural language processing. | ✓ | ✓ | 

#### 3. Community Detection Algorithms:

| Algorithm Type | What It Does | Example Uses | Spark | Neo4J |
| --- | --- | --- | --- | --- | 
| `Triangle Count and Clustering Coefficient `| Measures how many nodes form triangles and the degree to which nodes tend to cluster together. | Estimate group stability and whether the network might exhibit “small- world” behaviors seen in graphs with tightly knit clusters. | ✓ | ✓ | 
| `Strongly Connected Components` | Finds groups where each node is reachable from every other node in that same group following the direction of relationships.| Make product recommendations based on group affiliation or similar items.| ✓ | ✓ | 
| `Connected Components` | Finds groups where each node is reachable from every other node in that same group, regardless of the direction of relationships | Perform fast grouping for other algorithms and identify islands.| ✓ | ✓ | 
| `Label Propagation` | Infers clusters by spreading labels based on neighborhood majorities.| Understand consensus in social Label communities or find dangerous combinations of possible co- prescribed drugs.| ✓ | ✓ | 
| `Louvain Modularity` | Maximizes the presumed accuracy of groupings by comparing relationship weights and densities to a defined estimate or average. | In fraud analysis, evaluate whether a group has just a few discrete bad behaviors or is acting as a fraud ring. | ✗ | ✓ | 
