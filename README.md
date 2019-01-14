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

Example with Java 8:

```java -jar target/DocTrans-0.1-SNAPSHOT-jar-with-dependencies.jar -l en -i /home/cristinae/pln/git/DocTrans/provaDocs```

In case you use Java 9 with Stanford, add --add-modules java.xml.bind:

```java --add-modules java.xml.bind -jar DocTrans-0.1-SNAPSHOT-jar-with-dependencies.jar -l en -i /home/cristinae/pln/git/DocTrans/provaDocs```


## Run the topic annotation

### Learning the model

Monolingual

```
java -cp target/DocTrans-0.1-SNAPSHOT-jar-with-dependencies.jar cat.trachemys.topic.TopicLearnerMallet -h

usage: cat.trachemys.topic.TopicLearnerMallet 
 -e,--extension <arg>   Extension of the input documents (if different from the language)
 -h,--help              This help
 -i,--input <arg>       Input folder to annotate -one file per raw document-
 -l,--language <arg>    Language of the input text (en)
 -o,--output <arg>      File where to save model
```

Multiple languages

```
java -cp target/DocTrans-0.1-SNAPSHOT-jar-with-dependencies.jar cat.trachemys.topic.PolyTopicLearnerMallet -h
usage: Annotator
 -h,--help              This help
 -i,--input <arg>       Input folder to annotate -one file per raw
                        document and language-
 -l,--languages <arg>   Languages of the input texts separated by commas
                        (en,de,it,nl,ro)
 -o,--output <arg>      File where to save model

Ex: java -cp target/DocTrans-0.1-SNAPSHOT-jar-with-dependencies.jar cat.trachemys.topic.PolyTopicLearnerMallet -l en,de,it,nl,ro -i /media/cristinae/DATA1/pln/experiments/IWSLT/TED/union -o ted.union.topic100
```

### Assigning the topic of documents

```
java -cp target/DocTrans-0.1-SNAPSHOT-jar-with-dependencies.jar cat.trachemys.topic.TopicLabellerMallet -h

usage: cat.trachemys.topic.TopicLabellerMallet
 -e,--extension <arg>   Extension of the input documents (if different from the language)              
 -f,--inferencer <arg>  Previously trained inferencer
 -h,--help              This help
 -i,--input <arg>       Input folder to annotate -one file per raw document-
 -l,--language <arg>    Language of the input text
 -m,--model <arg>       Previously trained model
 -o,--output <arg>      File where to save the statistics
```
 
### Annotate documents with topics for translation

```
java -cp target/DocTrans-0.1-SNAPSHOT-jar-with-dependencies.jar cat.trachemys.topic.Annotator -h

usage: cat.trachemys.topic.Annotator
 -h,--help          This help
 -i,--input <arg>   Input file with the topic information for each document
 ```                   

