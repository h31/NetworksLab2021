class ResourceRecord {
  static TYPE = {
    ipv4: 1,
    startOfAuthority: 6,
    mailExchange: 15,
    text: 16,
    ipv6: 28
  };

  static CLASS = {
    internet: 1,
    chaos: 3
  };

  static _ANY = 255;
}

module.exports = ResourceRecord;