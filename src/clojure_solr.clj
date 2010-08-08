(ns clojure-solr
  (:import (org.apache.solr.client.solrj.impl CommonsHttpSolrServer)
           (org.apache.solr.common SolrInputDocument)
           (org.apache.solr.client.solrj SolrQuery)))

(defn connect [url]
  (CommonsHttpSolrServer. url))

(defn make-document [doc]
  (let [sdoc (SolrInputDocument.)]
    (doseq [[key value] doc]
      (let [key (cond
                 (keyword? key) (apply str (rest (str key)))
                 :default (str key))]
        (.addField sdoc key value)))
    sdoc))

(defn add-document! [conn doc]
  (.add conn (make-document doc)))

(defn add-documents! [conn coll]
  (.add conn (to-array (map make-document coll))))

(defn doc-to-hash [doc]
  (let [field-names (.getFieldNames doc)
        value-pairs (map #(list % (.getFieldValue doc %)) field-names)]
    (apply hash-map (flatten value-pairs))))

(defn search [conn q]
  (let [query (SolrQuery.)]
    (.setQuery query q)
    (map doc-to-hash (.getResults (.query conn query)))))