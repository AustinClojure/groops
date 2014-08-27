(ns groops.data
  (:require [clojure.set :refer (index)]))

;; Using atoms until datomic is setup
;; registry-set
;; #{ {:user-name <user>
;;     :email-address <email>
;;     :twitter <handle> } }
;; room-set
;; #{ <room> {:user-vect [<user1> <user2>... ]
;;            :msg-vect [<time> {:author <user>
;;                               :message <message>} ] } }

(def registry-set (atom #{}))
(def room-set (atom (sorted-map)))

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
                                :msg-vect (atom (sorted-map))})
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

(defn push-message [room user message]
  (try
    (swap! (:msg-vect (get-room-map room))
           conj { (str (java.util.Date.))
                  {:author user :message message}})
    (catch Exception e (str "push-message exception: " e))))

(defn get-messages [room]
  (try
    (let [msg-vect (deref (:msg-vect (get-room-map room)))
          range-vect (map keyword (map str (range (count msg-vect))))
          vals-vect (vals msg-vect)]
      ;;(reduce conj (sorted-map) (zipmap range-vect vals-vect))
      msg-vect
      )
    (catch Exception e (str "get-messages exception: " e))))

(defn get-users-in-room [room]
  (try
    (if-let [user-vector (:user-vect (get-room-map room))]
      (deref user-vector)
      #{})
    (catch Exception e (str "get-users-in-room exception: " e))))
