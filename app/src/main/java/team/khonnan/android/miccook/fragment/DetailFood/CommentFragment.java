package team.khonnan.android.miccook.fragment.DetailFood;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import team.khonnan.android.miccook.R;
import team.khonnan.android.miccook.event.OnClickFood;
import team.khonnan.android.miccook.firebases.CommentModel;
import team.khonnan.android.miccook.firebases.DataModel;
import team.khonnan.android.miccook.firebases.NotiRequestModel;
import team.khonnan.android.miccook.firebases.NotiRespondModel;
import team.khonnan.android.miccook.firebases.NotificationModel;
import team.khonnan.android.miccook.firebases.SendNoti;
import team.khonnan.android.miccook.managers.ScreenManager;
import team.khonnan.android.miccook.networks.apis.RetrofitFactory;
import team.khonnan.android.miccook.networks.getFoodModels.FoodModel;

import static com.facebook.login.widget.ProfilePictureView.TAG;

/**
 * Created by apple on 8/25/17.
 */

public class CommentFragment extends Fragment {

    LinearLayout lnRating;
    EditText etComment;
    ImageView ivSend;


    private FirebaseDatabase firebaseDatabase;
    private FirebaseMessaging firebaseMessaging;

    String userID = "790895201087922";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment, container, false);
        if (isFirstTime) {
            EventBus.getDefault().register(this);
            isFirstTime = false;
        }
        while(foodModel==null){

        }
        setupFirebase();
        getLogs();
        setupUI(view);
        return view;
    }

    private void setupFirebase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseMessaging = FirebaseMessaging.getInstance();
    }

    boolean isFirstTime = true;
    private FoodModel foodModel;

    @Subscribe(sticky = true)
    public void onEvent(OnClickFood onClickFood) {
        foodModel = onClickFood.getFoodModel();
        Log.d(TAG, "onEventOneFragment: " + foodModel);
    }

    public void setupUI(View view){

        etComment = view.findViewById(R.id.et_comment);
        ivSend = view.findViewById(R.id.iv_send_comment);
        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendChat();
            }
        });
        lnRating = view.findViewById(R.id.ln_rating);
        RatingBar ratingBar = lnRating.findViewById(R.id.rb_inner);
        ratingBar.setRating(foodModel.getRating());
        lnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScreenManager.openFragment(getActivity().getSupportFragmentManager(),new FragmentRating(),R.id.drawer_layout);
                Log.d(TAG, "onClick: Click rating");
            }
        });
    }


    private void sendChat() {
        CommentModel commentModel = new CommentModel(etComment.getText().toString(), userID);
        DatabaseReference databaseReference = firebaseDatabase.getReference(foodModel.get_id());
        databaseReference.push().setValue(commentModel);

        DataModel dataModel = new DataModel("/tungthanh.1497");
        NotificationModel notificationModel = new NotificationModel(userID);
        NotiRequestModel notiRequestModel = new NotiRequestModel(foodModel.get_id(),
                notificationModel, dataModel);

        final SendNoti sendNoti = RetrofitFactory.getInstance().create(SendNoti.class);
        Call<NotiRespondModel> call = sendNoti.sendNoti(notiRequestModel);
        call.enqueue(new Callback<NotiRespondModel>() {

                         @Override
                         public void onResponse(Call<NotiRespondModel> call, Response<NotiRespondModel> response) {

                         }

                         @Override
                         public void onFailure(Call<NotiRespondModel> call, Throwable t) {

                         }
                     }
        );


        firebaseMessaging.subscribeToTopic(foodModel.get_id());
        Toast.makeText(getContext(), "Post successfully", Toast.LENGTH_SHORT).show();
    }

    public void getLogs() {
        DatabaseReference databaseReference = firebaseDatabase.getReference(foodModel.get_id());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    CommentModel commentModel = recipeSnapshot.getValue(CommentModel.class);
                    //TODO: Moi vong lap la 1 comment
                    Log.d(TAG, "onDataChange: " + commentModel);
                }
                Log.d(TAG, "onDataChange: ===============================================");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
