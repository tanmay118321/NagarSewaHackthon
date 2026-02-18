package com.deepdefender.nagarsewahackthon.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deepdefender.nagarsewahackthon.R;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
public class AdminActivity extends AppCompatActivity {

    RecyclerView rvNew;
    TextView tvPendingBadge;

    FirebaseFirestore db;

    List<Complaint> newList = new ArrayList<>();
    NewComplaintAdapter newAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        rvNew = findViewById(R.id.rvNewComplaints);
        tvPendingBadge = findViewById(R.id.tvPendingBadge);

        if (rvNew == null || tvPendingBadge == null) {
            Toast.makeText(this, "Layout ID mismatch!", Toast.LENGTH_LONG).show();
            return;
        }

        db = FirebaseFirestore.getInstance();

        newAdapter = new NewComplaintAdapter(newList, this::openDetail);

        rvNew.setLayoutManager(new LinearLayoutManager(this));
        rvNew.setAdapter(newAdapter);

        loadComplaints();
    }

    private void loadComplaints() {

        db.collection("Complaints")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) return;

                    newList.clear();

                    for (DocumentSnapshot doc : value.getDocuments()) {

                        Complaint c = doc.toObject(Complaint.class);
                        if (c == null) continue;

                        c.setDocId(doc.getId());

                        if ("Pending".equalsIgnoreCase(c.getStatus())) {
                            newList.add(c);
                        }
                    }

                    newAdapter.notifyDataSetChanged();
                    tvPendingBadge.setText(newList.size() + " PENDING");
                });
    }

    private void openDetail(Complaint complaint) {

        Intent intent = new Intent(this, ComplaintDetailActivity.class);
        intent.putExtra("docId", complaint.getDocId());
        intent.putExtra("description", complaint.getDescription());
        intent.putExtra("address", complaint.getAddress());
        intent.putExtra("issues", complaint.getIssues());
        intent.putExtra("status", complaint.getStatus());
        intent.putExtra("imageBase64", complaint.getImageBase64());
        startActivity(intent);
    }
}
