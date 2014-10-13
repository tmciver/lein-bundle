# lein-bundle

A Leiningen plugin to facilitate building and deploying OSGi bundles.  The
intention of this project is to serve as a Clojure substitute for the [Maven
Bundle Plugin](http://felix.apache.org/site/apache-felix-maven-bundle-plugin-bnd.html).

## Usage

Put `[lein-bundle "0.1.0]` into the `:plugins` vector of your project.clj.

Add the following metadata to your project file:

```clojure
:bnd {"Bundle-SymbolicName" ~'com.example.mybundle
      "Bundle-Activator" ~'com.example.mybundle.MyActivator
      "Export-Package" [com.example.mybundle]
      "Import-Package" [org.osgi.framework
                       ;; other packages
                       ]}
:bundle {:obr-url "http://a-host.amazonaws.com:8080/repository.xml"
         :scp {:host "http://a-host.amazonaws.com"
               :repo-path "/var/www/repository.xml"
               :username "username"}}
```

The main task is 'deploy' which deploys the BND-created bundle to the
repository named *releases* in the :repository key of the project file and also
updates the OBR located at :obr-url with the meta data for the bundle.

    $ lein bundle deploy

Note that the implementation uses scp to update the remote OBR and this
requires a properly configured ssh-agent on the development system.

## License

Copyright © 2014 Tim McIver

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
