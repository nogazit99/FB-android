package com.example.foobar.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

//import com.example.foobar.adapters.Adapter_Feed;
import com.example.foobar.R;
import com.example.foobar.adapters.Adapter_Feed;
import com.example.foobar.entities.Post_Item;
import com.example.foobar.webApi.UserPostsAPI;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class AddPostWindow extends AppCompatActivity  {

    public interface PostIdUpdater {
        void updateNextPostId();
    }

    public interface OnPostAddedListener {
        void onPostAdded(Post_Item newPost);
        Adapter_Feed getAdapter(); // New method to retrieve the adapter

    }


    private static final int REQUEST_IMAGE_PICK = 1;

    private EditText postContentEditText;
    private Button submitButton;
    private ImageView attachedImageView;
    private ImageButton attachImageButton;

    private Uri selectedImageUri;
    public static OnPostAddedListener listener;
    private FeedActivity feedActivity; // Reference to FeedActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_post_popup);

        int nextPostId = getIntent().getIntExtra("nextPostId", 0);

        listener = AddPostWindow.listener; // Retrieve the listener from the static variable
        if (listener == null) {
            Toast.makeText(this, "Error: Listener not passed properly", Toast.LENGTH_SHORT).show();
            finish(); // Finish the activity if listener is not provided
            return;
        }

        // Find the EditText for post content and the submit button
        postContentEditText = findViewById(R.id.editTextMessage);
        submitButton = findViewById(R.id.buttonPost);
        attachImageButton = findViewById(R.id.imageViewAttach);
        attachedImageView = findViewById(R.id.attachedImageView);

        attachImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_PICK);
            }
        });
        // Set click listener for the submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the content of the post from the EditText
                String postContent = postContentEditText.getText().toString();
                String currentTime = getCurrentTime();
                // Retrieve username and profile picture URI from intent extras
                String username = getIntent().getStringExtra("username");
                String profilePicture = getIntent().getStringExtra("profilePicture");

                // Check if the user has provided both content and an image
                if (postContent.isEmpty() && selectedImageUri == null) {
                    // Show an error message to the user
                    Toast.makeText(AddPostWindow.this, "Please enter post content and attach an image", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Convert the selected image URI to a base64-encoded string
                String imageBase64 = null;
                if (selectedImageUri != null) {
                    try {
                        Bitmap bitmap = getBitmapFromUri(selectedImageUri);
                        imageBase64 = bitmapToBase64(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // Create a new Post_Item object with the post content and base64-encoded image
                Post_Item newPost = new Post_Item(postContent, imageBase64, username, false);


                // Notify the listener that a new post has been added
                if (listener != null) {
                    listener.onPostAdded(newPost);
                }

                // Increment the next post ID for the next post
                if (feedActivity != null) {
                    feedActivity.updateNextPostId();
                }

                // Finish the activity
                finish();
            }
        });
        ImageButton closeButton = findViewById(R.id.buttonClose);
        // Set click listener for the close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the current activity and go back to the previous one
                finish();
            }
        });

    }

    // Default constructor
    public AddPostWindow() {
        // Default constructor must be empty
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the listener when the activity is destroyed
        listener = null;
    }

    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

//    private void addToFeed(Post_Item newPost) {
//        // Directly call getAdapter() method from the listener
//            Adapter_Feed adapter = listener.getAdapter();
//            if (adapter != null) {
//                adapter.getPosts().add(0, newPost);
//                adapter.notifyDataSetChanged();
//            }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            // Get selected image URI
            selectedImageUri = data.getData();

            // Display the selected image in ImageView
            attachedImageView.setImageURI(selectedImageUri);
            attachedImageView.setVisibility(View.VISIBLE); // Make ImageView visible
        }
    }




    // Convert URI to Bitmap
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        InputStream input = getContentResolver().openInputStream(uri);
        return BitmapFactory.decodeStream(input);
    }

    // Convert Bitmap to base64 string
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

}
