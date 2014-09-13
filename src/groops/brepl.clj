(ns groops.brepl
  (:require [net.cgrand.enlive-html :as html]
            [cemerick.austin.repls]))

(defn brepl []
  (let [repl-env (reset! cemerick.austin.repls/browser-repl-env
                         (cemerick.austin/repl-env))]
    (cemerick.austin.repls/cljs-repl repl-env)))

(defn brepl-injection []
  (html/append (html/html
                [:script
                 (cemerick.austin.repls/browser-connected-repl-js)])))
