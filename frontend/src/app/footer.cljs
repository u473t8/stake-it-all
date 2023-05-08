(ns app.footer
  (:require
    [re-frame.core :as rf]))


(rf/reg-sub :footer :-> :footer)


(defmulti footer (fn [& xs] (first xs)))


(defmethod footer :default
  [& _]
  [:div
   [:h1 "Default footer!"]])


(defmethod footer :user-logged
  [& _]
  [:div
   [:h1 (if-let [username @(rf/subscribe [:username])]
          (str "Bye, " username "!")
          "Bye, bye!")]
   [:div {:style {:color :red}} "Red"]])

