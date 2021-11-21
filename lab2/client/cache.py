import os
import pickle
import time


class CacheEntry:
    def __init__(self, ttl, since, data):
        self.ttl = int(ttl, 2)
        self.since = since
        self.data = data


def add(entry: CacheEntry):
    with open('cache.txt', 'rb+') as fp:
        cache = pickle.load(fp) if os.path.getsize('cache.txt') > 0 else []
    if entry not in cache:
        cache.append(entry)
    with open('cache.txt', 'wb+') as fp:
        pickle.dump(cache, fp)


def update():
    new_cache = []
    with open('cache.txt', 'rb+') as fp:
        old_cache = pickle.load(fp) if os.path.getsize('cache.txt') > 0 else []
    for element in old_cache:
        if element.ttl > time.time() - element.since:
            new_cache.append(element)
    with open('cache.txt', 'wb+') as fp:
        pickle.dump(new_cache, fp)


def get(rrtype: str, domain_name: str):
    with open('cache.txt', 'rb+') as fp:
        cache = pickle.load(fp)
    for element in cache:
        if element.data['QTYPE'] == rrtype and element.data['QNAME'] == domain_name:
            return element.data['ANS']
