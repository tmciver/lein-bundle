(defproject lein-bundle "0.1.1-SNAPSHOT"
  :description "A Leiningen plugin to facilitate creation and deployment of
  OSGi bundles."
  :url "https://github.com/tmciver/lein-bundle"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  :dependencies [[obr-clj "0.1.0"]
                 [lein-bnd "0.1.0"]
                 [clj-ssh "0.5.11"]]
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy" "clojars"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])
