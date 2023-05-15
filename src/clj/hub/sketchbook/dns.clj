(ns hub.sketchbook.dns
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import
   (java.util HexFormat)
   (java.net DatagramSocket InetAddress)))

(defn hexify [byte-seq]
  (.formatHex (HexFormat/of) byte-seq))
(defn unhexify [hex-str]
  (.parseHex (HexFormat/of) hex-str))


(defn mk-header [m]
  ;; `m` must contain at least `id` is two bytes and `flags` is one byte
  (merge {:num-questions 0 :num-answers 0 :num-authorities 0 :num-additionals 0} m))

(defn int->2-bytes [n]
  [(bit-and (bit-shift-right n 8) 0xFF)
   (bit-and n 0xFF)])

(defn header->bytes [header]
  (byte-array (mapcat int->2-bytes [(:id header)
                                    (:flags header)
                                    (:num-questions header)
                                    (:num-answers header)
                                    (:num-authorities header)
                                    (:num-additionals header)])))

(defn encode-dns-name [domain-name]
  (let [parts (map #(.getBytes % "US-ASCII")
                   (string/split domain-name #"\."))]
    (byte-array
     (concat
      (reduce (fn [acc byte-arr]
                (concat acc [(byte (count byte-arr))] byte-arr))
              [] parts)
      [0x00]))))

(defn question->bytes [question]
  (byte-array (concat (:name question)
                      (mapcat int->2-bytes
                              [(:type question) (:class question)]))))

(def TYPE-A 1)
(def CLASS-IN 1)
(def RECURSION-DESIRED 2r10000000)
(defn build-query [domain-name record-type]
  (byte-array (concat (header->bytes (mk-header {:id            (rand-int 65535)
                                                 :num-questions 1
                                                 :flags         2r10000000}))
                      (question->bytes {:name  (encode-dns-name domain-name)
                                        :type  record-type
                                        :class CLASS-IN}))))

(defn truncate-response [response]
  (byte-array (reverse (drop-while zero? (reverse response)))))

(defn ip-addr->bytes [ip-addr]
  (byte-array (map (comp byte #(Integer/parseInt %))
                   (string/split ip-addr #"\."))))

(defn udp-send [socket query]
  (let [packet (java.net.DatagramPacket. query (count query))]
    (.setData packet query)
    (.send socket packet)))

(defn udp-recv [socket]
  (let [buffer (byte-array 1024)
        packet (java.net.DatagramPacket. buffer 1024)]
    (.receive socket packet)
    (truncate-response buffer)))

(defn perform-dns-query
  "`ip-addr` is a string with ipv4 formatting, e.g. 8.8.8.8
  `port` is an integer and `query` is a byte-array."
  [ip-addr port query]
  (with-open [socket (DatagramSocket.)]  ; a DatagramSocket is how you do UDP in Java
    (.connect socket (InetAddress/getByAddress (ip-addr->bytes ip-addr)) port)
    (udp-send socket query)
    (udp-recv socket)))

(comment
  (hexify
   (perform-dns-query "8.8.8.8" 53
                      (build-query "www.example.com" TYPE-A)))
  )
