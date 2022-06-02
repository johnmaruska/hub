(ns hub.discljord.dice
  (:require
   [clojure.string :as string]
   [discljord.formatting :refer [mention-user]]
   [hub.dice :as dice]
   [hub.discljord.util :as util :refer [ticks]]))

(def sum (partial reduce + 0))

(defn term->str [term]
  (let [n (::dice/n term)
        d (::dice/d term)]
    (str n (when (not= 1 d) (str "d" d)))))

(defn terms->str [terms]
  (->> terms
       (remove #(= 1 (::dice/d %)))
       (map (fn [term]
              (str (ticks (term->str term)) " rolled "
                   (ticks (string/join "," (::dice/rolls term))))))
       (string/join "\n")))

(defn terms->expression [terms]
  (let [first-term (term->str (first terms))
        rest-terms (->> (rest terms)
                        (map (fn [term] (str " "
                                             (::dice/sign term)
                                             " "
                                             (term->str term)))))]
    (apply str first-term rest-terms)))

(defn roll [bot event]
  (let [terms   (dice/roll-terms (dice/parse (:content event)))
        message (str (mention-user (:author event))
                     " prompted with " (ticks (terms->expression terms)) " rolled "
                     (ticks (sum (map dice/eval-term terms)))
                     "\n" (terms->str terms))]
    (util/reply bot event message)))
