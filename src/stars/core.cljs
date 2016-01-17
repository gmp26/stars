(ns ^:figwheel-always stars.core)

(enable-console-print!)

(def dragger {:id "drag-line"
              :opacity 0.5
              :stroke "rgba(0, 128, 128, 1)"
              :stroke-width 10
              :stroke-linecap "round"})

(defonce drag-chord (atom {:spec nil   ; variable attributes of drag line
                           :start 0    ; start dot index
                           :end 0}))   ; end dot index

(defonce svg-point (atom nil)) ; will be created to calculate svg screen transform

(defonce model
  (atom {:stars-n 8
         :stars-t [0.1 0 0 0]
         :dragging false
         :t 0
         }))

(defn el [id] (js/document.getElementById id))

;; main entry, not yet
