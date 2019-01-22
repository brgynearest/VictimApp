package com.thesis.sad.victimapp.Remote;


import com.thesis.sad.victimapp.Model.FCMResponse;
import com.thesis.sad.victimapp.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content_Type:application/json",
            "Authorization:key=AAAA-QeAQZU:APA91bHWW6h4TzYky5KK4zVF_xZK8S_9XFmaP3GaGbccWfDxKPchExiuo-JdmLfc0aVBfzlL9w0bmJnJlzSWdobPDotRlNWR3MGAyqlYKo0eEUCvjrrLx9686E2jP1YDzcaaFnvDV1zp"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);
}
