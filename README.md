# Clojure Hub

I'm just going to put all my side-project Clojure stuff into this catch-all project until each part grows large enough to justify being split into their own specific thing. Secondary motivation is to run everything as a single EC2 instance without bothering trying to set up dependencies between repos because ehhh...

## Components added, checked in REPL

1. World of Warcraft arena match log, i.e. The Guide
1. discord bot roles interaction
1. id3 tag fix for Windows Media Player CD rips
1. Playing cards deck

## Components to add, written somewhere else already

1. Pokemon utilities
   - PokeJobs assignments
1. Dice Bag
1. Conway game of life
1. Advent of Code exercises

## Ideas for components to write

1. Discord bot interaction with The Guide
1. Discord bot interaction with Dice Bag
1. Discord bot interaction with playing cards
1. Discord bot interaction for @ing video game groups,
   i.e. programmatic role assignment
   ( this is roughly just about done? )
1. Web interaction with The Guide

## Nicely Wrap things up

1. can run id3 tag fix from command line, root-dir as optional argument

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

Each of these extracted tsv files should be in `./resources/imdb/`

This is still in here because I planned on doing it in Clojure
originally but had problems with reading the
TSVs... `clojure.data.csv` gets mad about an unexpected `)` and I
couldn't figure out how to make it work. If you successfully
`clojure.data.csv/read-csv` the file `title.basics.tsv`, let me know
how that exception was resolved.
