(ns app.core
  (:require
    [reagent.core :as r]
    [reagent.dom :as dom]))


(defn app
  []
  [:div [:h1 "Hi!"]])


(defn mount-app []
  (dom/render [app]
    (.-body js/document)))


(mount-app)
