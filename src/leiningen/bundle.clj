(ns leiningen.bundle
  (:require [leiningen.bundle.util :as util]
            [obr-clj.core :as obr]
            [leiningen.bnd :as bnd]
            [leiningen.pom :as pom]
            [leiningen.deploy :as deploy]
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

(defn deploy-meta
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
    (util/scp-repo updated-repo project)
    (println (str "Updated repository at " remote-obr-url " with metadata for bundle at " local-bundle-url))))

(defn deploy-bundle
  "Deploys the project's bundle to a repository."
  [project]
  (let [id (util/project-identifier project)
        repo (if (pom/snapshot? project)
               "snapshots"
               "releases")
        version (:version project)
        bundle-path (util/bundle-path project)]
    (deploy/deploy project repo id version bundle-path)
    (println (str "Deployed bundle " bundle-path " to \"" repo "\" repository."))))

(defn deploy
  "Deploys the project's bundle to the Maven repository and updates the remote
  OBR with the bundle's meta data."
  [project & args]
  ;; first, make sure the bundle has been built
  (when (not (util/bundle-exists? project))
    (apply bundle-it project args))
  ;; then, deploy the bundle file
  (deploy-bundle project)
  ;; then, update the OBR
  (apply deploy-meta project args))

(defn ^{:subtasks [#'index #'bundle #'deploy-meta #'deploy-bundle #'deploy #'help]}
  bundle
  "Main entry point to the bnd plugin."
  ([project]
     (help))
  ([project subtask & args]
     (case subtask
       "index" (apply obr/index args)
       "bundle" (apply bundle-it project args)
       "deploy-meta" (apply deploy-meta project args)
       "deploy-bundle" (apply deploy-bundle project args)
       "deploy" (apply deploy project args)

       "help" (help))))
