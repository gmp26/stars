(ns ^:figwheel-always stars.cards

  (:require
   [rum.core :as rum]
   [stars.core :as core]
   [stars.components :as comp])

  (:require-macros
   [devcards.core :as dc :refer [defcard deftest]]
   ))

(enable-console-print!)

#_(defcard basic-stars-rose
  (comp/basic-stars-rose core/model))

#_(defcard four-draw-methods
  (comp/side-by-side core/model))

(defcard stars
  (comp/stars))

#_(defcard timer
  (comp/timer))
