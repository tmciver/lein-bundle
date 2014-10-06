# lein-bundle

A Leiningen plugin to facilitate building and deploying OSGi bundles.  The
intention of this project is to serve as a Clojure substitute for the [Maven
Bundle Plugin](http://felix.apache.org/site/apache-felix-maven-bundle-plugin-bnd.html).

## Usage

Put `[lein-bundle "0.1.0-SNAPSHOT"]` into the `:plugins` vector of your project.clj.

Add the following metadata to your project file:

```clojure
:bundle {:obr-url "http://a-host.amazonaws.com:8080/repository.xml"
         :scp {:host "http://a-host.amazonaws.com"
               :repo-path "/var/www/repository.xml"
               :username "user"}}
:osgi {:bnd {"Bundle-SymbolicName" "com.example.my-bundle"
             "Bundle-Activator" "com.example.my-bundle.Activator"
             "Export-Package" [com.example.my-bundle.api]
             "Import-Package" [...]}}
```

To update the remote OBR with metadata for the project's bundle:

    $ lein bundle deploy-to-obr

Note that the implementation uses scp to update the remote OBR and this
requires a properly configured ssh-agent on the development system.

## License

Copyright © 2014 Tim McIver

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
