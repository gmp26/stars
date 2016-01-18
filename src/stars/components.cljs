(ns ^:figwheel-always stars.components
  (:require
   [rum.core :as rum]
   [stars.core :as core]
   [stars.events :as events]
   ))

;;;
;; constants
;;;
(def min-nodes 2)
(def max-nodes 16)
(def stars-r 180)
(def stars-o {:x 200 :y 200})
(def t-steps 100)
(def pi (.-PI js/Math))
(def two-pi (* 2 pi))

;;;
;; utilities
;;;

(defn mean [a b] (/ (+ a b) 2))

(defn gcd [a b]
  (if (zero? b)
    a
    (recur b (mod a b))))

(defn visit [n start end]
  (prn "visit"))

(defn dot-coord [key theta]
  (+ (key stars-o) (* stars-r ((if (= key :x) Math.cos Math.sin) theta))))

(defn deg [rad]
  (/ (* rad 180) (.-PI js/Math)))

(defn i->theta [n i]
  (/ (* 2 Math.PI i) n))

(defn thetas [n]
  (map #(i->theta n %) (range n)))

(defn xy->theta [x y]
  (let [angle (.atan2 js/Math (- y (:y stars-o)) (- x (:x stars-o)))]
    (if (pos? angle) angle (+ two-pi angle)))
  )

;; TODO
(defn closest-sector [x y]
  (let [angle (xy->theta x y)
        n (:stars-n @core/model)]
    (apply min-key
           #(let [diff (.abs js/Math (- (i->theta n %) angle))]
              (if (> diff pi) (- two-pi diff) diff))
           (range n))))

(defn closest-dot [x y]
  (let [t (i->theta (:stars-n @core/model) (closest-sector x y))]
    [(dot-coord :x t) (dot-coord :y t)]))

(defn ramp [at width x]
  (let [a (+ at width)]
    (cond (< x at) 0
          (< x (+ at width)) (/ (- x at) width)
          :else 1)))
;;;
;; event handlers
;;;
(defn handle-start [x y event]
  (prn "dot start")
  (swap! core/model assoc :dragging true)
  (swap! core/drag-chord assoc
         :start (closest-sector x y)
         :spec (merge core/dragger {:x1 x :y1 y :x2 x :y2 y})))

(defn handle-move [event]
  (when (:dragging @core/model)
    (prn "move")
    (when-let [svg (core/el "svg-container")]
      (let [[x2 y2] (events/mouse->svg svg event)]
        (swap! core/drag-chord #(assoc %
                                       :spec (assoc (:spec %) :x2 x2 :y2 y2)
                                       :end (closest-sector x2 y2)))))))

(defn handle-dot-out [event]
  (prn "dot-out"))

(defn handle-out [event]
  (prn "out"))

(defn handle-end [event]
  (prn "end")
  (swap! core/model assoc :dragging false :clock [0 0])
  (let [timer core/timer] (.stop timer) (.start timer)))

;;;
;; component renders
;;;
(rum/defc dot [theta]
  (let [x (dot-coord :x theta)
        y (dot-coord :y theta)]
    [:circle {:style {:cursor "pointer"}
              :stroke "#ffffff" :stroke-width 3 :fill "#CCCCCC" :r 20
              :cx x
              :cy y
              :on-mouse-down #(handle-start x y %)
              :on-touch-start #(handle-start x y %)
              :on-mouse-move handle-move
              :on-touch-move handle-move
              :on-mouse-out handle-out
              :on-mouse-up handle-end
              :on-touch-end handle-end
              }]))

(rum/defc n-slider < rum/static [model]
  [:input {:type "range" :value (:stars-n model) :min min-nodes :max max-nodes
           :style {:width "100%"}
           :on-change #(swap! core/model  assoc
                              :stars-n (-> % .-target .-value))}])

(rum/defc count-input < rum/reactive []
  [:span.node-count
   [:span
    [:input.inp-default
     {:type "number"
      :min min-nodes :max max-nodes :value (:stars-n (rum/react core/model))
      :on-change #(swap! core/model assoc :stars-n (.parseInt js/Number (-> % .-target .-value)))}]
    "points"]
   (n-slider (rum/react core/model))])

(rum/defc dots-on-circle [stars-n]
  [:g  (map-indexed  #(rum/with-key (dot %2) %1) (thetas stars-n))])

(rum/defc chord [sector1 sector2 t]
  (let [theta1 (i->theta (:stars-n @core/model) sector1)
        theta2 (i->theta (:stars-n @core/model) sector2)
        x1 (dot-coord :x theta1)
        y1 (dot-coord :y theta1)
        x2 (dot-coord :x theta2)
        y2 (dot-coord :y theta2)]
    [:line {:x1 x1
            :y1 y1
            :x2 (+ (* x1 (- 1 t)) (* x2 t))
            :y2 (+ (* y1 (- 1 t)) (* y2 t))
            :stroke-linecap "round"
            :stroke "rgba(0,128,128,1)" ;"#08f"
            :stroke-width 10
            :marker-end (if (< t 1) "url(#arrow)" "none")
            }]))

; TODO! merge in dragger here so it's not in drag-core state?
(rum/defc drag-line < rum/reactive []
  [:line (merge  {:style {:cursor "pointer"
                          :pointer-events "none"}}
                 (:spec (rum/react core/drag-chord)))])

(defn step [m dc]
  (mod (- (:start dc) (:end dc)) (:stars-n m)))

(defn addm [a b modulus]
  (mod (+ a b) modulus))

(rum/defc chords < rum/static [m dc]
  (let [t (/ (second (:clock m)) 500)]
    [:g
     #_(map-indexed (fn [idx sector]
                    (chord
                     (mod (+ (:start dc) sector (step m dc)) (:stars-n m) )
                     (addm (:start dc) sector (:stars-n m))
                     (ramp idx 1 t)))
                  (range  (:stars-n m) 0 (- (step m dc))))
     (chord (:start dc) (:end dc) 0.5)
     ]))

(rum/defc star < rum/static [m dc]
  [:div {:style {:padding "2%" :display "inline-block" :width "96%"}}
   [:svg {:id "svg-container"
          :view-box "0 0 400 400"
          :on-mouse-move handle-move
          :on-touch-move handle-move
          :on-mouse-up handle-end
          :on-touch-end handle-end
          }
    [:defs
     [:marker {:id "arrow"
               :view-box "-0.5 -0.5 1 1"
               }
      [:circle {:cx 0 :cy 0 :r 0.3 :fill "black"}]]]
    [:g
     [:circle.outlined {:fill "none" :stroke "black" :stroke-width 2 :cx 200 :cy 200 :r stars-r}]
     (dots-on-circle (:stars-n m))
     (if (:dragging m) (drag-line) (chords m dc)
         )
     ]]])

(rum/defc stars < rum/reactive []
  [:div
   (count-input)
   [:div {:style {:clear "both"}}
    (star (rum/react core/model) (rum/react core/drag-chord))
    [:p (str (rum/react core/drag-chord))]
    [:p (str (rum/react core/model))]]])
