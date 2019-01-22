package com.thesis.sad.victimapp.Service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.thesis.sad.victimapp.Common.Common;
import com.thesis.sad.victimapp.Model.Token;

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        // Common.curretToken = refreshedToken;

        updateTokenToServer(refreshedToken);//when refrash to token , we need to update



    }

    private void updateTokenToServer(String refreshedToken) {

        FirebaseDatabase db  = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);

        Token token = new Token(refreshedToken);

        if (FirebaseAuth.getInstance().getCurrentUser() !=null) //if already login , must update Token

            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(token);


    }
}
