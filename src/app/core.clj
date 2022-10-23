(ns app.core
  (:require
    [aleph.http :as http]
    [aleph.netty :as netty]
    [muuntaja.core :as m]
    [reitit.ring :as ring]
    [reitit.ring.middleware.muuntaja :as muuntaja]))


(defonce servers (atom {}))


(defn hello-page
  [request]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "Hello!\nWe are nice to meet you!"})


(defn greeting-message
  [request]
  {:status 200
   :headers {"content-type" "application/json"}
   :body {:message (format "Hello, %s!" (:first-name (:body-params request)))}})


(defn primitive-middleware [handler id]
  (fn [request]
    (handler (update request ::acc (fnil conj []) id))))


(def app
  (ring/ring-handler
    (ring/router
      ["/"
       ["" {:get  {:handler #'hello-page}
            :post {:handler #'greeting-message}}]]
      {:data
       {:muuntaja m/instance
        :middleware [muuntaja/format-middleware]}})))


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


(comment
  @servers
  (start-server! :port 5000)
  (restart-server! :port 4000)
  (stop-server! :port 5000))
