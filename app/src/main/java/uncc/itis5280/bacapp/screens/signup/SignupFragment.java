package uncc.itis5280.bacapp.screens.signup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uncc.itis5280.bacapp.R;
import uncc.itis5280.bacapp.databinding.FragmentSignupBinding;
import uncc.itis5280.bacapp.screens.profile.User;
import uncc.itis5280.bacapp.util.Globals;
import uncc.itis5280.bacapp.util.RetrofitInterface;
import uncc.itis5280.bacapp.util.UserResult;

public class SignupFragment extends Fragment {
    FragmentSignupBinding binding;
    RetrofitInterface retrofitInterface;
    Retrofit retrofit;

    public SignupFragment() {
        // Required empty public constructor
    }

    IListener mListener;
    public interface IListener {
        public void registerCancelled();
        public void signupSuccess(String email, String password);
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

    public static SignupFragment newInstance(String adminUserEmail, String adminUserPassword) {
        SignupFragment fragment = new SignupFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        retrofit = new Retrofit.Builder()
                .baseUrl(Globals.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Create New Account");

        binding.btnRegisterCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.registerCancelled();
            }
        });

        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] error = new String[1];

                String firstName = binding.inputFirstName.getText().toString();
                String lastName = binding.inputLastName.getText().toString();
                String city = binding.inputCity.getText().toString();
                String email = binding.inputEmailAddress.getText().toString();
                String password = binding.inputPassword.getText().toString();

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
                } else if (email.isEmpty()) {
                    Toast.makeText(getActivity(), "Enter email", Toast.LENGTH_LONG).show();
                    error[0] = "Valid email required";
                    showAlert(error[0]);
                } else if (password.isEmpty()) {
                    Toast.makeText(getActivity(), "Enter password", Toast.LENGTH_LONG).show();
                    error[0] = "Password required";
                    showAlert(error[0]);
                } else {
                    HashMap<String, String> data = new HashMap<>();
                    data.put("email", email);
                    data.put("password", password);
                    data.put("firstName", firstName);
                    data.put("lastName", lastName);
                    data.put("city", city);
                    data.put("gender", gender);

                    Call<UserResult> call = retrofitInterface.signup(data);
                    call.enqueue(new Callback<UserResult>() {
                        @Override
                        public void onResponse(Call<UserResult> call, Response<UserResult> response) {
                            if (response.code() == 200) {
                                UserResult result = response.body();
                                Toast.makeText(getActivity(), "You're signed up", Toast.LENGTH_LONG).show();
                                mListener.signupSuccess(result.getEmail(), password);
                            } else {
                                Toast.makeText(getActivity(), "Something went wrong with registration", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<UserResult> call, Throwable t) {
                            Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

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