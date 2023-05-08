(ns app.core
  (:require
    [app.footer :as footer]
    [clojure.string :as str]
    [re-frame.core :as rf]
    [reagent.core :as r]
    [reagent.dom.client :as dom]))


(rf/reg-sub :db :-> identity)

(rf/reg-sub :theme :-> :theme)

(rf/reg-sub :username :-> :username)

(rf/reg-sub :paid-content? :-> :paid-content?)

(rf/reg-sub
  :label
  :<- [:username]
  (fn [username _]
    (if username
      "Log out"
      "Log in")))



(rf/reg-event-db
  :log-in
  (fn [db _]
    (assoc db
      :username (if (:username db) nil "Person"))))


(defn set-page-theme!
  [theme]
  (set!
    (.-href (.getElementById js/document "theme"))
    (str "styles/" (str/replace (name theme) #"[-_]+" "_") ".css")))


(defn browser-dark-theme?
  []
  (-> js/window
    (.matchMedia "(prefers-color-scheme: dark)")
    (.-matches)))


(rf/reg-event-db
  :change-page-theme
  (fn [db [_ & args]]
    (let [dark? (browser-dark-theme?)
          theme (case (:theme db)
                  :light :dark
                  :dark  :light)]
      (set-page-theme! theme)
      (assoc db
        :theme theme))))


(rf/reg-event-db
  :page-load
  (fn [db _]
    (let [dark? (browser-dark-theme?)
          theme (if dark? :dark :light)]
      (set-page-theme! theme)
      (assoc db
        :theme               theme ;; We need to intercept user's preferences.
        :theme/browser-dark? dark?
        :paid-content?       false
        :username            nil))))


(rf/reg-event-db
  :open-paid-content
  (fn [db _]
    (assoc db
      :paid-content? (if (true? (:paid-content? db)) false true)
      :footer        (if (nil? (:footer db)) :user-logged nil))))



(defn navigation
  [& args]
  [:nav {:style {:background    :pink
                 :border-radius :10px
                 :margin        "20px 0 50px 0"
                 :padding       "10px 10px 10px 20px"}}
   (into [:<>] args)])


(defn button-change-theme
  []
  [:input {:type     :button
           :value    @(rf/subscribe [:theme])
           :on-click #(rf/dispatch [:change-page-theme])}])


(defn button-log-in
  []
  [:input {:type     :button
           :value    @(rf/subscribe [:label])
           :on-click #(rf/dispatch [:log-in])}])


(defn article
  [arg & xs]
  [:<>
   [:div.article
    (when (not (nil? arg)) [:div arg])
    [:input {:type     :button
             :value    "Buy content"
             :on-click #(rf/dispatch [:open-paid-content])}]
    (if arg
      (rest (for [x xs] ^{:key x} [:div x]))
      (first (for [x xs] [:div x])))]])


(defn greeting
  [username]
  [:header
   [:h1
    (if @(rf/subscribe [:username])
      (if @(rf/subscribe [:username])
        (str "Hello, " @(rf/subscribe [:username]) "!")
        "Hello, <username>!")
      "Hi there!")]])


(rf/reg-event-db
  :change-browser-theme
  (fn [db _]
    (assoc db
      :theme/browser-dark? (.-matches (.matchMedia js/window "(prefers-color-scheme: dark)")))))


(defn app
  []
  (set! (.-title js/document) "Hello, World!")
  [:<>
   [:div {} "The state of page: " @(rf/subscribe [:db])]
   [navigation [:div {:style {:display         :flex
                              :justify-content :space-between}}
                [button-change-theme]
                [button-log-in]]]
   [greeting]
   [article @(rf/subscribe [:paid-content?]) "Common content" "Paid super-duper content"]
   [footer/footer @(rf/subscribe [:footer])]])


(defonce root
  (dom/create-root (.getElementById js/document "app")))


(defn ^:dev/after-load mount
  "Mounts application in the body of the main page."
  []
  (rf/clear-subscription-cache!)
  (rf/dispatch [:page-load])
  (dom/render root [app]))


(.addEventListener (.matchMedia js/window "(prefers-color-scheme: dark)")
  "change" #(rf/dispatch [:change-browser-theme]))
