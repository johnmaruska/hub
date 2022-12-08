(ns hub.advent-of-code.2022.day07
  (:require
   [hub.advent-of-code.util :as util]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as string]))

(defn parse [file]
  (map (fn [s] (string/split s #" "))
       (string/split-lines (slurp file))))

(defn add-dir [dirs dir]
  (if (get dirs dir)
    dirs
    (assoc dirs dir {:files #{}})))

(defn add-file [dirs path file]
  (update-in dirs [path :files] conj file))

(defn file-system [lines]
  (loop [[line & rem] lines
         ls-output?   false
         curr-path    ["/"]
         dirs         {["/"] {:files #{}}}]
    (cond
      (nil? line) dirs

      (= ["$" "cd" ".."] line)
      (recur rem false (pop curr-path) dirs)

      (= ["$" "cd" "/"] line)
      (recur rem false ["/"] dirs)

      (= ["$" "cd"] (take 2 line))
      (let [[_ _ dir] line
            new-path  (conj curr-path dir)]
        (recur rem false new-path dirs))

      (= ["$" "ls"] line)
      (recur rem true curr-path dirs)

      (and ls-output? (= "dir" (first line)))
      (let [new-dir (conj curr-path (second line))]
        (recur rem true curr-path (add-dir dirs new-dir)))

      ls-output?
      (let [new-file {:name (second line) :size (first line)}]
        (recur rem true curr-path (add-file dirs curr-path new-file)))

      :else
      (println "?????" line))))

(defn subdirectories [dirs dir]
  (filter (fn [[path data]]
            (and (= (inc (count dir)) (count path))
                 (= dir (drop-last path))))
          dirs))

(defn size-of [dirs dir]
  (let [files   (->> (:files (get dirs dir))
                     (map (comp edn/read-string :size))
                     (reduce +))
        subdirs (->> (subdirectories dirs dir)
                     (map first)
                     (map (fn [x] (size-of dirs x)))
                     (reduce +))]
    (+ files subdirs)))

(defn deepest-first [fs]
  (sort-by (comp - count) (keys fs)))

(defn part1 []
  (let [fs (file-system (parse (io/file "resources" (util/input 2022 7))))
        all-dirs (deepest-first fs)]
    (->> all-dirs
         (map #(size-of fs %))
         (filter #(< % 100000))
         (reduce +))))

(defn part2 []
  (let [total-available 70000000
        unused-required 30000000

        fs         (file-system (parse (io/file "resources" (util/input 2022 7))))
        all-dirs   (deepest-first fs)
        space-used (size-of fs ["/"])
        deletion-required (- (- total-available space-used unused-required))]
    (->> all-dirs
         (map #(size-of fs %))
         (filter #(< deletion-required %))
         sort first)))
