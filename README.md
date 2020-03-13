<img src="https://github.com/dice-group/GraphCompression/blob/develop/logo.png" width="250" alt="Logo" align="middle">

Compression of RDF graphs using the gRePair algorithm. 
Further on provides an even smaller compression using KD2 Trees. 

# Download and install

## General
Download standalone.zip from the latest release. 
```bash
wget https://.../standalone.zip
unzip standalone.zip
cd rdfrepair
```

## Ubuntu and Debian: 
Download deb package install using 
```
not yet ready
```

## Arch Linux: 
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

# Querying KD2 with Fuseki

1. Download the latest apache jena fuseki from https://jena.apache.org/download/#jena-fuseki
2. Copy the rdfrepair-1.0.jar from the latest release to `run/extra/` in the fuseki folder. 
3. Download the fuseki_example.ttl from https://github.com/dice-group/GraphCompression/blob/develop/fuseki_example.ttl
4. change the last line fileName to the actual fileName of your KD2 compressed file. 
5. Start fuseki using `fuseki-server --config=fuseki_example.ttl` 

You can reach the read-only service at http://localhost:3030/kd2/sparql 

Depending on the size it may take a bit to load. 


# Download precompressed files
Name | Size | Triples | Link
------------ | ------------- | -------------  | -------------
ArchivesHub | | 1361815 | 
Jamendo | | 1047950 | 
ScholaryData (rich) | | 859840 |
DBLP 2017 | | 88150324 | 
