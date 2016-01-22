(ns ^:figwheel-always stars.cards

  (:require
   [rum.core :as rum]
   [stars.core :as core]
   [stars.components :as comp])

  (:require-macros
   [devcards.core :as dc :refer [defcard deftest]]
   ))

(enable-console-print!)

(defcard stars
  (comp/stars))
