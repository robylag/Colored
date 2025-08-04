package com.example.coloredapp.ishihawaPlates;

import com.example.coloredapp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ishiharaList {
    private static List<IshiharaPlates> allPlates = new ArrayList<>();
    private int deuteranopiaPoint, protanopiaPoint, tritanopiaPoint;
    public class IshiharaList {
        public List<IshiharaPlates> getAllPlates() {
            List<IshiharaPlates> allPlates = new ArrayList<>();
            allPlates.add(new IshiharaPlates(R.drawable.plate_01, "0", "1", "2", null));
            allPlates.add(new IshiharaPlates(R.drawable.plate_01, "4", "5", "6", null));
            allPlates.add(new IshiharaPlates(R.drawable.plate_01, "8", "9", "10", null));
            allPlates.add(new IshiharaPlates(R.drawable.plate_01, "12", "13", "14", null));
            return allPlates;
        }
    }
    public static List<IshiharaPlates> loadRandomPlates() {
        Collections.shuffle(allPlates);
        return allPlates.subList(0, 12);
    }

    public static void resultCheckAnswer(String answer){

    }
}
