package com.example.thebechain.models;

public class UserModel {
    private String sName,sBirthDate,sProfession,sBio,sHobbies,sStatus;

    public UserModel(String sName, String sBirthDate, String sProfession, String sBio, String sHobbies, String sStatus) {
        this.sName = sName;
        this.sBirthDate = sBirthDate;
        this.sProfession = sProfession;
        this.sBio = sBio;
        this.sHobbies = sHobbies;
        this.sStatus = sStatus;
    }

    public String getsName() {
        return sName;
    }

    public String getsBirthDate() {
        return sBirthDate;
    }

    public String getsProfession() {
        return sProfession;
    }

    public String getsBio() {
        return sBio;
    }

    public String getsHobbies() {
        return sHobbies;
    }

    public String getsStatus() {
        return sStatus;
    }
}
