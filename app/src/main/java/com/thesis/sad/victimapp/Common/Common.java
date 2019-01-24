package com.thesis.sad.victimapp.Common;

import com.thesis.sad.victimapp.Remote.FCMClient;
import com.thesis.sad.victimapp.Remote.IFCMService;

public class Common {
    public static final String available_Ambulance = "AvailableAmbulance";
    public static final String barangay_ambulance = "BarangayAmbulance";
    public static final String victim_information = "Victim";
    public static final String pickup_request = "helprequest";

    public static final String CANCEL_BROADCAST = "cancel_pickup";

    public static final String token_tbl = "Tokens";
    public static final String fcmURL = "https://fcm.googleapis.com/";
    public static final String user_field = "email";
    public static final String pwd_field = "password";

    
    
    public static final double mlasLocation = 0;


    public static IFCMService getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }

}
