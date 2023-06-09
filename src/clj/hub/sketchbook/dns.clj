(ns hub.sketchbook.dns
  "Implement DNS by Julia Evans https://implement-dns.wizardzines.com/"
  (:require
   [clojure.string :as string])
  (:import
   (java.io File RandomAccessFile)
   (java.net DatagramSocket InetAddress)
   (java.util HexFormat)))

;;;; Helpers

(defn hexify [byte-seq]
  (.formatHex (HexFormat/of) byte-seq))
(defn unhexify [hex-str]
  (.parseHex (HexFormat/of) hex-str))

(defn int->byte-pair [n]
  [(bit-and (bit-shift-right n 8) 0xFF)
   (bit-and n 0xFF)])

(defn bytes->long [bytes]
  (when (< 4 (count bytes))
    (throw (ex-info "Integer should have no more than 4 bytes" {:bytes bytes})))
  (long
   (reduce (fn [acc b]
             (bit-or (bit-shift-left (bit-and acc 0xFFFFFFFF) 8) (bit-and b 0xFF)))
           0x00 bytes)))

(defn ip-addr->bytes [ip-addr]
  (byte-array (map (fn [x] (bit-and (Integer/parseInt x) 0xFF))
                   (string/split ip-addr #"\."))))

(defn bytes->ip-addr [bytes]
  (string/join "." (map #(Integer/toString %) bytes)))

(defn truncate-response [response]
  (byte-array (reverse (drop-while zero? (reverse response)))))

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

;;;; Constants

(def TYPE-A 1)
(def TYPE-NS 2)
(def TYPE-TXT 16)

(def CLASS-IN 1)

(def RECURSION-DESIRED 2r10000000)

;;;; Part 1

(defrecord DnsHeader
    [id flags num-questions num-answers num-authorities num-additionals])
(defrecord DnsQuestion
    [name type class])

(defn mk-header [m]
  ;; `m` must contain at least `id` is two bytes and `flags` is one byte
  (map->DnsHeader
   (merge {:num-questions 0 :num-answers 0 :num-authorities 0 :num-additionals 0}
          m)))

(defn header->bytes [^DnsHeader header]
  (byte-array (mapcat int->byte-pair [(:id header)
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
                      (mapcat int->byte-pair [(:type question) (:class question)]))))

(defn build-query [domain-name record-type flags]
  (byte-array (concat (header->bytes (mk-header {:id            0x8298 #_ (rand-int 65535)
                                                 :num-questions 1
                                                 :flags         flags}))
                      (question->bytes (map->DnsQuestion
                                        {:name  (encode-dns-name domain-name)
                                         :type  record-type
                                         :class CLASS-IN})))))

;;;; Part 2

(defn bytes->random-access-file
  "We use a RandomAccessFile for read() and seek() operations that don't exist on
  streams or arrays. Convert bytes into this class."
  [bytes-data]
  (let [file (File/createTempFile "dns" "dat")
        raf  (RandomAccessFile. file "rw")]
    (.write raf bytes-data)
    (.seek raf 0)
    raf))

(defn read-n [n reader]
  (vec (repeatedly n #(.read reader))))

(defn parse-header [reader]
  (apply ->DnsHeader (map bytes->long (partition 2 (read-n 12 reader)))))

(declare decode-compressed-name)
(defn decode-name [reader]
  (let [parts (loop [parts []]
                (let [length (.read reader)]
                  (cond
                    (= -1 length)
                    (throw (ex-info "Could not decode name. No length byte remaining."
                                    {:parts parts}))

                    (zero? length)
                    parts

                    ;; leading 2-bits 2r11 mean it's compressed
                    (= 2r11000000 (bit-and length 2r11000000))
                    (decode-compressed-name length reader)

                    :else
                    (recur (vec (concat parts [(read-n length reader)]))))))]
    (string/join "." (map #(apply str (map char %)) parts)))  )

(defn decode-compressed-name [length ^RandomAccessFile reader]
  (let [pointer     (bytes->long [(byte (bit-and length 2r00111111)) (.read reader)])
        current-pos (.getFilePointer reader)
        _           (.seek reader pointer)
        result      (decode-name reader)]
    (.seek reader current-pos)
    result))

(defn parse-question [reader]
  (let [name  (decode-name reader)
        type  (bytes->long (read-n 2 reader))
        class (bytes->long (read-n 2 reader))]
    (->DnsQuestion name type class)))

(defrecord DnsRecord
    [name type class ttl data])

(defn parse-record [^RandomAccessFile reader]
  (let [name_    (decode-name reader)
        type_    (bytes->long (read-n 2 reader))
        class_   (bytes->long (read-n 2 reader))
        ttl      (bytes->long (read-n 4 reader))
        data-len (bytes->long (read-n 2 reader))
        data     (cond
                   (= TYPE-NS type_)
                   (decode-name reader)

                   (= TYPE-A type_)
                   (bytes->ip-addr (read-n data-len reader))

                   :else
                   (read-n data-len reader))]
    (->DnsRecord name_ type_ class_ ttl data)))

(defrecord DnsPacket
    [header questions answers authorities additionals])

(defn parse-dns-packet [bytes-data]
  (let [reader      (bytes->random-access-file bytes-data)
        header      (parse-header reader)
        questions   (vec (repeatedly (:num-questions header) #(parse-question reader)))
        answers     (vec (repeatedly (:num-answers header) #(parse-record reader)))
        authorities (vec (repeatedly (:num-authorities header) #(parse-record reader)))
        additionals (vec (repeatedly (:num-additionals header) #(parse-record reader)))]
    (->DnsPacket header questions answers authorities additionals)))

(defn lookup-domain [domain-name]
  (let [query (build-query domain-name TYPE-A RECURSION-DESIRED)]
    (-> (perform-dns-query "8.8.8.8" 53 query)
        parse-dns-packet :answers first :data bytes->ip-addr)))

;;;; Part 3: Implement our resolver

(defn send-query [ip-addr domain-name record-type]
  (let [query (build-query domain-name record-type 0)]
    (parse-dns-packet (perform-dns-query ip-addr 53 query))))

(defn get-answer [packet]
  (:data (first (filter #(= (:type %) TYPE-A) (:answers packet)))))

(defn get-nameserver-ip [packet]
  (:data (first (filter #(= (:type %) TYPE-A) (:additionals packet)))))

(defn get-nameserver [packet]
  (:data (first (filter #(= (:type %) TYPE-NS) (:authorities packet)))))

(defn resolve-domain [domain-name record-type]
  (loop [nameserver "198.41.0.4"]
    (println "Querying" nameserver "for" domain-name)
    (let [response (send-query nameserver domain-name record-type)]
      (cond
        (get-answer response)        (get-answer response)
        (get-nameserver-ip response) (recur (get-nameserver-ip response))
        (get-nameserver response)    (recur (resolve-domain (get-nameserver response) TYPE-A))
        :else                        (throw (ex-info "Something went wrong" response))))))
