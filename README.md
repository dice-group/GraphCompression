Compression of RDF graphs using k2 trees

# Download and install

### General

Download standalone.zip from the latest release. 
```bash
wget https://github.com/dice-group/GraphCompression/releases/download/v1.0.0-k2/standalone.zip
unzip standalone.zip
cd rdf2k2
```

### Maven

Add the following to your pom 
```XML
<dependency>
  <groupId>org.dice-group</groupId>
  <artifactId>rdf2k2</artifactId>
  <version>1.0.0-k2</version>
</dependency>
```

and 
```XML
<repository>
  <id>github_dice-group_GraphCompression</id>
  <name>GitHub dice-group Apache Maven Packages</name>
  <url>https://maven.pkg.github.com/dice-group/GraphCompression</url>
</repository>
```

# Usage

Compression using KD2 Trees
```bash
./rdf2k2 -c -tkd2 IN_FILE.ttl OUT_FILE.k2
```

Decompression using TURTLE format (N-TRIPLE, RDF/XML or TURTLE is possible)
```bash
./rdf2k2 -d -out TURTLE -tkd2 IN_FILE.k2 OUT_FILE.ttl
```

To adjust the RAM usage to 4GB use the following:

```
export RDF_2_K2_JVM=-Xmx4g
```

# Precompressed Datasets

You can get precompressed datasets at https://hobbitdata.informatik.uni-leipzig.de/rdf2k2/

* [ArchivesHub](https://hobbitdata.informatik.uni-leipzig.de/rdf2k2/archiveshub/)
* [Jamendo](https://hobbitdata.informatik.uni-leipzig.de/rdf2k2/jamendo/)
* [scholarly data](https://hobbitdata.informatik.uni-leipzig.de/rdf2k2/scholarlydata/)
* [DBLP 2017](https://hobbitdata.informatik.uni-leipzig.de/rdf2k2/dblp-2017/)
* [DBpedia en 03.2021 in sections](https://hobbitdata.informatik.uni-leipzig.de/rdf2k2/dbpedia-03_2021/sections/)
* [DBpedia en 03.2021 single file](https://hobbitdata.informatik.uni-leipzig.de/rdf2k2/dbpedia-03_2021/)
* [DBpedia all other available languages without nif datasets 10.2016](https://hobbitdata.informatik.uni-leipzig.de/rdf2k2/dbpedia-10_2016/)
* [DBpedia all other available languages with nif datasets 10.2016](https://hobbitdata.informatik.uni-leipzig.de/rdf2k2/dbpedia-10_2016-nif/)


# Querying KD2 with Fuseki

### Prepacked 

Download fuseki-k2.tar.gz from the latest release

```bash
wget https://github.com/dice-group/GraphCompression/releases/download/v1.0.0-k2/fuseki-k2.tar.gz
tar -xzvf fuseki-k2.tar.gz
cd apache-jena-fuseki-3.13.1/
cp YOUR_KD2_COMPRESSED_FILE file.k2
./fuseki-server --config=k2_example.ttl
```


### From scratch

1. Download the latest [apache jena fuseki](https://jena.apache.org/download/#jena-fuseki)
2. Copy the [rdf2k2-1.0.0-k2.jar](https://github.com/dice-group/GraphCompression/releases/download/v1.0.0-k2/rdf2k2-1.0.0-k2.jar) from the latest release to `run/extra/` in the fuseki folder. 
3. Download the [fuseki_example.ttl](https://github.com/dice-group/GraphCompression/blob/kd2/fuseki_example.ttl)
4. change the last line fileName to the actual fileName of your K2 compressed file. 
5. Start fuseki using `fuseki-server --config=fuseki_example.ttl` 

You can reach the read-only service at [http://localhost:3030/kd2/sparql](http://localhost:3030/kd2/sparql)

Depending on the size it may take a bit to load. 


### Using mutliple compressed files in one graph

If you want to use multiple kd2 compressed files in one graph download fuseki-k2.tar.gz from the latest release and 
instead of copying one file to file.k2 copy all your k2 compressed files to `apache-jena-fuseki-3.13.1/k2_files/`

```
wget https://github.com/dice-group/GraphCompression/releases/download/v1.0.0-k2/fuseki-k2.tar.gz
tar -xzvf fuseki-k2.tar.gz
cd apache-jena-fuseki-3.13.1/
cp YOUR_KD2_COMPRESSED_FILES k2_files/
./fuseki-server --config=k2_multi_file.ttl
```

Alternatevly (un)comment the following in your config file:
```
        #rdf:type dice:KD2Graph ;
        #dice:fileName "file.grp" .
        rdf:type dice:KD2Graph ;  #If you want several compressed files to be loaded in one graph
        dice:folder "./k2_files/" . #If you want several compressed files to be loaded in one graph
```
