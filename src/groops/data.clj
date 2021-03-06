(ns groops.data
  (:require [clojure.set :refer (index)]
            [groops.async :as async]))

;; Using atoms until datomic is setup
;; registry-set
;; #{ {:user-name <user>
;;     :email-address <email>
;;     :twitter <handle> } }
;; room-set
;; #{ <room> {:user-vect [<user1> <user2>... ]
;;            :msg-vect [<time> {:author <user>
;;                               :message <message>} ] } }

(defonce registry-set (atom #{}))
(defonce room-set (atom (sorted-map)))

(defn register-user [user email handle]
  (try
    (let [new-user {:user-name user :email-address email :twitter handle}]
      (swap! registry-set conj {:user-name user
                                :email-address email
                                :twitter handle})
      {:user-name user :email-address email :twitter handle})
    (catch Exception e (str "register-user exception: " e))))

(defn create-room [room]
  (try
    (swap! room-set assoc room {:user-vect (atom (sorted-set))
                                :msg-vect (atom (sorted-map))
                                :msg-count (atom 0)})
    (catch Exception e (str "create-room exception: " e))))

(defn get-rooms-list []
  (keys @room-set))

(defn get-room-map [room]
  (try
    (get @room-set room)
    (catch Exception e (str "get-room-map exception: " e))))

(defn get-room-user-count [room]
  (try
    (count (deref (:user-vect (get-room-map room))))
    (catch Exception e (str "get-room-user-count exception: " e))))

(defn get-room-count-list []
  (try
    (let [names (get-rooms-list)
          counts (map get-room-user-count names)]
      (reverse (zipmap names counts)))
    (catch Exception e (str "get-room-count-map exception: " e))))

(defn get-room-count-map []
  (try
    (let [names (get-rooms-list)
          counts (map get-room-user-count names)]
      (reduce conj (sorted-map) (zipmap names counts)))
    (catch Exception e (str "get-room-count-map exception: " e))))

(defn add-user-to-room [room user-name]
  (try
    (swap! (:user-vect (get-room-map room)) conj user-name)
    (catch Exception e (str "add-user-to-room exception: " e))))

(defn remove-user-from-room [room user-name]
  (try
    (swap! (:user-vect (get-room-map room)) disj user-name)
    (catch Exception e (str "remove-user-from-room exception: " e))))

(defn push-message [room user message gravatar-uri]
  (let [room-map (get-room-map room)
        message-num (deref (:msg-count room-map))
        message-map {(str message-num)
                     {:author user 
                      :message message 
                      :gravatar-uri gravatar-uri
                      :time-posted (str (java.util.Date.))
                      :msg-number message-num}}]
    (swap! (:msg-vect room-map) conj message-map)
    (swap! (:msg-count room-map) inc)
    (async/send-message message-map room)))

(defn get-messages [room]
  (try
    (deref (:msg-vect (get-room-map room)))
    (catch Exception e (str "get-messages exception: " e))))

(defn get-users-in-room [room]
  (try
    (if-let [user-vector (:user-vect (get-room-map room))]
      (deref user-vector)
      #{})
    (catch Exception e (str "get-users-in-room exception: " e))))
