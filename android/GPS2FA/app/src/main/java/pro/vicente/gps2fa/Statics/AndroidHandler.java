package pro.vicente.gps2fa.Statics;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class AndroidHandler {
    private static final String TAG = "AndroidHandler";

    /**
     * Oculta el teclado de la activity que recibe como parametro
     * @param activity activity que queremos ocultar el teclado
     */
    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Muestra el teclado en la activity que pasemos por parametro
     * @param activity activity en la que queremos que se muestre el teclado
     */
    public static void showKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}
