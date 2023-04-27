(ns app.core
  (:require
    [reagent.core :as r]
    [reagent.dom :as dom]))


(def the-state (r/atom {:theme  :light
                        :reader? false}))


(defn navigation
  [arg]
  [:nav {:style {:background    :pink
                 :border-radius :10px
                 :margin        "20px 0 50px 0"
                 :padding       "10px 10px 10px 20px"}}
   arg])


(defn footer?
  [x]
  (boolean (and x @the-state)))


(defmulti footer (fn [& xs] (first xs)))


(defmethod footer :default
  [& _]
  [:div
   [:h1 "Default footer."]])


(defmethod footer :after-switch
  [& _]
  [:div
   [:h1 "Bye, <username>!"]
   [:div {:style {:color :red}} "Red"]])


(defn div
  [x]
  [:div x])


(defn lightbox
  []
  [:div.lightbox
   {:on-click (fn [_]
                (swap! the-state assoc
                  :theme (case (:theme @the-state)
                           :light     :semi-dark
                           :semi-dark :dark
                           :dark      :light)))}
   (:theme @the-state)])


(defn reader
  [arg & xs]
  [:<>
   [:div.reader
    (when (not (nil? arg)) [:div arg])
    [:input {:type    :button
             :value   "THE MAIN BUTTON"
             :on-click (fn [_]
                         (swap! the-state assoc
                           :reader? (if (:reader? @the-state) false true)
                           :footer  (if (nil? (:footer @the-state)) :after-switch nil)))}]
    (if arg
      (rest (map div xs))
      (first (map div xs)))]])


(defn log-in-button
  []
  [:div
   [:input {:type     :button
            :value    (if (:username @the-state) "Log out" "Log in")
            :on-click (fn [_]
                        (swap! the-state assoc
                          :username (if (:username @the-state) nil "Person")))}]])


(defn greeting
  [username]
  [:header
   [:h1
    (if (:username @the-state)
      (if (:username @the-state)
        (str "Hello, " (:username @the-state) "!")
        "Hello, <username>!")
      "Hi there!")]])


(defn app
  []
  (set! (.-title js/document) "Hello, World!")
  [:<>
   [:div {} "The state of page: " @the-state]
   [navigation [:<>
                [lightbox]
                [log-in-button]]]
   [greeting]
   [reader (:reader? @the-state) "Common content" "Paid super-duper content"]
   [footer (:footer @the-state)]])


(defn mount
  "Mounts application in the body of the main page."
  []
  (dom/render [app]
    (.-body js/document)))


(mount)
