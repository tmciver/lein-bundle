(ns leiningen.bundle
  (:require [leiningen.bundle.util :as util]
           ;; [leiningen.bundle.http :as http]
            [clj-ssh.ssh :as ssh]
            [obr-clj.core :as obr]
            [clojure.java.io :as io]
            [leiningen.pom :as pom]))

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

(defn- scp-repo
  "SCPs the given repository data to the given host at the given path."
  [repo project]
  (let [tmp-file (java.io.File/createTempFile "repo-" ".xml")
        {:keys [host server-file-path repo-path username] :or {:repo-path "repository.xml"}} (get-in project [:osgi :bundle :remote-obr])
        _ (println host)]
    ;; write the repo to the temporary file
    (with-open [wrtr (io/writer tmp-file)]
      (obr/write-repo repo wrtr))
    ;; post file to the given url
    #_(doto (http/post url {:body (io/input-stream tmp-file)}) prn)
    (let [agent (ssh/ssh-agent {})]
      (let [session (ssh/session agent host {:strict-host-key-checking :no :username username})]
        (ssh/with-connection session
          (ssh/scp-to session (str tmp-file) (str server-file-path "/scpd-repo.xml")))))
    #_(let [session (ssh/session host :strict-host-key-checking :no :username username)]
        (ssh/scp-to session tmp-file server-file-path))))

(defn deploy-to-obr
  "Adds this project's bundle meta data to the remote OBR."
  [project & args]
  (let [remote-obr (util/obr-url project)
        bundle-url (util/bundle-url project)
        updated-repo (obr/update-repo remote-obr bundle-url)]
    (scp-repo updated-repo project)
    #_(with-open [wrtr (io/writer "updated-repo.xml")]
      (obr/write-repo updated-repo wrtr))
    #_(-> (doto (obr/update-repo remote-obr bundle-url) (#(prn (type %))))
        (http/post-repo remote-obr))))

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
