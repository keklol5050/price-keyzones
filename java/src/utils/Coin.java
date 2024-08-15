package src.utils;

import lombok.Getter;

@Getter
public enum Coin {
    BTCUSDT("BTCUSDT", "bitcoin", new String[]{"count", "feeValue", "inputCount", "inputValue", "minedValue", "outputCount", "outputValue", "fee_avg"}),
    ETHUSDT("ETHUSDT","ethereum", new String[]{"gas", "gasValue", "amount", "count", "gasPrice"});
    private final String name;
    private final String fullName;
    private final String[] fundamentalCols;

    Coin(String name, String fullName, String[] fundamentalCols) {
        this.name = name;
        this.fullName = fullName;
        this.fundamentalCols = fundamentalCols;
    }
}
