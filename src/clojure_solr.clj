(ns clojure-solr
  (:import (org.apache.solr.client.solrj.impl CommonsHttpSolrServer)
           (org.apache.solr.common SolrInputDocument)
           (org.apache.solr.client.solrj SolrQuery)))

(defn connect [url]
  (CommonsHttpSolrServer. url))

(defn- make-document [doc]
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

(defn- make-param [p]
  (cond
   (string? p) (into-array String [p])
   (coll? p) (into-array String (map str p))
   :else (into-array String [(str p)])))

(defn search [conn q & flags]
  (let [query (SolrQuery. q)]
    (doseq [[key value] (partition 2 flags)]
      (.setParam query (apply str (rest (str key))) (make-param value)))
    (map doc-to-hash (.getResults (.query conn query)))))