(ns clojure-solr
  (:import (org.apache.solr.client.solrj.impl CommonsHttpSolrServer)
           (org.apache.solr.common SolrInputDocument)
           (org.apache.solr.client.solrj SolrQuery)
           (org.apache.solr.common.params ModifiableSolrParams)))

(declare *connection*)

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

(defn add-document! [doc]
  (.add *connection* (make-document doc)))

(defn add-documents! [coll]
  (.add *connection* (to-array (map make-document coll))))

(defn commit! []
  (.commit *connection*))

(defn- doc-to-hash [doc]
  (let [field-names (.getFieldNames doc)
        value-pairs (map #(list % (.getFieldValue doc %)) field-names)]
    (apply hash-map (flatten value-pairs))))

(defn- make-param [p]
  (cond
   (string? p) (into-array String [p])
   (coll? p) (into-array String (map str p))
   :else (into-array String [(str p)])))

(defn search [q & flags]
  (let [query (SolrQuery. q)]
    (doseq [[key value] (partition 2 flags)]
      (.setParam query (apply str (rest (str key))) (make-param value)))
    (map doc-to-hash (.getResults (.query *connection* query)))))

(defn delete-id! [id]
  (.deleteById *connection* id))

(defn delete-query! [q]
  (.deleteByQuery *connection* q))

(defn data-import [type]
  (let [type (cond (= type :full) "full-import"
                   (= type :delta) "delta-import")
        params (doto (ModifiableSolrParams.)
                 (.set "qt" (make-param "/dataimport"))
                 (.set "command" (make-param type)))]
    (.query *connection* params)))

(defmacro with-connection [conn & body]
  `(binding [*connection* ~conn]
     ~@body))
