Map reduce samples with input files. Below is the example to run it directly from local machine, you can create your own & follow below steps to run.

Once jar file is generated you can run the with below steps under project root folder

* Copy the input file to cluster  
```
hadoop fs -mkdir -p NYSE/input
hadoop fs -copyFromLocal data/NYSE_daily NYSE/input
```

* Run the map reduce program  
```
hadoop jar build\libs\mapreduce-1.0.jar com.exp.ihadoop.mapreduce.stock.Volume NYSE/input NYSE/output/1
```

* Check the content  
```
hadoop fs -cat NYSE/output/1/part-r-00000
```
