(ns leiningen.bundle.util
  (:require [clojure.string :as str]
            [leiningen.jar :as jar]
            [clojure.java.io :as io]
            [obr-clj.core :as obr]
            [clj-ssh.ssh :as ssh]))

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

(defn scp-repo
  "SCPs the given repository data to the given host at the given path."
  [repo project]
  (let [tmp-file (java.io.File/createTempFile "repo-" ".xml")
        {:keys [host repo-path username] :or {:repo-path "repository.xml"}} (get-in project [:bundle :scp])
        repo-path (if (.startsWith repo-path "/") repo-path (str "/" repo-path))]
    ;; write the repo to the temporary file
    (with-open [wrtr (io/writer tmp-file)]
      (obr/write-repo repo wrtr))
    ;; copy the repo to the remote server
    (let [agent (ssh/ssh-agent {})]
      (let [session (ssh/session agent host {:strict-host-key-checking :no :username username})]
        (ssh/with-connection session
          (ssh/scp-to session (str tmp-file) repo-path))))))
