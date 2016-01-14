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

(defn i->theta [n i]
  (/ (* 2 Math.PI i) n))

(defn thetas [n]
  (map #(i->theta n %) (range n)))

(defn ramp [at width x]
  (let [a (+ at width)]
    (cond (< x at) 0
          (< x (+ at width)) (/ (- x at) width)
          :else 1)))

(defn handle-start [x y event]
  (prn "dot start")
  (swap! core/model assoc :dragging true)
  (swap! core/drag-chord #(merge core/dragger (assoc % :x1 x :y1 y :x2 x :y2 y))))

(defn handle-move [event]
  (when (:dragging @core/model)
    (prn "move")
    (when-let [svg (core/el "svg-container")]
      (let [[x2 y2] (events/mouse->svg svg event)]
        (swap! core/drag-chord assoc :x2 x2 :y2 y2)))))

(defn handle-dot-out [event]
  (prn "dot-out"))

(defn handle-out [event]
  (prn "out"))

(defn xy->theta [x y]
  (let [angle (.atan2 js/Math y x)]
    (if (pos? angle) angle (+ (* 2 (.-PI js/Math)) angle)))
  )

(defn closest-dot [x y]
  (let [angle (xy->theta x y)
        n (:stars-n @core/model)
        sector (apply min-key
                #(.abs js/Math (- (i->theta n %) angle))
                (range n))
        t (i->theta n sector)
        ]
    [(dot-coord :x t) (dot-coord :y t)]
    ))

(defn handle-end [x1 y1 event]
  (let [chord @core/drag-chord
        x (:x2 chord)
        y (:y2 chord)
        theta (xy->theta x y)
        [x2 y2] (closest-dot x y)
        ]
    (swap! core/model assoc :dragging false)
    (swap! core/drag-chord assoc :x2 x2 :y2 y2))
  )

(defn svg-event-coords [svg event]
  (events/mouse->svg svg event))

(rum/defc dot [theta]
  (let [x (dot-coord :x theta)
        y (dot-coord :y theta)]
    [:circle {:style {:cursor "pointer"}
              :stroke "#ffffff" :stoke-width 20 :fill "#CCCCCC" :r 8
              :cx x
              :cy y
              :on-mouse-down #(handle-start x y %)
              :on-touch-start #(handle-start x y %)
              :on-mouse-out handle-out
              :on-mouse-up #(handle-end x y %)
              :on-touch-end #(handle-end x y %)
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

(rum/defc drag-line < rum/reactive []
  [:line (rum/react core/drag-chord)])

(rum/defc chords < rum/reactive []
  [:g])

(rum/defc star < rum/reactive []
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
     [:circle.outlined {:fill "none" :stroke "black" :stroke-width 2 :cx 200 :cy 200 :r 190}]
     (dots-on-circle ((rum/react core/model) :stars-n))
     (chords)
     (drag-line)]]])

(rum/defc stars []
  [:div
   (count-input)
   [:div {:style {:clear "both"}}
    (star)]])
