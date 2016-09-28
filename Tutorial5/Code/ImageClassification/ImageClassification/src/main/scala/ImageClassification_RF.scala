import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}

import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.{DecisionTree, RandomForest}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Naga on 19-09-2016.
  */
object ImageClassification_RF {
  def main(args: Array[String]) {
    //val IMAGE_CATEGORIES = Array("Yeast", "Bird", "SeaLion", "Swan", "Beaver")
    System.setProperty("hadoop.home.dir", "C:\\winutils")
    //    Logger.getLogger("org").setLevel(Level.ERROR)
    //    Logger.getLogger("akka").setLevel(Level.ERROR)
    val sparkConf = new SparkConf().setAppName("ImageClassification").setMaster("local[*]")
    val sc = new SparkContext(sparkConf)
    val train = sc.textFile("C:/Users/DR042460/Documents/UMKC/Fall2016/CS5543/ImageClassification/ImageClassification/data/train")
    val test = sc.textFile("C:/Users/DR042460/Documents/UMKC/Fall2016/CS5543/ImageClassification/ImageClassification/data/test")
    val parsedData = train.map { line =>
      val parts = line.split(',')
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
    }
    val testData1 = test.map(line => {
      val parts = line.split(',')
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
    })


    val trainingData = parsedData


    val numClasses = 4
    val categoricalFeaturesInfo = Map[Int, Int]()
    val impurity = "gini"
    val maxDepth = 7
    val maxBins = 32
    val featureSubsetStrategy = "auto"
    val numTrees = 5

    val model = RandomForest.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)

    val classify1 = testData1.map { line =>
      val prediction = model.predict(line.features)
      (line.label, prediction)
    }

    val prediction1 = classify1.groupBy(_._1).map(f => {
      var fuzzy_Pred = Array(0, 0, 0, 0, 0)
      f._2.foreach(ff => {
        fuzzy_Pred(ff._2.toInt) += 1
      })
      var count = 0.0
      fuzzy_Pred.foreach(f => {
        count += f
      })
      var i = -1
      var maxIndex = 5
      val max = fuzzy_Pred.max
      val pp = fuzzy_Pred.map(f => {
        val p = f * 100 / count
        i = i + 1
        if(f == max)
          maxIndex=i
        (i, p)
      })
      (f._1, pp, maxIndex)
    })
    prediction1.foreach(f => {
      println("\n\n\n" + f._1 + " : " + f._2.mkString(";\n"))
    })
    val y = prediction1.map(f => {
      (f._1, f._3.toDouble)
    })

    y.collect().foreach(println(_))

    val metrics = new MulticlassMetrics(y)

    val file = "C:/Users/DR042460/Documents/UMKC/Fall2016/CS5543/RF_output.txt"
    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))
    writer.write("CONFUSION MATRIX: \n"+metrics.confusionMatrix+"\n");
    writer.write("F-MEASURE: "+metrics.weightedFMeasure+"\n");
    writer.write("RECALL: " + metrics.weightedRecall + "\n");
    writer.write("PRECISION: " + metrics.weightedPrecision + "\n");
    writer.close()

    println("Confusion Matrix:")
    println(metrics.confusionMatrix)
    println("Confusion Matrix:")
  }
}