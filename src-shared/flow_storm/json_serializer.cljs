(ns flow-storm.json-serializer
  (:require [cognitect.transit :as transit :refer [write-handler]]
            [flow-storm.utils :refer [log-error]]
            [flow-storm.types :as types]))

(defn serialize [o]
  (try
    (let [writer (transit/writer :json {:handlers {js/RegExp (write-handler
                                                              (fn [_ _] "regex")
                                                              str)
                                                   types/ValueRef (write-handler
                                                                   (fn [_] "flow_storm.types.ValueRef")
                                                                   (fn [vref] (str (:vid vref))))
                                                   :default (write-handler
                                                             (fn [_ _] "object")
                                                             pr-str)}})]
      (transit/write writer o))
    (catch js/Error e (log-error (str "Error serializing " o) e) (throw e))))

(defn deserialize [^String s]
  (try
    (let [reader (transit/reader :json {:handlers {"object" (fn [s] s)
                                                   "regex" (fn [s] (re-pattern s))
                                                   "flow_storm.types.ValueRef" (fn [vid] (types/make-value-ref (js/parseInt vid)))}})]
      (transit/read reader s))
    (catch js/Error e (log-error (str "Error deserializing " s " ERROR: " (.-message e))) (throw e))))
