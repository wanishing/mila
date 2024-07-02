(ns mila.repositories.repository-test
  (:require
   [clojure.test :as t :refer [deftest is testing]]
   [mila.repositories.repository :refer [make-repository]]))

(def workspace "/tmp")
(def config {:workspace workspace})
(def ssh-url "git@github.com:psf/requests.git")
(def https-url "https://github.com/psf/requests.git")
(def repo-name "requests")

(deftest repository-metadata

  (testing "should resolve repository name by given url"
    (is (= (:name (make-repository config {:url ssh-url}))
           repo-name))
    (is (= (:name (make-repository config {:url https-url}))
           repo-name)))

  (testing "should return path to local workspace"
    (is (= (:local-path (make-repository config {:url ssh-url}))
           (str workspace "/" repo-name))))

  (testing "should return given url"
    (is (= (:url (make-repository config {:url ssh-url}))
           ssh-url))))
