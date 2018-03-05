package com.example.andrey.TestEB;

import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.andrey.testdb.R;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    //TODO в больших вопросах текст не влазит в экран
    //TODO поиск по номеру вопроса????

    Cursor c = null;
    TextView tVQ,cQs;
    RadioButton rB1, rB2, rB3;
    RadioGroup rG;
    MenuItem mIRandom, mIPrompting;
    Boolean bIRandom=false, bIPrompting=false; // для сохранения значений меню после поворта экрана.
    int trueAnswer=0, totalAnswer=0, totalQuestions=0, numTrueAnswer=0, numQuestion=-1;
    Random r;
    private Timer mTimer;
    private MyTimerTask mMyTimerTask;
    private Menu menu;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (bIPrompting){   //восстановление подсветки правильных ответов после поворота экрана
            lTA(numTrueAnswer);
            menu.findItem(R.id.menu_prompting).setChecked(true);
        }
        menu.findItem(R.id.menu_random).setChecked(bIRandom);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(!item.isChecked());
        int id = item.getItemId();
        switch (id){
            case R.id.menu_prompting:
                bIPrompting = item.isChecked();
                if (item.isChecked()) lTA (numTrueAnswer); else lTA(0);
                return true;
            case R.id.menu_random:
                bIRandom = item.isChecked();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void nextQuestion (){
        if (bIRandom) numQuestion=r.nextInt(totalQuestions-1);
        else {
            numQuestion++;
            if (numQuestion>totalQuestions-1) numQuestion=0;
        }

        if (c.moveToPosition(numQuestion)) {
            Random rq = new Random();
            int nq1, nq2=2, nq3=3;
            nq1 = rq.nextInt(3)+1; //номер случайного ответа от 1 до 3
            switch (nq1){
                case 1: nq2=2; nq3=3; numTrueAnswer=1; if (bIPrompting) rB1.setBackgroundColor(Color.GREEN); break;
                case 2: nq2=1; nq3=3; numTrueAnswer=2; if (bIPrompting) rB2.setBackgroundColor(Color.GREEN); break;
                case 3: nq2=2; nq3=1; numTrueAnswer=3; if (bIPrompting) rB3.setBackgroundColor(Color.GREEN); break;
            }
            tVQ.setText(c.getString(c.getColumnIndex("question")));
            rB1.setText(c.getString(nq1+2));
            rB2.setText(c.getString(nq2+2));
            rB3.setText(c.getString(nq3+2));
            cQs.setText("Верно: "+trueAnswer+" из "+totalAnswer);
            totalAnswer++;
        }

    }

    private void lTA(int nTA){// подстветка правильного ответа
        switch (nTA){
            case 0:
                rB1.setBackgroundColor(Color.TRANSPARENT);
                rB2.setBackgroundColor(Color.TRANSPARENT);
                rB3.setBackgroundColor(Color.TRANSPARENT);
                break;
            case 1: rB1.setBackgroundColor(Color.GREEN); break;
            case 2: rB2.setBackgroundColor(Color.GREEN); break;
            case 3: rB3.setBackgroundColor(Color.GREEN); break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tVQ = (TextView) findViewById(R.id.tVQ);
        cQs = (TextView) findViewById(R.id.countQuestions);
        rG = (RadioGroup) findViewById(R.id.radioGroup);
        rB1 = (RadioButton) findViewById(R.id.rB1);
        rB2 = (RadioButton) findViewById(R.id.rB2);
        rB3 = (RadioButton) findViewById(R.id.rB3);
        mIRandom = (MenuItem) findViewById(R.id.menu_random);
        mIPrompting = (MenuItem) findViewById(R.id.menu_prompting);

        View.OnClickListener radioListener;


        DataBaseHelper myDbHelper = new DataBaseHelper(MainActivity.this);
        try {
            myDbHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        try {
            myDbHelper.openDataBase();
        } catch (SQLException sqle) {
            throw sqle;
        }

        c = myDbHelper.query("Questions", null, null, null, null, null, null);
        totalQuestions = c.getCount();
        r = new Random();
        if (savedInstanceState != null) { //достаю данные после поворота экрана
            trueAnswer = savedInstanceState.getInt("trueAnswer",0);
            totalAnswer = savedInstanceState.getInt("totalAnswer",0)-1;// так как счетчик вопросов каждое вращение увеличивается на 1
            numQuestion = savedInstanceState.getInt("numQuestion",0);
            bIPrompting = savedInstanceState.getBoolean("mPrompting", false);
            bIRandom = savedInstanceState.getBoolean("mRandom", true);
        }

        nextQuestion();

        radioListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.rB1:
                        if (numTrueAnswer==1){
                            rB1.setBackgroundColor(Color.GREEN);
                            trueAnswer++;
                            Toast.makeText(MainActivity.this, "Верно", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            rB1.setBackgroundColor(Color.RED);
                            lTA(numTrueAnswer);
                            Toast.makeText(MainActivity.this, "Неверно", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.rB2:
                        if (numTrueAnswer==2){
                            rB2.setBackgroundColor(Color.GREEN);
                            trueAnswer++;
                            Toast.makeText(MainActivity.this, "Верно", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            rB2.setBackgroundColor(Color.RED);
                            lTA(numTrueAnswer);
                            Toast.makeText(MainActivity.this, "Неверно", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.rB3:
                        if (numTrueAnswer==3){
                            rB3.setBackgroundColor(Color.GREEN);
                            trueAnswer++;
                            Toast.makeText(MainActivity.this, "Верно", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            rB3.setBackgroundColor(Color.RED);
                            lTA(numTrueAnswer);
                            Toast.makeText(MainActivity.this, "Неверно", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                rB1.setEnabled(false);
                rB2.setEnabled(false);
                rB3.setEnabled(false);

                mTimer = new Timer();
                mMyTimerTask = new MyTimerTask();

                //здесь отложенный запуск кнопки
                mTimer.schedule(mMyTimerTask, 1000);
            }
        };

        rB1.setOnClickListener(radioListener);
        rB2.setOnClickListener(radioListener);
        rB3.setOnClickListener(radioListener);
    }
    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rG.clearCheck();
                    lTA(0);
                    nextQuestion();
                    //nextQuestion(r.nextInt(totalQuestions-1));
                    rB1.setEnabled(true);
                    rB2.setEnabled(true);
                    rB3.setEnabled(true);
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) { //сохраняю даные до поворота
        outState.putInt("trueAnswer", trueAnswer);
        outState.putInt("totalAnswer", totalAnswer);
        outState.putInt("numQuestion", numQuestion);
        outState.putBoolean("mRandom", menu.findItem(R.id.menu_random).isChecked());
        outState.putBoolean("mPrompting", menu.findItem(R.id.menu_prompting).isChecked());
        super.onSaveInstanceState(outState);
    }

}
