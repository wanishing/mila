(ns mila.repositories.repository-test
  (:require
   [clojure.test :as t :refer [deftest is testing]]
   [matcher-combinators.test]
   [mila.repositories.repository :refer [make-repository]]))

(def workspace "/tmp")
(def config {:workspace workspace})
(def ssh-url "git@github.com:psf/requests.git")
(def https-url "https://github.com/psf/requests.git")
(def repo-name "requests")

(deftest repository-metadata

  (testing "should resolve repository name by given url"
    (is (match? {:name repo-name}
                (make-repository config {:url ssh-url})))
    (is (match? {:name repo-name}
                (make-repository config {:url https-url}))))

  (testing "should return path to local workspace"
    (is (match? {:local-path (str workspace "/" repo-name)}
                (make-repository config {:url ssh-url}))))

  (testing "should return given url"
    (is (match? {:url ssh-url}
                (make-repository config {:url ssh-url})))))
