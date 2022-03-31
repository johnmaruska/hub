(ns hub.sketchbook.music-test
  (:require
   [hub.sketchbook.music :as sut]
   [clojure.test :refer [deftest is testing]]
   [clojure.string :as string]))


(deftest note-type
  (testing "works with :flat"
    (is (= :flat  (sut/note-type "Bf"))))
  (testing "works with :sharp"
    (is (= :sharp (sut/note-type "C#"))))
  (testing "works with :full"
    (is (= :full  (sut/note-type "F")))))


(deftest starting-with
  (testing "wraps around after G"
    (is (= ["G" "Af" "A" "Bf" "B"]
           (take 5 (sut/starting-with (:flat sut/all-frets) "G"))))))

(deftest note->type
  (testing "flat converts to sharp"
    (is (= "G#" (sut/note->type "Af" :sharp))))
  (testing "flat converts to full"
    (is (nil? (sut/note->type "Af" :full))))
  (testing "flat converts to flat"
    (is (= "Af" (sut/note->type "Af" :flat))))

  (testing "sharp converts to sharp"
    (is (= "C#" (sut/note->type "C#" :sharp))))
  (testing "sharp converts to full"
    (is (nil? (sut/note->type "C#" :full))))
  (testing "sharp converts to flat"
    (is (= "Df" (sut/note->type "Df" :flat))))

  (testing "full converts to sharp"
    (is (= "F" (sut/note->type "F" :sharp))))
  (testing "full converts to full"
    (is (= "F" (sut/note->type "F" :full))))
  (testing "full converts to sharp"
    (is (= "F" (sut/note->type "F" :flat)))))

(deftest tuning->fretboard
  (testing "basic setup"
    (is (= '(("A" "A#" "B" "C"))
           (sut/tuning->fretboard ["A"] 3))))
  (testing "works with multiple strings"
    (is (= '(("E" "F" "F#" "G" "G#" "A")
             ("A" "A#" "B" "C" "C#" "D"))
           (sut/tuning->fretboard ["E" "A"] 5))))
  (testing "works with flat"
    (is (= '(("Bf" "B" "C" "Df"))
           (sut/tuning->fretboard ["Bf"] 3)))))

(deftest fret->str
  (testing "all dashes for nil, i.e. fret is a sharp/flat and mode is full"
    (is (= "-----" (sut/fret->str "C#" :full))))
  (testing "centered between dashes for whole notes"
    (is (= "--C--" (sut/fret->str "C" :full))))
  (testing "flat displays after centered note"
    (is (= "--Bf-" (sut/fret->str "Bf" :flat))))
  (testing "converts to display type"
    (is (= "--C#-" (sut/fret->str "C#" :sharp)))
    (is (= "-----" (sut/fret->str "C#" :full)))
    (is (= "--Df-" (sut/fret->str "C#" :flat)))))

(deftest string->str
  (testing "works with one note on the string"
    (is (= "E |||" (sut/string->str '("E") :full))))
  (testing "works with many notes on the string"
    (is (= "E ||--F--|-----|--G--|-----|--A--|"
           (sut/string->str '("E" "F" "F#" "G" "G#" "A") :full))))
  (testing "can start on a flat tuning"
    (is (= "A# ||--B--|--C--|--C#-|--D--|"
           (sut/string->str '("A#" "B" "C" "C#" "D") :sharp))))
  (testing "still displays open string when it would be hidden as a fret"
    (is (= "A# ||--B--|--C--|-----|--D--|"
           (sut/string->str '("A#" "B" "C" "C#" "D") :full))))
  (testing "works with flat open note"
    (is (= "Bf ||--B--|--C--|-----|"
           (sut/string->str '("Bf" "B" "C" "Df") :full)))))

(deftest tuning->str
  (testing "end-to-end, Drop C"
    (let [strings ["F ||-----|--G--|-----|--A--|-----|--B--|--C--|-----|--D--|-----|--E--|--F--|"
                   "C ||-----|--D--|-----|--E--|--F--|-----|--G--|-----|--A--|-----|--B--|--C--|"
                   "G ||-----|--A--|-----|--B--|--C--|-----|--D--|-----|--E--|--F--|-----|--G--|"
                   "C ||-----|--D--|-----|--E--|--F--|-----|--G--|-----|--A--|-----|--B--|--C--|"]
          expected (string/join "\n" strings)]
      (is (= expected (sut/tuning->str ["C" "G" "C" "F"] 12 :full))))))
