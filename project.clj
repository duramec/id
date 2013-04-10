(defproject com.duramec/id "0.1.2"
            :description "Duramec id management library"
            :repositories [["internal" {:url "https://repo.duramec.com/internal"
                                        :creds :gpg}]]
            :java-source-paths ["src/java"]
            :javac-options["-target" "1.7"
                           "-source" "1.7"
                           "-deprecation"]
            :dependencies [[com.duramec/time "0.1.3"]])
