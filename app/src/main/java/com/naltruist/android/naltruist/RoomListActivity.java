package com.naltruist.android.naltruist;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class RoomListActivity extends ListActivity {

    public static final String ROOMS_CHILD = "helprooms";
    private  FirebaseListAdapter mRoomAdapter;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        ListView mRoomListView = (ListView) findViewById(android.R.id.list) ;
         DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference().child(ROOMS_CHILD);


          FirebaseListAdapter mRoomAdapter = new FirebaseListAdapter<HelpRoom>(this, HelpRoom.class, android.R.layout.two_line_list_item, roomRef) {
            @Override
            protected void populateView(View v, HelpRoom newroom, int position) {
                DatabaseReference messRef = getRef(position);
                final String key = messRef.getKey();

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View iv) {
                        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                        intent.putExtra("roomId", key);
                        startActivity(intent);
                    }
                });
                TextView name = (TextView) v.findViewById(android.R.id.text1);
                if (newroom.getHelprequestor() != null) {
                    name.setText(newroom.getHelprequestor().toString()+" # : "+ position);

                }
                TextView message = (TextView) v.findViewById(android.R.id.text2);
                if (newroom.getFirstmessage() != null) {
                    message.setText(newroom.getFirstmessage().toString());
                }


            }


        };
        mRoomListView.setAdapter(mRoomAdapter);





    }

   /* @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

     Object listitem = mRoomAdapter.getItem(position);
        if (listitem != null) {
            String item = mRoomAdapter.getRef(position).getKey();
            if (item == null) {
                finish();
                return;
            }
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("roomId", item);
            startActivity(intent);
        } else {
            String item = "abcdefghijk";
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("roomId", item);
            startActivity(intent);
        }


    } */

    @Override
    protected void  onDestroy() {
        super.onDestroy();

    }



}
