(ns ^:figwheel-always stars.components
  (:require
   [rum.core :as rum]
   [stars.core :as core]
   [stars.events :as events]
   ))

(def min-nodes 2)
(def max-nodes 16)
(def stars-r 190)
(def stars-o {:x 200 :y 200})
(def t-steps 100)

(defn dot-coord [key theta]
  (+ (key stars-o) (* stars-r ((if (= key :x) Math.cos Math.sin) theta))))

(defn deg [rad]
  (/ (* rad 180) (.-PI js/Math)))

(defn coord-theta [[x y :as pt]]
  (let [[ox oy :as origin] [(:x stars-o) (:y stars-o)]
        theta1 (.acos js/Math (/ (- x ox) stars-r))
        theta2 (.asin js/Math (/ (- y oy) stars-r))]
    (prn "thetas = " (deg theta1) "," (deg theta2))
    ))

(defn i->theta [n i]
  (/ (* 2 Math.PI i) n))

(defn thetas [n]
  (map #(i->theta n %) (range n)))

(defn ramp [at width x]
  (let [a (+ at width)]
    (cond (< x at) 0
          (< x (+ at width)) (/ (- x at) width)
          :else 1)))

(rum/defc dot [theta]
  [:circle {:style {:cursor "pointer"}
            :stroke "#ffffff" :stoke-width 20 :fill "#CCCCCC" :r 8
            :cx (dot-coord :x theta)
            :cy (dot-coord :y theta)
            :on-mouse-down #(if-let [svg (core/el "svg-container")]
                              (prn (coord-theta (events/mouse->svg svg %)) (deg theta)))}])

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

(defn mean [a b] (/ (+ a b) 2))

(rum/defc chord [theta1 theta2 t]
  (let [x2 (dot-coord :x theta2)
        y2 (dot-coord :y theta2)]

    [:line {:x1 (dot-coord :x theta1)
            :y1 (dot-coord :y theta1)
            :x2 (+ (* (dot-coord :x theta1) (- 1 t)) (* x2 t))
            :y2 (+ (* (dot-coord :y theta1) (- 1 t)) (* y2 t))
            :stroke "rgba(0,128,255,0.3)" ;"#08f"
            :stroke-width 2
            :marker-end (if (< t 1) "url(#arrow)" "none")
            }]))

(rum/defc chords < rum/reactive []
  [:g])


(rum/defc stars-rose < rum/reactive []
  [:div {:style {:padding "2%" :display "inline-block" :width "96%"}}
   [:svg {:id "svg-container"
          :view-box "0 0 400 400"}
    [:defs
     [:marker {:id "arrow"
               :view-box "-0.5 -0.5 1 1"
               }
      [:circle {:cx 0 :cy 0 :r 0.3 :fill "black"}]]]
    [:g
     [:circle.outlined {:fill "none" :stroke "black" :stroke-width 2 :cx 200 :cy 200 :r 190}]
     (dots-on-circle ((rum/react core/model) :stars-n))
     (chords)
     ]]])


(rum/defc stars []
  [:div
   (count-input)
   [:div {:style {:clear "both"}}
    (stars-rose)]
]
  )
