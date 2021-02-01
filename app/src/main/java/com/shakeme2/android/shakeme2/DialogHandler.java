package com.shakeme2.android.shakeme2;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Window;

/**
 * Created by CHIRAG on 01-11-2017.
 */

public class DialogHandler {

    Library library;

    public  DialogHandler()
    {
        library = new Library();
    }

    public void aboutUsDialog(Context context)
    {
        AlertDialog.Builder mBuilder;

        mBuilder = new AlertDialog.Builder(context, R.style.Theme_Dialog_Dark);

        mBuilder.setIcon(R.mipmap.about_us_icon);
        mBuilder.setTitle("About Us");
        mBuilder.setMessage("\nI'm the sole developer of Shake-intosh\n\nThis app was made for you and friends to share a laugh and have Fun\n\nI intend to constantly update this app with new features and content\n\nEnjoy!");
        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = mBuilder.create();
        alertDialog.show();
    }

    public void rateUsDialog(final Context context)
    {
        AlertDialog.Builder mBuilder;

        mBuilder = new AlertDialog.Builder(context, R.style.Theme_Dialog_Dark);

        if(Math.random()<=0.5)
            mBuilder.setIcon(R.mipmap.please_icon_1);
        else
            mBuilder.setIcon(R.mipmap.please_icon_2);

        mBuilder.setTitle("Rate App");
        mBuilder.setMessage("If you like this app, please take a moment to rate it in the Play Store");
        mBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rateApp(context);
            }
        });
        mBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = mBuilder.create();
        alertDialog.show();
    }

    public void rateApp(Context context)
    {
        try
        {
            Intent rateIntent = rateIntentForUrl("market://details", context);
            context.startActivity(rateIntent);
        }
        catch (ActivityNotFoundException e)
        {
            Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details", context);
            context.startActivity(rateIntent);
        }
    }

    private Intent rateIntentForUrl(String url, Context context)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, context.getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= 21)
        {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        }
        else
        {
            //noinspection deprecation
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);
        return intent;
    }

    private AlertDialog alertDialog;

    public void showHelloDialog(final Context context,final SharedPreferences sharedPreferences, MainActivity mainActivity)
    {
        if(sharedPreferences.contains("Name"))
            return;

        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(context, R.style.Theme_Dialog_Dark);
        Rect displayRectangle = new Rect();
        Window window = mainActivity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        View mView = LayoutInflater.from(context).inflate(R.layout.dialog_hello, null);
        mView.setMinimumWidth((int) (displayRectangle.width() * 0.9f));

        final EditText name = (EditText) mView.findViewById(R.id.nameView);
        name.setHintTextColor(ContextCompat.getColor(context, R.color.white));
        Button nameButton = (Button) mView.findViewById(R.id.button_name);

        nameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!name.getText().toString().isEmpty())
                {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("Name", name.getText().toString());
                    editor.commit();
                    alertDialog.dismiss();
                }
                else
                {
                    Toast.makeText(context, "Please Enter your Name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBuilder.setView(mView);

        alertDialog = mBuilder.create();
        alertDialog.show();
    }

    public void statsDialog(final Context context,final SharedPreferences sharedPreferences, MainActivity mainActivity, int score, int highscore, int lifetimeScore)
    {
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(context, R.style.Theme_Dialog_Dark);
        Rect displayRectangle = new Rect();
        Window window = mainActivity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        View mView = LayoutInflater.from(context).inflate(R.layout.dialog_stats, null);
        mView.setMinimumWidth((int) (displayRectangle.width() * 0.9f));

        TextView scoreViewStat = (TextView) mView.findViewById(R.id.score_view_stats);
        TextView highscoreViewStat = (TextView) mView.findViewById(R.id.highscore_view_stats);
        TextView lifetimeScoreViewStat = (TextView) mView.findViewById(R.id.lifetime_score_view);
        TextView numberOfGamesViewStat = (TextView) mView.findViewById(R.id.total_games_view);
        TextView averageScoreViewStat = (TextView) mView.findViewById(R.id.average_score_view);
        TextView totalTimwViewStat = (TextView) mView.findViewById(R.id.total_time_view);
        TextView averageTimeViewStat = (TextView) mView.findViewById(R.id.average_time_view);

        int totalGames = sharedPreferences.getInt("TotalGames", 1);
        float averageScore = lifetimeScore/totalGames;
        float totalTime = library.round(sharedPreferences.getFloat("TotalTime", 0.0f), 2);
        float averageTime = library.round((totalTime/totalGames)*60, 2);

        scoreViewStat.setText(Integer.toString(score));
        highscoreViewStat.setText(Integer.toString(highscore));
        lifetimeScoreViewStat.setText(Integer.toString(lifetimeScore));
        numberOfGamesViewStat.setText(Integer.toString(totalGames));
        averageScoreViewStat.setText(Float.toString(averageScore));
        totalTimwViewStat.setText(Float.toString(totalTime));
        averageTimeViewStat.setText(Float.toString(averageTime));

        mBuilder.setView(mView);

        alertDialog = mBuilder.create();
        alertDialog.show();
    }

    public void achievementsDialog(Context context)
    {
        AlertDialog.Builder mBuilder;

        mBuilder = new AlertDialog.Builder(context, R.style.Theme_Dialog_Dark);

        mBuilder.setIcon(R.mipmap.hourglass_icon);
        mBuilder.setTitle("Achievements");
        mBuilder.setMessage("\nWow, such empty\n\nAchievements, trophies, minigames, jokes, riddles, memes are coming 'really' 'really' soon..\n\nplease hold on!");
        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = mBuilder.create();
        alertDialog.show();
    }
}
