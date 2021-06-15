FROM clojure:lein

# Create directory for app
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
# Build project and copy into image
COPY project.clj /usr/src/app/
## Dependencies
RUN lein deps
COPY . /usr/src/app
# Execute that jar
CMD ["lein", "run", "server"]
