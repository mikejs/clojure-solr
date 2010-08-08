============
clojure-solr
============

Clojure bindings for `Apache Solr <http://lucene.apache.org/solr/>`_.

Installation
============

To use within a Leiningen project, add the following to your
project.clj file:

::

    [clojure-solr "0.1.0-SNAPSHOT"]

To build from source, run:

::

    lein deps
    lein jar

Usage
=====

::

    (def conn (connect "http://127.0.0.1:8983/solr"))    
    (add-document! conn {"id" "testdoc", "name" "A Test Document"})
    (add-documents! conn [{"id" "testdoc.2", "name" "Another test"}
                          {"id" "testdoc.3", "name" "a final test"}])
    (commit! conn)
    (search conn "test")
    (search conn "test" :rows 2)
