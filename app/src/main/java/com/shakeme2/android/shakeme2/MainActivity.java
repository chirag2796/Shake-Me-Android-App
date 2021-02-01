package com.shakeme2.android.shakeme2;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements SensorEventListener, OnSeekBarChangeListener{

    private SeekBar bar; // seekbar for controlling sensitivity
    private TextView sensitivity;

    // MediaPlayer controls playing the mp3
    private MediaPlayer mp;
    private boolean playing = false;

    // Display the gif in a webview for simplicity
    private WebView wv;

    // Stuff for detecting shakes
    private SensorManager sm;
    private Sensor accel;
    private float xAccel, yAccel, zAccel;
    private boolean initialized = false;
    private double NOISE = 5.0;
    private ArrayList<Double> previousNoise;
    private final int MAXNOISECOUNT = 3;

    private String html;
    private TextView scoreView, highscoreView, sensitivityView, lifetimeScoreView, nameView;
    private int score, highscore, lifetimeScore;

    public static final String mypreference = "mypref";
    SharedPreferences sharedPreferences;

    private Drawable troll;
    private int progress;
    private boolean background;

    private MediaPlayer hsmp;
    private boolean toggleMpCustom;

    private DrawerLayout mDrawerlayout;
    private ActionBarDrawerToggle mToggle;
    private View horizontalBar1, horizontalBar2, horizontalBar3;

    private AdView mAdView;

    private NavigationView navigationView;
    DialogHandler dialogHandler;
    private String name;

    private Library library;
    private int backgroundId;
    private double startTime, endTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        MobileAds.initialize(this, "ca-app-pub-6381221538444322~3573598600");
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        //AdRequest adRequest = new AdRequest.Builder().addTestDevice("7A6947F9FDE16207614A7CCEA6306196").build();
        mAdView.loadAd(adRequest);
        library = new Library();

        bar = (SeekBar) findViewById(R.id.seekBar);
        bar.setOnSeekBarChangeListener(this);
        sensitivity = (TextView) findViewById(R.id.sensitivity);

        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);

        mp = MediaPlayer.create(this, R.raw.batman);
        mp.setVolume(1.0f, 1.0f);

        // setup the webview
        wv = (WebView) findViewById(R.id.web);
        html = "<html><body style = 'background:black;'><img src = 'file:///android_res/raw/g1.gif' style = 'width:100%;'></body></html>";
        wv.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
        wv.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        wv.setVisibility(View.INVISIBLE);

        previousNoise = new ArrayList<Double>();

        score = 0;
        scoreView = (TextView) findViewById(R.id.score);
        highscoreView = (TextView) findViewById(R.id.highscoreView);
        //shakeView = (TextView) findViewById(R.id.shake);
        sensitivityView = (TextView) findViewById(R.id.sensitivity);
        lifetimeScoreView = (TextView) findViewById(R.id.lifetime_score);


        sharedPreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);
        if (sharedPreferences.contains("HighScore")) {
            highscore = sharedPreferences.getInt("HighScore", 0);
        } else
            highscore = 0;

        if (sharedPreferences.contains("LifeTimeScore")) {
            lifetimeScore = sharedPreferences.getInt("LifeTimeScore", 0);
        } else
            lifetimeScore = 0;

        highscoreView.setText(Integer.toString(highscore));
        lifetimeScoreView.setText(Integer.toString(lifetimeScore));
//////////////
        /*SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();*/
/////////////

        if (sharedPreferences.getBoolean("my_first_time", true)) {
            //the app is being launched for first time, do something
            firstTimeRun();
            sharedPreferences.edit().putBoolean("my_first_time", false).commit();
        }

        dialogHandler = new DialogHandler();
        dialogHandler.showHelloDialog(MainActivity.this, sharedPreferences, this);
        name = library.getName(sharedPreferences);

        progress = 5;
        troll = ContextCompat.getDrawable(this, R.drawable.trollface);

        background = true;

        int hsSound = getRandomInt(1, 4);
        switch (hsSound) {
            case 1:
                hsmp = MediaPlayer.create(this, R.raw.hs1);
                break;
            case 2:
                hsmp = MediaPlayer.create(this, R.raw.hs2);
                break;
            case 3:
                hsmp = MediaPlayer.create(this, R.raw.hs3);
                break;
            case 4:
                hsmp = MediaPlayer.create(this, R.raw.hs4);
                break;
            default:
                hsmp = MediaPlayer.create(this, R.raw.hs1);
                break;
        }
        hsmp.setVolume(1.0f, 1.0f);

        toggleMpCustom = false;

        mDrawerlayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerlayout, R.string.open, R.string.close);

        mDrawerlayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        horizontalBar1 = (View) findViewById(R.id.horizontal_bar_1);
        horizontalBar2 = (View) findViewById(R.id.horizontal_bar_2);
        horizontalBar3 = (View) findViewById(R.id.horizontal_bar_3);

        navigationView = (NavigationView) findViewById(R.id.navigatin_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case (R.id.nav_achievements):
                        dialogHandler.achievementsDialog(MainActivity.this);
                        break;
                    case (R.id.nav_settings):
                        Intent in = new Intent(getApplicationContext(), SettingsActivity.class);
                        startActivity(in);
                        break;
                    case (R.id.nav_stats):
                        dialogHandler.statsDialog(MainActivity.this, sharedPreferences, MainActivity.this, score, highscore, lifetimeScore);
                }
                return true;
            }
        });
        library.updateName(sharedPreferences, navigationView);
        name = library.getName(sharedPreferences);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("TotalGames", sharedPreferences.getInt("TotalGames", 0) + 1);
        editor.commit();

        startTime = System.nanoTime();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.title_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_rate_us)
        {
            /*SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.commit();*/
            dialogHandler.rateUsDialog(MainActivity.this);
        }

        if(item.getItemId() == R.id.action_stats)
        {
            dialogHandler.statsDialog(MainActivity.this, sharedPreferences, MainActivity.this, score, highscore, lifetimeScore);
        }

        if(item.getItemId() == R.id.action_about_us)
        {
            dialogHandler.aboutUsDialog(MainActivity.this);
        }

        if(item.getItemId() == R.id.action_close)
        {
            System.exit(0);
        }

        if(mToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){
        super.onResume();
        sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);

        if(sharedPreferences.getBoolean("Reset",false))
        {
            this.score = 0;
            this.highscore = 0;
            this.lifetimeScore = 0;

            scoreView.setText(Integer.toString(0));
            highscoreView.setText(Integer.toString(0));
            lifetimeScoreView.setText(Integer.toString(0));

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("Reset", false);
            editor.commit();
        }

        backgroundId = sharedPreferences.getInt("BackgroundId", 1);
        View view = this.getWindow().getDecorView();
        if(backgroundId == 2)
        {
            view.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
            sensitivityView.setTextColor(ContextCompat.getColor(this, R.color.white));
            lifetimeScoreView.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
        else if(backgroundId == 3)
        {
            view.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            sensitivityView.setTextColor(ContextCompat.getColor(this, R.color.grey));
            lifetimeScoreView.setTextColor(ContextCompat.getColor(this, R.color.grey));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sm.unregisterListener(this);
    }

    private void playGif(){
        mp.start();
        mp.setLooping(true);
        wv.setVisibility(View.VISIBLE);
    }

    private void stopGif(){
        mp.pause();
        mp.seekTo(mp.getCurrentPosition());
        wv.setVisibility(View.INVISIBLE);
    }

    public void onSensorChanged(SensorEvent event) {

        library.updateName(sharedPreferences, navigationView);


        if (!initialized){
            xAccel = event.values[0];
            yAccel = event.values[1];
            zAccel = event.values[2];
            initialized = true;
        }
        else {
            float dAX = xAccel - event.values[0];
            float dAY = yAccel - event.values[1];
            float dAZ = zAccel - event.values[2];
            double noiseVector = Math.sqrt(Math.pow(dAX,2)+Math.pow(dAY,2)+Math.pow(dAZ,2));

            xAccel = event.values[0];
            yAccel = event.values[1];
            zAccel = event.values[2];
            previousNoise.add(noiseVector);
            while (previousNoise.size() > MAXNOISECOUNT){
                previousNoise.remove(0);
            }
            if (previousNoise.size() == MAXNOISECOUNT && !playing){
                double sum = 0;
                for (int i = 0; i < MAXNOISECOUNT; i++) sum += previousNoise.get(i);
                if (sum/MAXNOISECOUNT > NOISE){
                    //playGif();
                    nextGif();
                    playing = true;

                }
            }
            else if (playing){
                double sum = 0;
                for (int i = 0; i < MAXNOISECOUNT; i++) sum += previousNoise.get(i);
                if (sum/MAXNOISECOUNT < NOISE){
                    if (mp.isPlaying()){
                        stopGif();
                        if(toggleMpCustom) {
                            toggleMpCustom = false;
                            mp.stop();
                            mp = MediaPlayer.create(this, R.raw.batman);
                        }
                    }
                    playing = false;
                }
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        sensitivity.setText("Sensitivity (" + progress + ")");
        NOISE = (10-progress);

        this.progress = progress;

        if(bar.isPressed())
        {
            switch(progress)
            {
                case 1:
                    bar.setThumb(ContextCompat.getDrawable(this, R.drawable.troll_1));
                    break;
                case 2:
                    bar.setThumb(ContextCompat.getDrawable(this, R.drawable.troll_2));
                    break;
                case 3:
                    bar.setThumb(ContextCompat.getDrawable(this, R.drawable.troll_3));
                    break;
                case 4:
                    bar.setThumb(ContextCompat.getDrawable(this, R.drawable.troll_4));
                    break;
                case 5:
                    bar.setThumb(ContextCompat.getDrawable(this, R.drawable.troll_5));
                    break;
                case 6:
                    bar.setThumb(ContextCompat.getDrawable(this, R.drawable.troll_6));
                    break;
                case 7:
                    bar.setThumb(ContextCompat.getDrawable(this, R.drawable.troll_7));
                    break;
                case 8:
                    bar.setThumb(ContextCompat.getDrawable(this, R.drawable.troll_8));
                    break;
                case 9:
                    bar.setThumb(ContextCompat.getDrawable(this, R.drawable.troll_9));
                    break;
                case 10:
                    bar.setThumb(ContextCompat.getDrawable(this, R.drawable.troll_10));
                    break;
                default:
                    bar.setThumb(troll);
                    break;
            }
        }

        else
            bar.setThumb(troll);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if(bar.isPressed())
        {
            switch(progress)
            {
                case 0:
                    bar.setThumb(ContextCompat.getDrawable(this, R.drawable.troll_0));
                    break;
                case 1:
                    bar.setThumb(ContextCompat.getDrawable(this, R.drawable.troll_1));
                    break;
                case 2:
                    bar.setThumb(ContextCompat.getDrawable(this, R.drawable.troll_2));
                    break;
                case 3:
                    bar.setThumb(ContextCompat.getDrawable(this, R.drawable.troll_3));
                    break;
                case 4:
                    bar.setThumb(ContextCompat.getDrawable(this, R.drawable.troll_4));
                    break;
                case 5:
                    bar.setThumb(ContextCompat.getDrawable(this, R.drawable.troll_5));
                    break;
                case 6:
                    bar.setThumb(ContextCompat.getDrawable(this, R.drawable.troll_6));
                    break;
                case 7:
                    bar.setThumb(ContextCompat.getDrawable(this, R.drawable.troll_7));
                    break;
                case 8:
                    bar.setThumb(ContextCompat.getDrawable(this, R.drawable.troll_8));
                    break;
                case 9:
                    bar.setThumb(ContextCompat.getDrawable(this, R.drawable.troll_9));
                    break;
                case 10:
                    bar.setThumb(ContextCompat.getDrawable(this, R.drawable.troll_10));
                    break;
                default:
                    bar.setThumb(troll);
                    break;
            }
        }

        else
            bar.setThumb(troll);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        bar.setThumb(troll);
    }

    public void nextGif()
    {
        if(sharedPreferences.getBoolean("Vibration", false))
        {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(150);
        }

        score++;
        lifetimeScore++;
        int n = getRandomInt(1, 12);
        //int n=10;
        switch(n)
        {
            case 1:
                html = "<html><body style = 'background:black;'><img src = 'file:///android_res/raw/g1.gif' style = 'width:100%;'></body></html>";
                break;
            case 2:
                html = "<html><body style = 'background:black;'><img src = 'file:///android_res/raw/g2.gif' style = 'width:100%;'></body></html>";
                break;
            case 3:
                html = "<html><body style = 'background:black;'><img src = 'file:///android_res/raw/g3.gif' style = 'width:100%;'></body></html>";
                break;
            case 4:
                html = "<html><body style = 'background:black;'><img src = 'file:///android_res/raw/g4.gif' style = 'width:100%;'></body></html>";
                break;
            case 5:
                html = "<html><body style = 'background:black;'><img src = 'file:///android_res/raw/g5.gif' style = 'width:100%;'></body></html>";
                break;
            case 6:
                html = "<html><body style = 'background:black;'><img src = 'file:///android_res/raw/g6.gif' style = 'width:100%;'></body></html>";
                break;
            case 7:
                html = "<html><body style = 'background:black;'><img src = 'file:///android_res/raw/g7.gif' style = 'width:100%;'></body></html>";
                break;
            case 8:
                html = "<html><body style = 'background:black;'><img src = 'file:///android_res/raw/g8.gif' style = 'width:100%;'></body></html>";
                break;
            case 9:
                html = "<html><body style = 'background:black;'><img src = 'file:///android_res/raw/nyan.gif' style = 'width:100%;'></body></html>";
                toggleMpCustom = true;
                mp = MediaPlayer.create(this, R.raw.nyan_song);
                break;
            case 10:
                html = "<html><body style = 'background:black;'><img src = 'file:///android_res/raw/nyan_scare.gif' style = 'width:100%;'></body></html>";
                toggleMpCustom = true;
                mp = MediaPlayer.create(this, R.raw.nyan_scare_song);
                break;
            case 11:
                html = "<html><body style = 'background:black;'><img src = 'file:///android_res/raw/g9.gif' style = 'width:100%;'></body></html>";
                break;
            case 12:
                html = "<html><body style = 'background:black;'><img src = 'file:///android_res/raw/g10.gif' style = 'width:100%;'></body></html>";
                break;
            default:
                html = "<html><body style = 'background:black;'><img src = 'file:///android_res/raw/nyan.gif' style = 'width:100%;'></body></html>";
                break;

        }

        wv.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);

        mp.start();
        mp.setLooping(true);
        wv.setVisibility(View.VISIBLE);

        if(!hsmp.isPlaying())
            mp.setVolume(1.0f, 1.0f);

        updateScore();
        checkHighScore();
    }

    private boolean highScoreToggle = true;

    private void checkHighScore()
    {
        if(score > highscore)
        {
            if(highScoreToggle && highscore != 0)
            {
                doHighScoreThings(this);
                highScoreToggle = false;
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("HighScore", score);
            editor.commit();
        }



        //shakeView.setTextColor(getRandomColor());
        int color = getRandomColor();
        horizontalBar1.setBackgroundColor(color);
        horizontalBar2.setBackgroundColor(color);
        horizontalBar3.setBackgroundColor(color);
        backgroundChange();
    }

    private void updateScore()
    {
        endTime = System.nanoTime();

        float elapsedTime = (float)((endTime - startTime)/3600000000000f);

        scoreView.setText(Integer.toString(score));
        lifetimeScoreView.setText(Integer.toString(lifetimeScore));

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("LifeTimeScore", lifetimeScore);
        editor.putFloat("TotalTime", (sharedPreferences.getFloat("TotalTime",0.0f) + elapsedTime ));
        editor.commit();

        startTime = endTime;
    }

    private void backgroundChange()
    {
        View view = this.getWindow().getDecorView();

        if(backgroundId==1 && Math.random()<0.2)
        {
            if(background)
            {
                background = false;
                view.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
                sensitivityView.setTextColor(ContextCompat.getColor(this, R.color.white));
                lifetimeScoreView.setTextColor(ContextCompat.getColor(this, R.color.white));
            }
            else
            {
                background = true;
                view.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                sensitivityView.setTextColor(ContextCompat.getColor(this, R.color.grey));
                lifetimeScoreView.setTextColor(ContextCompat.getColor(this, R.color.grey));
            }
        }

    }

    private void doHighScoreThings(Context context)
    {
        mp.setVolume(0.25f, 0.25f);
        hsmp.start();
        scoreView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryRed));
       // scoreView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 48);

        highscoreView.setTextColor(ContextCompat.getColor(context, R.color.colorCoolBlue));
        highscoreView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);

        troll = ContextCompat.getDrawable(context, R.drawable.trollking1);
        bar.setThumb(troll);

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);

        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(ContextCompat.getColor(this, R.color.gold));
        sensitivityView.setTextColor(ContextCompat.getColor(this, R.color.coral));

        horizontalBar1.setVisibility(View.INVISIBLE);
        horizontalBar2.setVisibility(View.INVISIBLE);
        horizontalBar3.setVisibility(View.INVISIBLE);
    }

    private void firstTimeRun()
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("BackgroundId", 1);
        editor.putBoolean("Vibration", false);

        editor.putInt("TotalGames", 0);
        editor.putFloat("TotalTime", 0.0f);
        editor.commit();


    }



    int getRandomInt(int min, int max) {
        Random rand = new Random();
        int  i = rand.nextInt(max) + min;
        return i;
    }

    public int getRandomColor(){
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    public void onScoreViewClick(View v)
    {
        Toast.makeText(getApplicationContext(), "Score", Toast.LENGTH_SHORT).show();
    }

    public void onHighScoreViewClick(View v)
    {
        Toast.makeText(getApplicationContext(), "High Score", Toast.LENGTH_SHORT).show();
    }

    public void onLifeTimeScoreViewClick(View v)
    {
        Toast.makeText(getApplicationContext(), "Lifetime Score", Toast.LENGTH_SHORT).show();
    }
}
