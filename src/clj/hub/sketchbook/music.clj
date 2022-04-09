(ns hub.sketchbook.music
  "Goofin on some music stuff. So far it's just to generate notation chart for a
  fretboard with arbitrary tuning."
  (:require [clojure.string :as string]))

;;; Notes

(def Af "Af")
(def A "A")
(def A# "A#")
(def Bf "Bf")
(def B "B")
(def C "C")
(def C# "C#")
(def Df "Df")
(def D "D")
(def D# "D#")
(def Ef "Ef")
(def E "E")
(def F "F")
(def F# "F#")
(def Gf "Gf")
(def G "G")
(def G# "G#")

(def all-frets
  "Collection of note sequences in various notation types.
  Order and starting element _do_ matter. All start on A."
  {:sharp [A A# B C C# D D# E F F# G G#]
   :flat  [A Bf B C Df D Ef E F Gf G Af]
   :full  [A nil B C nil D nil E F nil G nil]})

(defn note-type [note]
  (cond
    (string/ends-with? note "f") :flat
    (string/ends-with? note "#") :sharp
    :else :full))

(defn starting-with [notes-seq note]
  (lazy-seq
   (->> (apply concat (repeat notes-seq))
        (drop-while #(not= note %)))))

(defn note->type
  "Converts a given note to specified notation type."
  [note target-type]
  {:pre [(contains? #{:sharp :full :flat} target-type)]}
  (let [source-type  (note-type note)
        source-notes (get all-frets source-type)]
    (nth (get all-frets target-type)
         (.indexOf source-notes note))))

(defn sharp-or-flat [note]
  (let [type (note-type note)]
    (if (= type :full) :sharp type)))

(defn tuning->fretboard
  "Generate fretboard notation chart for `tuning`, assuming a neck of
  `fretboard-length` frets."
  [tuning fretboard-length]
  (for [open-note tuning]
    (let [frets (get all-frets (sharp-or-flat open-note))]
      (->> (starting-with frets open-note)
           (take (inc fretboard-length))))))

(defn fret->str [note display-type]
  (let [full? (= :full (note-type note))
        note  (note->type note display-type)]
    (cond
      full?       (str "--" note "--")
      (nil? note) "-----"
      :else       (str "--" note "-"))))

(defn string->str [string display-type]
  (let [open-note (first string)
        frets     (->> (rest string)
                       (map #(fret->str % display-type))
                       (string/join "|"))]
    (str open-note " ||" frets "|")))

(defn fretboard->str [fretboard display-type]
  (->> fretboard
       reverse
       (map #(string->str % display-type))
       (string/join "\n")))

;;; tunings

(defn tuning->str [tuning frets display-type]
  (-> tuning
      (tuning->fretboard frets)
      (fretboard->str display-type)))
