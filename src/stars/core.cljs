(ns ^:figwheel-always stars.core)

(enable-console-print!)


(defonce svg-point (atom nil)) ; will be created to calculate svg screen transform

(defonce model
  (atom {:stars-n 8
         :t 0
         :dragging false}))

(defonce drag-chord (atom {:spec nil   ; variable attributes of drag line
                           :start 0    ; start dot index
                           :end 0}))   ; end dot index

(def dragger {:id "drag-line"
              :opacity 0.5
              :stroke "rgba(0, 128, 128, 1)"
              :stroke-width 10
              :stroke-linecap "round"})

(defn el [id] (js/document.getElementById id))

;; main entry, not yet
