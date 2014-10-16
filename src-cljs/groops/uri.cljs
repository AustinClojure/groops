(ns groops.uri
  (:require  [clojure.string :as string]
             [cljs-hash.md5 :as md5]))

(defn email-hash [email]
  (if email
    (-> email
        (string/trim)
        (string/lower-case)
        (md5/md5)
        (str))
    "00000000000000000000000000000000"))

(defn gravatar-uri [email]
  (str "http://www.gravatar.com/avatar/" (email-hash email)))

(defn twitter-uri [username]
  (when (not-empty username)
    (str "https://twitter.com/"
         (-> username
             (string/trim)
             (string/replace "@" "")))))

(defn decode-uri [uri]
  (js/decodeURIComponent uri))
