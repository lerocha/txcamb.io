# Open Cambio

Open Cambio is a REST API for currency exchange rates from the [European Central Bank](http://www.ecb.europa.eu/stats/policy_and_exchange_rates/euro_reference_exchange_rates/html/index.en.html).

Exchange rates are usually updated around 16:00 CET on business days.

## API Documentation

#### 1. Get all currencies
```
GET /v1/currencies
```
* The currency display name is in the language specified by the `Accept-Language` request header. If no language is specified, then the default is `en-US`.

Example:
```
curl http://api.opencambio.org/v1/currencies
```

#### 2. Get currency
```
GET /v1/currencies/{code}
```
* Currency code is in the [ISO 4217 format.](https://www.iso.org/iso-4217-currency-codes.html)

Example:
```
curl http://api.opencambio.org/v1/currencies/CAD
```
```
{
  "displayName": "Canadian Dollar",
  "startDate": "1999-01-04",
  "endDate": "2017-03-14",
  "currencyCode": "CAD",
  "_links": {
    "self": {
      "href": "http://api.opencambio.org/v1/currencies/CAD"
    }
  }
}
```
* `startDate` and `endDate` indicates the date range of available exchange rates for this currency.

#### 3. Get currency exchange rates between dates
```
GET /v1/currencies/{code}/rates?start={start}&end={end}
```
* Exchange rates are based on the currency specified by `code`.
* Exchange rates are available for business days only.
* The response is paginated and each page will contain exchange rates for the same month.

Example:
```
curl http://api.opencambio.org//v1/currencies/USD/rates?start=2016-01-01&end=2016-12-31
```
```
{
  "_embedded": {
    "rates": [
      {
        "date": "2016-01-04",
        "base": "USD",
        "rates": {
          "AUD": 1.390439,
          "BGN": 1.794642,
          "BRL": 4.039549,
          "CAD": 1.392183,
          "CHF": 0.999358,
          "CNY": 6.534043,
          "CZK": 24.796293,
          "DKK": 6.847128,
          "EUR": 0.9176,
          "GBP": 0.677281,
          "HKD": 7.750413,
          "HRK": 7.011838,
          "HUF": 289.401726,
          "IDR": 13942.585796,
          "ILS": 3.914388,
          "INR": 66.633328,
          "JPY": 119.086071,
          "KRW": 1190.924941,
          "MXN": 17.375023,
          "MYR": 4.346027,
          "NOK": 8.852542,
          "NZD": 1.481557,
          "PHP": 47.158195,
          "PLN": 3.941549,
          "RON": 4.144798,
          "RUB": 73.108644,
          "SEK": 8.414021,
          "SGD": 1.425308,
          "THB": 36.159846,
          "TRY": 2.954029,
          "USD": 1,
          "ZAR": 15.558635
        },
        "_links": {
          "self": {
            "href": "http://api.opencambio.org/v1/currencies/USD/rates/2016-01-04"
          }
        }
      },
      
      .
      .
      .
      
      {
        "date": "2016-01-29",
        "base": "USD",
        "rates": {
          "AUD": 1.409158,
          "BGN": 1.791026,
          "BRL": 4.056228,
          "CAD": 1.406869,
          "CHF": 1.020513,
          "CNY": 6.576008,
          "CZK": 24.749085,
          "DKK": 6.834066,
          "EUR": 0.915751,
          "GBP": 0.699726,
          "HKD": 7.793132,
          "HRK": 7.014653,
          "HUF": 285.741759,
          "IDR": 13706.630037,
          "ILS": 3.945422,
          "INR": 67.860715,
          "JPY": 121.108059,
          "KRW": 1207.509158,
          "MXN": 18.319964,
          "MYR": 4.147986,
          "NOK": 8.68544,
          "NZD": 1.537363,
          "PHP": 47.69414,
          "PLN": 4.065751,
          "RON": 4.153114,
          "RUB": 75.8674,
          "SEK": 8.560715,
          "SGD": 1.423993,
          "THB": 35.689561,
          "TRY": 2.963828,
          "USD": 1,
          "ZAR": 16.018957
        },
        "_links": {
          "self": {
            "href": "http://api.opencambio.org/v1/currencies/USD/rates/2016-01-29"
          }
        }
      }
    ]
  },
  "_links": {
    "self": {
      "href": "http://api.opencambio.org/v1/currencies/USD/rates?start=2016-01-01&end=2016-12-31&page=0"
    },
    "first": {
      "href": "http://api.opencambio.org/v1/currencies/USD/rates?start=2016-01-01&end=2016-12-31&page=0"
    },
    "last": {
      "href": "http://api.opencambio.org/v1/currencies/USD/rates?start=2016-01-01&end=2016-12-31&page=11"
    },
    "next": {
      "href": "http://api.opencambio.org/v1/currencies/USD/rates?start=2016-01-01&end=2016-12-31&page=1"
    }
  }
}
```

#### 4. Get currency exchange rates by date
```
GET /v1/currencies/{code}/rates/{date}
```
* Exchange rates are based on the currency specified by `code`.
* If there are no exchange rates for the requested date, then returns the exchange rates for the last business day before. 

Example:
```
curl http://api.opencambio.org/v1/currencies/USD/rates/2017-03-10
```
```
{
    "date": "2017-03-10",
    "base": "USD",
    "rates": {
        "AUD": 1.328965,
        "BGN": 1.844051,
        "BRL": 3.176882,
        "CAD": 1.350368,
        "CHF": 1.012918,
        "CNY": 6.914577,
        "CZK": 25.477089,
        "DKK": 7.008392,
        "EUR": 0.942863,
        "GBP": 0.822648,
        "HKD": 7.766076,
        "HRK": 6.995098,
        "HUF": 294.427683,
        "IDR": 13375.231002,
        "ILS": 3.685273,
        "INR": 66.634453,
        "JPY": 115.425232,
        "KRW": 1156.213465,
        "MXN": 19.770036,
        "MYR": 4.453235,
        "NOK": 8.618235,
        "NZD": 1.447106,
        "PHP": 50.236659,
        "PLN": 4.07873,
        "RON": 4.289082,
        "RUB": 59.114087,
        "SEK": 9.030549,
        "SGD": 1.41816,
        "THB": 35.410146,
        "TRY": 3.752688,
        "USD": 1,
        "ZAR": 13.264285
    },
    "_links": {
        "self": {
            "href": "http://api.opencambio.org/v1/currencies/USD/rates/2017-03-10"
        }
    }
}
```

#### 5. Get latest currency exchange rates
```
GET /v1/currencies/{code}/rates/latest
```
* Exchange rates are based on the currency specified by `code`.

Example:
```
curl http://api.opencambio.org/v1/currencies/USD/rates/latest
```
