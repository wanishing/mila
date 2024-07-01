(ns mila.parsers.files.poetry.parser
  (:require
   [instaparse.combinators :as c]
   [mila.parsers.common.helpers :refer [parsable?
                                        parse]]))

(def pyproject-grammar
  {:body (c/hide-tag
          (c/cat
           (c/hide (c/star (c/nt :newline)))
           (c/plus (c/nt :section))))
   :section (c/cat
             (c/hide (c/star (c/nt :comment)))
             (c/hide-tag (c/nt :section-tag))
             (c/star (c/alt
                      (c/hide (c/nt :comment))
                      (c/hide-tag (c/nt :assignment)))))
   :section-tag (c/cat
                 (c/hide (c/nt :lbrace))
                 (c/nt :section-terms)
                 (c/hide (c/nt :rbrace))
                 (c/hide (c/star (c/nt :newline))))
   :section-terms (c/cat
                   (c/plus (c/nt :term))
                   (c/star (c/nt :term)))
   :term (c/regexp "[a-zA-Z_.]+")
   :assignment (c/cat
                (c/nt :lvalue)
                (c/hide (c/nt :equal))
                (c/plus (c/nt :const))
                (c/hide (c/plus (c/nt :newline))))
   :equal (c/string "=")
   :lbrace (c/string "[")
   :rbrace (c/string "]")
   :lvalue (c/hide-tag (c/regexp "[a-zA-Z][a-zA-Z0-9_]*"))
   :const (c/hide-tag (c/regexp "\\S+"))
   :comment (c/cat
             (c/nt :hash)
             (c/nt :any))
   :hash (c/string "#")
   :any (c/regexp ".+?\\n+")
   :newline (c/string "\n")})


(def transform-options {:lvalue keyword
                        :section-terms (partial apply str)})


(defn parse-pyproject [input]
  (when (parsable? input)
    (let [parsed (parse pyproject-grammar
                        input
                        {:start :body
                         :transformations transform-options})]
      (vec parsed))))

(comment

  (def pyproject-content
    "[tool.poetry]\nname = \"dummy-project\"\nversion = \"0.1.0\"\ndescription = \"\"\nauthors = [\"wanishing <tal.vanish@gmail.com>\"]\nreadme = \"README.md\"\n\n[tool.poetry.dependencies]\npython = \"^3.12\"\npyspark = \"3.2.0\"\npython-dateutil = \"2.8.2\"\npandas = \"1.4.3\"\n\n[build-system]\nrequires = [\"poetry-core\"]\nbuild-backend = \"poetry.core.masonry.api\"\n")

  (def debug
    "[tool.poetry]\nname = \"dummy-project\"\n")

  (instaparse.core/parser pyproject-grammar :start :body)
  (parse-pyproject pyproject-content))
