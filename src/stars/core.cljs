(ns ^:figwheel-always stars.core)

(enable-console-print!)

(defonce drag-chord {})

(defonce svg-point (atom nil)) ; will be created to calculate svg screen transform

(defonce model
  (atom {:stars-n 10
         :stars-t [0.1 0 0 0]
         :chords [{:from 0 :to 3 :t 0.5}]  ; half-drawn chord from 0 to 3
         }))

(defn el [id] (js/document.getElementById id))

;; main entry, not yet
