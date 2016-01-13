(ns ^:figwheel-always stars.events

    (:require
     [stars.core :as core]
     ))

(defn touchXY
  "get position of first changed touch"
  [event]
  (let [touch (aget (.-changedTouches event) 0)]
    [(.-clientX touch) (.-clientY touch)]))

(defn mouseXY
  "get mouse position"
  [event]
  [(.-clientX event) (.-clientY event)])

(defn eventXY
  "get touch or mouse position"
  [event]
  (let [type (subs (.-type event) 0 5)]
    (condp = type
      "mouse" (mouseXY event)
      "touch" (touchXY event)
      )))

(defn mouse->svg
  "browser independent transform from mouse/touch coords to svg viewport"
  [svg event]
  (let [pt (if @core/svg-point
             @core/svg-point
             (reset! core/svg-point (.createSVGPoint svg)))
        matrix (.inverse (.getScreenCTM svg))
        [x y] (eventXY event)
        ]
    (aset pt "x" x)
    (aset pt "y" y)
    (prn pt)
    (reset! core/svg-point (.matrixTransform pt matrix))
    [(.-x @core/svg-point) (.-y @core/svg-point)]))
