package com.example.enggo.util;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.example.enggo.api.ApiClient;
import com.example.enggo.api.ApiService;
import com.example.enggo.database.Database;
import com.example.enggo.auth.LoginActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogoutHelper {

    public static void logout(Activity activity) {

        Database.Dao dao = new Database.Dao(activity);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        if (dao.getAll().isEmpty()) {
            goToLogin(activity);
            return;
        }

        String token = dao.getAll().get(0).token;

        apiService.logout(token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("LOGOUT", "Logout success, code=" + response.code());
                clearAndRedirect(activity, dao);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("LOGOUT", "Logout API failed", t);
                clearAndRedirect(activity, dao);
            }
        });
    }

    private static void clearAndRedirect(Activity activity, Database.Dao dao) {
        dao.deleteAll();
        goToLogin(activity);
    }

    private static void goToLogin(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}