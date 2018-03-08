package mx.edu.utng.firebase.Activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;
import mx.edu.utng.firebase.AdapterMessages;
import mx.edu.utng.firebase.Entity.User;
import mx.edu.utng.firebase.R;
import mx.edu.utng.firebase.Entity.ReceiveMessage;
import mx.edu.utng.firebase.Entity.SendMessage;

public class MainActivity extends AppCompatActivity {

    private CircleImageView civPhoto;
    private TextView txvName;
    private RecyclerView rcvMessages;
    private EditText edtMessage;
    private Button btnSend;
    private Button btnLogout;
    private AdapterMessages adapterMessages;
    private ImageButton imbSendPicture;

    private FirebaseDatabase database;
    private DatabaseReference reference;
    private static final int photo_send = 1;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private static final int photo_profile= 2;
    private String profilePhoto;

    private FirebaseAuth mAuth;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        civPhoto = findViewById(R.id.civ_photo);
        txvName = findViewById(R.id.txv_name);
        rcvMessages = findViewById(R.id.rcv_messages);
        edtMessage = findViewById(R.id.edt_message);
        btnSend = findViewById(R.id.btn_send);
        btnLogout = findViewById(R.id.btn_logout);
        imbSendPicture = findViewById(R.id.imb_gallery);
        profilePhoto = "";

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("chatV2");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("media_chat");
        mAuth = FirebaseAuth.getInstance();

        adapterMessages = new AdapterMessages(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvMessages.setLayoutManager(linearLayoutManager);
        rcvMessages.setAdapter(adapterMessages);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reference.push().setValue(new SendMessage(edtMessage.getText().toString(),
                        username, profilePhoto, "1", ServerValue.TIMESTAMP));
                edtMessage.setText("");
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                returnLogin();
            }
        });

        imbSendPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("image/jpeg");
                i.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(Intent.createChooser(i,"Selecciona una foto"),photo_send);
            }
        });

        civPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("image/jpeg");
                i.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(Intent.createChooser(i,"Selecciona una foto"),photo_profile);
            }
        });

        adapterMessages.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                setScrollbar();
            }
        });

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ReceiveMessage m =  dataSnapshot.getValue(ReceiveMessage.class);
                adapterMessages.addMessage(m);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setScrollbar() {
        rcvMessages.scrollToPosition(adapterMessages.getItemCount() - 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == photo_send && resultCode == RESULT_OK){
            Uri u = data.getData();
            storageReference = storage.getReference("media_chat");
            final StorageReference photoReference = storageReference.child(u.getLastPathSegment());
            photoReference.putFile(u).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri u = taskSnapshot.getDownloadUrl();
                    SendMessage m = new SendMessage(username+" ha enviado una foto",
                            u.toString(),username,profilePhoto,
                            "2",ServerValue.TIMESTAMP);
                    reference.push().setValue(m);
                }
            });
        }else if(requestCode == photo_profile && resultCode == RESULT_OK){
            Uri u = data.getData();
            storageReference = storage.getReference("media_profile");
            final StorageReference photoReference = storageReference.child(u.getLastPathSegment());
            photoReference.putFile(u).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri u = taskSnapshot.getDownloadUrl();
                    profilePhoto = u.toString();
                    SendMessage m = new SendMessage(username+" ha cambiado su foto de perfil",
                            u.toString(),username,profilePhoto,
                            "2",ServerValue.TIMESTAMP);
                    reference.push().setValue(m);
                    Glide.with(MainActivity.this).load(u.toString()).into(civPhoto);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            btnSend.setEnabled(false);
            //All right
            DatabaseReference databaseReference = database.getReference("Users/"+currentUser.getUid());
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    username = user.getName();
                    txvName.setText(username);
                    btnSend.setEnabled(true);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else{
            returnLogin();
        }
    }

    public void returnLogin(){
        startActivity(new Intent(MainActivity.this,LoginActivity.class));
        finish();
    }
}

