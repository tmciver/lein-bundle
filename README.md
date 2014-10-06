# lein-bundle

A Leiningen plugin to facilitate building and deploying OSGi bundles.

## Usage

Put `[lein-bundle "0.1.0-SNAPSHOT"]` into the `:plugins` vector of your project.clj.

Add the following metadata to your project file:

```clojure
:osgi {:bundle {:remote-obr {:host <host-where-obr-is-located>
                             :port <port> ;; not required; defaults to 80
                             :repo-path <path-to-repository-that-comes-after-host> ;; not required; defaults to "repository.xml"
                             :server-file-path <host-direcotory-where-repository-file-is-located>
                             :username <username-of-ssh-account-on-host>}}
       :bnd {"Bundle-SymbolicName" "com.example.my-bundle"
             "Bundle-Activator" "com.example.my-bundle.Activator"
             "Export-Package" [com.example.my-bundle.api]
             "Import-Package" [...]}}
```

To update the remote OBR with metadata for the project's bundle:

    $ lein bundle deploy-to-obr

Note that the implementation uses scp to update the remote OBR and this
requires a properly configured ssh-agent.

## License

Copyright © 2014 Tim McIver

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
