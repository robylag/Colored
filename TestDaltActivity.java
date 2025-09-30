package com.example.coloredapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.coloredapp.floatingButtons.FloatingAnimations;
import com.example.coloredapp.floatingButtons.FloatingHistoric;
import com.example.coloredapp.ishihawaPlates.IshiharaPlates;
import com.example.coloredapp.ishihawaPlates.ishiharaList;
import com.example.coloredapp.floatingButtons.FloatingTouchButtons;
import java.util.List;
public class TestDaltActivity extends AppCompatActivity {
    int currentIndex = 0;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FloatingTouchButtons.getActivity(this);
        setTitle("Teste de Daltonismo");
        FloatingHistoric.closeFloating();
        setContentView(R.layout.test_layout);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) View confirmButton = findViewById(R.id.confirm_button);

        confirmButton.setOnClickListener(v -> startTest());
    }
    @SuppressLint("SetTextI18n")
    private void startTest(){
        List<IshiharaPlates> plates = ishiharaList.loadRandomPlates();
        setContentView(R.layout.test_dalt);
        currentIndex = 0;

        ImageView plate = findViewById(R.id.plate);
        EditText numeroInput = findViewById(R.id.numeroInput);
        View nextButton = findViewById(R.id.next_button);

        // exemplo: mostrar a primeira imagem
        if (!plates.isEmpty()) {
            plate.setImageResource(plates.get(0).getImageResId());
        }
        nextButton.setOnClickListener(v1 -> {
            String userAnswer = numeroInput.getText().toString();
            plates.get(currentIndex).setUserAnswer(numeroInput.getText().toString());
            ishiharaList.resultCheckAnswer(userAnswer, plates.get(currentIndex).getCorrectAnswer(), plates.get(currentIndex).getCategory());
            currentIndex++;
            if (currentIndex < plates.size()) {
                FloatingAnimations.plateChange(plate, plates.get(currentIndex).getImageResId());
                plate.setImageResource(plates.get(currentIndex).getImageResId());
                numeroInput.setText(""); // limpa campo
            } else {
                String result = ishiharaList.resultTotal();
                setContentView(R.layout.result_test);
                TextView resultText = findViewById(R.id.result);
                TextView descriptionText = findViewById(R.id.result_description);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView inatelImg = findViewById(R.id.inatel_img);
                resultText.setText(result);

                String description;

                switch (result) {
                    case "Visão normal de cores":
                        description = "Parabéns! Seus resultados indicam que sua percepção de cores está dentro do esperado. Você é capaz de distinguir com precisão a maioria das cores do espectro visível, o que facilita atividades diárias como identificar sinais, escolher roupas e interpretar imagens coloridas sem dificuldades.";
                        inatelImg.setImageResource(R.drawable.inatel_normal_vision);
                        break;
                    case "Suspeita de Protanopia":
                        description = "Protanopia é um tipo de daltonismo caracterizado pela dificuldade ou incapacidade de perceber tons de vermelho. Isso ocorre devido à ausência ou disfunção dos cones sensíveis ao vermelho na retina. Pessoas com protanopia podem confundir vermelhos com verdes ou marrons escuros, o que pode afetar tarefas como distinguir semáforos, selecionar alimentos maduros ou ler gráficos coloridos. Apesar dessas limitações, a maioria aprende estratégias para compensar essa condição no dia a dia.";
                        inatelImg.setImageResource(R.drawable.inatel_prota_vision);
                        break;
                    case "Suspeita de Deuteranopia":
                        description = "Deuteranopia é uma forma comum de daltonismo onde há dificuldade na percepção do verde devido à ausência ou disfunção dos cones verdes na retina. Pessoas com deuteranopia frequentemente confundem tons de vermelho e verde, o que pode impactar a identificação correta de sinais de trânsito, escolha de roupas combinando cores e leitura de mapas ou gráficos coloridos. Embora seja uma condição permanente, existem recursos visuais e aplicativos que ajudam na distinção das cores afetadas.";
                        inatelImg.setImageResource(R.drawable.inatel_deuta_vision);
                        break;
                    case "Suspeita de Tritanopia":
                        description = "Tritanopia é um tipo raro de daltonismo que afeta a percepção das cores azul e amarelo, causada pela ausência ou alteração dos cones sensíveis ao azul. Pessoas com tritanopia podem ter dificuldade para distinguir entre tons de azul e verde, além de amarelo e rosa, o que pode complicar tarefas como interpretar sinais marítimos, identificar certas flores ou alimentos, e diferenciar cores em ambientes naturais. O impacto no cotidiano pode variar, e adaptações visuais podem ser úteis para melhorar a distinção das cores.";
                        inatelImg.setImageResource(R.drawable.inatel_trita_vision);
                        break;
                    default:
                        description = "Não foi possível determinar um diagnóstico conclusivo com base nos resultados.";
                        break;
                }
                descriptionText.setText(description);

                LinearLayout platesContainer = findViewById(R.id.plates_container);
                platesContainer.removeAllViews(); // limpa antes
                LayoutInflater inflater = LayoutInflater.from(this);
                for (IshiharaPlates plateItem : plates) {
                    View itemView = inflater.inflate(R.layout.plates_result, platesContainer, false);

                    ImageView plateImage = itemView.findViewById(R.id.plate_image);
                    TextView correctAnswer = itemView.findViewById(R.id.correct_answer);
                    TextView userAnswerView = itemView.findViewById(R.id.user_answer);

                    plateImage.setImageResource(plateItem.getImageResId());
                    correctAnswer.setText("Resposta correta: " + plateItem.getCorrectAnswer());

                    String userResp = plateItem.getUserAnswer();
                    if (userResp == null || userResp.isEmpty()) userResp = "Não respondido";
                    userAnswerView.setText("Sua resposta: " + userResp);

                    platesContainer.addView(itemView);
                }
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) View finishTest = findViewById(R.id.finish_button);
                finishTest.setOnClickListener(v2 -> {
                    FloatingHistoric.openFloating();
                    finish();
                });

                View restartTest = findViewById(R.id.restart_button);
                restartTest.setOnClickListener(v3 ->{
                    ishiharaList.reset();
                    startTest();
                });
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        FloatingHistoric.openFloating();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FloatingHistoric.closeFloating();
    }
}