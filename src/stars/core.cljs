(ns ^:figwheel-always stars.core)

(enable-console-print!)

(def dragger {:id "drag-line"
              :opacity 0.5
              :stroke "rgba(0, 128, 128, 1)"
              :stroke-width 4
              :stroke-linecap "round"})

(defonce drag-chord (atom nil))

(defonce svg-point (atom nil)) ; will be created to calculate svg screen transform

(defonce model
  (atom {:stars-n 8
         :stars-t [0.1 0 0 0]
         :chords [{:from 0 :to 3 :t 0.5}]  ; half-drawn chord from 0 to 3
         :dragging false
         }))

(defn el [id] (js/document.getElementById id))

;; main entry, not yet
