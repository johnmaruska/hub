(ns hub.sketchbook.bio
  (:require [clojure.core.match :refer [match]]))

(def invert
  {\A \T
   \C \G
   \G \C
   \T \A})

(defn amino-acid [bases]
  (match [(vec bases)]
         ;; multiple
         ;;; breaks
         [(:or [\A \G (:or \T \C)]
               [\T \C _])]           :serine

         ;;; works
         [(:or [\T \T (:or \A \G)]
               [\C \T _])]           :leucine
         ;; T
         [[\T \T (:or \T \C)]]       :phenylalanine
         [[\T \A (:or \T \C)]]       :tyrosine

         ;;; breaks
         [(:or [\T \G \A]
               [\T \A (:or \A \G)])] :STOP

         [[\T \G (:or \T \C)]]       :cysteine
         [[\T \G \G]]                :tryptophan
         ;; C
         [[\C \C _]]                 :proline
         [[\C \A (:or \T \C)]]       :histidine
         [[\C \A (:or \A \G)]]       :glutamine
         [[\C \G _]]                 :arginine
         ;; A
         [[\A \T (:or \T \C \A)]]    :isoleucine
         [[\A \T \G]]                :methionine
         [[\A \C _]]                 :threonine
         [[\A \A (:or \T \C)]]       :asparagine
         [[\A \A (:or \A \G)]]       :lysine
         [[\A \G (:or \A \G)]]       :arginine
         ;; G
         [[\G \T _]]                 :valine
         [[\G \C _]]                 :alanine
         [[\G \A (:or \T \C)]]       :aspartic-acid
         [[\G \A (:or \A \G)]]       :glutamic-acid
         [[\G \G _]]                 :glycine))

(defn generate-paired-strand
  "Read a DNA strand, 3' to 5', and generate its pair, ordered 3' to 5'."
  [strand]
  (apply str (reverse (map invert strand))))

(defn read-strand [strand]
  (->> strand
       (partition 3)
       (drop-while #(not (= :methionine (amino-acid %))))
       (take-while #(not (= :STOP (amino-acid %))))))

(defn count-amino-acids [strand]
  (reduce #(update %1 (amino-acid %2) (fnil inc 0))
          {}
          (read-strand strand)))
