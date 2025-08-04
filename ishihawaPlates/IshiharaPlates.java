package com.example.coloredapp.ishihawaPlates;

public class IshiharaPlates {
    public int imageResId;
    public String normalAnswer, protanopiaAnswer, deuteranopiaAnswer, tritanopiaAnswer;

    public IshiharaPlates(int imageResId, String normalAnswer, String protanopiaAnswer, String deuteranopiaAnswer, String tritanopiaAnswer){
        this.imageResId = imageResId;
        this.normalAnswer = normalAnswer;
        this.protanopiaAnswer = protanopiaAnswer;
        this.deuteranopiaAnswer = deuteranopiaAnswer;
        this.tritanopiaAnswer = tritanopiaAnswer;
    }
}
