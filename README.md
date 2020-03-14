![Java CI with Maven](https://github.com/dice-group/GraphCompression/workflows/Java%20CI%20with%20Maven/badge.svg)

![Logo][logo]

[logo]: https://github.com/dice-group/GraphCompression/raw/develop/logo.png "RDFRePair Logo"

Compression of RDF graphs using the gRePair algorithm. 
Further on provides an even smaller compression using KD2 Trees. 

# Download and install

### General

Download standalone.zip from the latest release. 
```bash
wget https://github.com/dice-group/GraphCompression/releases/download/v1.0.1/standalone.zip
unzip standalone.zip
cd rdfrepair
```

### Ubuntu and Debian: 

Download deb package install using 
```
not yet ready
```

### Arch Linux: 

NOT READY YET!
```bash
yaourt -S rdfrepair
```

### Maven

Add the following to your pom 
```XML
<dependency>
  <groupId>org.dice-group</groupId>
  <artifactId>RDFRePair</artifactId>
  <version>1.0.1</version>
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
rdfrepair -c -tkd2 IN_FILE.ttl OUT_FILE.grp
```

Compression using gRePair algorithm
```bash
rdfrepair -c -tkd2 -digrams IN_FILE.ttl OUT_FILE.grp
```

Decompression (both KD2 as well as gRePair algorithm) using TURTLE format (N-TRIPLE, RDF/XML or TURTLE is possible)
```bash
rdfrepair -d -out TURTLE -tkd2 IN_FILE.grp OUT_FILE.ttl
```

To adjust the RAM usage to 4GB use the following:

```
export RDF_RE_PAIR_XMX=4g
```

# Querying KD2 with Fuseki

### Prepacked 

Download fuseki-kd2.tar.gz from the latest release

```bash
wget https://github.com/dice-group/GraphCompression/releases/download/v1.0.1/fuseki-kd2.tar.gz
tar -xzvf fuseki-kd2.tar.gz
cd apache-jena-fuseki-3.13.1/
cp YOUR_KD2_COMPRESSED_FILE file.grp
./fuseki-server --config=kd2_example.ttl
```


### From scratch

1. Download the latest [apache jena fuseki](https://jena.apache.org/download/#jena-fuseki)
2. Copy the [rdfrepair-1.0.jar](https://github.com/dice-group/GraphCompression/releases/download/v1.0.1/rdfrepair-1.0.jar) from the latest release to `run/extra/` in the fuseki folder. 
3. Download the [fuseki_example.ttl](https://github.com/dice-group/GraphCompression/blob/develop/fuseki_example.ttl)
4. change the last line fileName to the actual fileName of your KD2 compressed file. 
5. Start fuseki using `fuseki-server --config=fuseki_example.ttl` 

You can reach the read-only service at [http://localhost:3030/kd2/sparql](http://localhost:3030/kd2/sparql)

Depending on the size it may take a bit to load. 


### Using mutliple compressed files in one graph

If you want to use multiple kd2 compressed files in one graph download fuseki-kd2.tar.gz from the latest release and 
instead of copying one file to file.grp copy all your kd2 compressed files to `apache-jena-fuseki-3.13.1/kd2_files/`

```
wget https://github.com/dice-group/GraphCompression/releases/download/v1.0.1/fuseki-kd2.tar.gz
tar -xzvf fuseki-kd2.tar.gz
cd apache-jena-fuseki-3.13.1/
cp YOUR_KD2_COMPRESSED_FILES kd2_files/
./fuseki-server --config=kd2_multi_file.ttl
```

Alternatevly (un)comment the following in your config file:
```
        #rdf:type dice:KD2Graph ;
        #dice:fileName "file.grp" .
        rdf:type dice:KD2Graph ;  #If you want several compressed files to be loaded in one graph
        dice:folder "./kd2_files/" . #If you want several compressed files to be loaded in one graph
```

# Download precompressed files
Name | Size | Triples | Link
------------ | ------------- | -------------  | -------------
ArchivesHub | 13MB | 1.361.815 | [Download](https://hobbitdata.informatik.uni-leipzig.de/rdfrepair/ArchivesHub_04-03-2014.kd2)
Jamendo | 12MB | 1.047.950 | [Download](https://hobbitdata.informatik.uni-leipzig.de/rdfrepair/jamendo.kd2)
ScholaryData (rich) | 7.3MB | 859.840 | [Download](https://hobbitdata.informatik.uni-leipzig.de/rdfrepair/scholarydata.kd2)
DBLP 2017 | 773MB | 88.150.324 | [Download](https://hobbitdata.informatik.uni-leipzig.de/rdfrepair/dblp-01-24-2017.kd2)
