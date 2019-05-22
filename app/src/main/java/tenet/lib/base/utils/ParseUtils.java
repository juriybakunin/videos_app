package tenet.lib.base.utils;

import android.util.Patterns;

public class ParseUtils {
    public static boolean isValidEmail(String email){
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();

    }
}
