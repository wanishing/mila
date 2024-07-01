(ns mila.parse
  (:require
    [clojure.test :refer [is]]
    [instaparse.combinators :as c]
    [instaparse.core :as insta]))


(def chrb-setup "/Users/talwanish/dev/chargeback-trainer/setup.py")

(def debug-setup "/Users/talwanish/dev/clojure/mila/debug.py")

(def chrb-str (slurp debug-setup))


(def package-info
  (insta/parser
    "
   package-info = package-name package-qualifier package-version
   package-name = word
   package-qualifier = '=='
   package-version = major '.' minor '.' patch
   major = <number>
   minor = <number>
   patch = <number>
   word = #'[a-zA-Z-]+'
   number = #'[0-9]+'"))


(defn parse
  [grammer input {:keys [start transformations]}]
  (let [parser (insta/parser grammer :start start)]
    (->> input
         parser
         (insta/transform transformations))))


(def package-info-grammer
  {:package-info (c/cat (c/nt :name) (c/nt :qualifier) (c/nt :version))
   :name (c/nt :word)
   :qualifier (c/string "==")
   :version (c/cat
              (c/nt :major)
              (c/hide (c/nt :dot))
              (c/nt :minor)
              (c/hide (c/nt :dot))
              (c/nt :patch))
   :dot  (c/string ".")
   :word (c/hide-tag  (c/regexp "[0-9a-zA-Z-_]*"))
   :major (c/nt :number)
   :minor  (c/nt :number)
   :patch (c/nt :number)
   :number (c/hide-tag  (c/regexp "[0-9]+"))})


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
  (parse package-info-grammer
         input
         {:start :package-info
          :transformations transform-package-info}))


(is (= (parse-package-info "ml_ops-e2e==2.1.34")
       {:name "ml_ops-e2e",
        :qualifier "==",
        :version {:major "2", :minor "1", :patch "34"}}))


(def packages-grammer
  {:packages (c/cat
               (c/nt :package)
               (c/star (c/cat
                         (c/hide (c/star (c/nt :whitespace)))
                         (c/hide (c/nt :comma))
                         (c/hide (c/star (c/nt :whitespace)))
                         (c/nt :package)
                         (c/hide (c/star (c/nt :whitespace))))))
   :package (c/hide-tag (c/cat
                          (c/hide (c/nt :quote))
                          (c/nt :package-info)
                          (c/hide (c/nt :quote))))
   :whitespace (c/regexp "\\s*")
   :space (c/string " ")
   :comma (c/string ",")
   :quote (c/string "'")})


(def transform-packages
  {:packages (fn [& packages]
               {:packages (vec packages)})})


(defn parse-packages
  [input]
  (let [grammer (merge packages-grammer package-info-grammer)
        transformations (merge transform-package-info transform-packages)]
    (parse grammer
           input
           {:start :packages
            :transformations         transformations})))


(let [expected {:packages [{:name "trainer-client",
                            :qualifier "==",
                            :version {:major "1", :minor "2", :patch "3"}}
                           {:name "deepchecks",
                            :qualifier "==",
                            :version {:major "0", :minor "8", :patch "3"}}]}]
  (is (= (parse-packages "'trainer-client==1.2.3','deepchecks==0.8.3'")
         expected))
  (is (= (parse-packages "'trainer-client==1.2.3',           'deepchecks==0.8.3'")
         expected)))


(is (= (parse-packages "'trainer-client==2.2.14',
        'deepchecks==0.8.3',
        'riskipop==2.10.18',
        'mlops_e2e_tools==0.1.17'")
       {}))


(def config-grammer
  {:config (c/cat
             (c/hide (c/nt :packages-key))
             (c/hide           (c/nt :lbracket))
             (c/star (c/nt :packages))
             (c/hide           (c/nt :rbracket)))
   :packages-key (c/cat
                   (c/hide           (c/nt :deps-key))
                   (c/hide           (c/nt :equal)))
   :deps-key (c/string "install_requires")
   :lbracket  (c/string "[")
   :rbracket  (c/string "]")
   :equal (c/string "=")})


(defn parse-config
  [input]
  (let [grammer (merge config-grammer packages-grammer package-info-grammer)
        transformations (merge transform-package-info transform-packages)]
    (parse grammer
           input
           {:start :config
            :transformations         transformations})))


(parse-config "install_requires=['trainer-client==2.2.14','deepchecks==0.8.3','riskipop==2.10.18']")


(parse-config "install_requires=['trainer-client==2.2.14',
        'deepchecks==0.8.3',
        'riskipop==2.10.18',
        'mlops_e2e_tools==0.1.17'
    ]")


(def deps
  (insta/parser
    "deps = setup-tag lbrace setup-content rbrace any*
   setup-tag = 'setup'
   setup-content = item+ ','*  newline*
   item = 'install_requires' '=' '[' dep* ']'
   dep = word qualifier version
   qualifier = ['==']
   version = major '.' minor '.' patch
   major = number
   minor = number
   patch = number
   lbrace = '('
   rbrace = ')'
   word = #'[a-zA-Z | _]+'
   number = #'[0-9]+'
   any = [#'.' | newline | whitespace]*
   whitespace = #'\\s+'
   newline = '\n'"))


(deps chrb-str)
