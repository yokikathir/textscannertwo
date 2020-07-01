package com.ppms.textscannertwo;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;
import com.ppms.textscannertwo.Camera.FileCompressor;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class Main2Activity extends BaseActivity implements View.OnClickListener {
    public static final int MULTIPLE_PERMISSIONS = 10;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CONTACTS = 1;
    static String ServerKey = null;
    private ArrayList<String> permissions = new ArrayList<>();
    private static String[] PERMISSIONS_CONTACT = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private ArrayList<String> permissionsToRequest;
    private final static int ALL_PERMISSIONS_RESULT = 101;
    File mPhotoFile;
    FileCompressor mCompressor;
    AutoCompleteTextView autoCompleteTextView;
    EditText editetext;
    String items;
    String autoitems;
    String fullTxt;
    String word_search;
    String[] languages = {"Android ", "java", "IOS", "SQL", "JDBC", "Web services"};
    String[] array_fullTxt;
    private Bitmap myBitmap;
    private Button searchbtn;
    private ImageView myImageView;
    private TextView myTextView, gpstextview;
    private HashMap<String, Integer> textMap;
    private boolean isResults = true;
    int printsimilarity=0;
    int frontprintsimilarity=0;
    double longitude;
    double latitude;
    String datetimee;
    GPSTracker locationTrack;
    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0; /* both strings are zero length */
        }
        /* // If you have Apache Commons Text, you can use it to calculate the edit distance:
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        return (longerLength - levenshteinDistance.apply(longer, shorter)) / (double) longerLength; */
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

    }

    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button searchbtn = findViewById(R.id.searchbtn);
        // autoCompleteTextView=findViewById(R.id.auto);
        editetext = findViewById(R.id.editetext);
        gpstextview=findViewById(R.id.textView1);
        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }
        if (!checkPermissions()) {
            isRuntimePermission();
        }
        mCompressor = new FileCompressor(this);
        myTextView = findViewById(R.id.textView);
        /*myTextView.setText("smart kathiravan family object  hello welcome");

        fullTxt = myTextView.getText().toString();*/
        myImageView = findViewById(R.id.imageView);
        findViewById(R.id.checkText).setOnClickListener(this);
        findViewById(R.id.camera).setOnClickListener(this);

      /*  fullTxt = myTextView.getText().toString();
        if (fullTxt!=null) {
            String[] arrayy = fullTxt.split("\n");
            String[] markierty = new String[arrayy.length];
            ArrayAdapter adapterr = new
                    ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, markierty);

            autoCompleteTextView.setAdapter(adapterr);
            autoCompleteTextView.setThreshold(1);
        }*/
        autoitems = editetext.getText().toString();

        editetext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


              /* int countt=0;
                for (int j=0; j<s.length();j++){
                    for (int k=0; k<fullTxt.length();k++){
                        Log.e("equalstring",":"+s.charAt(j)+"---->"+fullTxt.charAt(k));
                        if (s.charAt(j)==(fullTxt.charAt(k))){
                            countt++;
                        }
                    }
                    Log.e("DuplicateCount", ":" + s.charAt(j) + "    " + countt);
                    String str = String.valueOf(s.charAt(j));
                    s = fullTxt.replace(str, "");
                    countt = 0;

                }*/
                word_search = s.toString();


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//OCR search text
                boolean isResultsone = true;
                boolean isResultstwo = true;
                String subinputone=null;
                String subinputtwo=null;
                isResults = true;
                int count = 0;
                String main = null;
                String string1 = null;
                //Reverse concept
                if (word_search != null) {
                    if (fullTxt != null) {
                        main = word_search;
// Extract all the words whose length is > 1 and remove duplicates
                        Set<String> mainWords = new HashSet<>();
                        for (String s : main.split("\\W")) {
                            if (s.length() > 1) {
                                mainWords.add(s.toLowerCase());
                            }
                        }
                        //String string1 = "hi people what is the need................ how to ................... do the needful.................................. and you can only need the change......................................... in the fine situation................................................. to do the task................... i am alright............... are you okay?";
                        string1 = fullTxt.replaceAll("[-+.^:,!@#$%&*()/?]","").toLowerCase();
                        ArrayList<String> mainWordsToFind = new ArrayList<>(mainWords);
                        Log.e("mainWordsToFind", ":" + mainWordsToFind);
                        ArrayList<String> threewords = new ArrayList<>();
                        String mainString = null;
                        String threeString = null;

                        String listString = "";
                        String finallistString = "";

                        for (String s : mainWordsToFind) {
                            listString += s;
                            finallistString += s;

                        }
                        if (listString.length() >= 3) {
                            int spaceCount = 0;
                            int poscount = 0;
                            String replacedTxt = string1.replaceAll("[^A-Za-z0-9][-+.^:,!@#$%&*()/?]", "");
                            String firststring = replacedTxt.replace(" ","");
                            String secondstring = main.trim();
                            String lastindexenterstring = main.trim();
                            String ocrtext = firststring;
                            String inputt=secondstring.replaceAll("[-+.^:,!@#$%&*()/?]","").toLowerCase();
                            String input = inputt.replace(" ","");
                            String maininput = input;
                            String out = null;
                            int pos = 0;
                            int totalpos = 0;
                            int inputtotalpos = 0;
                            ArrayList<String> imp = new ArrayList<>();
                            ArrayList<Character> posadd = new ArrayList<>();
                            boolean containsis = true;
                            boolean notcontains = true;
                            for (int i = 0; i <= input.length(); i++) {
                                if (containsis = true) {

                                    if (ocrtext.contains(maininput)) {
                                        containsis = false;
                                        pos = ocrtext.lastIndexOf(maininput);
                                        totalpos = pos + maininput.length();
                                        if (maininput.length() == input.length()) {
                                            inputtotalpos = maininput.length();
                                        } else {
                                            inputtotalpos = maininput.length() + pos + poscount;
                                        }
                                        break;
                                    } else if (ocrtext.contains(maininput.substring(1))) {
                                        subinputone = maininput.substring(1);
                                        isResultsone = false;

                                    } else if (ocrtext.contains(maininput.substring(2))) {
                                        subinputtwo = maininput.substring(2);
                                        isResultstwo = false;

                                    } else {
                                        notcontains = false;
                                        imp.add(maininput.substring(maininput.length() - 1).trim());
                                        maininput = (maininput.substring(0, maininput.length() - 1));
                                        poscount++;
                                    }

                                }

                            }
                            if (isResultsone == false) {
                                printSimilarity(subinputone, maininput);
                            } else if (isResultstwo == false) {
                                printSimilarity(subinputtwo, maininput);
                            }
                            if (isResultstwo && isResultsone) {
                                String total;
                                // if (notcontains=false){
                                for (int i = totalpos; i < inputtotalpos; i++) {
                                    posadd.add(ocrtext.charAt(i));
                                }
                                StringBuilder builder = new StringBuilder();
                                for (Character s : posadd) {
                                    builder.append(s);
                                }
                                String str = builder.reverse().toString();
                                StringBuffer stb = new StringBuffer(str);
                                stb.reverse();
                                total = maininput + stb;
                                 printSimilarity(total.trim(), input);
                                Log.e("sssssssssssss", ":" + total);
                            }
                        }

                    } else {
                        Toast.makeText(Main2Activity.this, "OCR text are empty", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(Main2Activity.this, "Please enter the text", Toast.LENGTH_LONG).show();
                }
//Front concept
/*
                if (word_search != null) {
                    if (fullTxt != null) {
                      String frontsubinputone=null;
                      String frontsubinputtwo=null;
                        main = word_search;
// Extract all the words whose length is > 1 and remove duplicates
                        Set<String> mainWords = new HashSet<>();
                        for (String s : main.split("\\W")) {
                            if (s.length() > 1) {
                                mainWords.add(s.toLowerCase());
                            }
                        }
                        string1 = fullTxt.replace(" ","").toLowerCase();
                        ArrayList<String> frontmainWordsToFind = new ArrayList<>(mainWords);
                        Log.e("mainWordsToFind", ":" + frontmainWordsToFind);
                        String mainString = null;
                        String threeString = null;

                        String listString = "";
                        String finallistString = "";

                        for (String s : frontmainWordsToFind) {
                            listString += s;
                            finallistString += s;

                        }
                        if (listString.length() >= 3) {
                            int spaceCount = 0;
                            int frontposcount = 0;
                            String replacedTxt = string1.replaceAll("[^A-Za-z0-9]", "");
                            String firststring = replacedTxt;
                            String secondstring = main.trim();
                            String lastindexenterstring = main.trim();
                            String ocrtext = firststring;
                            String input = secondstring.replace(" ","").toLowerCase();
                            String frontmaininput = input;
                            String out = null;
                            int frontpos = 0;
                            int fronttotalpos = 0;
                            int frontinputtotalpos = 0;
                            ArrayList<String> frontimp = new ArrayList<>();
                            ArrayList<Character> frontposadd = new ArrayList<>();
                            boolean containsis = true;
                            boolean notcontains = true;
                            for (int i = 0; i <= input.length(); i++) {
                                if (containsis = true) {

                                    if (ocrtext.contains(frontmaininput)) {
                                        containsis = false;
                                        frontpos = ocrtext.lastIndexOf(frontmaininput);
                                        fronttotalpos = frontpos + frontmaininput.length();
                                        if (frontmaininput.length() == input.length()) {
                                            frontinputtotalpos = frontmaininput.length();
                                        } else {
                                            frontinputtotalpos = frontmaininput.length() + frontpos + frontposcount;
                                        }
                                        break;
                                    }*//* else if (ocrtext.contains(frontmaininput.substring(1))) {
                                        frontsubinputone = frontmaininput.substring(1);
                                        isResultsone = false;

                                    } else if (ocrtext.contains(frontmaininput.substring(2))) {
                                        frontsubinputtwo = frontmaininput.substring(2);
                                        isResultstwo = false;

                                    }*//* else {
                                        notcontains = false;
                                        frontimp.add(frontmaininput.substring(1));
                                        frontmaininput = (frontmaininput.substring(1));
                                        frontposcount++;
                                    }

                                }

                            }
                            if (isResultsone == false) {
                                secondprintSimilarity(frontsubinputone, frontmaininput);
                            } else if (isResultstwo == false) {
                                secondprintSimilarity(frontsubinputtwo, frontmaininput);
                            }
                            if (isResultstwo && isResultsone) {
                                String total;
                                // if (notcontains=false){
                                for (int i = fronttotalpos; i < frontinputtotalpos; i++) {
                                    frontposadd.add(ocrtext.charAt(i));
                                }
                                StringBuilder builder = new StringBuilder();
                                for (Character s : frontposadd) {
                                    builder.append(s);
                                }
                                String str = builder.reverse().toString();
                                StringBuffer stb = new StringBuffer(str);
                                stb.reverse();
                                total = frontmaininput + stb;
                                secondprintSimilarity(total.trim(), input);
                                Log.e("sssssssssssss", ":" + total);
                            }
                        }

                    } else {
                        Toast.makeText(Main2Activity.this, "OCR text are empty", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(Main2Activity.this, "Please enter the text", Toast.LENGTH_LONG).show();
                }*/
            }
        });


    }

    //----------------------------------------------------------------------------------------------------------------


//-------------------------------------------------------------------------------------------------------------------------------------------
    public void printSimilarity(String s, String t) {
        Toast.makeText(Main2Activity.this, similarity(s, t) * 100 + "% match"+"\nOCR TEXT     " +s.toString()+"\nINTPUT TEXT     "+t.toString(), Toast.LENGTH_LONG).show();
        System.out.println(String.format("%.3f is the similarity between \"%s\" and \"%s\"", similarity(s, t), s, t));

        printsimilarity= (int) (similarity(s, t) * 100);
        Log.e("results--->", ":" +printsimilarity);
      //  results();

    }
   /* public void secondprintSimilarity(String s, String t) {
       // Toast.makeText(Main2Activity.this, similarity(s, t) * 100 + "% match"+"\nOCR TEXT     " +s.toString()+"\nINTPUT TEXT     "+t.toString(), Toast.LENGTH_LONG).show();
        System.out.println(String.format("%.3f is the similarity between \"%s\" and \"%s\"", similarity(s, t), s, t));
        Log.e("results--->", ":" + s + "--->" + t);
        frontprintsimilarity= (int) (similarity(s, t) * 100);
        results();
    }*/
   /* public void results(){
        if (frontprintsimilarity<printsimilarity){
            Toast.makeText(Main2Activity.this, printsimilarity+ "% match", Toast.LENGTH_LONG).show();

        }else {
            Toast.makeText(Main2Activity.this, frontprintsimilarity+ "% match", Toast.LENGTH_LONG).show();

        }
    }*/

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.checkText:
                if (myBitmap != null) {
                    runTextRecog();
                }
                break;
            case R.id.camera:
                //Location track
                locationTrack = new GPSTracker(Main2Activity.this);


                String currentDateAndTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());



                datetimee= String.valueOf(currentDateAndTime);
                if (locationTrack.canGetLocation()) {


                    longitude = locationTrack.getLongitude();
                    latitude = locationTrack.getLatitude();
                    dispatchTakePictureIntent();
                    Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude)+"\n"+datetimee, Toast.LENGTH_SHORT).show();
                } else {

                    locationTrack.showSettingsAlert();
                }
               // dispatchTakePictureIntent();
               /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!isRuntimePermission()) {
                        requestCameraPermission();
                    } else {
                        dispatchTakePictureIntent();

                    }

                } else {
                    dispatchTakePictureIntent();
                }*/

                break;
        }
    }

    /*
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    case WRITE_STORAGE:
                        checkPermission(requestCode);
                        break;
                    case SELECT_PHOTO:
                        Uri dataUri = data.getData();
                        String path = MyHelper.getPath(this, dataUri);
                        if (path == null) {
                            myBitmap = MyHelper.resizePhoto(photo, this, dataUri, myImageView);
                        } else {
                            myBitmap = MyHelper.resizePhoto(photo, path, myImageView);
                        }
                        if (myBitmap != null) {
                            myTextView.setText(null);
                            myImageView.setImageBitmap(myBitmap);
                        }
                        break;

                }
            }
        }*/
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);

                mPhotoFile = photoFile;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
    public Bitmap combineImages(Bitmap background, Bitmap foreground) {


        Paint myCircle;
        Bitmap cs;
        cs = Bitmap.createBitmap(background.getWidth(), background.getHeight(), Bitmap.Config.ARGB_8888);

        //creating canvas by background image's width and height
        Canvas comboImage = new Canvas(cs);
        background = Bitmap.createScaledBitmap(background, background.getWidth(), background.getHeight(), true);

        //Drawing background to canvas
        comboImage.drawBitmap(background, 0, 0, null);

        //Drawing foreground (text) to canvas
        // comboImage.drawBitmap(foreground, myTextView.getCompoundPaddingLeft(),myTextView.getBottom(), null);
        Canvas myCanvas = new Canvas(foreground);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        paint.setTextSize(10);
        comboImage.drawBitmap(foreground, (gpstextview.getWidth() - foreground.getWidth()) / 2,  850, null);

        return cs;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                try {
                    mPhotoFile = mCompressor.compressToFile(mPhotoFile);
                    previewCapturedemployee(mPhotoFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }

    private void previewCapturedemployee(final File file) {

        try {

            String filePath = file.getPath();
            myBitmap = BitmapFactory.decodeFile(filePath);
            Log.e("mybitmap", ":" + myBitmap);


            try {

                if (myBitmap != null) {
                    gpstextview.setText("Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude) + "\n" + datetimee);
                    // myImageView.setImageBitmap(myBitmap);
                    gpstextview.post(new Runnable() {
                        @Override
                        public void run() {
                            //generate bitmap of textView by using getDrawingCache()
                            //  myTextView.setText("Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude));

                            gpstextview.buildDrawingCache();
                            Bitmap bmp = Bitmap.createBitmap(gpstextview.getDrawingCache());
                            myImageView.buildDrawingCache();
                            Bitmap bitmapBackground = myBitmap;

//                Bitmap bitmapBackground = mImageView.getDrawingCache();

//combining two bitmaps
                            Bitmap combined = combineImages(bitmapBackground, bmp);
                            ((ImageView) findViewById(R.id.imageView)).setImageBitmap(combined);
                        }
                    });
                }
            } catch (Exception e) {
                Toast.makeText(Main2Activity.this, "Something went wrong", Toast.LENGTH_LONG)
                        .show();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runTextRecog() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(myBitmap);
        FirebaseVisionTextDetector detector = FirebaseVision.getInstance().getVisionTextDetector();
        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText texts) {
                processExtractedText(texts);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure
                    (@NonNull Exception exception) {
                Toast.makeText(Main2Activity.this, "Exception", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void processExtractedText(FirebaseVisionText firebaseVisionText) {
        myTextView.setText(null);
        if (firebaseVisionText.getBlocks().size() == 0) {
            myTextView.setText(R.string.no_text);
            return;

        }
        for (FirebaseVisionText.Block block : firebaseVisionText.getBlocks()) {
            myTextView.append(block.getText());
           // myTextView.setText("smart family object kathiravan hello welcome");

            fullTxt = myTextView.getText().toString();

            String replacedTxt = fullTxt.replaceAll("[^A-Za-z0-9]", " ");
            array_fullTxt = replacedTxt.split(" ");

            textMap = new HashMap<>();
//        textMap.put("kathir", (int) similarity(name, "kathir") * 100);
//        textMap.put("kathira", (int) similarity(name, "kathira") * 100);
//        textMap.put("kathirava", (int) similarity(name, "kathirava") * 100);
//        textMap.put("kathiravan", (int) similarity(name, "kathiravan") * 100);
//        textMap.put("kathvan", (int) similarity(name, "kathvan") * 100);
//        textMap.put("kathin", (int) similarity(name, "kathin") * 100);
//        textMap.put("sfsff", (int) similarity(name, "sfsff") * 100);


        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new java.util.Date());
        String mFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File mFile = File.createTempFile(mFileName, ".jpg", storageDir);
        return mFile;
    }

    private boolean isRuntimePermission() {

        boolean value = true;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {

            return true;
        }
    }

    private void requestCameraPermission() {
        Log.i("TAG", "CAMERA permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(camera_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i("TAG",
                    "Displaying camera permission rationale to provide additional context.");

            ActivityCompat
                    .requestPermissions(Main2Activity.this, PERMISSIONS_CONTACT,
                            REQUEST_CONTACTS);

        } else {

            // Camera permission has not been granted yet. Request it directly.


            ActivityCompat.requestPermissions(this, PERMISSIONS_CONTACT, REQUEST_CONTACTS);
        }
        // END_INCLUDE(camera_permission_request)
    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }
    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }
    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationTrack.stopListener();
    }
}