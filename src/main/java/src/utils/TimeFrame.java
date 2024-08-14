package src.utils;

import lombok.Getter;

@Getter
public enum TimeFrame {
    FIVE_MINUTES("5m", 5),
    FIFTEEN_MINUTES("15m", 15),
    ONE_HOUR("1h", 60),
    FOUR_HOUR("4h", 240);

    private final String timeFrame;
    private final int minuteCount;

    TimeFrame(String timeFrame, int minuteCount) {
        this.timeFrame = timeFrame;
        this.minuteCount = minuteCount;
    }
}
