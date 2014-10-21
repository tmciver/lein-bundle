(ns leiningen.bundle.util
  (:require [clojure.string :as str]
            [leiningen.jar :as jar]
            [leiningen.pom :as pom]
            [clojure.java.io :as io]
            [obr-clj.core :as obr]
            [clj-ssh.ssh :as ssh]))

(defn deploy-repo
  "Returns the URL (as string) of the deploy repository (either \"releases\" or
  \"snapshots\") for the project"
  [project]
  (let [repo-type (if (pom/snapshot? project)
                             "snapshots"
                             "releases")]
    (->> (:repositories project)
         (filter #(= (first %) repo-type))
         first
         second
         :url)))

(defn maven-artifact-url
  "Returns a string of the URL to the artifact."
  ([repo-path group-id artifact-id version]
   (let [group-path (->> (str/split group-id #"\.")
                         (interpose "/")
                         (apply str))
         jar-name (str artifact-id "-" version ".jar")]
     (->> [repo-path group-path artifact-id version jar-name]
          (interpose "/")
          (apply str))))
  ([project]
   (let [repo-path (deploy-repo project)
         group-id (:group project)
         artifact-id (:name project)
         version (:version project)]
     (maven-artifact-url repo-path group-id artifact-id version))))

(defn uberjar-name
  "Returns a string of the uberjar name."
  [project]
  (str (:name project) "-" (:version project) "-standalone.jar"))

(defn bundle-path
  "Returns a string of the absolute path to the bundle file."
  [project]
  ;; Currently, the bundle file has the same name as the uberjar and is located in the project root.
  ;; This will likely change in the future.
  (str (:root project) (java.io.File/separator) (uberjar-name project)))

(defn bundle-exists?
  "Returns true if the bundle jar file exists, false otherwise."
  [project]
  (.exists (java.io.File. (bundle-path project))))

(defn bundle-url
  "Returns this project's bundle URL as string."
  [project]
  (str "file://" (bundle-path project)))

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

(defn project-identifier
  "Returns a string of the form \"<group-id>/<project-name>\""
  [project]
  (str (:group project) "/" (:name project)))

(defn backup-repo
  "Writes the given Repository to the directory ~/.lein/lein-bundle/repo-backups."
  [repo]
  (let [user-home (System/getProperty "user.home")
        backup-dir (java.io.File. (apply str (interpose (java.io.File/separator)
                                                        [user-home ".lein" "lein-bundle" "repo-backups"])))
        repo-name (or (.getName repo) "unnamed")
        backup-name (str repo-name "-" (System/currentTimeMillis) ".xml")
        backup-file (java.io.File. backup-dir backup-name)]
    ;; create backup directory
    (.mkdirs backup-dir)
    ;; create the file
    (.createNewFile backup-file)
    (with-open [wrtr (io/writer backup-file)]
      (obr/write-repo repo wrtr))))


