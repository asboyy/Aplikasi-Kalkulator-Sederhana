package com.example.kalkulatorsederhana;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private TextView display;
    private TextView expressionDisplay;
    private String currentInput = "";
    private double firstNumber = 0;
    private String operator = "";
    private boolean operatorPressed = false;
    private boolean resultDisplayed = false;
    private SoundPlayer soundPlayer;

    private List<String> historyList;
    private static final String PREFS_NAME = "CalculatorHistory";
    private static final String HISTORY_KEY = "history";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        display = findViewById(R.id.display);
        expressionDisplay = findViewById(R.id.expression_display);
        expressionDisplay.setText(""); // Opsional: Atur teks awal menjadi kosong
        display.setText("0");
        expressionDisplay.setText(""); // Pastikan kosong di awal
        soundPlayer = new SoundPlayer(this);

        historyList = new ArrayList<>();
        loadHistory();
    }

    private void animateDisplay() {
        Animation anim = new AlphaAnimation(0.3f, 1.0f);
        anim.setDuration(150);
        display.startAnimation(anim);
        expressionDisplay.startAnimation(anim); // Animasikan juga expressionDisplay
    }

    public void onNumberClick(View view) {
        Button b = (Button) view;
        soundPlayer.playClick();
        animateDisplay();

        String number = b.getText().toString();
        if (resultDisplayed) {
            currentInput = "";
            resultDisplayed = false;
            expressionDisplay.setText(""); // Hapus ekspresi saat memulai perhitungan baru
        }
        currentInput += number;
        display.setText(currentInput);
        updateExpressionDisplay(); // Perbarui ekspresi
    }

    public void onDotClick(View view) {
        soundPlayer.playClick();
        animateDisplay();

        if (!currentInput.contains(".")) {
            if (currentInput.isEmpty()) currentInput = "0";
            currentInput += ".";
            display.setText(currentInput);
            updateExpressionDisplay(); // Perbarui ekspresi
        }
    }

    public void onToggleSignClick(View view) {
        soundPlayer.playClick();
        animateDisplay();

        if (!currentInput.isEmpty()) {
            if (currentInput.startsWith("-")) {
                currentInput = currentInput.substring(1);
            } else {
                currentInput = "-" + currentInput;
            }
            display.setText(currentInput);
            updateExpressionDisplay(); // Perbarui ekspresi
        }
    }

    public void onClearClick(View view) {
        soundPlayer.playClick();
        currentInput = "";
        firstNumber = 0;
        operator = "";
        operatorPressed = false;
        resultDisplayed = false;
        display.setText("0");
        expressionDisplay.setText(""); // Kosongkan juga tampilan ekspresi
    }

    public void onOperatorClick(View view) {
        Button b = (Button) view;
        soundPlayer.playClick();
        animateDisplay();

        String opText = b.getText().toString();

        if (opText.equals("x")) {
            operator = "*";
        } else if (opText.equals("^")) {
            operator = "^";
        } else {
            operator = opText;
        }

        if (!currentInput.isEmpty()) {
            firstNumber = Double.parseDouble(currentInput);
            currentInput = "";
            operatorPressed = true;
            // Tampilkan angka pertama dan operator di expressionDisplay
            expressionDisplay.setText(formatNumber(firstNumber) + " " + operator + " ");
        }
    }

    public void onEqualClick(View view) {
        soundPlayer.playClick();
        animateDisplay();

        if (operatorPressed && !currentInput.isEmpty()) {
            double secondNumber = Double.parseDouble(currentInput);
            double result = 0;
            boolean error = false;

            String expressionToSave = formatNumber(firstNumber) + " " + operator + " " + formatNumber(secondNumber); // Simpan ekspresi sebelum dihitung

            switch (operator) {
                case "+":
                    result = firstNumber + secondNumber;
                    break;
                case "-":
                    result = firstNumber - secondNumber;
                    break;
                case "*":
                    result = firstNumber * secondNumber;
                    break;
                case "/":
                    if (secondNumber != 0) {
                        result = firstNumber / secondNumber;
                    } else {
                        display.setText(getString(R.string.error_message));
                        expressionDisplay.setText(""); // Hapus ekspresi saat error
                        error = true;
                    }
                    break;
                case "%":
                    result = firstNumber % secondNumber;
                    break;
                case "^":
                    result = Math.pow(firstNumber, secondNumber);
                    break;
                default:
                    display.setText(getString(R.string.invalid_message));
                    expressionDisplay.setText(""); // Hapus ekspresi saat invalid
                    error = true;
            }

            if (!error) {
                String resultStr = formatNumber(result); // Gunakan formatNumber untuk hasil juga
                display.setText(resultStr);
                expressionDisplay.setText(expressionToSave + " ="); // Tampilkan ekspresi lengkap dengan =
                currentInput = resultStr;
                resultDisplayed = true;

                // Tambahkan hasil ke history
                historyList.add(expressionToSave + " = " + resultStr);
                saveHistory();
            }
            operatorPressed = false;
        }
    }

    public void onHistoryClick(View view) {
        soundPlayer.playClick();
        showHistoryDialog();
    }

    public void onClearHistoryClick(View view) {
        soundPlayer.playClick();
        historyList.clear();
        saveHistory();
        display.setText("0");
        expressionDisplay.setText(""); // Kosongkan juga saat clear history
        // Toast.makeText(this, "History Cleared", Toast.LENGTH_SHORT).show();
    }

    private void showHistoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Calculation History");

        if (historyList.isEmpty()) {
            builder.setMessage("No history available.");
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    historyList
            );
            builder.setAdapter(adapter, null);
        }

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void saveHistory() {
        SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(historyList);
        editor.putString(HISTORY_KEY, json);
        editor.apply();
    }

    private void loadHistory() {
        SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPrefs.getString(HISTORY_KEY, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        historyList = gson.fromJson(json, type);

        if (historyList == null) {
            historyList = new ArrayList<>();
        }
    }

    // Metode pembantu untuk memperbarui expressionDisplay
    private void updateExpressionDisplay() {
        if (!currentInput.isEmpty() && !operator.isEmpty() && firstNumber != 0) {
            expressionDisplay.setText(formatNumber(firstNumber) + " " + operator + " " + currentInput);
        } else if (!currentInput.isEmpty() && operator.isEmpty()) { // Hanya angka yang diinput
            expressionDisplay.setText(""); // Kosongkan jika hanya angka baru
        } else if (currentInput.isEmpty() && operator.isEmpty() && firstNumber == 0) {
            expressionDisplay.setText(""); // Benar-benar kosong jika tidak ada input
        }
        // Kasus lain perlu dipertimbangkan, tergantung perilaku yang diinginkan
    }

    // Metode pembantu untuk memformat angka (menghindari .0 jika integer)
    private String formatNumber(double number) {
        if (number == (long) number) {
            return String.valueOf((long) number);
        }
        return String.valueOf(number);
    }
}