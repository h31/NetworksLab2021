import os
import pickle
import time


class CacheEntry:
    def __init__(self, ttl, since, data):
        self.ttl = int(ttl, 2)
        self.since = since
        self.data = data


def add(entry: CacheEntry):
    with open('cache.txt', 'rb') as fp:
        cache = pickle.load(fp) if os.path.getsize('cache.txt') > 0 else []
    if entry not in cache:
        cache.append(entry)
    with open('cache.txt', 'wb') as fp:
        pickle.dump(cache, fp)


def update():
    with open('cache.txt', 'rb') as fp:
        cache = pickle.load(fp) if os.path.getsize('cache.txt') > 0 else []
    for element in cache:
        if element.ttl <= time.time() - element.since:
            cache.remove(element)
    with open('cache.txt', 'wb') as fp:
        pickle.dump(cache, fp)


def get(rrtype: str, url: str):
    with open('cache.txt', 'rb') as fp:
        cache = pickle.load(fp)
    for element in cache:
        if element.data['rrtype'] == rrtype and element.data['QNAME'] == url:
            return element.data['ANS']
