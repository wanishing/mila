(ns mila.parsers.parser
  (:require
    [mila.parsers.files.setuppy.parser :refer [parse-setup-py]]))


(defmulti parse-deps :file)


(defmethod  parse-deps :setuppy
  [{:keys [location]}]
  (-> location
      slurp
      parse-setup-py))


(defmethod parse-deps :requirements
  [{:keys [location]}]
  (-> location
      slurp
      parse-setup-py))


(defmethod parse-deps :poetry
  [{:keys [location]}]
  (-> location
      slurp
      parse-setup-py))
