package com.example.demo.dto;

public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String password;

    public RegisterRequest() {}

    public RegisterRequest(String email, String password, String firstName, String lastName, String phone) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    //Getters
    public String getEmail() {
        return email;
    }
    public String getFirstName() {return firstName;}
    public String getLastName() {return lastName;}
    public String getPhone() {return phone;}
    public String getPassword() {
        return password;
    }

    //Setters
    public void setEmail(String email) {
        this.email = email;
    }
    public void setFirstName(String firstName) {this.firstName = firstName;}
    public void setLastName(String lastName) {this.lastName = lastName;}
    public void setPhone(String phone) {this.phone = phone;}
    public void setPassword(String password) {
        this.password = password;
    }


}