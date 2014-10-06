(ns leiningen.bundle
  (:require [leiningen.bundle.util :as util]
            [obr-clj.core :as obr]
            [clojure.java.io :as io]))

(defn- help
  "Prints a help message to standard out."
  []
  (println "usage: lein bundle <task>"))

(defn index
  "Creates a repository index from the bundle jars in the given directory.
  The repository index will be written to the current directory."
  [project & args]
  (let [dir (first args)]
    (obr/index dir)))

(defn deploy-to-obr
  "Adds this project's bundle meta data to the remote OBR."
  [project & args]
  (let [remote-obr (get-in project [:bundle :obr-url]) #_(util/obr-url project)
        bundle-url (util/bundle-url project)
        updated-repo (obr/update-repo remote-obr bundle-url)]
    (util/scp-repo updated-repo project)))

(defn deploy
  "Deploys the project's bundle to the Maven repository and updates the remote
OBR with the bundle's meta data."
  [project & args]
  ;; first, deploy the bundle file
  (let [;;bundle-file (first args)
        ;;bundle-url (io/resource bundle-file)
        ;;resource (obr/create-resource bundle-url)
        ;;
        ;;repo (obr/create-repo remote-obr)
        ]
    (doto (util/bundle-file-path project) prn)))

(defn ^{:subtasks [#'index #'deploy #'deploy-to-obr #'help]}
  bundle
  "Main entry point to the bnd plugin."
  ([project]
     (help))
  ([project subtask & args]
     (case subtask
       "index" (obr/index (first args))
       "deploy" (deploy project args)
       "deploy-to-obr" (deploy-to-obr project args)
       "help" (help))))
