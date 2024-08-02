package mp_pprl.dynamic_metric_space;

import mp_pprl.core.domain.RecordIdentifier;

public class Pivot {
    private final RecordIdentifier recordIdentifier;
    private int radius = 0;

    public Pivot(RecordIdentifier recordIdentifier) {
        this.recordIdentifier = recordIdentifier;
    }

    public RecordIdentifier getRecordIdentifier() {
        return recordIdentifier;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
