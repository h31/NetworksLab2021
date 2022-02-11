import requests
from bs4 import BeautifulSoup as bs


class Converter:
    def __init__(self):
        self.__default_currency = "EUR"
        self.__time = ""
        self.__error = 0  # 0 = ok, 1 = Unknown currency, 2 = No connection to the database

    def get_default_currency(self):
        return self.__default_currency

    def set_default_currency(self, currency):
        self.__default_currency = currency
    default_currency = property(get_default_currency, set_default_currency)

    def get_time(self):
        return self.__time

    def set_time(self, time):
        self.__time = time
    time = property(get_time, set_time)

    def get_error(self):
        return self.__error

    def set_error(self, error):
        self.__error = error
    error = property(get_error, set_error)

    def get_currency_list(self):
        report = self.get_currency_report()
        if report is None:
            return ""
        return " ".join(report)

    def get_currency_exchange_rate(self, currency):
        report = self.get_currency_report()
        if report is None:
            return ""
        currency = currency.upper()
        if currency in report:
            return report[currency]
        else:
            self.__error = 1
            return ""

    def convert(self, amount, currency_from, currency_to):
        report = self.get_currency_report()
        if report is None:
            return ""
        currency_from = currency_from.upper()
        currency_to = currency_to.upper()
        if self.__default_currency == currency_from:
            if currency_to in report:
                result = amount * float(report[currency_to])
            else:
                self.__error = 1
                return ""
        elif self.__default_currency == currency_to:
            if currency_from in report:
                result = amount / float(report[currency_from])
            else:
                self.__error = 1
                return ""
        else:
            if currency_from in report and currency_to in report:
                amount_in_eur = amount / float(report[currency_from])
                result = amount_in_eur * float(report[currency_to])
            else:
                self.__error = 1
                return ""
        return "{0:.4f}".format(result)

    def get_currency_report(self):
        try:
            r = requests.get("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml")
            if r.status_code == 200:
                soup = bs(r.text, "html.parser")
            else:
                raise requests.exceptions.ConnectionError
        except requests.exceptions.ConnectionError:
            self.__error = 2
            return None

        self.__time = soup.find("cube", {"time": True}).get("time")
        report = {}
        for tag in soup.find_all("cube", {"currency": True}):
            report[tag.get('currency')] = tag.get('rate')
        self.__error = 0
        return report
