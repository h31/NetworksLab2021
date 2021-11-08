class ResourceRecord {
  static TYPE = {
    ipv4: 1,
    canonicalName: 5,
    startOfAuthority: 6,
    mailExchange: 15,
    text: 16,
    ipv6: 28
  };

  static TYPE_ALIAS = {
    A: 'ipv4',
    CNAME: 'canonicalName',
    SOA: 'startOfAuthority',
    MX: 'mailExchange',
    TXT: 'text',
    AAAA: 'ipv6'
  };

  static CLASS = {
    internet: 1,
    chaos: 3
  };

  static CLASS_ALIAS = {
    IN: 'internet',
    CH: 'chaos'
  };
}

module.exports = ResourceRecord;