package mx.edu.utng.firebase.Activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import mx.edu.utng.firebase.Entity.User;
import mx.edu.utng.firebase.R;

/**
 * Created by Catherine on 2/28/2018.
 */

public class RegisterActivity extends AppCompatActivity{

    private EditText edtName;
    private EditText edtEmail;
    private EditText edtPassword;
    private EditText edtConfirmPassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    //private DatabaseReference reference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtName = findViewById(R.id.edt_name_register);
        edtEmail = findViewById(R.id.edt_email_register);
        edtPassword = findViewById(R.id.edt_password_register);
        edtConfirmPassword = findViewById(R.id.edt_confirm_register);
        btnRegister = findViewById(R.id.btn_register);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        //reference = database.getReference("Users");

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = edtEmail.getText().toString();
                final String name = edtName.getText().toString();
                if(isValidEmail(email) && isValidPassword() && isValidName(name)){
                    String password = edtPassword.getText().toString();

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Toast.makeText(RegisterActivity.this,"Se registró correctamente",Toast.LENGTH_SHORT).show();
                                        User user = new User();
                                        user.setEmail(email);
                                        user.setName(name);
                                        FirebaseUser currentUser = mAuth.getCurrentUser();
                                        DatabaseReference databaseReference = database.getReference("Users/"+currentUser.getUid());
                                        databaseReference.setValue(user);
                                        finish();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(RegisterActivity.this,"Error al registrarse",Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });
                }else{
                    Toast.makeText(RegisterActivity.this,"Validaciones funcionando",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private boolean isValidEmail(CharSequence target){
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public boolean isValidPassword(){
        String password;
        String confirmPassword;

        password = edtPassword.getText().toString();
        confirmPassword = edtConfirmPassword.getText().toString();

        if(password.equals(confirmPassword)){
            if(password.length() >= 6 && password.length() <= 16){
                return true;
            }else{
                return false;
            }
        }else {
            return false;
        }
    }

    public boolean isValidName(String name){
        return !name.isEmpty();
    }
}
