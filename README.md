![Logo][logo]

[logo]: https://github.com/dice-group/GraphCompression/raw/develop/logo.png "RDFRePair Logo"

Compression of RDF graphs using the gRePair algorithm. 
Further on provides an even smaller compression using KD2 Trees. 

# Download and install

### General

Download standalone.zip from the latest release. 
```bash
wget https://.../standalone.zip
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
wget http://.../fuseki-kd2.tar.gz
tar -xzvf fuseki-kd2.tar.gz
cd apache-jena-fuseki-3.13.1/
cp YOUR_KD2_COMPRESSED_FILE file.grp
./fuseki-server --config=kd2_example.ttl
```


### From scratch

1. Download the latest [apache jena fuseki](https://jena.apache.org/download/#jena-fuseki)
2. Copy the rdfrepair-1.0.jar from the latest release to `run/extra/` in the fuseki folder. 
3. Download the [fuseki_example.ttl](https://github.com/dice-group/GraphCompression/blob/develop/fuseki_example.ttl)
4. change the last line fileName to the actual fileName of your KD2 compressed file. 
5. Start fuseki using `fuseki-server --config=fuseki_example.ttl` 

You can reach the read-only service at [http://localhost:3030/kd2/sparql](http://localhost:3030/kd2/sparql)

Depending on the size it may take a bit to load. 


# Download precompressed files
Name | Size | Triples | Link
------------ | ------------- | -------------  | -------------
ArchivesHub | | 1.361.815 | 
Jamendo | | 1.047.950 | 
ScholaryData (rich) | | 859.840 |
DBLP 2017 | | 88.150.324 | 
