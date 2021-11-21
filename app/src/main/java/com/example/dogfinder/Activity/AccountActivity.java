package com.example.dogfinder.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;


import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.dogfinder.MainActivity;
import com.example.dogfinder.R;
import com.example.dogfinder.Utils.TextUtil;
import com.example.dogfinder.Utils.profilePopUpUtil;
import com.example.dogfinder.Utils.strayPopUpUtil;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountActivity extends BaseActivity {
    String photoPath;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int WRITE_PERM_CODE = 1;
    public static final int GALLERY_PROFILE_REQUEST_CODE = 105;
    CircleImageView profile_image;
    EditText username_field,password_field;
    TextView email_field;
    Button save_btn,back_btn;
    FirebaseAuth auth;
    StorageReference storageReference;
    FirebaseFirestore firebaseFirestore;
    DocumentReference documentReference;
    String username,email,imageUri,password;
    Uri image,imageUpdate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        password_field = findViewById(R.id.password_field);
        profile_image = findViewById(R.id.profile_photo);
        username_field = findViewById(R.id.username_field);
        email_field = findViewById(R.id.email_field);
        save_btn = findViewById(R.id.save_btn);
        back_btn = findViewById(R.id.back_profile);


        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate(ProfileActivity.class);
                finish();
            }
        });
        auth = FirebaseAuth.getInstance();

        storageReference = FirebaseStorage.getInstance().getReference("users");
        firebaseFirestore = FirebaseFirestore.getInstance();
        documentReference = firebaseFirestore.collection("users").document(auth.getCurrentUser().getUid());
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popProfileFormBottom();
            }
        });
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });
    }

    public void popProfileFormBottom() {
        profilePopUpUtil popup = new profilePopUpUtil(this, onClickListener);
        popup.showAtLocation(findViewById(R.id.profileLayout), Gravity.BOTTOM|Gravity.CENTER, 0, 0);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.camera_btn:
                    askCameraPermission();
                    break;
                case R.id.gallery_btn:
                    askProfileGalleryPermission();
                    break;
            }
        }
    };
    public void updateProfile() {
        String password1 = password_field.getText().toString().trim();
        String username1 = username_field.getText().toString().trim();
        if (image != null) {

            StorageReference storageReference1 = storageReference.child(System.currentTimeMillis() + "." + getExtension(image));
            UploadTask uploadTask = storageReference1.putFile(image);
            Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return storageReference1.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(Task<Uri> task) {
                    imageUpdate = task.getResult();
                    firebaseFirestore.runTransaction(new Transaction.Function<Object>() {
                        @Nullable
                        @Override
                        public Object apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.getResult().exists()){
                                        String imageUri = task.getResult().getString("image");
                                        if(imageUri != null){
                                            StorageReference fileReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUri);
                                            fileReference.delete();
                                        }
                                    }else {
                                        showToast("No profile exists.");
                                        auth.signOut();
                                        navigate(MainActivity.class);
                                        finish();
                                    }
                                }
                            });
                            transaction.update(documentReference, "username", username1);
                            transaction.update(documentReference, "image", imageUpdate.toString());

                            return null;
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Object>() {
                        @Override
                        public void onSuccess(Object o) {
                            if(password1.equals(password)){
                                auth.sendPasswordResetEmail(email);
                                showToast("Please check the email to reset the password.");
                                auth.signOut();
                                navigate(MainActivity.class);
                                finish();
                            }else if(TextUtil.isEmpty(password1)){
                                navigate(ProfileActivity.class);
                                finish();
                            }else if(!password1.equals(password)){
                                password_field.setError("Wrong password.");
                            }
                        }
                    });
                }
            });
        } else {
            firebaseFirestore.runTransaction(new Transaction.Function<Object>() {
                @Nullable
                @Override
                public Object apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                    //DocumentSnapshot snapshot = transaction.get(documentReference);
                    transaction.update(documentReference, "username", username1);
                    //transaction.update(documentReference,"image",image_path);
                    return null;
                }
            }).addOnSuccessListener(new OnSuccessListener<Object>() {
                @Override
                public void onSuccess(Object o) {
                    if(password1.equals(password)){
                        auth.sendPasswordResetEmail(email);
                        showToast("Please check the email to reset the password.");
                        auth.signOut();
                        navigate(MainActivity.class);
                        finish();
                    }else if(TextUtil.isEmpty(password1)){
                        navigate(ProfileActivity.class);
                        finish();
                    }else if(!password1.equals(password)){
                        password_field.setError("Wrong password.");
                    }
                }
            });
        }
    }
    @Override
    protected void onStart(){
        super.onStart();
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.getResult().exists()){
                    username = task.getResult().getString("username");
                    email = task.getResult().getString("email");
                    imageUri = task.getResult().getString("image");
                    password = task.getResult().getString("password");
                    username_field.setText(username);
                    email_field.setText(email);
                    if(imageUri == null){
                        profile_image.setImageResource(R.mipmap.profile);
                    }else{
                        Picasso.with(getApplicationContext()).load(imageUri).into(profile_image);
                    }
                    resetImage();
                }else {
                    showToast("No profile exists.");
                    auth.signOut();
                    navigate(MainActivity.class);
                    finish();
                }
            }
        });
    }
    private void askProfileGalleryPermission() {
        Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //pickImagePermission();
        galleryIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        galleryIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(galleryIntent,GALLERY_PROFILE_REQUEST_CODE);
    }

    private void askCameraPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            getCameraIntent();
        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},CAMERA_PERM_CODE);
        }
    }
    private void pickImagePermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            getCameraIntent();
        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[]permissions, @NonNull int[] grantResults){
        if(requestCode == CAMERA_PERM_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCameraIntent();
            }else{
                showToast("Camera permission is required.");
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                File f = new File(photoPath);
                image = Uri.fromFile(f);
                Intent intent = new Intent(AccountActivity.this,AccountActivity.class);
                intent.putExtra("image",image);
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_PERM_CODE);
                }
                //save image into gallery
                BitmapDrawable bitmapDrawable = (BitmapDrawable) profile_image.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                saveToGallery(bitmap);
                startActivity(intent);
                finish();
            }
        }
        if(requestCode == GALLERY_PROFILE_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                image = data.getData();
                Intent intent = new Intent(AccountActivity.this,AccountActivity.class);
                intent.putExtra("image",image);
                startActivity(intent);
                finish();
            }
        }
    }
    public void resetImage(){
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            image = (Uri)bundle.get("image");
            profile_image.setImageURI(image);
        }
    }
    private File createImageUri() throws IOException {
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = time+"_pic";
        File storeDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File storeDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(fileName,".jpg",storeDir);
        photoPath = image.getAbsolutePath();
        return image;
    }
    private void getCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageUri();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            }
        }
    }
    //save image to gallery
    public void saveToGallery(Bitmap bitmap) {
        OutputStream os;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "Img_" + ".jpg");
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            try {
                os = resolver.openOutputStream(Objects.requireNonNull(imageUri));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                Objects.requireNonNull(os);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    public String getExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

}