package me.harshithgoka.socmed;

import java.io.Serializable;

/**
 * Created by harshithgoka on 07/10/17.
 */

public class User implements Serializable {
    String uid;
    String name;
    String email;
    boolean following;
}
