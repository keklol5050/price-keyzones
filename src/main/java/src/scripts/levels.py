from typing import TypedDict, Tuple, Union, Literal, List
from numpy import mean
from pandas import DataFrame
import matplotlib.pyplot as plt
import utilities as utils
import sys

IReversalKind = Literal["resistance", "support"]

class IReversal(TypedDict):
    id: int
    kind: IReversalKind


class IKeyZone(TypedDict):
    id: int
    start: float
    end: float
    reversals: List[IReversal]


class IMinifiedCandlestick(TypedDict):
    ot: int
    h: float
    l: float


class IConfig(TypedDict):
    ds_path: str
    ds_start: Union[int, str, None] = None
    ds_end: Union[int, str, None] = None
    candlesticks_interval: utils.IIntervalID
    zone_size: float
    zone_merge_distance_limit: float
    coin: str
    output_path: str


class KeyZones:
    def __init__(self, raw_ds: DataFrame, CONFIG: IConfig):
        self.zones: List[IKeyZone] = []
        self.zone_size: float = CONFIG["zone_size"]
        self.zone_merge_distance_limit: float = CONFIG["zone_merge_distance_limit"]
        self._build(raw_ds[["ot", "h", "l"]].to_records())

    def _build(self, lookback: List[IMinifiedCandlestick]) -> None:
        for i in range(3, len(lookback) - 3, 1):

            if lookback[i]["h"] > lookback[i - 3]["h"] and \
                    lookback[i]["h"] > lookback[i - 2]["h"] and \
                    lookback[i]["h"] > lookback[i - 1]["h"] and \
                    lookback[i]["h"] > lookback[i + 1]["h"] and \
                    lookback[i]["h"] > lookback[i + 2]["h"] and \
                    lookback[i]["h"] > lookback[i + 3]["h"]:
                self._on_reversal(lookback[i], "resistance")

            elif lookback[i]["l"] < lookback[i - 3]["l"] and \
                    lookback[i]["l"] < lookback[i - 2]["l"] and \
                    lookback[i]["l"] < lookback[i - 1]["l"] and \
                    lookback[i]["l"] < lookback[i + 1]["l"] and \
                    lookback[i]["l"] < lookback[i + 2]["l"] and \
                    lookback[i]["l"] < lookback[i + 3]["l"]:
                self._on_reversal(lookback[i], "support")

        self._merge_nearby_zones()

    def _merge_nearby_zones(self) -> None:
        zones_total: int = len(self.zones)
        zones: List[IKeyZone] = []

        self.zones = sorted(self.zones, key=lambda d: d["start"])

        merged: bool = False
        for i in range(zones_total):
            if not merged:
                if i < zones_total - 1:
                    distance: float = utils.calculate_percentage_change(self.zones[i]["end"],
                                                                        self.zones[i + 1]["start"])

                    if distance <= self.zone_merge_distance_limit:
                        zones.append(self._merge_zones(self.zones[i], self.zones[i + 1]))
                        merged = True

                    else:
                        zones.append(self.zones[i])

                else:
                    zones.append(self.zones[i])

            else:
                merged = False

        self.zones = zones

    def _on_reversal(self, candlestick: IMinifiedCandlestick, reversal_kind: IReversalKind) -> None:
        start_price, end_price = self._calculate_price_range(candlestick, reversal_kind)

        zone_index: Union[int, None] = self._reversed_in_zone(start_price, end_price)

        if isinstance(zone_index, int):
            self.zones[zone_index]["reversals"].append({"id": candlestick["ot"], "kind": reversal_kind})

        else:
            self.zones.append({
                "id": candlestick["ot"],
                "start": start_price,
                "end": end_price,
                "reversals": [{"id": candlestick["ot"], "kind": reversal_kind}]
            })

    def _reversed_in_zone(self, start_price: float, end_price: float) -> Union[int, None]:
        for i in range(len(self.zones)):
            if (
                    self.zones[i]["start"] <= start_price <= self.zones[i]["end"]
            ) or \
                    (
                            self.zones[i]["start"] <= end_price <= self.zones[i]["end"]
                    ):
                return i
        return None

    def _merge_zones(self, z1: IKeyZone, z2: IKeyZone) -> IKeyZone:
        reversals: List[IReversal] = z1["reversals"] + z2["reversals"]

        reversals = sorted(reversals, key=lambda d: d["id"])

        return {
            "id": z1["id"] if z1["id"] < z2["id"] else z2["id"],
            "start": round(mean([z1["start"], z2["start"]]), 0),
            "end": round(mean([z1["end"], z2["end"]]), 0),
            "reversals": reversals
        }

    def _calculate_price_range(
            self,
            candlestick: IMinifiedCandlestick,
            reversal_kind: IReversalKind
    ) -> Tuple[float, float]:

        if reversal_kind == "resistance":
            return utils.alter_number_by_percentage(candlestick["h"], -(self.zone_size), precision=0), candlestick["h"]
        else:
            return candlestick["l"], utils.alter_number_by_percentage(candlestick["l"], self.zone_size, precision=0)


def get_levels(CONFIG):
    raw_ds = utils.get_historic_candlesticks(
        path=CONFIG["ds_path"],
        interval=CONFIG["candlesticks_interval"],
        start=CONFIG["ds_start"],
        end=CONFIG["ds_end"],
    )

    key_zones = KeyZones(raw_ds, CONFIG)
    zones_data = []
    for zone in reversed(key_zones.zones):
        zone_str = f"{utils.from_milliseconds_to_date_string(zone['id']).split(',')[0]}: ${zone['start']} -> ${zone['end']}"
        zone_str += f" | Reversals: {len(zone['reversals'])}"
        zones_data.append(zone_str)

    plt.figure(figsize=(20, 7))
    plt.plot(raw_ds["c"].tolist(), linewidth=1, color="black")
    colors = ["dimgray", "darkorange", "forestgreen", "lightsteelblue", "rosybrown", "burlywood", "darkgreen", "cornflowerblue",
              "lightcoral", "orange", "mediumseagreen", "midnightblue", "brown", "gold", "paleturquoise", "blue", "red",
              "darkkhaki", "teal", "cyan", "salmon", "darkolivegreen", "slategray", "cadetblue", "magenta", "steelblue",
              "violet", "peru", "hotpink", "crimson", "palegreen", "orangered", "darkorchid", "indigo", "lime", "tan",
              "goldenrod", "darkcyan", "fuchsia", "deepskyblue"]

    for i, zone in enumerate(key_zones.zones):
        plt.plot([zone["start"]] * raw_ds.shape[0], linewidth=1, color=colors[i])
        plt.plot([zone["end"]] * raw_ds.shape[0], linewidth=1, color=colors[i])
    coin = CONFIG["coin"]
    interval = CONFIG["candlesticks_interval"]
    plt.title(f"Close Prices with KeyZones {coin} {interval}")
    plt.grid(False)

    plt.savefig(CONFIG["output_path"])

    for data in zones_data:
        print(data)


if __name__ == "__main__":
    CONFIG = {
        "ds_path": sys.argv[1],
        "ds_start": sys.argv[2] if sys.argv[2] != "None" else None,
        "ds_end": sys.argv[3] if sys.argv[3] != "None" else None,
        "candlesticks_interval": sys.argv[4],
        "zone_size": float(sys.argv[5]),
        "zone_merge_distance_limit": float(sys.argv[6]),
        "coin": sys.argv[7],
        "output_path": sys.argv[8],
    }
    get_levels(CONFIG)
    