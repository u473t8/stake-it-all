(ns app.core
  (:require
   [yada.yada :as yada]))


(defonce servers (atom {}))

(def greeting
  (yada/yada "Hello!\nIt is nice to meet you!"))

(def app ["/" [["" #'greeting]]])

(defn get-config
  [server]
  (dissoc server :server :close))

(defn start-server!
  [& {:keys [port] :or {port 8888}}]
  (let [server (yada/listener app {:port port})]
    (swap! servers assoc port server)
    {:started (get-config server)}))

(defn stop-server!
  [& {:keys [port] :or {port 8888}}]
  (when-let [server (get @servers port)]
    (swap! servers dissoc port)
    ((:close server))
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
  (restart-server!)
  (stop-server!))

