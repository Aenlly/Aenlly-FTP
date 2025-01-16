package top.aenlly.ftp.utils.cache;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SharedPreferencesUtils {

    private static Map<String,SharedPreferences> SHAREDPREFERENCES_HOLDER = new ConcurrentHashMap<>();
    private static Map<String,SharedPreferences.Editor> EDITOR_HOLDER = new ConcurrentHashMap<>();

    private SharedPreferencesUtils() {
    }

    public static void init(String cacheName,Context context) {
        SHAREDPREFERENCES_HOLDER.computeIfAbsent(cacheName,v -> context.getSharedPreferences(cacheName, Context.MODE_PRIVATE));
        EDITOR_HOLDER.computeIfAbsent(cacheName,v -> context.getSharedPreferences(cacheName, Context.MODE_PRIVATE).edit());
    }

    public static void putString(String cacheName,String key,String value) {
        SharedPreferences.Editor editor = getEditor(cacheName);
        editor.putString(key, value);
        editor.apply(); // 保存数据
    }

    public static void putInt(String cacheName,String key,int value) {
        SharedPreferences.Editor editor = getEditor(cacheName);
        editor.putInt(key, value);
        editor.apply(); // 保存数据
    }

    public static void putBoolean(String cacheName,String key,boolean value) {
        SharedPreferences.Editor editor = getEditor(cacheName);
        editor.putBoolean(key, value);
        editor.apply(); // 保存数据
    }

    public static String getString(String cacheName,String key){
        return SHAREDPREFERENCES_HOLDER.get(cacheName).getString(key,"");
    }

    public static int getInt(String cacheName,String key){
        return SHAREDPREFERENCES_HOLDER.get(cacheName).getInt(key,0);
    }

    public static boolean getBoolean(String cacheName,String key){
        return SHAREDPREFERENCES_HOLDER.get(cacheName).getBoolean(key,false);
    }

    public static void removeValue(String cacheName,String key){
        SharedPreferences.Editor editor = getEditor(cacheName);
        editor.remove(key);
        editor.apply(); // 保存数据
    }

    public static void clearData(String cacheName) {
        SharedPreferences.Editor editor = getEditor(cacheName);
        editor.clear(); // 清空所有数据
        editor.apply();
    }

    private static SharedPreferences.Editor getEditor(String cacheName) {
        return EDITOR_HOLDER.get(cacheName);
    }
}