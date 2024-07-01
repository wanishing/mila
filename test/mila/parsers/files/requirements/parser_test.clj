(ns mila.parsers.files.requirements.parser-test
  (:require [clojure.test :as t :refer [deftest is testing]]
            [mila.parsers.files.requirements.parser :refer [parse-requirements]]))

(deftest requirments-file-parsing

  (testing "should parse empty requirement file"
    (is (nil? (parse-requirements "")))
    (is (nil? (parse-requirements nil))))

  (testing "should parse packages"
    (is (=
         (parse-requirements "pyspark==3.2.0\npython-dateutil==2.8.2\npandas==1.4.3\n")
         [{:name "pyspark", :qualifier "==", :version {:major "3", :minor "2", :patch "0"}}
          {:name "python-dateutil", :qualifier "==", :version {:major "2", :minor "8", :patch "2"}}
          {:name "pandas", :qualifier "==", :version {:major "1", :minor "4", :patch "3"}}]))))
