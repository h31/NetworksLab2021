import requests
from bs4 import BeautifulSoup as bs


class Converter:
    def __init__(self):
        self.default_currency = "EUR"
        self.last_update_time = ""

    def get_default_currency(self):
        return self.default_currency

    def get_last_update_time(self):
        return self.last_update_time

    def get_currency_list(self):
        report = self.get_currency_report()
        if report is None:
            return {"list": "There is no connection with database",
                    "status_code": 500}
        currencies = ""
        for currency in report:
            currencies += currency + " "
        return {"list": currencies, "status_code": 200}

    def get_currency_exchange_rate(self, currency):
        report = self.get_currency_report()
        if report is None:
            return {"rate": "There is no connection with database",
                    "status_code": 500}
        currency = currency.upper()
        if currency in report:
            return {"rate": report[currency], "status_code": 200}
        else:
            return {"rate": "Unknown currency", "status_code": 404}

    def convert(self, amount, currency_from, currency_to):
        report = self.get_currency_report()
        if report is None:
            return {"result": "There is no connection with database",
                    "status_code": 500}
        currency_from = currency_from.upper()
        currency_to = currency_to.upper()
        if self.default_currency == currency_from:
            if currency_to in report:
                result = amount * float(report[currency_to])
            else:
                return {"result": "Unknown currency", "status_code": 404}
        elif self.default_currency == currency_to:
            if currency_from in report:
                result = amount / float(report[currency_from])
            else:
                return {"result": "Unknown currency", "status_code": 404}
        else:
            if currency_from in report and currency_to in report:
                amount_in_eur = amount / float(report[currency_from])
                result = amount_in_eur * float(report[currency_to])
            else:
                return {"result": "Unknown currency", "status_code": 404}

        return {"result": "{0:.4f}".format(result), "status_code": 200}

    def get_currency_report(self):
        try:
            r = requests.get("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml")
            if r.status_code == 200:
                soup = bs(r.text, "html.parser")
            else:
                return None
        except requests.exceptions.ConnectionError:
            return None

        self.last_update_time = soup.find("cube", {"time": True}).get("time")
        report = {}
        for tag in soup.find_all("cube", {"currency": True}):
            report[tag.get('currency')] = tag.get('rate')
        return report
