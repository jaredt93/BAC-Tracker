package uncc.itis5280.bacapp.util;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;

public class ChartValueFormatter extends ValueFormatter {
    private DecimalFormat mFormat;

    public ChartValueFormatter() {
        mFormat = new DecimalFormat("#.00");
    }
}
