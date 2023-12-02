(ns keylog-analysis.core)

(defn remove-L-R-from-key
  "Removes any _L or _R suffix."
  [key]
  (if (or (clojure.string/ends-with? key "_L")
          (clojure.string/ends-with? key "_R"))
    (subs key 0 (- (count key) 2))
    key))

(def events (->> (slurp "resources/gobbo.log")
                 (clojure.string/split-lines)
                 (map-indexed (fn [line-nr line]
                                (let [[time event key] (clojure.string/split line #" ")]
                                  {:line-nr line-nr
                                   :time    (Long/parseLong time)
                                   :press?  (= event "press")
                                   :key     (keyword (remove-L-R-from-key key))})))))

(defn chords
  "A chord is a sequence of events that caused buttons to be  pressed down at the same time
   until one of these keys was released.
   Note that e.g. holding down Ctrl while spamming z,z,z,z results in four chords, all of
   the form Ctrl+z. More generally, when keys A and B are both pressed, when B is released
   the chord A+B is registered, when key C is pressed and released chord A+C is registered.
   This works for any number of keys in a chord.
   Returns a sequence of chords, where each chord is a sequence of events. The first half
   of the events in a chord are press events, the second half are the corresponding release
   events, in chronological order.
   Note that the first release event in a chord caused the chord to exist."
  [events]
  (loop [; Initially only press events form a chord,
         ; when the loop is over the corresponding release events are added. 
         chords [] 
         key->press-event {}
         previous-event-was-press? false
         press-event->release-event {}
         unseen (drop-while #(not (:press? %)) events)]
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
           chords))))

(chords events)



(def chords-by-keys
  (->> events
       chords
       (group-by (comp distinct #(map :key %)))))



; Most common chords
(->> chords-by-keys
     (sort-by (comp count second))
     reverse
     (map (juxt first (comp count second)))
     (take 25))


; Most common keys pressed
(->> events
     (map :key)
     frequencies
     (sort-by second)
      reverse
      (map (juxt first second))
     (take 50))
