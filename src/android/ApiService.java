package com.twilio.video.app.ui.room;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

    @FormUrlEncoded
    @POST("/api/Users/getToken")
    Single<MyResponse> getData(@Field("roomName") String roomName,
                               @Field("identity") String identity);
}
