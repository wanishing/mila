(ns mila.parsers.files.poetry.parser-test
  (:require [clojure.test :as t :refer [deftest is testing]]
            [mila.parsers.files.poetry.parser :refer [parse-pyproject]]))

(def pyproject-content
  "[tool.poetry]\nname = \"dummy-project\"\nversion = \"0.1.0\"\ndescription = \"\"\nauthors = [\"wanishing <tal.vanish@gmail.com>\"]\nreadme = \"README.md\"\n\n[tool.poetry.dependencies]\npython = \"^3.12\"\npyspark = \"3.2.0\"\npython-dateutil = \"2.8.2\"\npandas = \"1.4.3\"\n\n[build-system]\nrequires = [\"poetry-core\"]\nbuild-backend = \"poetry.core.masonry.api\"\n")

(deftest pyproject-file-parsing

  (testing "should parse empty pyproject file"
    (is (nil? (parse-pyproject "")))
    (is (nil? (parse-pyproject nil))))

  (testing "should parse packages"
    (is (=
         (parse-pyproject pyproject-content)
         [{:name "pyspark", :qualifier "==", :version {:major "3", :minor "2", :patch "0"}}
          {:name "python-dateutil", :qualifier "==", :version {:major "2", :minor "8", :patch "2"}}
          {:name "pandas", :qualifier "==", :version {:major "1", :minor "4", :patch "3"}}]))))
