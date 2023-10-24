(ns keylog-analysis.core)


(defn remove-L-R-from-key [key]
  (if (or (clojure.string/ends-with? key "_L")
          (clojure.string/ends-with? key "_R"))
    (subs key 0 (- (count key) 2))
    key))

(def data (->> (slurp "resources/clj.log")
               (clojure.string/split-lines)
               (map-indexed (fn [line-nr line]
                      (let [[time event key] (clojure.string/split line #" ")]
                        {:line-nr line-nr
                         :time    (Long/parseLong time)
                         :press?  (= event "press")
                         :key     (keyword (remove-L-R-from-key key))})))))

(->> data
     (map :time)
     (#(map vector (rest %) (butlast %)))
     (map (partial apply -))
     (map #(cond
             (<= % 500) 50
             (<= % 1000) 100
             (<= % 1500) 150
             (<= % 2000) 200
             (<= % 3000) 300
             (<= % 5000) 500
             (<= % 10000) 1000
             :else 9999))
     (frequencies)
     (sort-by first))

; chords: keys that are pressed at the same time.
; press, and everything that is pressed after, until release, is a chord.
; for not consider build a chord greedily.
; TODO: improvement: in a chord, as soon as a key is released, the chord is over.
;       if a new key is pressed while other keys in the chord are still pressed, it is a new chord.
;       but, if a key is release for the second time in a chord, there is no new chord;
;       a new chord is started as soon as a key is pressed, potentially with keys that were part
;       of a previous chords but hadn't been released yet.
;       if I want to archive key releases in a chord as well, could be that
;       there are many other key presses/releases in between, e.g. spamming ctrl+z,z,z,z,z
;       how to implement? could maintain a map of press to release; in first pass only add presses
;       to chords, in second wave add releases to chords, sort on time.
(defn chords [data]
  (let [data (drop-while #(not (:press? %)) data)]
    (loop [chords []
           key->press-event {}
           previous-event-was-press? false
           press-event->release-event {}
           unseen data]
      (if-let [event (first unseen)]
        (if (:press? event)
          (recur chords
                 (assoc key->press-event
                        (:key event)
                        event)
                 true
                 press-event->release-event
                 (rest unseen))
          (let [key->press-event' (dissoc key->press-event (:key event))
                press-event-to-release-event' (assoc press-event->release-event
                                                    (get key->press-event (:key event))
                                                    event)]
            (if (= 1 (count key->press-event))
              (recur chords
                     key->press-event'
                     false
                     press-event-to-release-event'
                     (rest unseen))
              (if previous-event-was-press?
                (recur (conj chords (vals key->press-event))
                       key->press-event'
                       false
                       press-event-to-release-event'
                       (rest unseen))
                (recur chords
                       key->press-event'
                       false
                       press-event-to-release-event'
                       (rest unseen))))))
        (map (fn [press-events]
               (->> (into press-events (map press-event->release-event press-events))
                    (sort-by :time)))
             chords)))))

#_(chords [{:time 1 :press? true :key :Control}
         {:time 2 :press? true :key :z}
         {:time 3 :press? false :key :z}
         {:time 4 :press? true :key :z}
         {:time 5 :press? false :key :z}
         {:time 6 :press? false :key :Control}])

(def chords-by-keys
  (->> data
       chords
       (group-by (comp distinct #(map :key %)))))

(->> chords-by-keys
     (sort-by (comp count second))
     reverse
     (map (juxt first (comp count second)))
     (take 25))

(->> data
     (map :key)
     frequencies
     (sort-by second)
      reverse
      (map (juxt first second))
     (take 25))