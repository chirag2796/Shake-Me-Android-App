package com.shakeme2.android.shakeme2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import static com.shakeme2.android.shakeme2.MainActivity.mypreference;

public class SettingsActivity extends AppCompatActivity {

    Button nameButton, backgroundButton, vibrationButton, resetButton;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        nameButton = (Button) findViewById(R.id.button_name_settings);
        backgroundButton = (Button) findViewById(R.id.button_background_settings);
        vibrationButton = (Button) findViewById(R.id.button_vibration_settings);
        resetButton = (Button) findViewById(R.id.button_reset_settings);

        sharedPreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);

       /* View view = this.getWindow().getDecorView();
        view.setBackgroundColor(ContextCompat.getColor(this, R.color.black));*/

        nameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNameDialog();
            }
        });

        backgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBackgroundDialog();
            }
        });

        vibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVibrationDialog();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResetScoreDialog();
            }
        });
    }

    private AlertDialog alertDialog;

    public void showNameDialog()
    {
        String name = sharedPreferences.getString("Name", "John Doe");

        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(this, R.style.Theme_Dialog_Dark);
        Rect displayRectangle = new Rect();
        Window window = this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        View mView = LayoutInflater.from(this).inflate(R.layout.dialog_name_settings, null);
        mView.setMinimumWidth((int) (displayRectangle.width() * 0.9f));
        Button nameButton = (Button) mView.findViewById(R.id.button_name_settings_dialog);

        final EditText nameView = (EditText) mView.findViewById(R.id.name_view_settings_dialog);

        nameView.setHint(name);
        nameView.setHintTextColor(ContextCompat.getColor(this, R.color.grey));

        nameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!nameView.getText().toString().isEmpty())
                {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("Name", nameView.getText().toString());
                    editor.commit();
                    alertDialog.dismiss();
                }
                else
                {
                   alertDialog.dismiss();
                }
            }
        });

        mBuilder.setView(mView);

        alertDialog = mBuilder.create();
        alertDialog.show();
    }

    public void showBackgroundDialog()
    {
        final int backgroundId = sharedPreferences.getInt("BackgroundId", 1);

        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(this, R.style.Theme_Dialog_Dark);
        Rect displayRectangle = new Rect();
        Window window = this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        View mView = LayoutInflater.from(this).inflate(R.layout.dialog_background_settings, null);
        mView.setMinimumWidth((int) (displayRectangle.width() * 0.9f));

        RadioGroup radioGroup = (RadioGroup) mView.findViewById(R.id.radioGroup_background);

        switch(backgroundId)
        {
            case(1):
                radioGroup.check(R.id.radiobutton_dynamic);
                break;
            case(2):
                radioGroup.check(R.id.radiobutton_dark);
                break;
            case(3):
                radioGroup.check(R.id.radiobutton_white);
                break;
            default:
                radioGroup.check(R.id.radiobutton_dynamic);
                break;
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                //RadioButton rb = (RadioButton) group.findViewById(checkedId);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                switch (checkedId)
                {
                    default:
                    case R.id.radiobutton_dynamic:
                        editor.putInt("BackgroundId", 1);
                        editor.commit();
                        alertDialog.dismiss();
                        break;
                    case R.id.radiobutton_dark:
                        editor.putInt("BackgroundId", 2);
                        editor.commit();
                        alertDialog.dismiss();
                        break;
                    case R.id.radiobutton_white:
                        editor.putInt("BackgroundId", 3);
                        editor.commit();
                        alertDialog.dismiss();
                        break;
                }
            }
        });

        mBuilder.setView(mView);

        alertDialog = mBuilder.create();
        alertDialog.show();
    }

    public void showVibrationDialog()
    {
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(this, R.style.Theme_Dialog_Dark);
        Rect displayRectangle = new Rect();
        Window window = this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        View mView = LayoutInflater.from(this).inflate(R.layout.dialog_vibration_settings, null);
        mView.setMinimumWidth((int) (displayRectangle.width() * 0.9f));

        SwitchCompat switchCompat = (SwitchCompat) mView.findViewById(R.id.switch_vibration_settings);

        switchCompat.setChecked(sharedPreferences.getBoolean("Vibration", false));

       switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               //sharedPreferences.edit().putBoolean("Vibration", isChecked);
               SharedPreferences.Editor editor = sharedPreferences.edit();
               editor.putBoolean("Vibration", isChecked);
               editor.commit();
              // alertDialog.dismiss();
           }
       });

        mBuilder.setView(mView);

        alertDialog = mBuilder.create();
        alertDialog.show();
    }
    int reset = 0;
    public void showResetScoreDialog()
    {
        AlertDialog.Builder mBuilder;

        mBuilder = new AlertDialog.Builder(this, R.style.Theme_Dialog_Dark);

        mBuilder.setTitle("Reset");
        mBuilder.setMessage("Reset the High Score and Lifetime Score?");
        mBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("HighScore",0);
                editor.putInt("LifeTimeScore",0);
                editor.putBoolean("Reset", true);
                editor.commit();
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

    @Override
    public void finish() {
        if(reset==1)
        {
            Intent intent = new Intent();
            intent.putExtra("score", 0);
            setResult(RESULT_OK, intent);
            //finish();
        }
        super.finish();
    }


}
