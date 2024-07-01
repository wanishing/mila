 (ns mila.parsers.common.helpers
   (:require
     [clojure.string :as string]
     [instaparse.combinators :as c]
     [instaparse.core :as insta]))


(defn parsable?
  [input]
  (not (or (nil? input) (string/blank? input))))


(defn parse
  [grammar input {:keys [start transformations]}]
  (let [parser (insta/parser grammar :start start :auto-whitespace :standard)
        parsed (->> input
                    parser
                    (insta/transform transformations))]
    (if (insta/failure? parsed)
      (throw (ex-info "Failed to parse"
                      {:input input
                       :error (insta/get-failure parsed)}))
      parsed)))


(def package-info-grammar
  {:package-info (c/cat (c/nt :name) (c/nt :qualifier) (c/nt :version))
   :name         (c/nt :word)
   :qualifier    (c/string "==")
   :version      (c/cat
                   (c/nt :major)
                   (c/hide (c/nt :dot))
                   (c/nt :minor)
                   (c/hide (c/nt :dot))
                   (c/nt :patch))
   :dot          (c/string ".")
   :word         (c/hide-tag (c/regexp "[0-9a-zA-Z-_]*"))
   :major        (c/nt :number)
   :minor        (c/nt :number)
   :patch        (c/nt :number)
   :number       (c/hide-tag (c/regexp "[0-9]+"))})


(def transform-package-info
  {:package-info (fn [name qualifier version]
                   {:name name
                    :qualifier qualifier
                    :version version})
   :name str
   :qualifier str
   :version (comp (partial into {}) list)})


(defn parse-package-info
  [input]
  (when (parsable? input)
    (parse package-info-grammar
           input
           {:start           :package-info
            :transformations transform-package-info})))
