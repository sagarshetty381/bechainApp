package com.example.thebechain;

public class Cards {
    private String userId;
    private String name;
    private  String profileImmageUrl;
    private String age;

    public Cards(String userId, String name, String age, String profileImmageUrl) {
        this.userId = userId;
        this.name = name;
        this.profileImmageUrl = profileImmageUrl;
        this.age = age;
    }

    public String getAge() { return age; }

    public void setAge(String age) { this.age = age;}

    public String getProfileImmageUrl() {
        return profileImmageUrl;
    }

    public void setProfileImmageUrl(String profileImmageUrl) {
        this.profileImmageUrl = profileImmageUrl;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
