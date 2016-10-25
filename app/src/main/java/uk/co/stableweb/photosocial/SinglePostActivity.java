package uk.co.stableweb.photosocial;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class SinglePostActivity extends AppCompatActivity {

    private String postKey = null;

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    private ImageView imageView;
    private TextView titleTv;
    private TextView descriptionTv;
    private TextView displayNameTv;
    private Button removeBtn;

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        databaseReference = FirebaseDatabase.getInstance().getReference("photos");
        auth = FirebaseAuth.getInstance();

        imageView = (ImageView) findViewById(R.id.image);
        titleTv = (TextView) findViewById(R.id.title);
        descriptionTv = (TextView) findViewById(R.id.description);
        displayNameTv = (TextView) findViewById(R.id.display_name);
        removeBtn = (Button) findViewById(R.id.remove_btn);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Share this photo...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        postKey = getIntent().getExtras().getString("post_key");

        databaseReference.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String imageUrlString = (String) dataSnapshot.child("image").getValue();
                String titleString = (String) dataSnapshot.child("title").getValue();
                String descriptionString = (String) dataSnapshot.child("desc").getValue();
                String displayName = (String) dataSnapshot.child("display_name").getValue();
                uid = (String) dataSnapshot.child("uid").getValue();

                Picasso.with(getApplicationContext()).load(imageUrlString).into(imageView);
                titleTv.setText(titleString);
                descriptionTv.setText(descriptionString);
                displayNameTv.setText("Uploaded by " + displayName);

                if(auth.getCurrentUser().getUid().toString().equals(uid)){
                    removeBtn.setVisibility(View.VISIBLE);
                }else{
                    removeBtn.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(auth.getCurrentUser().getUid().toString().equals(uid)){

                    AlertDialog.Builder builder = new AlertDialog.Builder(SinglePostActivity.this);

                    builder.setTitle("Are you sure?");
                    builder.setMessage("Do you want to delete the post you created? This will be permanent, it cannot be undone.");

                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing but close the dialog
                            dialog.dismiss();
                            Toast.makeText(SinglePostActivity.this, "Post Deleted!", Toast.LENGTH_SHORT).show();
                            databaseReference.child(postKey).removeValue();
                            startActivity(new Intent(SinglePostActivity.this, MainActivity.class));
                            finish();
                        }
                    });

                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // Do nothing
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });

    }

}
