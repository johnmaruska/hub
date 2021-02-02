FROM clojure:lein

# Create directory for app
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
# Build project and copy into image
COPY project.clj /usr/src/app/
## Dependencies
RUN lein deps
COPY . /usr/src/app
## Uberjar
RUN mv "$(lein uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" app-standalone.jar
# Execute that jar
CMD ["java", "-jar", "app-standalone.jar"]
