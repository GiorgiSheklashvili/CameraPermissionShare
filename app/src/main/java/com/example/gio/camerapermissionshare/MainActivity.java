package com.example.gio.camerapermissionshare;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST = 1;
    private static final int WRITE_STORAGE_PERMISSION_REQUEST = 2;
    private static final int CAMERA_REQUEST = 3;
    private static final String IMAGE_PATH = "IMAGE_PATH";
    private String lastPath;
    public TextView photoLastLocation;
    public ImageView imageView;
    Bitmap bitmap1;
    CallbackManager callbackmanager;
    LoginManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FacebookSdk.sdkInitialize(getApplicationContext());
        SavePreferences.initialize(this);
        photoLastLocation = (TextView) findViewById(R.id.photoLastLocation);
        imageView = (ImageView) findViewById(R.id.imageView1);
        loadImage();
    }

    public void OpenCamera(View view) {
        if (Tools.atLeastMarshmallow()) {
            requestCameraPermission();
        } else {
            openCamera();
        }
    }

    private File getExternalFile() {
        File f = new File(Environment.getExternalStorageDirectory() + "/GiTa/");
        if (!f.exists()) {
            f.mkdir();
        }
        File file = new File(f.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        lastPath = file.getAbsolutePath();
        return file;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            File f = new File(lastPath);
            try {
                f = fixImage(f);
            } catch (Exception e) {
                e.printStackTrace();
            }
            SavePreferences.saveString(IMAGE_PATH, lastPath);
            makeVisibleInGallery(f);
            loadImage();
        } else {
            callbackmanager.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void makeVisibleInGallery(File out) {
        if (Tools.atLeastKitKat()) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(out);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
        } else {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }

    private void loadImage() {
        String path = SavePreferences.getString(IMAGE_PATH);
        photoLastLocation.setText("Last photo location = " + path);
        Glide.with(this).load(path).into(imageView);
    }

    private File fixImage(File imageFile) throws Exception {
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;// amcirebs sawyis surats 4-jer sigrdze siganeshi
        bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        bitmap = fixExif(imageFile.getAbsolutePath(), bitmap, -1);
        File file = saveBitmap(imageFile, bitmap);
        bitmap1 = bitmap;
        bitmap.recycle();
        return file;
    }

    private Bitmap fixExif(String path, Bitmap bmp, int orientation) throws Exception {
        ExifInterface exif = new ExifInterface(path);
        int rotate = 0;

        if (orientation < 0) {
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } else {
            rotate = orientation;
        }

        if (rotate > 1) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotate);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            return bmp;
        }

        return bmp;
    }

    private File saveBitmap(File imageFile, Bitmap bmp) throws Exception {
        File file = new File(imageFile.getAbsolutePath());
        if (!file.exists()) {
            file.createNewFile();
        }

        FileOutputStream fOut = new FileOutputStream(imageFile);
        bmp.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
        fOut.flush();
        fOut.close();
        return file;
    }


    @TargetApi(23)
    private void requestCameraPermission() {
        int hasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
        List<String> permissions = new ArrayList<>();

        if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }
        if (!permissions.isEmpty()) {
            requestPermissions(permissions.toArray(new String[permissions.size()]), CAMERA_PERMISSION_REQUEST);
        } else if (hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission();
        }
    }

    @TargetApi(23)
    private void requestStoragePermission() {
        int hasStoragePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> permissions = new ArrayList<>();

        if (hasStoragePermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!permissions.isEmpty()) {
            requestPermissions(permissions.toArray(new String[permissions.size()]), WRITE_STORAGE_PERMISSION_REQUEST);
        } else if (hasStoragePermission == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        }
    }

    @Override
    @TargetApi(23)
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission();
                } else {
                    Toast.makeText(this, "No camera this time", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case WRITE_STORAGE_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(this, "No storage permission this time", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        File f = getExternalFile();
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        cameraIntent.putExtra("path", f.getAbsolutePath());
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    public void share(View view) {
        callbackmanager = CallbackManager.Factory.create();
        List<String> permissionNeeds = Arrays.asList("publish_actions");
        manager = LoginManager.getInstance();
        manager.logInWithPublishPermissions(this, permissionNeeds);
        manager.registerCallback(callbackmanager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                publishImage();
            }

            @Override
            public void onCancel() {
                System.out.println("onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                System.out.println("onError");
                Log.v("LoginActivity", error.getCause().toString());
            }
        });
    }

    private void publishImage() {
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(bitmap1)
                .setCaption("one two three")
                .build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();
        ShareApi.share(content, null);

    }

    public void simple_share(View view) {
        String path = lastPath;
        Bitmap icon = BitmapFactory.decodeFile(path);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(); // shevqmenit bytebis bufferi mexsierebashi
        icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);// davapataravet icon-i da shevinaxet bytes-shi
        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg"); // shevqmenit abstractuli faili
        try {
            f.createNewFile();// qmnis realur fails abstractuli failis misamartit
            FileOutputStream fo = new FileOutputStream(f); // monacemebis failshi chasaweri streami
            fo.write(bytes.toByteArray());// streamshi chavweret bytebi
        } catch (IOException e) {
            e.printStackTrace();
        }
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
        startActivity(Intent.createChooser(share, " Share Image"));

    }
}
