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
import android.net.Uri;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Main2Activity extends BaseActivity implements View.OnClickListener {
    public static final int MULTIPLE_PERMISSIONS = 10;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CONTACTS = 1;
    static String ServerKey = null;
    private static String[] PERMISSIONS_CONTACT = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,


    };
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
    private TextView myTextView;
    private HashMap<String, Integer> textMap;
    private boolean isResults = true;


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
                boolean isResultsone = true;
                boolean isResultstwo = true;
                String subinputone=null;
                String subinputtwo=null;
                isResults = true;
                int count = 0;
                String main = null;
                String string1 = null;
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
                        string1 = fullTxt.toLowerCase();
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
                            int countin = 0;
                            int countout = 0;
                            int countresult = 0;
                            int secCountresult = 0;
                            int secCountin = 0;
                            int secCountout = 0;
                            int poscount = 0;
                            String replacedTxt = string1.replaceAll("[^A-Za-z0-9]", " ");
                            String firststring = string1;
                            String secondstring = main.trim();
                            String lastindexenterstring = main.trim();
                            String sss = firststring;
                            String input = secondstring.toLowerCase();
                            String ss = input;
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

                                    if (sss.contains(ss)) {
                                        containsis = false;
                                        pos = sss.lastIndexOf(ss);
                                        totalpos = pos + ss.length();
                                        if (ss.length() == input.length()) {
                                            inputtotalpos = ss.length();
                                        } else {
                                            inputtotalpos = ss.length() + pos + poscount;
                                        }

                                        //ss =ss.substring(ss.length(),0).trim();
                                        break;
                                    } else if (sss.contains(ss.substring(1))) {
                                        subinputone = ss.substring(1);
                                        isResultsone = false;

                                        Log.e("aaaaaaaaa", "----->" + ss.substring(1) + "---->" + ss);
                                    } else if (sss.contains(ss.substring(2))) {
                                        Log.e("aaaaaaaaa", "----->" + ss.substring(2) + "---->" + ss);
                                        subinputtwo = ss.substring(2);
                                        isResultstwo = false;

                                    } else {
                                        notcontains = false;
                                        imp.add(ss.substring(ss.length() - 1).trim());
                                        ss = (ss.substring(0, ss.length() - 1));
                                        poscount++;
                                    }

                                }

                            }
                            if (isResultsone == false) {
                                printSimilarity(subinputone, ss);
                            } else if (isResultstwo == false) {
                                printSimilarity(subinputtwo, ss);
                            }
                            if (isResultstwo && isResultsone) {
                                String total;
                                // if (notcontains=false){
                                for (int i = totalpos; i <= inputtotalpos; i++) {
                                    posadd.add(sss.charAt(i));
                                }
                                StringBuilder builder = new StringBuilder();
                                for (Character s : posadd) {
                                    builder.append(s);
                                }
                                String str = builder.reverse().toString();
                                StringBuffer stb = new StringBuffer(str);
                                stb.reverse();
                                total = ss + stb;
                                 printSimilarity(total.trim(), input);
                                Log.e("sssssssssssss", ":" + total);
                            }
                        }
                           /* if (firststring.contains(secondstring)) {
                                printSimilarity(secondstring, secondstring);
                                isResults=false;
                            } else if (isResults){
                                for (int i = 0; i < lastindexenterstring.length(); i++) {
                                    lastindexenterstring= (lastindexenterstring.substring(0,lastindexenterstring.length()-1));
                                    Log.e("lastenterdata",":"+lastindexenterstring.toString());
                                    if(firststring.contains(lastindexenterstring)){
                                        printSimilarity(finallistString, lastindexenterstring);
                                       // break;
                                    }
                                    isResults=false;
                                   // if(firststring.contains(secondstring-1))
                                }
                            } if (firststring.contains(secondstring.substring(1))) {
                                printSimilarity(secondstring.substring(1), secondstring);
                            } else if (firststring.contains(secondstring.substring(2))) {
                                printSimilarity(secondstring.substring(2), secondstring);
                            }
                            else if (isResults){
                                for (String word : replacedTxt.split(" ")) {
                                    if (word.toLowerCase().length() > 1) {
                                        if (listString.length() >= 3) {

                                            String bar = listString.toString();
                                            String desiredString = bar.substring(0, 3);
                                            if (word.contains(desiredString) && word.length() > 0) {
                                                threewords.add(word);
                                                threeString = word;
                                                printSimilarity(finallistString, threeString);
                                                countin++;
                                            } else {
                                                countout++;
                                            }
                                        } else {
                                            Toast.makeText(Main2Activity.this, "Please enter the first three letter", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            }
                            countresult = countout - countin;
                            if (countout <= countresult && countout != 0) {
                                // Toast.makeText(Main2Activity.this, "Move to second text", Toast.LENGTH_LONG).show();
                                for (String word : replacedTxt.split(" ")) {
                                    if (word.toLowerCase().length() > 1 && listString.toLowerCase().length() != 0 && !(word.equals(""))) {
                                        if (listString.length() >= 3) {
                                            String bar = listString.toString();
                                            String removestring = bar.substring(1);
                                            if (removestring.length() >= 3) {
                                                String desiredString = removestring.substring(0, 3);
                                                String desiredWord = null;
                                                if (word.toLowerCase().length() > 4) {
                                                    desiredWord = word.substring(1, word.length());
                                                } else {
                                                    desiredWord = word.substring(0, word.length());
                                                }
                                                if (desiredWord.contains(desiredString) && desiredWord.length() > 0) {
                                                    threewords.add(desiredWord);
                                                    threeString = desiredWord;
                                                    printSimilarity(finallistString, threeString);
                                                    secCountin++;
                                                } else {
                                                    secCountout++;
                                                }
                                            } else {
                                                Toast.makeText(Main2Activity.this, "Can't find ,Please enter the extra letter", Toast.LENGTH_LONG).show();
                                            }
                                        }else {
                                            Toast.makeText(Main2Activity.this, "Please enter the first three letter", Toast.LENGTH_LONG).show();
                                        }
                                        secCountresult = secCountout - secCountin;
                                    } else if (secCountout <= secCountresult && secCountout != 0) {
                                        Toast.makeText(Main2Activity.this, "Not match please upload the another image", Toast.LENGTH_LONG).show();

                                    }
                                }
                            }

// Print the percent of word found
                            int mainint;
                            if (mainString != null) {
                                mainint = mainString.length();
                            } else {
                                mainint = 1;
                            }
                            int enterint = listString.length();
                            System.out.println("percentage" + (double) (double) enterint / mainint * 100);
                            int result = enterint / mainint * 100;
                            Log.e("resultdata", ":" + result);
                            // Toast.makeText(Main2Activity.this,"percentage"+(double) enterint /mainint*100,Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(Main2Activity.this, "Please enter the first three letter", Toast.LENGTH_LONG).show();

                        }*/
                    } else {
                        Toast.makeText(Main2Activity.this, "OCR text are empty", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(Main2Activity.this, "Please enter the text", Toast.LENGTH_LONG).show();
                }
            }
        });

        /*searchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string1 = word_search;


                String string2 = fullTxt;

                int count = 0;
                String[] array = string1.split(" ");
                int sss= string1.length();
                ArrayList<String> processedWords = new ArrayList<>();
                for(String str : array){

                    if (string2.contains(str) && !processedWords.contains(str) && str.length() > 0){
                        System.out.println(str);

                        processedWords.add(str);
                        count++;
                    }

                }
                System.out.println("Total words: " +sss);
                System.out.println("Count: " +count);

                System.out.println("Percent value: " +((float)sss/count)*100);

            }
        });*/

       /* searchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoitems=editetext.getText().toString();


                int total = 0;
                //word_search = autoitems.trim().toLowerCase();
                fullTxt = myTextView.getText().toString();
                // match(fullTxt,word_search);

                String[] array = array_fullTxt;
                String[] markiert = new String[array.length];
                String word;
                StringBuilder st = new StringBuilder();
                for (int i = 0; i < array.length; i++) {
                    word = array[i];
                    if (word.toLowerCase().contains(word_search)) {
                        word_search = autoitems.trim().toLowerCase();
                        String name = word_search;

                        int maxArrayPosition = 0;
                        for (int j = 0; j < array.length - 1; j++) {
                            for (String c : array) {
                                int next = c.indexOf(c, i);
                            }
                            if (similarity(name, array[j]) > similarity(name, array[j + 1])) {
                                maxArrayPosition = j;
                            } else {
                                maxArrayPosition = j + 1;
                            }
                        }
                        printSimilarity(name, array[maxArrayPosition]);
                        Log.e("wordtext", ":" + word + "------>" + word_search + "----->");

                        markiert[i] = word.trim();//this is result in array do whatever you want with it
                        st.append("<b><i>" + markiert[i] + "</i></b>");
                        total++;
                    } else {
                        st.append(word);
                    }
                    st.append("<br>");
                }
                //Toast.makeText(MainActivity.this,"Text"+st+"",Toast.LENGTH_LONG).show();
                // Toast.makeText(MainActivity.this,"Text"+total+"",Toast.LENGTH_LONG).show();

            }
        });*/


    }

    //----------------------------------------------------------------------------------------------------------------

    /*
    public  boolean match(String s, String p) {
        String us = s.toUpperCase();
        int i = 0;
        for (char c : p.toUpperCase().toCharArray()) {
            int next = us.indexOf(c, i);
            if (next < 0) {
                return false;
            }
            i = next+1;


        }
        return true;
    }
    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0; *//* both strings are zero length *//*
        }
    *//* // If you have Apache Commons Text, you can use it to calculate the edit distance:
    LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
    return (longerLength - levenshteinDistance.apply(longer, shorter)) / (double) longerLength; *//*
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

    }

    // Example implementation of the Levenshtein Edit Distance
    // See http://rosettacode.org/wiki/Levenshtein_distance#Java
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
                        Log.e("results--->",":"+lastValue+"--->"+costs[j]);
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }
 public void printSimilarity(String s, String t) {
        Toast.makeText(MainActivity.this,similarity(s, t)*100 + "% match",Toast.LENGTH_LONG).show();
        System.out.println(String.format("%.3f is the similarity between \"%s\" and \"%s\"", similarity(s, t), s, t));
        Log.e("results--->",":"+s+"--->"+t);
    }
   */
//-------------------------------------------------------------------------------------------------------------------------------------------
    public void printSimilarity(String s, String t) {
        Toast.makeText(Main2Activity.this, similarity(s, t) * 100 + "% match", Toast.LENGTH_LONG).show();
        System.out.println(String.format("%.3f is the similarity between \"%s\" and \"%s\"", similarity(s, t), s, t));
        Log.e("results--->", ":" + s + "--->" + t);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.checkText:
                if (myBitmap != null) {
                    runTextRecog();
                }
                break;
            case R.id.camera:
                dispatchTakePictureIntent();
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
                    myTextView.setText(null);
                    myImageView.setImageBitmap(myBitmap);
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

}