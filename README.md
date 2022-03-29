# Clojure Hub

I'm just going to put all my side-project Clojure stuff into this
catch-all project until each part grows large enough to justify being
split into their own specific thing. Secondary motivation is to run
everything as a single EC2 instance without bothering trying to set up
dependencies between repos because ehhh...

## Pre-Requisites / Tech Stack

- Java (v1.8, others untested)
```
$ java -version
openjdk version "1.8.0_272"
OpenJDK Runtime Environment (build 1.8.0_272-b10)
OpenJDK 64-Bit Server VM (build 25.272-b10, mixed mode)
```

- Leiningen (others untested)
```
$ lein -v
Leiningen 2.9.1 on Java 1.8.0_272 OpenJDK 64-Bit Server VM
```

- Docker

## Working Pieces / How to run

Webserver and Discord bot
```
lein run server
```

Spotify artifact generators
```
lein run spotify
```

mp3 file metadata tag correction
```
lein run id3
```

Or any combination of above, e.g.
```
lein run server spotify id3
```


## Components to add, written somewhere else already

1. Pokemon utilities
   - PokeJobs assignments
1. Dice Bag
1. Advent of Code exercises

## Conway's Game of Life

Small Stuff:
- implement inputs, either filename or randomize given dimensions +
  density

## Discord bot

- Set up always-on server to run it. RaspberryPi? NAS? AWS? DigitalOcean?
- interactions with Playing Cards
- interaction with The Guide, retrievals

## ID3 Tags

- Can run from command line, either specify dir or default to usual
  spot

## World of Warcraft - The Guide

- Web form UI for messing with The Guide


## Playground ideas

1. Clara rules to implement games, e.g. tic tac toe, Pokemon, D&D

## Extra Scripts

### imdb.py

This is just some throwaway plotting of IMDB data, originally for
episode ratings to compare seasons of a series. IMDB data isn't
committed here because yuck. Three files are needed:

  - title.basics.tsv
  - title.episode.tsv
  - title.ratings.tsv

source: https://datasets.imdbws.com/

Each of these extracted tsv files should be in `./data/imdb/`

This is still in here because I planned on doing it in Clojure
originally but had problems with reading the
TSVs... `clojure.data.csv` gets mad about an unexpected `)` and I
couldn't figure out how to make it work. If you successfully
`clojure.data.csv/read-csv` the file `title.basics.tsv`, let me know
how that exception was resolved.
