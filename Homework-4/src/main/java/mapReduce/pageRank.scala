package mapReduce

import org.apache.spark.{SparkConf, SparkContext}


/**
  * Created by sravani on 3/13/17.
  */

object pageRank {

  def main(args : Array[String]){

    try {

      val conf = new SparkConf()
                    .setAppName("pageRank")
                    //For local run
                    .setMaster("local")
                    //To run on EMR set the master to yarn
                    // .setMaster("yarn")

      val sc = new SparkContext(conf)

      //Parsing initial list of unique nodes from the wikiParser file
      var nodesList = sc.textFile(args(0))//reading input file
                        .map(line => Bz2WikiParser.parse(line))//sending file to wikiParser for formatting
                        .filter(line => !line.contains("Invalid"))//removing invalid node(containing ~) from list
                        .map(line => line.split("SB"))//Splitting file based on delimited to differentiate pageName and its linkpagess
                        .map(line => if(line.size == 1)
                        {
                          (line(0),List())//if outlinks from node zero,assigning an empty list to it
                        }
                        else{
                          (line(0),line(1).split("~").toList) //if contains outlinks splitting based on ~ and saving as list
                        })


      //Ensuring faster access
      nodesList.persist()

     //extracting dangling nodes from the extracted nodes
      var nodesWithDangling = nodesList.values //list of outlinks for each node
                                        .flatMap{node => node} //combining list of outlinks
                                        .keyBy(node => node) //creating key for each outlinks in list
                                        .map(line => (line._1 ,List[String]())) //creating key-value pair where key is outlinkName and values is empty list


      //computing new list of nodes consisting all nodes and dangling nodes
      val finalNodes = nodesList.union(nodesWithDangling)
                                .reduceByKey((value1,value2) => value1.++(value2))


      val totalPages = finalNodes.count() //total count of unique outLinks


      val iterationCount = 10

      val initial_pageRank : Double = 1.0 / totalPages

      val alpha : Double  = 0.15 //random probability jump

      //key-value pair with node and pagerank
      var nodeListWithPageRank = finalNodes.keys
                                           .map(line => (line,initial_pageRank))



      //iterating 10 times to calculate page rank
      for(i <- 1 to iterationCount){

        try {

          //accumulaor to store dangling value
          var danglingFactor =  sc.accumulator(0.0)

          //key-value pair for calculating pagerank
          var pageRankData = finalNodes.join(nodeListWithPageRank).values

                                        .flatMap{

                                          case(outLinks, pageRank) => {

                                            val size = outLinks.size

                                            if(size == 0){

                                              danglingFactor +=  pageRank
                                              List()

                                            }else{

                                              outLinks.map(url =>  (url,pageRank/size))

                                            }
                                          }
                                        }
                                        .reduceByKey(_+_)

          //if any action is not performed on RDD,dangling statistics will be zero
          pageRankData.first


          val danglingValue : Double = danglingFactor.value


          //to get the nodes which doesn't have any inlinks
          var missingData  = nodeListWithPageRank.subtractByKey(pageRankData)


          //initializing pageRank to 0.0 for above extracted nodes and merging them key-value pair calculated
          var allNodes = missingData.map(page  => (page._1,0.0))
                                    .union(pageRankData)


          //computing pagerank value
          nodeListWithPageRank = allNodes.mapValues[Double](accumulatedPageRank => (alpha * initial_pageRank + (1 - alpha) * (danglingValue / totalPages + accumulatedPageRank)))

        }catch{

          case e : Exception => println("Exception: " +e)
        }

      }


      //to get top100 
      var temp = nodeListWithPageRank.takeOrdered(100)(Ordering[Double].reverse.on{line => line._2})


      //storing the output
      sc.parallelize(temp)
        .saveAsTextFile(args(1))

    } catch{

      case e : Exception => println("Exception: " +e)
    }

  }

}
