package com.penguin.fri.penguin;

import java.io.Serializable;

/**
 * Created by Nejc on 29. 12. 2015.
 */
public class OfferClass implements Serializable {

    String id;
    String companyID;
    String rules;
    String name;
    String hashtags;
    String prize;
    String start;
    String finish;
    String extras;

    public OfferClass(String id, String companyID, String rules, String name, String hashtags, String prize,
                      String start, String finish, String extras){
        this.id = id;
        this.companyID = companyID;
        this.rules = rules;
        this.name = name;
        this.hashtags =hashtags;
        this.prize = prize;
        this.start = start;
        this.finish = finish;
        this.extras = extras;
    }

}
