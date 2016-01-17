(ns ^:figwheel-always stars.core)

(enable-console-print!)


(defonce svg-point (atom nil)) ; will be created to calculate svg screen transform

(defonce model
  (atom {:stars-n 8
         :stars-t [0.1 0 0 0]
         :dragging false
         :clock [0 0]
         }))

(defonce drag-chord (atom {:spec nil   ; variable attributes of drag line
                           :start 0    ; start dot index
                           :end 0}))   ; end dot index

(def dragger {:id "drag-line"
              :opacity 0.5
              :stroke "rgba(0, 128, 128, 1)"
              :stroke-width 10
              :stroke-linecap "round"})

(defn create-timer [speed]
  (let [timer (goog.Timer. speed)]
    (.listen timer
             (.-TICK goog.Timer)
             (fn [] (let [now (goog.now)]
                     (swap! model update
                            :clock #(let [[t0 t] %] (if (zero? t0) [now 0] [t0 (- now t0)]))))))
    timer)
  )

(defonce timer (create-timer 5))

(defn el [id] (js/document.getElementById id))

;; main entry, not yet
