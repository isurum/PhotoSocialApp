package uk.co.stableweb.photosocial;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.List;

public class PostImageActivity extends AppCompatActivity {

    // Initialization of views
    private EditText titleEditText;
    private EditText descriptionEditText;
    private ImageButton seletedImage;
    private Button submitBtn;
    private ProgressDialog progressDialog;

    // Stores the selected image uri
    private Uri selectedImageUri = null;
    private static final int GALLERY_REQUEST = 1;

    // Declares Firebase related variables
    private StorageReference firebaseStorage;
    private DatabaseReference databaseRef;
    private FirebaseAuth auth;
    private DatabaseReference userDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_image);

        // Declaration of view variables
        seletedImage = (ImageButton) findViewById(R.id.add_image_btn);
        titleEditText = (EditText) findViewById(R.id.title_et);
        descriptionEditText = (EditText) findViewById(R.id.desc_et);
        submitBtn = (Button) findViewById(R.id.submit_btn);
        progressDialog = new ProgressDialog(this);

        firebaseStorage = FirebaseStorage.getInstance().getReference();
        databaseRef = FirebaseDatabase.getInstance().getReference().child("photos");
        auth = FirebaseAuth.getInstance();
        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(auth.getCurrentUser().getUid());

        // Checks the SDK version and manage new permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.MEDIA_CONTENT_CONTROL},
                    100);
        }

        seletedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gallaryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                gallaryIntent.setType("image/*");
                startActivityForResult(gallaryIntent, GALLERY_REQUEST);

            }
        });
        
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postImage();
            }
        });

        auth = FirebaseAuth.getInstance();
    }

    private void postImage() {

        progressDialog.setMessage("Uploading....");
        progressDialog.show();

        final String titleValue = titleEditText.getText().toString();
        final String descriptionValue = descriptionEditText.getText().toString();

        if(!TextUtils.isEmpty(titleValue) && !TextUtils.isEmpty(descriptionValue) && selectedImageUri != null){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.MEDIA_CONTENT_CONTROL},
                        100);
            }else {

                // Storage path of the photos
                StorageReference filePath = firebaseStorage.child("photos").child(selectedImageUri.getLastPathSegment());

                filePath.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        // Gets the download URL of the uploaded image
                        final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        // Creates new post reference
                        final DatabaseReference newPost = databaseRef.push();

                        userDatabaseRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Add following data to the database
                                newPost.child("title").setValue(titleValue);
                                newPost.child("desc").setValue(descriptionValue);
                                newPost.child("image").setValue(downloadUrl.toString());
                                newPost.child("uploaded_time").setValue(ServerValue.TIMESTAMP);
                                newPost.child("uid").setValue(auth.getCurrentUser().getUid());
                                newPost.child("profile_image").setValue(dataSnapshot.child("image").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Log.d("PostImageActivity", "Profile image uploaded!");
                                        }
                                    }
                                });
                                newPost.child("display_name").setValue(dataSnapshot.child("display_name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            startActivity(new Intent(PostImageActivity.this, MainActivity.class));
                                            finish();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        progressDialog.dismiss();

                        // Start the MainActivity after the posting of image.
                        startActivity(new Intent(PostImageActivity.this, MainActivity.class));
                    }
                });
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){

            selectedImageUri = data.getData();

            seletedImage.setImageURI(selectedImageUri);

            seletedImage.setClickable(false);
        }else{
            seletedImage.setBackground(getResources().getDrawable(R.mipmap.add_btn));
        }
    }
}
