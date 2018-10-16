(ns asgnx.kvstore
  "Provides a protocol for working with key/value stores.

  Different key/value (kv) store implementations can be hidden behind this
  protocol. The default implementation below uses a Clojure atom to provide
  management of a kv store. The asgnx.aws.s3 namespace provides an implementation
  backed by S3.

  DO NOT use these for real applications unless you completely understand the
  ramifications of not having transactions.
  "
  (:require [clojure.core.async :as async :refer [go]]))

(defprotocol KeyStore
  (put!     [s key value])
  (remove!  [s key])
  (get!     [s key] [s key not-found])
  (list!    [s key-prefix]))


;; Asgn 2
;;
;; @Todo:
;;
;; Implement a state put function that inserts the provided value `v`
;; into the map `m` at the key path `ks`.
;;
;; Examples:
;;
;; (state-put {} [:a :b :c] 1) => {:a {:b {:c 1}}})
;; (state-put {:a {:b 2}} [:a :b] 3) => {:a {:b 3})
;;
;; See the tests in asgnx.kvstore-test for a complete spec.
;;
(defn state-put [m ks v]
  (cond
    ; Special case for nil
    (nil? ks) {nil nil}
    ; Return mapping of value to key if key path has one key
    (= 1 (count ks)) (assoc m (first ks) v)
    ; Return empty map if key path is empty
    (empty? ks) {}
    ; Otherwise, put the key into m and associate with what is
    ; returned when state-put is called on the rest of the keypath
    :else (assoc m (first ks) (state-put (get m (first ks)) (rest ks) v))))


;; Helper function to test if a given key path is in a map
;; Arguments:
;;    m: map to check for key
;;    ks: key path to check for
(defn check-path [m ks]
  (cond
    ; True if path is empty
    (empty? ks) true
    ; If first key in path is in map, try rest of keypath
    (contains? m (first ks)) (check-path ((first ks) m) (rest ks))
    ; False if first key in path is not in map
    :else false))



;; Asgn 2
;;
;; @Todo:
;;
;; Implement a state remove function that removes the value at
;; keypath `ks`
;;
;; Examples:
;;
;; (state-remove {:a {:b {:c 1}}} [:a :b :c]) => {:a {:b {}}})
;; (state-remove {:a {:b 2}} [:a]) => {})
;; (state-remove {:a {:b 2 :d 3}} [:a :d]) => {:a {:b 2}})
;;
;; See the tests in asgnx.kvstore-test for a complete spec.
;;
(defn state-remove [m ks]
  (cond
    ; nil if the map is already nil
    (nil? m) nil
    ; If map is empty, return empty map
    (empty? m) {}
    ; If keypath is not in map, return the map
    (false? (check-path m ks)) m
    ; If there is only 1 key left, remove it from map and return the
    ; rest of the map
    (= 1 (count ks)) (dissoc m (first ks))
    ; If the keypath is in the map and there are multiple keys left in
    ; the key path, let k be the value for the first key in the key path
    ; and v be the value returned by calling state-remove on the rest
    ; of the key path. If the value returned is empty, simply dissociate
    ; k from the current map. If the value returned is not empty, dissociate k
    ; from the current map, and then reassociate k with the returned value.
    :else (let [v (state-remove ((first ks) m) (rest ks))
                k (first ks)]
            (if (empty? v)
              (dissoc m k)
              (assoc (dissoc m k) k v)))))




;; Asgn 2
;;
;; @Todo:
;;
;; Implement a multi-arity state get function that retrieves the value at
;; keypath `ks`
;;
;; One arity should take a default value that is returned if there is no value
;; at the specified key path.
;;
;; Examples:
;;
;; (state-get {:a {:b {:c 1}}} [:a :b :c]) => 1
;; (state-get {:a {:b 2}} [:a]) => {:b 2})
;; (state-get {:a {:b 2 :d 3}} [:a :d]) => 3
;; (state-get {:a {:b 2 :d 3}} [:a :f] 100) => 100
;;
;; See the tests in asgnx.kvstore-test for a complete spec.
;;
(defn state-get [m ks]
  (cond
    ; Return nil in empty case
    (empty? m) nil
    ; Return value at key if key path contains one key
    (= 1 (count ks)) (get m (first ks))
    ; If multiple keys left in key path, canll state-get on the value
    ; in map m for the first key and specify the new key path as the
    ; rest of the keys
    :else (state-get (get m (first ks)) (rest ks))))


;; Asgn 2
;;
;; @Todo:
;;
;; Implement a state keys function that retrieves the keys beneath
;; keypath `ks`
;;
;; Examples:
;;
;; (state-keys {:a {:b {:c 1}}} [:a :b]) => [:c]
;; (state-keys {:a {:b 2}} [:a]) => [:b])
;; (state-keys {:a {:b 2 :d 3}} [:a]) => [:b :d]
;;
;; See the tests in asgnx.kvstore-test for a complete spec.
;;
(defn state-keys [m ks]
  (cond
    ; Return nil if the map is empty
    (empty? m) nil
    ; Return all keys in map as vector if key path is empty
    (empty? ks) (vec (keys m))
    ; If keys are left in key path, call statekeys with map as what is
    ; at the first key and the key path as the rest of the keypath
    :else (state-keys (get m (first ks)) (rest ks))))

;; An in-memory store that mimics the side-effect based stores
;;  that will typically be used (e.g., db, file system, etc.).
;;  A Clojure atom is used to provide atomic access to the state.
(deftype MemStore [matom]
  KeyStore
  (put! [this ks v]  (go (swap! matom state-put ks v)))
  (remove! [this ks] (go (swap! matom state-remove ks)))
  (get! [s ks]       (go (state-get @matom ks)))
  (get! [s ks dv]    (go (state-get @matom ks dv)))
  (list! [s ks]      (go (state-keys @matom ks))))


(defn create
  ([] (MemStore. (atom {})))
  ([matom] (MemStore. matom)))


(defn action-assoc-in [system {:keys [ks v]}]
  (put! (:state-mgr system) ks v))


(defn action-dissoc-in [system {:keys [ks]}]
  (remove! (:state-mgr system) ks))


(def action-handlers
  {:assoc-in action-assoc-in
   :dissoc-in action-dissoc-in})
