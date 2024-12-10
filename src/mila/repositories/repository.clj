(ns mila.repositories.repository
  (:require
    [babashka.fs :as fs]
    [clojure.string :as string]))


(defn make-repository
  [{:keys [workspace]} {:keys [url]}]
  (let [repo-name (-> url
                      fs/file-name
                      (string/replace #".git" ""))
        local-path (->> repo-name
                        (fs/path workspace)
                        fs/expand-home
                        str)]
    {:name repo-name
     :url url
     :local-path local-path}))


(comment
  (def workspace "/tmp")
  (def config {:workspace workspace})
  (def ssh-url "git@github.com:psf/requests.git")
  (make-repository config {:url ssh-url}))
