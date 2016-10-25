package uk.co.stableweb.photosocial;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Map;

import uk.co.stableweb.photosocial.model.User;

public class UserProfile extends AppCompatActivity {

    private EditText displayNameInput;
    private EditText nameInput;
    private EditText emailInput;
    private ImageButton profileImage;
    private Button saveBtn;
    private ProgressDialog progressDialog;

    private static int GALLARY_REQUEST = 1;

    private FirebaseAuth auth;
    private DatabaseReference databaseUserRef;
    private StorageReference storageReference;

    private Uri profileImageUri;

    private final String TAG = "USER PROFILE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        displayNameInput = (EditText) findViewById(R.id.display_name);
        nameInput = (EditText) findViewById(R.id.name);
        emailInput = (EditText)findViewById(R.id.email);
        profileImage = (ImageButton) findViewById(R.id.profile_image);
        saveBtn = (Button) findViewById(R.id.save_btn);
        progressDialog = new ProgressDialog(this);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallaryIntent = new Intent();
                gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);
                gallaryIntent.setType("image/*");
                startActivityForResult(gallaryIntent, GALLARY_REQUEST);
            }
        });

        // Get the instance of FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Database reference for the user child
        databaseUserRef = FirebaseDatabase.getInstance().getReference().child("users");

        // Get the storage reference
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images");

        if(auth.getCurrentUser() == null){
            startActivity(new Intent(UserProfile.this, LoginActivity.class));
            finish();
        }else{
            emailInput.setText(auth.getCurrentUser().getEmail());

            Query queryRef = databaseUserRef.orderByChild(auth.getCurrentUser().getUid());

            queryRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    if(auth.getCurrentUser().getUid().equals(dataSnapshot.getKey())){
                        User user = dataSnapshot.getValue(User.class);

                        Log.d(TAG, "User ID - 100 " + auth.getCurrentUser().getUid());
                        Log.d(TAG, "Display Name - 101" + user.getDisplay_name());
                        Log.d(TAG, "Name - 102" + user.getName());

                        nameInput.setText(user.getName());
                        displayNameInput.setText(user.getDisplay_name());

                        if(!user.getImage().equals("default")){
                            Log.d(TAG, "Image - 108" + user.getImage());
                            Picasso.with(getApplicationContext()).load(user.getImage()).into(profileImage);
                        }
                    }


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

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String nameString = nameInput.getText().toString();
                final String displayNameString = displayNameInput.getText().toString();

                if(!TextUtils.isEmpty(displayNameString) && !TextUtils.isEmpty(nameString) && profileImageUri != null){

                    // Shows the progress dialog while app uploads the image
                    progressDialog.setMessage("Uploading Profile Picture...");
                    progressDialog.show();

                    StorageReference filePath = storageReference.child(profileImageUri.getLastPathSegment());

                    filePath.putFile(profileImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            String downloadUrlForImage = taskSnapshot.getDownloadUrl().toString();

                            databaseUserRef.child(auth.getCurrentUser().getUid()).child("image").setValue(downloadUrlForImage);

                            databaseUserRef.child(auth.getCurrentUser().getUid()).child("name").setValue(nameString);
                            databaseUserRef.child(auth.getCurrentUser().getUid()).child("display_name").setValue(displayNameString);

                            // Dismiss the progress dialog after the upload
                            progressDialog.dismiss();


                            startActivity(new Intent(UserProfile.this, MainActivity.class));
                            finish();

                        }
                    });
                }
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLARY_REQUEST && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                profileImageUri = result.getUri();
                profileImage.setImageURI(profileImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
