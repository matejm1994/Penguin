package com.penguin.fri.penguin;

import java.io.Serializable;

/**
 * Created by Nejc on 30.11.2015.
 */
@SuppressWarnings("serial")
public class CompanyClass implements Serializable {

    int id;
    String name;
    String email;
    //String password;


    public CompanyClass(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
