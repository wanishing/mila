(ns mila.parsers.files.requirements.parser
  (:require
    [instaparse.combinators :as c]
    [mila.parsers.common.helpers :refer [package-info-grammar
                                         transform-package-info
                                         parsable?
                                         parse]]))


(def requirements-grammar
  {:packages (c/hide-tag
               (c/cat
                 (c/nt :package-info)
                 (c/star
                   (c/cat (c/hide (c/nt :newline))
                          (c/nt :package-info)))))

   :newline (c/string "\n")})


(defn parse-requirements
  [input]
  (let [grammar (merge requirements-grammar
                       package-info-grammar)
        transformations transform-package-info]

    (when (parsable? input)
      (let [parsed (parse grammar
                          input
                          {:start :packages
                           :transformations transformations})]
        (vec parsed)))))
