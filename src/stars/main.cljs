(ns ^:figwheel-always stars.main
    (:require [rum.core :as rum]
              [stars.core :as core]
              [stars.components :as comp]))

(if-let [node (core/el "main-app-area")]
  (rum/mount (comp/stars core/model) node))
