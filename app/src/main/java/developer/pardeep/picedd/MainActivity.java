package developer.pardeep.picedd;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button selectNewButton,existingHistoryButton;
    private static CharSequence[] selectOptions={"Select photo","Capture a photo","Cancel"};
    private static final int selectImage=1;
    private static String imageFullPath="";
    public static final String DATA_PATH= Environment.getExternalStorageDirectory().toString() + "/tesseract_languages/";
    public static final String lang = "eng";
    public boolean dirExist=false;
    public static boolean imageIsLoad=false;
    public static String imagePathInGallery=""; // this path is used to display the image from gallery
    public static String textOfImage=""; // this variable is for storing text of image temporary
    protected String _path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectNewButton = (Button) findViewById(R.id.selectphotoButton);
        existingHistoryButton = (Button) findViewById(R.id.seeExistingPhotoButton);
        selectNewButton.setOnClickListener(this);
        existingHistoryButton.setOnClickListener(this);

        Toolbar tool=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(tool);

        Log.d("data :", "" + DATA_PATH);

        /*
        Below Code is for creating the directory in external storage
         */

        File dir = new File(DATA_PATH + "tessdata/");
        if (!dir.exists()) {
            dir.mkdirs();
            dirExist = true;
            if (!dirExist) {
                Log.d("msg :", "ERROR: Creation of directory " + DATA_PATH + "tessdata/" + " on sdcard failed");
                return;
            } else {
                Log.d("msg :", "Created directory " + DATA_PATH + "tessdata/" + " on sdcard");
            }
        }

        /*
        Read the lang file from assets folder
        and copy in file of external storage
         */

        File file = new File(DATA_PATH + "tessdata/" + "eng.traineddata");
        if (!(file.exists())) {

            System.out.println("open file");

            AssetManager assetManager = getAssets();
            String[] files = null;
            try {
                files = assetManager.list("tessdata");
            } catch (IOException e) {
                Log.e("tag", e.getMessage());
            }


                //System.out.println("File name => " + filename);
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = assetManager.open("tessdata/" +"eng.traineddata");   // if files resides inside the "Files" directory itself
                    out = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/tesseract_languages/"+"tessdata/"+"eng.traineddata");
                    copyFile(in, out);
                    in.close();
                    in = null;
                    out.flush();
                    out.close();
                    out = null;
                } catch (Exception e) {
                    Log.d("tag error :", e.getMessage());
                }



            }


        }
    /*
    Function for copy assets folder to external storage for lang File
     */

    private void copyFile(InputStream in, OutputStream out) {

        byte[] buffer = new byte[1024];
        int read;
        try {
            System.out.println("File read");
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        if(v==selectNewButton){
            showSelectOptions();
        }

    }

    private void showSelectOptions() {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
        alertDialog.setItems(selectOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                switch (position){
                    case 0:
                        selectPhotoFromGallery(); //dialog option for select photo from gallery
                        break;
                    case 1:
                        capturePhoto();
                        break;
                    case 2:
                        dialog.dismiss();
                        break;
                    default:
                        dialog.dismiss();
                }
            }
        });
        AlertDialog dialog1=alertDialog.create();
        dialog1.show();
    }

    private void selectPhotoFromGallery() {
        Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, selectImage);
    }

    private void capturePhoto() {
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,selectImage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == selectImage && data != null) {
                System.out.println("Hello");
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imageFullPath = cursor.getString(columnIndex);
                System.out.println("image ...." + imageFullPath);
                cursor.close();
                imagePathInGallery = imageFullPath; // give the image path to display image that hoose by user

           /* Bitmap bitmap= BitmapFactory.decodeFile(imageFullPath);
            System.out.println("bitmap... :" + bitmap);

            String text=detectText(bitmap);
            textOfImage=text; // display text of image
            progreess.dismiss();
            Toast.makeText(MainActivity.this, "image text :"+text, Toast.LENGTH_LONG).show();
            changeFragment(new ImageShowFragment());*/

                new LoadImage(imageFullPath).execute();
            }
        }
    catch (Exception e){
        e.printStackTrace();
    }

    }

    private void changeFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,fragment,"fragment").setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack("fragment").commit();
    }

   /* public String detectText(Bitmap bitmap) {

        //TessDataManager.initTessTrainedData(context);
        progreess=new ProgressDialog(this);
        progreess.setMessage("Please wait..");
        progreess.show();
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.setDebug(true);
        tessBaseAPI.init(DATA_PATH, "eng"); //Init the Tess with the trained data file, with english language

        //For example if we want to only detect numbers
        //tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "1234567890");
       /*tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-qwertyuiop[]}{POIU" +
                "YTREWQasdASDfghFGHjklJKLl;L:'\"\\|~`xcvXCVbnmBNM,./<>?");*/


       /* tessBaseAPI.setImage(bitmap);

        String text = tessBaseAPI.getUTF8Text();

        Log.d("data :", "Got data: " + text);
        tessBaseAPI.end();


        return text;
    }*/
    public class LoadImage extends AsyncTask<String,Boolean,Void>{

        private String imageUrl="";
           ProgressDialog progressDialog;

       public LoadImage(String imageFullPath){
         /*  progressDialog=new ProgressDialog(getApplicationContext());
           progressDialog.setMessage("Please wait");
            progressDialog.show();*/
           this.imageUrl=imageFullPath;

        }

           @Override
           protected void onPreExecute() {
               super.onPreExecute();
               progressDialog=new ProgressDialog(MainActivity.this);
               progressDialog.setMessage("Please wait..");
               progressDialog.setIndeterminate(false);
               progressDialog.setCancelable(false);
               progressDialog.setProgress(0);
               progressDialog.show();
           }
           @Override
        protected Void doInBackground(String... params) {
            //imageUrl=params[0];
           /* Bitmap bitmap=BitmapFactory.decodeFile(imageFullPath);*/
          //  progressDialog.dismiss();
               performOcr(imageFullPath);
            return null;
        }

           @Override
           protected void onPostExecute(Void aVoid) {
               super.onPostExecute(aVoid);
               progressDialog.dismiss();
               changeFragment(new ImageShowFragment());

           }
           private void performOcr(String imagePath){

               Bitmap bitmap=BitmapFactory.decodeFile(imageFullPath);

               try {
                   ExifInterface exif = new ExifInterface(imagePath);
                   int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                   Log.v("Image :", "Orient: " + exifOrientation);

                   int rotate = 0;
                   switch (exifOrientation) {
                       case ExifInterface.ORIENTATION_ROTATE_90:
                           rotate = 90;
                           break;
                       case ExifInterface.ORIENTATION_ROTATE_180:
                           rotate = 180;
                           break;
                       case ExifInterface.ORIENTATION_ROTATE_270:
                           rotate = 270;
                           break;
                   }

                   Log.v("Pic Rotate:", "Rotation: " + rotate);

                   if (rotate != 0) {

                       // Getting width & height of the given image.
                       int w = bitmap.getWidth();
                       int h = bitmap.getHeight();

                       // Setting pre rotate
                       Matrix mtx = new Matrix();
                       mtx.preRotate(rotate);

                       // Rotating Bitmap
                       bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
                       // tesseract req. ARGB_8888
                       bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                   }


               } catch (IOException e) {
                   e.printStackTrace();
               }
               TessBaseAPI tessBaseAPI = new TessBaseAPI();
               tessBaseAPI.setDebug(true);
               tessBaseAPI.init(DATA_PATH, "eng");
               tessBaseAPI.setImage(bitmap);
               String text = tessBaseAPI.getUTF8Text();
               Log.d("data :", "Got data: " + text);
               tessBaseAPI.end();
               textOfImage=text;
               imageIsLoad=true;




               /*BitmapFactory.Options options = new BitmapFactory.Options();
               options.inSampleSize = 4;

               Bitmap bitmap = BitmapFactory.decodeFile(imageFullPath, options);
               try {
                   ExifInterface exif = new ExifInterface(_path);
                   int exifOrientation = exif.getAttributeInt(
                           ExifInterface.TAG_ORIENTATION,
                           ExifInterface.ORIENTATION_NORMAL);

                   Log.v("Tag :", "Orient: " + exifOrientation);

                   int rotate = 0;

                   switch (exifOrientation) {
                       case ExifInterface.ORIENTATION_ROTATE_90:
                           rotate = 90;
                           break;
                       case ExifInterface.ORIENTATION_ROTATE_180:
                           rotate = 180;
                           break;
                       case ExifInterface.ORIENTATION_ROTATE_270:
                           rotate = 270;
                           break;
                   }

                   Log.v("Tag :", "Rotation: " + rotate);

                   if (rotate != 0) {

                       // Getting width & height of the given image.
                       int w = bitmap.getWidth();
                       int h = bitmap.getHeight();

                       // Setting pre rotate
                       Matrix mtx = new Matrix();
                       mtx.preRotate(rotate);

                       // Rotating Bitmap
                       bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
                   }

                   // Convert to ARGB_8888, required by tess
                   bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

               } catch (IOException e) {
                   Log.e("Tag :", "Couldn't correct orientation: " + e.toString());
               }

               // _image.setImageBitmap( bitmap );

               Log.v("Tag :", "Before baseApi");

               TessBaseAPI baseApi = new TessBaseAPI();
               baseApi.setDebug(true);
               baseApi.init(DATA_PATH, lang);
               baseApi.setImage(bitmap);

               String recognizedText = baseApi.getUTF8Text();

               baseApi.end();

               // You now have the text in recognizedText var, you can do anything with it.
               // We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
               // so that garbage doesn't make it to the display.
               Log.v("Tag :", "OCRED TEXT: " + recognizedText);

               if ( lang.equalsIgnoreCase("eng") ) {
                   recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
               }

               recognizedText = recognizedText.trim();

              *//* if ( recognizedText.length() != 0 ) {
                   _field.setText(_field.getText().toString().length() == 0 ? recognizedText : _field.getText() + " " + recognizedText);
                   _field.setSelection(_field.getText().toString().length());
               }*/
               
       }

       }
}
