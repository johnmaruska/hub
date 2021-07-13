(ns hub.sketchbook.chess)

(def sides
  {:white {:pawn-rank 2
           :forward   +
           :backward  -}
   :black {:pawn-rank 7
           :forward   -
           :backward  +}})

(defn make-piece [type color]
  {:type type :color color})

(defn on-board? [[rank file]]
  (and (<= 1 rank 8) (<= 1 file 8)))

(defn position->indices [[rank file]]
  [(- 8 rank) (dec file)])

(defn piece-at [board position]
  (get-in board (position->indices position)))

(defn enemy? [attacker defender]
  (not= (:color attacker) (:color defender)))

(defn valid-space? [piece board position]
  (and (on-board? position)
       (let [occupant (piece-at board position)])
       (or (nil? occupant)
           (enemy? piece occupant))))

(defn up-from [n] (range (inc n) (inc 8)))
(defn down-from [n] (range (dec n) 0 -1))

(defn until-blocked
  "Restrict seq of positions to only those prior to and including a blocked space."
  [board positions]
  (let [[open [blocker & _]] (split-with #(nil? (piece-at board %1)) positions)]
    (if blocker (conj open blocker) open)))

(defn diagonals-from [[rank file]]
  [(map list (up-from rank)   (up-from file))
   (map list (up-from rank)   (down-from file))
   (map list (down-from rank) (up-from file))
   (map list (down-from rank) (down-from file))])

(defn straights-from [[rank file]]
  [(map list (up-from rank)   (repeat file))
   (map list (down-from rank) (repeat file))
   (map list (repeat rank)    (up-from file))
   (map list (repeat rank)    (down-from file))])

(defn pawn-moves [piece board]
  (let [side           (get sides (:color piece))
        forward        (:forward side)
        [rank file]    (:position piece)
        standard-moves [[(forward rank 1) file]
                        ;; enemy front left
                        ;; enemy front right
                        ]
        double-start   [[(forward rank 2) file]
                        ;; enemy en-passant left
                        ;; enemy en-passant right
                        ]]
    (if (= (:pawn-rank side) rank)
      (concat standard-moves double-start)
      standard-moves)))

(defn rook-moves [{:keys [position]} piece board]
  (->> (straights-from position)
       (map (partial until-blocked board))
       (apply concat)))

(defn bishop-moves [{:keys [position]} board]
  (->> (diagonals-from position)
       (map (partial until-blocked board))
       (apply concat)))

(defn knight-moves [piece board]
  (let [[rank file] (:position piece)]
    (->> (for [rank-dir [+ -]
               file-dir [+ -]]
           [[(rank-dir rank 2) (file-dir file 1)] ; go tall
            [(rank-dir rank 1) (file-dir file 2)] ; go wide
            ])
         (apply concat))))

(defn queen-moves [{:keys [position]} board]
  (->> (concat (straights-from position)
               (diagonals-from board position))
       (map (partial until-blocked board))
       (apply concat)))

(defn king-moves [piece board]
  (let [[rank file] (:position piece)]
    (->> (for [rank-dir [dec inc identity]
               file-dir [dec inc identity]]
           [(rank-dir rank) (file-dir file)]))))

(def moves-fn
  {:pawn   pawn-moves
   :rook   rook-moves
   :bishop bishop-moves
   :knight knight-moves
   :queen  queen-moves
   :king   king-moves})

(defn moves [board piece]
  ((move-fn (:type piece)) piece board))

(defn valid-moves [{:keys [position] :as piece} board]
  ;; TODO: add castling
  (->> (moves piece board position)
       (filter #(valid-space? piece board %))
       ;; (filter #(checked? board piece %))
       ))

(defn threatened-by [piece board]
  (let [enemy-pieces      (->> (apply concat board)
                               (remove nil?)
                               (filter #(enemy? piece)))
        threatened-spaces (->> enemy-pieces
                               (mapcat #(moves board %))
                               (into #{}))]
    (contains? threatened-spaces (:position piece))))

(def BP (make-piece :pawn   :black))
(def BR (make-piece :rook   :black))
(def BB (make-piece :bishop :black))
(def BN (make-piece :knight :black))
(def BQ (make-piece :queen  :black))
(def BK (make-piece :king   :black))

(def WP (make-piece :pawn :white))
(def WR (make-piece :rook :white))
(def WB (make-piece :bishop :white))
(def WN (make-piece :knight :white))
(def WQ (make-piece :queen :white))
(def WK (make-piece :king :white))

(def xx nil)

(defn add-positions [board]
  (let [all-pos (for [rank (range 1 9)
                      file (range 1 9)]
                  [rank file])]
    (reduce (fn [acc position]
              (if (piece-at board position)
                (assoc-in acc (concat (position->indices position) [:position]) position)
                acc))
            board all-pos)))

(def init-board
  (add-positions
   [[BR BN BB BK BQ BB BN BR]
    [BP BP BP BP BP BP BP BP]
    [xx xx xx xx xx xx xx xx]
    [xx xx xx xx xx xx xx xx]
    [xx xx xx xx xx xx xx xx]
    [xx xx xx xx xx xx xx xx]
    [WP WP WP WP WP WP WP WP]
    [WR WN WB WK WQ WB WN WR]]))

(defn remaining-pieces [board]
  (->> board
       (apply concat)
       (remove nil?)))
