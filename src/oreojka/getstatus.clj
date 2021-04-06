(ns oreojka.getstatus
  (:require [clojure.string :as string]
            [clj-http.client :as http])
  (:import [java.net DatagramSocket InetAddress DatagramPacket])
  (:gen-class))

(def config (-> "resources/config.edn" slurp read-string))

(defn q3-req [command host port]
  (with-open [datagram-socket (DatagramSocket.)]
    (let [inet-address (InetAddress/getByName host)
          port port
          message (byte-array (concat [0xff 0xff 0xff 0xff] (.getBytes command)))
          datagram-packet-req (DatagramPacket. message (count message) inet-address port)
          _ (.send datagram-socket datagram-packet-req)
          datagram-packet-resp (DatagramPacket. (byte-array 65507) 65507)
          _ (.setSoTimeout datagram-socket 1000)
          _ (.receive datagram-socket datagram-packet-resp)]
      (String. (.getData datagram-packet-resp)))))

(defn get-server-status
  [[host port]]
  (q3-req "getstatus" host port))

(defn get-player-count
  [status-response]
  (as-> status-response x
    (string/split x #"\\")
    (last x)
    (string/split x #"\n")
    (drop-last x)
    (rest x)
    (map #(string/split % #" ") x)
    (map last x)
    (count x)))

(defn scan-servers
  [hosts]
  (doall (map #(vector (key %)
                       (try (-> % val get-server-status get-player-count)
                            (catch Exception e e)))
              hosts)))

(defn get-message-embed-fields
  [config scan-result]
  (->> scan-result
       (map #(hash-map :name (format "%s (%s)" (first %) (clojure.string/join #":" (get (:servers config) (first %))))
                       :value (second %)))
       (into [])))

(defn -main
  [& args]
  (let [scan-result (filter #(and (not= 0 (second %))
                                  (not= :error (second %)))
                            (scan-servers (:servers config)))]
    (when (seq scan-result)
      (http/post (:webhook-url config)
                 {:form-params {:content "Players are online"
                                :embeds [{:author {:name "Status Bot"}
                                          :fields (get-message-embed-fields config scan-result)}]}
                  :content-type :json}))))