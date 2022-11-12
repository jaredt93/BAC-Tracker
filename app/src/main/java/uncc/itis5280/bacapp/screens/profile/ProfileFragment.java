package uncc.itis5280.bacapp.screens.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uncc.itis5280.bacapp.databinding.FragmentProfileBinding;
import uncc.itis5280.bacapp.util.Globals;
import uncc.itis5280.bacapp.util.RetrofitInterface;
import uncc.itis5280.bacapp.util.UserResult;

public class ProfileFragment extends Fragment {
    FragmentProfileBinding binding;
    RetrofitInterface retrofitInterface;
    Retrofit retrofit;
    private static final String USER = "USER";
    User user;

    IListener mListener;
    public interface IListener {
        public void signOut();
        public void updateUserProfile(HashMap<String, Object> data, User user);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof IListener) {
            mListener = (IListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement IListener");
        }
    }

    public ProfileFragment() {
        //empty
    }

    public static ProfileFragment newInstance(User user) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable(USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.user = (User) getArguments().getSerializable(USER);
        }

        retrofit = new Retrofit.Builder()
                .baseUrl(Globals.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        getActivity().setTitle("My Profile");
        binding.inputUserProfileFirstName.setFocusable(true);
        binding.inputUserProfileLastName.setFocusable(true);
        binding.radioBtnUserProfileMale.setEnabled(true);
        binding.radioBtnUserProfileFemale.setEnabled(true);
        binding.imageButtonSave.setVisibility(View.VISIBLE);
        binding.imageButtonLogout.setVisibility(View.VISIBLE);

        binding.inputUserProfileFirstName.setText(user.getFirstName());
        binding.inputUserProfileLastName.setText(user.getLastName());
        binding.inputUserProfileCity.setText(user.getCity());

        if (User.FEMALE.equals(user.getGender())) {
            binding.radioBtnUserProfileFemale.setChecked(true);
        } else if (User.MALE.equals(user.getGender())) {
            binding.radioBtnUserProfileMale.setChecked(true);
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.imageButtonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Call<UserResult> call = retrofitInterface.updateUser("LOGGING-OUT", new HashMap());
                call.enqueue(new Callback<UserResult>() {
                    @Override
                    public void onResponse(Call<UserResult> call, Response<UserResult> response) {
                        if (response.code() == 200) {
                            UserResult result = response.body();
                            Toast.makeText(getActivity(), "Gone with the wind", Toast.LENGTH_LONG).show();
                            mListener.signOut();
                        } else {
                            Toast.makeText(getActivity(), "Cannot exit app at this time", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserResult> call, Throwable t) {
                        Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        binding.imageButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] error = new String[1];

                String firstName = binding.inputUserProfileFirstName.getText().toString();
                String lastName = binding.inputUserProfileLastName.getText().toString();
                String city = binding.inputUserProfileCity.getText().toString();

                int selectedRadioButton = binding.radioGroup.getCheckedRadioButtonId();
                RadioButton radioButton = binding.radioGroup.findViewById(selectedRadioButton);

                String gender = "";

                if (radioButton == binding.radioBtnUserProfileMale) {
                    gender = User.MALE;
                } else if (radioButton == binding.radioBtnUserProfileFemale) {
                    gender = User.FEMALE;
                }

                if (firstName.isEmpty()) {
                    Toast.makeText(getActivity(), "First name is required", Toast.LENGTH_LONG).show();
                    error[0] = "First name is required";
                    showAlert(error[0]);
                } else if (lastName.isEmpty()) {
                    Toast.makeText(getActivity(), "Last name is required", Toast.LENGTH_LONG).show();
                    error[0] = "Last name is required";
                    showAlert(error[0]);
                } else if (city.isEmpty()) {
                    Toast.makeText(getActivity(), "City is required", Toast.LENGTH_LONG).show();
                    error[0] = "City is required";
                    showAlert(error[0]);
                } else if (gender.isEmpty()) {
                    Toast.makeText(getActivity(), "Please select a gender", Toast.LENGTH_LONG).show();
                    error[0] = "No gender selected";
                    showAlert(error[0]);
                } else {
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("email", user.email);
                    data.put("firstName", firstName);
                    data.put("lastName", lastName);
                    data.put("city", city);
                    data.put("gender", gender);

                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setCity(city);
                    user.setGender(gender);

                    mListener.updateUserProfile(data, user);
                }
            }
        });
    }

    private void showAlert(String error) {
        if (error != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Error")
                    .setMessage(error)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
        }
    }
}