package com.example.coloredapp.ishihawaPlates;

import android.util.Log;

import com.example.coloredapp.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ishiharaList {
    private static final List<IshiharaPlates> allPlates = new ArrayList<>();
    private static int deuteranopiaPoint = 0;
    private static int protanopiaPoint = 0;
    private static int tritanopiaPoint = 0;
    private static int correctPoint = 0;

    static {
        allPlates.add(new IshiharaPlates(R.drawable.plate_01,"6",null,"Protanopia"));
        allPlates.add(new IshiharaPlates(R.drawable.plate_02,"9",null,"Protanopia"));
        allPlates.add(new IshiharaPlates(R.drawable.plate_03,"4",null,"Protanopia"));
        allPlates.add(new IshiharaPlates(R.drawable.plate_06,"1",null,"Deuteranopia"));
        allPlates.add(new IshiharaPlates(R.drawable.plate_07,"9",null,"Deuteranopia"));
        allPlates.add(new IshiharaPlates(R.drawable.plate_08,"6",null,"Deuteranopia"));
        allPlates.add(new IshiharaPlates(R.drawable.plate_11,"5",null,"Tritanopia"));
        allPlates.add(new IshiharaPlates(R.drawable.plate_12,"4",null,"Tritanopia"));
        allPlates.add(new IshiharaPlates(R.drawable.plate_13,"6",null,"Tritanopia"));
    }

    public static List<IshiharaPlates> loadRandomPlates() {
        Collections.shuffle(allPlates);
        return allPlates.subList(0, 6);
    }

    public static void resultCheckAnswer(String answer, String correctAnswer, String DaltonicType) {
        if(Objects.equals(answer, correctAnswer)){
            Log.d("Resposta", answer + " == " + correctAnswer + " Resposta correta");
            correctPoint++;
        }else{
            Log.d("Resposta", answer+ " != " + correctAnswer + " Resposta incorreta, analizando o qual o tipo");
            if(DaltonicType.equals("Protanopia")){
                protanopiaPoint++;
            }
            if(DaltonicType.equals("Deuteranopia")){
                deuteranopiaPoint++;
            }
            if(DaltonicType.equals("Tritanopia")){
                tritanopiaPoint++;
            }
        }
    }

    public static String resultTotal(){
        Log.d("Pontos", "Protanopia: " + protanopiaPoint);
        Log.d("Pontos", "Deuteranopia: " + deuteranopiaPoint);
        Log.d("Pontos", "Tritanopia: " + tritanopiaPoint);
        Log.d("Pontos", "Acertos: " + correctPoint);

        int maxPoints = Math.max(protanopiaPoint, Math.max(deuteranopiaPoint, tritanopiaPoint));

        // Se a pessoa acertou quase tudo, assume visão normal
        if (correctPoint >= 5) {
            return "Visão normal de cores";
        }

        if (maxPoints == protanopiaPoint) {
            return "Suspeita de Protanopia";
        } else if (maxPoints == deuteranopiaPoint) {
            return "Suspeita de Deuteranopia";
        } else {
            return "Suspeita de Tritanopia";
        }
    }

    public static void reset() {
        protanopiaPoint = 0;
        deuteranopiaPoint = 0;
        tritanopiaPoint = 0;
        correctPoint = 0;
        for (IshiharaPlates plate : allPlates) {
            plate.setUserAnswer(null);
        }
    }
}
