(ns gpt2.hugging-face
  (:require [libpython-clj2.require :refer [require-python]]
            [libpython-clj2.python :refer [py. py.. py.-] :as py]))

(py/from-import transformers GPT2Tokenizer GPT2LMHeadModel)
(require-python 'torch)

(def tokenizer (py. GPT2Tokenizer from_pretrained "gpt2"))

(def text "Who was Jim Henson? Jim Henson was a" )

(def indexed-tokens (py. tokenizer encode text))

(def tokens-tensor (py. (torch/tensor [indexed-tokens]) to "cuda" ))


(def model (py. GPT2LMHeadModel from_pretrained "gpt2"))
(py. model to "cuda")

(def tokens-tensor (py. tokens-tensor to "cuda"))

(def predictions (py/with [r (torch/no_grad)]
                   (last (py. (model tokens-tensor) to_tuple))))

(def predicted-index (let [last-word-predictions (-> predictions first last)
                           arg-max (torch/argmax last-word-predictions)]
                       (py. arg-max item)))

(def predicted-text (py. tokenizer decode (-> (into [] indexed-tokens) 
                                              (conj predicted-index))))

;;longer sequences
(def generated (into [] (py. tokenizer encode "The manhattan bridge" )))
(def context (py. (torch/tensor [generated]) to "cuda" ))

(defn generate-sequence-step
  [{:keys [generated-tokens context past]}]
  (let [[output past] (py. (model context :past_key_values past) 
                           to_tuple)
        token (-> (torch/argmax (first output)))
        new-generated (conj generated-tokens (py. token tolist))]
    {:generated-tokens new-generated
     :context (py. token unsqueeze 0)
     :past past
     :token token}))

(defn decode-sequence
  [{:keys [generated-tokens]}]
  (println "decode" )
  (py. tokenizer decode generated-tokens))

#_(loop [step {:generated-tokens generated
             :context context
             }
       i 100]
  (println i)
  (if (pos? i)
    (recur (generate-sequence-step step) (dec i))
    (decode-sequence step)))

(defn generate-text
  [starting-text count-predicted-words gpu?]
  (let [tokens (into [] (py. tokenizer encode starting-text))
        context (cond-> 
                  (torch/tensor [tokens])
                  gpu? (py. to "cuda"))
        prediction (reduce (fn [r i] (generate-sequence-step r)) {:generated-tokens tokens 
                                                                  :context context} 
                           (range count-predicted-words))]
    (decode-sequence prediction)))

(generate-text "The Manhattan bridge" 100 true)
(generate-text "Hugging Face, Inc. is an American company that develops tools for building applications using machine learning" 20 true)

(generate-text "Clojure is a dynamic, general purpose programming language, combining the approachability and interactive" 20 true)
