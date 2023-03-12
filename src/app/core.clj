(ns app.core
  (:require
    [aleph.http :as http]
    [aleph.netty :as netty]
    [muuntaja.core :as m]
    [reitit.ring :as ring]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [ring.middleware.session :as ring-session]
    [ring.middleware.session.memory :as memory]))


(defonce servers (atom {}))
(defonce session-store (memory/memory-store))


(defn hello
  [{:keys [session]}]
  (if (seq session)
    {:status 200
     :body   (format "Hi, %s! How do you do?" (:user session))}
    {:status 200
     :body   (format "Hello! Who are you?")}))


(defn greeting-message
  [request]
  {:status 200
   :body   {:message (format "Hello, %s!" (:first-name (:body-params request)))}})


(defn login
  [request]
  (let [user (:user (:body-params request))]
    (if (contains? #{"egor" "petr"} user)
      {:status  200
       :session {:date (java.util.Date.)
                 :user user}}
      {:status 401})))


(defn logout
  [_]
  {:status  302
   :session nil
   :headers {"Location" "/"}})


(def app
  (ring/ring-handler
   (ring/router
    ["/"
     [[""
       {:get  {:handler #'hello}
        :post {:handler #'greeting-message}}]
      ["login"
       {:post {:handler #'login}}]
      ["logout"
       {:get {:handler #'logout}}]]]
    {:data
     {:muuntaja   m/instance
      :middleware [muuntaja/format-middleware
                   [ring-session/wrap-session {:store        session-store
                                               :cookie-attrs {:http-only false}}]]}})))


(defn get-config
  [server]
  (when (some? server)
    {:port (netty/port server)}))


(defn start-server!
  [& {:keys [port] :or {port 8888}}]
  (let [server (http/start-server #'app {:port port})]
    (swap! servers assoc port server)
    {:started (get-config server)}))


(defn stop-server!
  [& {:keys [port] :or {port 8888}}]
  (when-let [server (get @servers port)]
    (swap! servers update port #(.close %))
    {:stopped (get-config server)}))


(defn restart-server!
  [& {:keys [port] :or {port 8888} :as opts}]
  (let [opts (-> @servers
                 (get port)
                 (get-config)
                 (merge opts))]
    (merge
     (stop-server! opts)
     (start-server! opts))))


(defn -main
  []
  (restart-server! :port 3434))



(comment
  @servers
  (restart-server! :port 3434)
  (stop-server! :port 3434)

  (require '[ring.middleware.session.store :as store])

  (store/delete-session session-store "57c4b788-f21c-4b9b-9595-077a4e09ac4b")
  (store/read-session session-store "57c4b788-f21c-4b9b-9595-077a4e09ac4b"))
