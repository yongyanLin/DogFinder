package com.example.dogfinder.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    public static boolean isEmpty(String text){
        if(text.equals("") || text == null){
            return true;
        }
        return false;
    }
    public static boolean isEmail(String email){
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.find();
    }
}
