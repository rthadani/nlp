(ns spam.nlp
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk.webserver :as webserver] 
            [nextjournal.beholder :as beholder] 
            [scicloj.ml.core :as ml]
            [scicloj.ml.metamorph :as mm]
            [scicloj.ml.dataset :as ds]
            [aerial.hanami.templates :as ht]
            [aerial.hanami.common :as hc]
            [aerial.hanami.core :as hci]
            [tablecloth.api :as tc]))

(comment  (webserver/serve! {:port 7777})
          (clerk/show! "src/spam/nlp.clj")
          (beholder/watch #(clerk/file-event %) "src"))

(def dataset 
  (tc/dataset  "resources/data/emails.csv" {:key-fn keyword}))

(clerk/table dataset)


;(clerk/vl (hc/xform (ht/bar-chart) ))


(ds/select-columns dataset [:spam])
(defn foo
  "I don't do a whole lot."
  [x]
  (prn x "Hello, World!"))
