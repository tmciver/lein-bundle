(ns leiningen.bundle
  (:require [leiningen.bundle.util :as util]
            [obr-clj.core :as obr]
            [leiningen.bnd :as bnd]
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

(defn bundle-it
  "Creates a bundle jar for this project."
  [project & args]
  (bnd/bundle project))

(defn deploy-to-obr
  "Adds this project's bundle meta data to the remote OBR."
  [project & args]
  (when (not (util/bundle-exists? project))
    (apply bundle-it project args))
  (let [remote-obr-url (get-in project [:bundle :obr-url])
        local-bundle-url (util/bundle-url project)
        remote-bundle-url (util/maven-artifact-url project)
        res (-> (obr/create-resource local-bundle-url)
                (obr/set-resource-uri remote-bundle-url))
        updated-repo (-> (obr/create-repo remote-obr-url)
                         (obr/add-resource res))]
    (util/scp-repo updated-repo project)))

(defn deploy
  "Deploys the project's bundle to the Maven repository and updates the remote
  OBR with the bundle's meta data."
  [project & args]
  ;; first, make sure the bundle has been built
  (when (not (util/bundle-exists? project))
    (apply bundle-it project args))
  ;; then, deploy the bundle file
  (util/deploy-bundle project)
  ;; then, update the OBR
  (apply deploy-to-obr project args))

(defn ^{:subtasks [#'index #'bundle #'deploy #'deploy-to-obr #'help]}
  bundle
  "Main entry point to the bnd plugin."
  ([project]
     (help))
  ([project subtask & args]
     (case subtask
       "index" (apply obr/index args)
       "bundle" (apply bundle-it project args)
       "deploy" (apply deploy project args)
       "deploy-to-obr" (apply deploy-to-obr project args)
       "help" (help))))
