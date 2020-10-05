import csv
import functools as fp
import matplotlib.pyplot as plt

### raw tsv reader stuff.

BASICS_TSV = 'resources/imdb/title.basics.tsv'
EPISODE_TSV = 'resources/imdb/title.episode.tsv'
RATINGS_TSV = 'resources/imdb/title.ratings.tsv'

def tsv_reader(f):
    return csv.DictReader(f, delimiter='\t', quoting=csv.QUOTE_NONE)

def find(pred, coll):
    """Return the first member of `coll` which matches `pred`."""
    return next(el for el in coll if pred(el))

def tconst(primary_title):
    """Scan the basics for an entry with `primary_title` and returns its `tconst`.
This is a fairly expensive IO scanning operationg. Please god just save the value
instead of repeatedly calling this."""
    with open(basics_tsv, encoding='utf-8') as f:
        return find(
            lambda row: row['primaryTitle'] == primary_title,
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

def boxplot_season_ratings(data):
    """Create and show a boxplot for the ratings of all seasons.
`data` must be a dict of seasons to list of ratings."""
    ratings_vecs = [ x[1] for x in
                     sorted([[k, v] for k, v in data.items()],
                            key = lambda x: int(x[0]))]
    fig, ax = plt.subplots()
    ax.boxplot(ratings_vecs)
    plt.show()

### pandas for messing with the data


SOUTH_PARK_TCONST = "tt0121955"

boxplot_season_ratings(by_season(with_ratings(episodes(tconst("Steven Universe")))))
