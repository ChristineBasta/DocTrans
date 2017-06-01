# DocTrans
### Pre-processing for Document-Level NMT

## Installation

```git clone https://github.com/cristinae/DocTrans.git```

```mvn clean dependency:copy-dependencies package```


## Run the coreference annotation

```
java -jar target/DocTrans-0.1-SNAPSHOT-jar-with-dependencies.jar -h

usage: Annotator

 -e,--extension <arg>   Extension of the input documents (if different from the language)
 -h,--help              This help
 -i,--input <arg>       Input folder to annotate -one file per raw document-
 -j,--json <arg>        Save annotations in json file 1/0 (default: 1)
 -l,--language <arg>    Language of the input text (en)
 -o,--txt <arg>         Save document with correferences tagged 1/0 (default: 1)
```

Example:

```java -jar target/DocTrans-0.1-SNAPSHOT-jar-with-dependencies.jar -l en -i /home/cristinae/pln/git/DocTrans/provaDocs```



## Run the topic annotation

### Learning the model

```
java -cp target/DocTrans-0.1-SNAPSHOT-jar-with-dependencies.jar cat.trachemys.topic.TopicLearnerMallet -h

usage: cat.trachemys.topic.TopicLearnerMallet 
 -e,--extension <arg>   Extension of the input documents (if different from the language)
 -h,--help              This help
 -i,--input <arg>       Input folder to annotate -one file per raw document-
 -l,--language <arg>    Language of the input text (en)
 -o,--output <arg>      File where to save model
```

### Assigning the topic of documents

```
java -cp target/DocTrans-0.1-SNAPSHOT-jar-with-dependencies.jar cat.trachemys.topic.TopicLabellerMallet -h

usage: cat.trachemys.topic.TopicLabellerMallet
 -e,--extension <arg>   Extension of the input documents (if different from the language)              
 -h,--help              This help
 -i,--input <arg>       Input folder to annotate -one file per raw document-
 -l,--language <arg>    Language of the input text (en)
 -m,--model <arg>       Previously trained model
 -o,--output <arg>      File where to save the statistics
```

### Annotate documents with topics for translation