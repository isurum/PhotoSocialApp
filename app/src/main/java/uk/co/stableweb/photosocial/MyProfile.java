package uk.co.stableweb.photosocial;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.stableweb.photosocial.model.Photo;

public class MyProfile extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseRefLikes;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    Query currentUserQuery;

    private boolean isPostImpressionClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("photos");

        databaseRefLikes = FirebaseDatabase.getInstance().getReference().child("likes");

        databaseReference.keepSynced(true);

        firebaseAuth = FirebaseAuth.getInstance();

        currentUserQuery = databaseReference.orderByChild("uid").equalTo(firebaseAuth.getCurrentUser().getUid());

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginActivityIntent = new Intent(MyProfile.this, LoginActivity.class);
                    startActivity(loginActivityIntent);
                }
            }
        };

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {

        View view;
        ImageButton thumsupBtn;
        ImageButton shareBtn;

        DatabaseReference databaseLikeRefViewHolder;
        FirebaseAuth auth;

        public PhotoViewHolder(View itemView) {
            super(itemView);

            view = itemView;

            thumsupBtn = (ImageButton) view.findViewById(R.id.thumbs_up_button);
            shareBtn = (ImageButton) view.findViewById(R.id.share_button);

            databaseLikeRefViewHolder = FirebaseDatabase.getInstance().getReference().child("likes");
            databaseLikeRefViewHolder.keepSynced(true);

            auth = FirebaseAuth.getInstance();
        }

        public void setLikeBtn(final String post_key){

            databaseLikeRefViewHolder.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(post_key).hasChild(auth.getCurrentUser().getUid())){
                        thumsupBtn.setImageResource(R.mipmap.ic_thumb_up_black_24dp);
                    }else {
                        thumsupBtn.setImageResource(R.mipmap.ic_thumb_up_white_24dp);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        public void setProfilePic(Context context, String image) {
            ImageView profileImageView = (ImageView) view.findViewById(R.id.profile_pic);
            Picasso.with(context).load(image).into(profileImageView);
        }

        public void setName(String name) {
            TextView nameTv = (TextView) view.findViewById(R.id.name);
            nameTv.setText(name);
        }

        public void setUploadedTime(Long time) {
            TextView timestampTv = (TextView) view.findViewById(R.id.timestamp);

            Date date = new Date(time);
            Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            timestampTv.setText(format.format(date));
        }

        public void setTitle(String title) {
            TextView titleTv = (TextView) view.findViewById(R.id.post_title);
            titleTv.setText(title);
        }

        public void setDesc(String description) {
            TextView descriptionTv = (TextView) view.findViewById(R.id.post_description);
            descriptionTv.setText(description);
        }

        public void setImage(Context context, String image) {
            ImageView imageView = (ImageView) view.findViewById(R.id.post_image);
            Picasso.with(context).load(image).into(imageView);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);

        FirebaseRecyclerAdapter<Photo, MyProfile.PhotoViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Photo, MyProfile.PhotoViewHolder>(Photo.class, R.layout.feed_item, MyProfile.PhotoViewHolder.class, currentUserQuery) {

            @Override
            protected void populateViewHolder(MyProfile.PhotoViewHolder viewHolder, Photo model, int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setProfilePic(getApplicationContext(), model.getProfile_image());
                viewHolder.setName(model.getDisplay_name());
                viewHolder.setUploadedTime(model.getUploaded_time());
                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setLikeBtn(post_key);

                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent singlePostIntent = new Intent(MyProfile.this, SinglePostActivity.class);
                        singlePostIntent.putExtra("post_key", post_key);
                        startActivity(singlePostIntent);
                    }
                });

                // Setting On Click Listener on Thumbs Up Btn
                viewHolder.thumsupBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        isPostImpressionClicked = true;

                        databaseRefLikes.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (isPostImpressionClicked) {

                                    if (dataSnapshot.child(post_key).hasChild(firebaseAuth.getCurrentUser().getUid())) {
                                        databaseRefLikes.child(post_key).child(firebaseAuth.getCurrentUser().getUid()).removeValue();

                                        isPostImpressionClicked = false;
                                    } else {
                                        databaseRefLikes.child(post_key).child(firebaseAuth.getCurrentUser().getUid()).setValue("liked");

                                        isPostImpressionClicked = false;
                                    }

                                } else {

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                databaseRefLikes.child(post_key).child(firebaseAuth.getCurrentUser().getUid()).removeValue();

                                isPostImpressionClicked = false;
                            }
                        });

                    }
                });

            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

}
