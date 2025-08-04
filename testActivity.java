package com.example.coloredapp;

import android.app.Activity;
import android.os.Bundle;

public class testActivity  extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Teste de Daltonismo");
        setContentView(R.layout.test_layout);
    }
}
