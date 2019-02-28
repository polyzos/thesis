### Measure The Speed Of News Spread in Social Networks For Real-Time Fake News Detection
<br>

### Requirements:
-----------------
**1. Data Collection** (to be reimplemented in Kotlin)                   
**2. Data Wrangling & Preprocessing** - Spark and Kotlin      
**3. Graph Creation & Crud In Cypher**- Cypher and Kotlin  
**4. Graph Algorithms**               - Cypher and Spark GraphFrames and Kotlin 

### 1. Data Collection
----------------------
- **thesis-crawler** contains an akka based crawler, that generates our dataset.

More specifically, the program comprises of three actor: <br>
1. StreamListenerActor - monitors a list of sources that have been characterized as sources that generate fake news
More information can be found on this link: https://arxiv.org/pdf/1707.07592.pdf
2. RetweetHandlerActor - when the StreamListenerActor receives retweets, RetweetHandlerActor fetches the 100 most recent posts for that tweet.
3. SampleStreamListenerActor - Monitors the twitter live stream and fetcher all the incoming data.

Running the crawler for almost a week generates about 25GB of data, from the live stream as well as from the sources that are marked as Fake News Generators
### 2. Preprocessing (ETL-Pipeline)
----------------------------------
* TODO: Description about the preprocessing


### 3. Graph Builder
--------------------
* TODO: Description about the graph builder


### 4. Graph Algorithms
-----------------------   
* TODO: Description about the graph algorithms

### TODO:
---------
1. Add logging instead to all the projects
2. Extract variables to configuration and load the from there
3. Add graph algorithms
4. Add Testing
5. Containarize the project and add bash scripts
6. Add CI/CD