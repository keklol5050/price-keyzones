from typing import Union, Literal, Dict, List, Tuple
from datetime import datetime
from pandas import DataFrame, to_datetime
import mplfinance as mpl
import pandas as pd
import numpy as np


def calculate_percentage_change(old_value: float, new_value: float, precision: int = 2) -> float:
    if old_value == 0:
        return 0

    change: float = 0.0

    if new_value > old_value:
        increase: float = new_value - old_value
        change = (increase / old_value) * 100

    elif old_value > new_value:
        decrease: float = old_value - new_value
        change = -((decrease / old_value) * 100)

    return round(change if change >= -100 else -100, precision)


def alter_number_by_percentage(value: float, percent: float, precision: int = 2) -> float:
    new_value = value

    if percent > 0:
        new_value = ((percent / 100) + 1) * value

    elif percent < 0:
        new_value = -(((percent * -1) / 100) - 1) * value

    return round(new_value, precision)


def from_date_string_to_timestamp(date_str: str) -> int:
    if isinstance(date_str, str):
        date_split: List[str] = date_str.split('/')
        dt: datetime = datetime(int(date_split[2]), int(date_split[1]), int(date_split[0]))
        return int(dt.timestamp() * 1000)
    else:
        return date_str


def from_milliseconds_to_seconds(milliseconds: Union[int, float]) -> int:
    return int(milliseconds / 1000)


def from_seconds_to_milliseconds(seconds: Union[int, float]) -> int:
    return int(seconds * 1000)


def add_minutes(timestamp_ms: Union[int, float], minutes: int) -> int:
    return int(timestamp_ms + (from_seconds_to_milliseconds(60) * minutes))


def currency(value: Union[int, float], symbol: str = "$") -> str:
    return f"{value:,}{symbol}"


def from_milliseconds_to_date_string(ms: int) -> str:
    return datetime.fromtimestamp(from_milliseconds_to_seconds(ms)).strftime("%d/%m/%Y, %H:%M:%S")

IColumnID = Literal[
    "ot",  # Open Timestamp: the time at which the interval started (milliseconds)
    "ct",  # Close Timestamp: the time at which the interval ended (milliseconds)
    "o",  # Open Price: the price at which the interval started
    "h",  # High Price: the highest price found in the interval
    "l",  # Low Price: the lowest price found in the interval
    "c",  # Close Price: the last price found in the interval
    "v"  # Volume: the total amount of USDT traded in the interval
]

IIntervalID = Literal[
   "5m", "15m", "1h", "4h"
]

IIntervalDataset = Dict[IColumnID, List[Union[int, float]]]

def get_historic_candlesticks(
        path: str,
        interval: IIntervalID,
        start: Union[int, str, None] = None,
        end: Union[int, str, None] = None
) -> DataFrame:
    df = pd.read_csv(f'{path}/{interval}.csv', index_col=0, parse_dates=True)
    df = df.loc[start:end]
    df.reset_index(inplace=True)
    df.rename(
        columns={
            'open_time': 'ot',
            'close_time': 'ct',
            'open': 'o',
            'high': 'h',
            'low': 'l',
            'close': 'c',
            'volume': 'v',
        }, inplace=True)
    df['ot'] = pd.to_datetime(df['ot']).astype(np.int64) / int(1e6)
    df['ct'] = pd.to_datetime(df['ct']).astype(np.int64) / int(1e6)
    return df