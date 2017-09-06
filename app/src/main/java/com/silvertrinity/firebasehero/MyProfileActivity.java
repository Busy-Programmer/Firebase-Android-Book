package com.silvertrinity.firebasehero;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MyProfileActivity extends AppCompatActivity {

    // Declaration of views
    private EditText titleEditText;
    private EditText descriptionEditText;
    private ImageButton selectedImage;
    private Button submitBtn;
    private ProgressDialog progressDialog;

    // Stores the selected image Uri
    private Uri selectedImageUri = null;
    private static final int GALLERY_REQUEST = 1;

    // Declaration of the FirebaseStorage variable
    private StorageReference firebaseStorage;
    // Declaration of FirebaseDatabase reference
    private DatabaseReference databaseReference;
    //Declaration of FirebaseAuth variable
    private FirebaseAuth auth;
    // Declaration of User database reference
    private DatabaseReference userDatabaseRef;
    // Declaration variable
    private FirebaseAuth.AuthStateListener mAuthListener;

    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the FirebaseAuth instance
        auth = FirebaseAuth.getInstance();

        // Get the instance of the FirebaseStorage
        firebaseStorage = FirebaseStorage.getInstance().getReference();

        //capture view objects from the layout
        selectedImage = (ImageButton) findViewById(R.id.add_image_btn);
        titleEditText = (EditText) findViewById(R.id.name_et);
        descriptionEditText = (EditText) findViewById(R.id.desc_et);
        submitBtn = (Button) findViewById(R.id.submit_btn);
        progressDialog = new ProgressDialog(this);

        // Get the reference to the "photos" folder
        databaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("photos");

        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in: " +
                            user.getUid());

                    // Get the reference to the "users" database path
                    userDatabaseRef = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("users")
                            .child(auth.getCurrentUser().getUid());


                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }

            }
        };
        // [END auth_state_listener]

        // set the on click listener to the selectedImage
        selectedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to get the gallery image
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        // set the on click listener to the submit button
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // invoke postImage() method
                postImage();
            }
        });


    }

    private void postImage() {

        // show the progress dialog
        progressDialog.setMessage("Uploading....");
        progressDialog.show();

        // get the user inputs
        final String titleValue = titleEditText.getText().toString();
        final String descriptionValue = descriptionEditText.getText().toString();

        //validate titleValue and descriptionValue
        if (!TextUtils.isEmpty(titleValue)
                && !TextUtils.isEmpty(descriptionValue)
                && selectedImageUri != null) {

            // Storage path of the photos
            StorageReference filePath = firebaseStorage.child("photos")
                    .child(selectedImageUri.getLastPathSegment());

            // upload the file using putFile() method
            filePath.putFile(selectedImageUri).addOnSuccessListener(
                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // Gets the download URL of the uploaded image
                            final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                            // Creates new post reference
                            final DatabaseReference newPost = databaseReference.push();

                            userDatabaseRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // Add following data to the database
                                    newPost.child("title")
                                            .setValue(titleValue);
                                    newPost.child("desc")
                                            .setValue(descriptionValue);
                                    newPost.child("image")
                                            .setValue(downloadUrl.toString());
                                    newPost.child("uploaded_time")
                                            .setValue(ServerValue.TIMESTAMP);
                                    newPost.child("uid")
                                            .setValue(auth.getCurrentUser().getUid());

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.d(TAG, "Image Upload cancelled");

                                }
                            });

                            progressDialog.dismiss();

                        /*// Start the MainActivity after the posting of image.
                        startActivity(new Intent(PostImageActivity.this, MainActivity.class));*/
                        }
                    });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check the requestCode
        if (requestCode == GALLERY_REQUEST
                && resultCode == RESULT_OK) {

            // Store the path of the selected image
            selectedImageUri = data.getData();

            // Set the ImageButton to the path value
            selectedImage.setImageURI(selectedImageUri);

            // Disable the ImageButton
            selectedImage.setClickable(false);
        } else {
            // If the requestCode is different
            // set the image to default value
            selectedImage
                    .setBackground(getResources()
                            .getDrawable(R.drawable.ic_account_circle_black_48dp));
        }
    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(mAuthListener);
    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            auth.removeAuthStateListener(mAuthListener);
        }
    }

}
