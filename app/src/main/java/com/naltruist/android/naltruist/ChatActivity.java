package com.naltruist.android.naltruist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.util.Constants;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import de.hdodenhof.circleimageview.CircleImageView;



public class ChatActivity extends AppCompatActivity {

    public TextView messageTextView;
    public TextView messengerTextView;
    public CircleImageView messengerImageView;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private StaggeredGridLayoutManager mStagGridLayoutManager;
    private ProgressBar mProgressBar;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername;
     private SharedPreferences mSharedPreferences;
    public static final String MESSAGES_CHILD = "helpmessages";
    public static final String ANONYMOUS = "anonymous";
    private FirebaseRecyclerAdapter<NalMessage, MessageViewHolder> mFirebaseAdapter;

    private DatabaseReference mFirebaseDatabaseReference;

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public TextView messengerTextView;
        public CircleImageView messengerImageView;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUsername = ANONYMOUS;

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
      //mStagGridLayoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);


        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();

//            mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
        }
        Bundle extras = getIntent().getExtras();
        String roomId = extras.getString("roomId");
        Toast.makeText(this, "this is the "+roomId, Toast.LENGTH_SHORT).show();

            mFirebaseAdapter = new FirebaseRecyclerAdapter<NalMessage, MessageViewHolder>(

                    NalMessage.class, R.layout.message, MessageViewHolder.class, mFirebaseDatabaseReference.child(MESSAGES_CHILD + "/" + roomId)) {

                @Override
                protected void populateViewHolder(MessageViewHolder viewHolder, NalMessage nalMessage, int position) {
                    mProgressBar.setVisibility(ProgressBar.INVISIBLE);

                    viewHolder.messageTextView.setText(nalMessage.getText());
                    viewHolder.messengerTextView.setText(nalMessage.getName());
                    if (nalMessage.getPhotoUrl() == null) {
                        viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(ChatActivity.this,
                                R.drawable.ic_account_circle_black_36dp));
                    } else {
                        Glide.with(ChatActivity.this)
                                .load(nalMessage.getPhotoUrl())
                                .into(viewHolder.messengerImageView);
                    }
                }
            };



        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

       RecyclerView.ItemDecoration mItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mMessageRecyclerView.addItemDecoration(mItemDecoration);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
    }
}
