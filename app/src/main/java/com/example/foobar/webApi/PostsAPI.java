package com.example.foobar.webApi;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.foobar.daos.FeedDao;
import com.example.foobar.entities.Post_Item;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PostsAPI {

    private MutableLiveData<List<Post_Item>> postListData;

    private FeedDao feedDao;
    private Retrofit retrofit;
    private WebServiceAPI webServiceAPI;

    public PostsAPI(MutableLiveData<List<Post_Item>> postListData , FeedDao feedDao) {

        this.postListData = postListData;
        this.feedDao = feedDao;

        retrofit = new Retrofit.Builder()
                //.baseUrl(MyApplication.context.getString(R.string.BaseUrl))  //we need to change it later to be save in R string
                .baseUrl("http://192.168.1.24:12345/api/")  //we need to change it later to be save in R string

                .addConverterFactory(GsonConverterFactory.create())
                .build();
        webServiceAPI = retrofit.create(WebServiceAPI.class);
    }


    public void getPosts(String authToken) {
        Call<List<Post_Item>> call = webServiceAPI.getPosts(authToken);
        call.enqueue(new Callback<List<Post_Item>>() {
            @Override
            public void onResponse(Call<List<Post_Item>> call, Response<List<Post_Item>> response) {
                    postListData.setValue(response.body());
            }

            @Override
            public void onFailure(Call<List<Post_Item>> call, Throwable t) {
                Log.e("PostsAPI", "Failed to get posts: " + t.getMessage());
            }
        });
    }
}
