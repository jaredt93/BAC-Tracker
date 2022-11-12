package uncc.itis5280.bacapp.screens.login;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;


import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uncc.itis5280.bacapp.R;
import uncc.itis5280.bacapp.util.Globals;
import uncc.itis5280.bacapp.util.RetrofitInterface;
import uncc.itis5280.bacapp.util.UserResult;


public class LoginFragment extends Fragment {
    RetrofitInterface retrofitInterface;
    Retrofit retrofit;

    public LoginFragment() {
        // Required empty public constructor
    }

    IListener mListener;

    public interface IListener {
        public void signup();
        public void loginSuccess(String email, String password);
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

    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        btnSignin(view);
        btnCreateAccount(view);
        return view;
    }

    private void btnCreateAccount(final View view) {
        view.findViewById(R.id.btnCreateAccount).setOnClickListener(v -> mListener.signup());
    }

    private void btnSignin(final View view) {
        EditText inputAddress = view.findViewById(R.id.inputAddress);
        EditText inputPassword = view.findViewById(R.id.inputPassword);

        view.findViewById(R.id.btnRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> data = new HashMap<>();
                data.put("email", inputAddress.getText().toString());
                data.put("password", inputPassword.getText().toString());

                Call<UserResult> call = retrofitInterface.login(data);
                call.enqueue(new Callback<UserResult>() {
                    @Override
                    public void onResponse(Call<UserResult> call, Response<UserResult> response) {
                        if (response.code() == 200) {
                            UserResult result = response.body();
                            Toast.makeText(getActivity(), "found you " + result.getFirstName(), Toast.LENGTH_LONG).show();
                            mListener.loginSuccess(result.getEmail(), inputPassword.getText().toString());
                        } else {
                            Toast.makeText(getActivity(), "you were not found   ", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserResult> call, Throwable t) {
                        Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

                final String[] error = new String[1];
                if (inputAddress.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "Enter Email Address", Toast.LENGTH_LONG).show();
                } else if (inputPassword.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "Enter password", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}