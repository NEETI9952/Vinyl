package com.example.vinyl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Callback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 105;

     FirebaseFirestore db= FirebaseFirestore.getInstance();
     FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
     String uid = user.getUid();
     DocumentReference currentUserDocumentReference = db.collection("Users").document(uid);

    public static StorageReference storageReference;
    String currentPhotoPath;
    CircleImageView profileImage;
    TextInputLayout username,userStatus;
    private String imageUrl;
    ProgressBar p1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

//        getActionBar().setTitle("Profile");

        profileImage=findViewById(R.id.profilePicture);
        username=findViewById(R.id.fullNameProfile);
        userStatus=findViewById(R.id.StatusProf);

        int Permission_All=1;
        String[] Permissions = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE };
        if(!hasPermissions(this, Permissions)){
            ActivityCompat.requestPermissions(Profile.this, Permissions, Permission_All);
        }

        currentUserDocumentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(!(documentSnapshot.getString("Username") =="")){
                            username.getEditText().setText(documentSnapshot.getString("Username"));
                        }

                        if(!(documentSnapshot.getString("Status") =="")) {
                            userStatus.getEditText().setText(documentSnapshot.getString("Status"));
                        }

                        if(!(documentSnapshot.getString("profileImage") =="")) {
                            try {
                                Picasso.get().load(imageUrl).into(profileImage, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        p1.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                    }
                                });
                            }catch(Exception e){

                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Profile","Couldnt load profile");

                    }
                });

        username.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                currentUserDocumentReference.update("Username",username.getEditText().getText().toString().trim());
            }
        });

        userStatus.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                currentUserDocumentReference.update("Status",userStatus.getEditText().getText().toString().trim());
            }
        });
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        Log.i("testingpermission", "reached");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                Log.i("testingpermission", "reached3");
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.i("testingpermission", "reached5");
                    return false;
                }
            }
        }
        return true;
    }

    public void ProfilePictureClick(View v){
        galleryCameraDialogBox();
    }

    private void galleryCameraDialogBox(){
        MaterialAlertDialogBuilder builder=new MaterialAlertDialogBuilder(Profile.this);
        builder.setIcon(R.drawable.addimage);
//                builder.setBackground(getResources().getDrawable(R.drawable.alertdiabogbg,null));
        builder.setMessage("Choose image source...").setPositiveButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cameraPermission();
            }
        }).setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery, GALLERY_REQUEST_CODE);
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                File f = new File(currentPhotoPath);
//                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(f);
//                mediaScanIntent.setData(contentUri);
//                this.sendBroadcast(mediaScanIntent);
//                selectedImageFront.setImageURI(Uri.fromFile(f));

                Log.i("testimageupload", Uri.fromFile(f).toString());
//                issueImage.setImageURI(Uri.fromFile(f));
//                issueImage.setTag("uploaded");
                uploadImageToFirebase(f.getName(),contentUri);
            }}


        /////////////Gallery

        if (requestCode == GALLERY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                Uri contentUri = data.getData();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "." + getFileExt(contentUri);
                Log.d("testimage", "onActivityResult: Gallery Image Uri:  " + imageFileName);
//                selectedImageFront.setImageURI(Uri.fromFile(f));
//                        Log.i("testimageupload", Uri.fromFile(f).toString());
//                issueImage.setImageURI(contentUri);
//                issueImage.setTag("uploaded");
                uploadImageToFirebase(imageFileName,contentUri);
            }}
    }

    public void cameraPermission() {
        if (ContextCompat.checkSelfPermission(Profile.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Profile.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
            if(ContextCompat.checkSelfPermission(Profile.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
            }
        } else {
            Log.i("testimageupload", "2");
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent () {
        Log.i("testimageupload", "3");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Log.i("testimageupload", "4");
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(Profile.this.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            Log.i("testimageupload", "5");
            try {
                photoFile = createImageFile();
                Log.i("testimageupload", "6");
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i("testimageupload", "7");

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.i("testimageupload", "8");
                Uri photoURI = FileProvider.getUriForFile(Profile.this,
                        "com.example.android.fileprovider",
                        photoFile);
                Log.i("testimageupload", "9");
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }
    private String getFileExt(Uri contentUri) {
//        private String getFileExt(Uri contentUri) {
        ContentResolver c = Profile.this.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
//        }
    }
    private File createImageFile () throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_"+uid;
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Profile.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void uploadImageToFirebase(String name, Uri contentUri) {
        p1.setVisibility(View.VISIBLE);
        Profile.this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        Log.i("TestingImageUpload",name.toString());
        storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference imageStorageReference = storageReference.child("PlaceImages/"+ name);
        imageStorageReference.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Log.i("testimageupload", "image uploaded to firebase storage");
                imageStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.i("testimageupload", "DownloadedUri: " + uri);

                        imageUrl=uri.toString();

                        LoadImage();
                        Profile.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("testimageupload", "Downloading Uri failed");
                        Log.i("testimageupload", "Dow: "+ e);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("testimageupload", "image not uploaded to firebase storage");
                Log.i("testimageupload", "Dow: "+ e);
            }
        });
    }


    public void LoadImage(){
        try {
            Picasso.get().load(imageUrl).into(profileImage, new Callback() {
                @Override
                public void onSuccess() {
                    p1.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                }
            });
        }catch(Exception e){

        }
    }
}