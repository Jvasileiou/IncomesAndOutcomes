package com.jvvas.incomesoutcomes;

public class Transaction {

    private String amount;
    private String date;
    private String type;
    private int photo;
    private String imgUrl = "";

    public Transaction(){}

    @Override
    public String toString() {
        if(photo == 0)
            return "Ημ/νια: " + date + "\nΠοσο: "+amount + " €\nΕιδος: " + type;
        else
            return "Ημ/νια: " + date + "\nΠοσο: "+amount + " €\nΕιδος: " + type + "\nΑποδειξη...";
    }

    public Transaction(String amount, String date, String type, int photo, String imgUrl) {
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.photo = photo;
        this.imgUrl = imgUrl;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPhoto() {
        return photo;
    }

    public void setPhoto(int photo) {
        this.photo = photo;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
