(ns app.core
  (:require
   [aleph.http :as http]
   [aleph.netty :as netty]))


(defonce servers (atom {}))


(defn app
  [_req]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "hello!!"})

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
  (restart-server!)
  (stop-server!))


(deref servers)


{:id :example
 :description "The description to this example resource"
 :summary "An example resource"
 :access-control "..."
 :properties "..."
 :parameters {:query "..." :path "..." :header "..."}
 :produces "..."
 :consumes "..."
 :methods {:get "..." :put "..." :post "..." :delete "..." :patch "..."}
 :responses {,,,}
 :path-info? false
 :sub-resource "..."
 :logger "..."
 :interceptor-chain "..."
 :error-interceptor-chain "..."
 :custom/other "..."}
