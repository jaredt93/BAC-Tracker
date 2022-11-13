package uncc.itis5280.bacapp.screens.history;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

import uncc.itis5280.bacapp.databinding.FragmentHistoryBinding;
import uncc.itis5280.bacapp.screens.bac.Reading;
import uncc.itis5280.bacapp.screens.profile.User;
import uncc.itis5280.bacapp.util.ChartValueFormatter;

public class HistoryFragment extends Fragment {
    private FragmentHistoryBinding binding;
    LineChart chart;
    User user;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        user = (User)getArguments().getSerializable("user");

        chart = (LineChart) binding.chart;
        chart.setTouchEnabled(true);
        chart.setPinchZoom(true);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (user != null && user.getReadingHistory() != null) {
            if (getDataSet() != null) {
                LineData data = new LineData(getDataSet());
                chart.setData(data);
            }
            chart.animateXY(1000, 1000);
            chart.invalidate();

            chart.getDescription().setText("BAC History");

            if(getDataSet().getXMax() <= 10) {
                Log.d("JWT", "onViewCreated: " + "xmaxless10");
                chart.getXAxis().setAxisMinimum(0);
                chart.getXAxis().setAxisMaximum(10);
                chart.setVisibleXRange(0, 10);
            } else {
                chart.getXAxis().setAxisMinimum(getDataSet().getXMax() - 9);
                chart.getXAxis().setAxisMaximum(getDataSet().getXMax());
                chart.setVisibleXRange(getDataSet().getXMax() - 9, getDataSet().getXMax());
            }


            chart.getXAxis().setLabelCount(11, /*force: */true);

            chart.getAxisLeft().setAxisMinimum(0);
            chart.getAxisLeft().setAxisMaximum((float) 0.50);
            chart.getAxisRight().setAxisMinimum(0);
            chart.getAxisRight().setAxisMaximum((float) 0.50);
            chart.setVisibleYRange(0, (float) 0.50, YAxis.AxisDependency.LEFT);

            chart.getAxisLeft().setLabelCount(26, /*force: */true);
            chart.getAxisRight().setLabelCount(26, /*force: */true);
            chart.setDrawGridBackground(false);
        }
    }

    private LineDataSet getDataSet() {
        ArrayList<Entry> values = new ArrayList<>();
        ArrayList<Reading> readings = user.getReadingHistory();

        for (Reading reading: readings) {
            Log.d("JWT", "getDataSet: " + reading.getId());
            values.add(new Entry(reading.getId(), reading.getBac()));
        }

        LineDataSet set = new LineDataSet(values, "BAC readings");
        set.setColor(Color.RED);
        set.setDrawFilled(true);
        set.setFillColor(Color.RED);
        set.setValueTextSize(14);
        set.setCircleColor(Color.DKGRAY);
        set.setValueFormatter(new ChartValueFormatter());
        return set;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}