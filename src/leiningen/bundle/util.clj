(ns leiningen.bundle.util
  (:require [clojure.string :as str]
            [leiningen.jar :as jar]))

(defn maven-artifact-url
  "Returns a string of the URL to the artifact for the given repository path
and artifact metadata."
  [repo-path group-id artifact-id version]
  (let [group-path (->> (str/split group-id #"\.")
                        (interpose "/")
                        (apply str))
        jar-name (str artifact-id "-" version ".jar")]
    (->> [repo-path group-path artifact-id version jar-name]
         (interpose "/")
         (apply str))))

(defn uberjar-name
  "Returns a string of the uberjar name."
  [project]
  (str (:name project) "-" (:version project) "-standalone.jar"))

(defn bundle-file-path
  "Returns a string of the absolute path to the bundle file."
  [project]
  ;; Currently, the bundle file has the same name as the uberjar and is located in the project root.
  ;; This will likely change in the future.
  (str (:root project) (java.io.File/separator) (uberjar-name project)))

(defn bundle-url
  "Returns this project's bundle URL as string."
  [project]
  (str "file://" (bundle-file-path project)))

(defn obr-url
  "Returns the OBR URL constructed from the project metadata."
  [project]
  (let [{:keys [host port repo-path] :or {:repo-path "repository.xml"}} (get-in project [:osgi :bundle :remote-obr])
        host (if (.startsWith host "http") host (str "http://" host))
        port (if (= port "80") "" port)
        repo-path (if (.startsWith repo-path "/") repo-path (str "/" repo-path))]
    (str host ":" port repo-path)))
