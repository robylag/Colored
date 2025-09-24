package com.example.coloredapp.ishihawaPlates;

public class IshiharaPlates {
    public int imageResId;
    public String correctAnswer, userAnswer,DaltonicType;

    public IshiharaPlates(int imageResId, String correctAnswer, String userAnswer,String DaltonicType){
        this.imageResId = imageResId;
        this.correctAnswer = correctAnswer;
        this.userAnswer = userAnswer;
        this.DaltonicType = DaltonicType;
    }
    public int getImageResId() {
        return imageResId;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public String getCategory() {
        return DaltonicType;
    }
}
