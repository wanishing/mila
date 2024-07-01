(ns mila.parsers.files.setuppy.parser
  (:require
    [clojure.string :as string]
    [instaparse.combinators :as c]
    [instaparse.core :as insta]))


(defn- valid-input?
  [input]
  (not (or (nil? input) (string/blank? input))))


(defn- parse
  [grammar input {:keys [start transformations]}]
  (let [parser (insta/parser grammar :start start :auto-whitespace :standard)
        parsed     (->> input
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
   :word         (c/hide-tag  (c/regexp "[0-9a-zA-Z-_]*"))
   :major        (c/nt :number)
   :minor        (c/nt :number)
   :patch        (c/nt :number)
   :number       (c/hide-tag  (c/regexp "[0-9]+"))})


(def transform-package-info
  {:package-info (fn [name qualifier version]
                   {:name      name
                    :qualifier qualifier
                    :version   version})
   :name         str
   :qualifier    str
   :version      (comp (partial into {}) list)})


(defn parse-package-info
  [input]
  (when (valid-input? input)
    (parse package-info-grammar
           input
           {:start           :package-info
            :transformations transform-package-info})))


(def packages-grammar
  {:packages   (c/cat
                 (c/nt :package)
                 (c/star (c/cat
                           (c/hide (c/nt :comma))
                           (c/nt :package))))
   :package    (c/hide-tag (c/cat
                             (c/hide (c/nt :quote))
                             (c/nt :package-info)
                             (c/hide (c/nt :quote))))
   :comma      (c/string ",")
   :quote      (c/string "'")})


(def transform-packages
  {:packages (comp vec list)})


(defn parse-packages
  [input]
  (let [grammar         (merge packages-grammar
                               package-info-grammar)
        transformations (merge transform-packages
                               transform-package-info)]
    (if (valid-input? input)
      (parse grammar
             input
             {:start           :packages
              :transformations transformations})
      [])))


(def config-grammar
  {:config (c/cat
             (c/hide (c/nt :packages-key))
             (c/hide (c/nt :lbracket))
             (c/star (c/nt :packages))
             (c/hide (c/nt :rbracket))
             (c/hide (c/nt :any)))
   :packages-key (c/cat
                   (c/hide           (c/nt :deps-key))
                   (c/hide           (c/nt :equal)))
   :deps-key     (c/string "install_requires")
   :lbracket     (c/string "[")
   :rbracket     (c/string "]")
   :any-but-rbracket (c/regexp "[^]]*")
   :any          (c/regexp "[.\n]*")
   :equal        (c/string "=")})


(defn parse-config
  [input]
  (let [grammar         (merge config-grammar
                               packages-grammar
                               package-info-grammar)
        transformations (merge transform-packages
                               transform-package-info)]
    (if (valid-input? input)
      (let [parsed  (parse grammar
                           input
                           {:start           :config
                            :transformations transformations})
            [_ result] parsed]
        (or result []))
      [])))


(defn- find-range
  [input]
  (when (valid-input? input)
    (let [index-of (fn [reg input]
                     (let [m (re-matcher reg input)]
                       (when (.find m)
                         (.start m))))
          start-idx (index-of #"install_requires" input)
          end-idx (when start-idx
                    (index-of #"]" (subs input start-idx)))]
      (when (and start-idx end-idx)
        [start-idx, (+ start-idx end-idx 1)]))))


(defn parse-setup-py
  [input]
  (if-let [[start, end] (find-range input)]
    (parse-config (subs input start end))
    []))
