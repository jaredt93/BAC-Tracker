package uncc.itis5280.bacapp.screens.bac;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import uncc.itis5280.bacapp.R;
import uncc.itis5280.bacapp.databinding.FragmentBacTrackBinding;

public class BACTrackFragment extends Fragment {

    private FragmentBacTrackBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBacTrackBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonConnect.setText(getArguments().getString("buttonStatus"));

        binding.buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.connectNearestClicked();
            }
        });

        binding.connectionStatus.setText(getArguments().getString("connectionStatus"));

        binding.buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.startBacTestClicked();
            }
        });

        if(!getArguments().getBoolean("startVisibility")) {
            binding.buttonStart.setVisibility(View.INVISIBLE);
        } else {
            binding.buttonStart.setVisibility(View.VISIBLE);
        }

        if(!getArguments().getBoolean("countdownVisibility")) {
            binding.countdownLabel.setVisibility(View.INVISIBLE);
            binding.direction.setVisibility(View.INVISIBLE);
            binding.countdownStatus.setVisibility(View.INVISIBLE);
        } else {
            binding.countdownLabel.setVisibility(View.VISIBLE);
            binding.direction.setVisibility(View.VISIBLE);
            binding.direction.setText(getArguments().getString("direction"));
            binding.countdownStatus.setText(getArguments().getString("countdownStatus"));
            binding.countdownStatus.setVisibility(View.VISIBLE);
        }

        if(!getArguments().getBoolean("blowVisibility")) {
            binding.blowLabel.setVisibility(View.INVISIBLE);
            binding.blowStatus.setVisibility(View.INVISIBLE);
        } else {
            binding.blowLabel.setVisibility(View.VISIBLE);
            binding.direction.setText(getArguments().getString("direction"));
            binding.blowStatus.setText(getArguments().getString("blowStatus"));
            binding.blowStatus.setVisibility(View.VISIBLE);
        }

        if(!getArguments().getBoolean("analyzeVisibility")) {
            binding.analyzeLabel.setVisibility(View.INVISIBLE);
            binding.analyzingStatus.setVisibility(View.INVISIBLE);
        } else {
            binding.analyzeLabel.setVisibility(View.VISIBLE);
            binding.direction.setText(getArguments().getString("direction"));
            binding.analyzingStatus.setText(getArguments().getString("analyzingStatus"));
            binding.analyzingStatus.setVisibility(View.VISIBLE);
        }

        if(!getArguments().getBoolean("resultVisibility")) {
            binding.resultLabel.setVisibility(View.INVISIBLE);
            binding.result.setVisibility(View.INVISIBLE);
            binding.imageResultView.setVisibility(View.INVISIBLE);
        } else {
            binding.resultLabel.setVisibility(View.VISIBLE);
            binding.direction.setText(getArguments().getString("direction"));
            binding.result.setText(getArguments().getString("result"));

            if(getArguments().getFloat("measuredBac") > 0.08) {
                binding.imageResultView.setImageResource(R.drawable.ic_baseline_no_drinks_24);
            } else {
                binding.imageResultView.setImageResource(R.drawable.ic_baseline_check_circle_outline_24);
            }
            binding.imageResultView.setVisibility(View.VISIBLE);

            binding.result.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    IListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (IListener) context;
    }

    public interface IListener {
        void connectNearestClicked();
        void startBacTestClicked();
    }
}