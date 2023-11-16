import csv
import functools as fp
import gzip
import os
import requests
import sys
import matplotlib.pyplot as plt

from io import BytesIO

DATA_DIR = "data/imdb"

BASICS_GZ = "https://datasets.imdbws.com/title.basics.tsv.gz"
EPISODE_GZ = "https://datasets.imdbws.com/title.episode.tsv.gz"
RATINGS_GZ = "https://datasets.imdbws.com/title.ratings.tsv.gz"

BASICS_TSV = f"{DATA_DIR}/title.basics.tsv"
EPISODE_TSV = f"{DATA_DIR}/title.episode.tsv"
RATINGS_TSV = f"{DATA_DIR}/title.ratings.tsv"

def download_gz(url, output_path):
    os.makedirs(DATA_DIR, exist_ok=True)
    response = requests.get(url)
    if response.status_code != 200:
        print(f"Failed to download file from {url}. Status code: {response.status_code}")
        return None

    filename = url.split("/")[-1].replace(".gz", "")
    with gzip.GzipFile(fileobj=BytesIO(response.content), mode='rb') as gz_file:
        with open(output_path, 'wb') as output_file:
            output_file.write(gz_file.read())

    print(f"Downloaded and unzipped {url} to {output_path}")
    return output_path

def fetch_imdb_data():
    for url, filename in [[BASICS_GZ, BASICS_TSV],
                          [EPISODE_GZ, EPISODE_TSV],
                          [RATINGS_GZ, RATINGS_TSV]]:
        if not os.path.exists(filename):
            print(f"Downloading IMDB data to  ./{filename}")
            download_gz(url, filename)


def tsv_reader(f):
    return csv.DictReader(f, delimiter='\t', quoting=csv.QUOTE_NONE)


def find(pred, coll):
    """Return the first member of `coll` which matches `pred`."""
    return next(el for el in coll if pred(el))


def tconst(primary_title):
    """Scan the basics for an entry with `primary_title` and returns its `tconst`.
This is a fairly expensive IO scanning operationg. Please god just save the value
instead of repeatedly calling this."""
    with open(BASICS_TSV, encoding='utf-8') as f:
        return find(
            lambda row: row['primaryTitle'] == primary_title and row['titleType'] == 'tvSeries',
            tsv_reader(f)
        )['tconst']


def episodes_iter(series_tconst):
    """Scan title.episode.tsv for episodes matching the parent `series_tconst`.
Ditto to above expense warning. Don't call multiple times."""
    # I wanted to make this lazy but it has to be realized before exiting file context
    with open(EPISODE_TSV, encoding='utf-8') as f:
        # this scans the whole file but it's a third the size of above so uhhhh maybe it's okay?
        return [row for row in tsv_reader(f) if row['parentTconst'] == series_tconst]


def episodes(series_tconst):
    """Returns a new dict of episodes by tconst.
Calls episodes_generator so, don't call multiple times."""
    return dict((ep['tconst'], ep) for ep in episodes_iter(series_tconst))


def with_ratings(eps):
    """Scan title.ratings.tsv, adding ratings to any matching tconsts in `episodes`.
This function mutates the existing dictionary, but I know I'll forget to not namebind
so it also returns the passed in map."""
    with open(RATINGS_TSV, encoding='utf-8') as f:
        return [{'averageRating': float(row['averageRating']),
                 'numVotes': int(row['numVotes']),
                 'episodeNumber': eps[row['tconst']]['episodeNumber'],
                 'seasonNumber': eps[row['tconst']]['seasonNumber']}
                for row in tsv_reader(f)
                if row['tconst'] in eps]


def by_season(rated_episodes):
    """(group-by "season" rated_episodes) in clojure, lol"""
    def ratings_by_season_reducer(acc, episode):
        """ this reduce mutates the `acc`, ack. """
        season = episode['seasonNumber']
        rating = episode['averageRating']
        if season in acc:
            acc[season].append(rating)
        else:
            acc[season] = [rating]
        return acc
    return fp.reduce(ratings_by_season_reducer, rated_episodes, {})


def plot_episode_ratings(data):
    def se(ep):
        return "{}{}".format(ep['seasonNumber'], ep['episodeNumber'].zfill(2))
    ratings = [ row['averageRating'] for row in sorted(data, key = se) ]
    fig, ax = plt.subplots()
    ax.scatter(range(len(ratings)), ratings)
    plt.ylim(0,10)
    plt.show()


def sort_season_key(pair):
    """
    Sort a pair of [season, episode_ratings], placing '\\N' at the end.
    """
    if pair[0] == '\\N':
        return sys.maxsize  # largest int
    else:
        return int(pair[0])

def boxplot_season_ratings(data, title):
    """
    Create and show a boxplot for the ratings of all seasons.
    `data` must be a dict of seasons to list of ratings.
    """
    ratings_vecs = [ v for k, v in
                     sorted([[k, v] for k, v in data.items()],
                            key=sort_season_key) ]
    fig, ax = plt.subplots()

    ax.boxplot(ratings_vecs)
    ax.set_title(title)
    ax.set_xlabel('Seasons')
    ax.set_ylabel('Ratings')

    plt.ylim(0,10)
    plt.show()


### pandas for messing with the data

fetch_imdb_data()

TITLE = "SpongeBob SquarePants"
TCONST = tconst(TITLE)
# TCONST = "tt8050756"
EPISODES = episodes(TCONST)
RATINGS = with_ratings(EPISODES)
SEASONS = by_season(RATINGS)
boxplot_season_ratings(SEASONS, TITLE)
