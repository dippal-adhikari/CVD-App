package com.example.cvd_draft_1;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<StorageReference> videoList;
    private Context context;

    public VideoAdapter(List<StorageReference> videoList, Context context) {
        this.videoList = videoList;
        this.context = context;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        StorageReference videoRef = videoList.get(position);
        holder.videoTitle.setText(videoRef.getName());

//        // Fetch and display the timestamp
//        videoRef.getMetadata().addOnSuccessListener(metadata -> {
//            String timestamp = metadata.getCustomMetadata("timestamp");
//
//            if (timestamp != null) {
//                long timestampLong = Long.parseLong(timestamp);
//                String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(timestampLong));
//                holder.videoTimestamp.setText("Uploaded on: " + formattedDate);
//            } else {
//                holder.videoTimestamp.setText("No timestamp available");
//            }
//        }).addOnFailureListener(e -> {
//            holder.videoTimestamp.setText("Error fetching timestamp");
//        });

        // View video button click listener
        holder.btnView.setOnClickListener(v -> videoRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setDataAndType(uri, "video/mp4");
            context.startActivity(intent);
        }));

        // Download video button click listener
        holder.btnDownload.setOnClickListener(v -> videoRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(browserIntent);
        }));



        // Delete button functionality
        holder.btnDelete.setOnClickListener(v -> {
            videoRef.delete().addOnSuccessListener(aVoid -> {
                Toast.makeText(context, "Video deleted successfully", Toast.LENGTH_SHORT).show();
                videoList.remove(position); // Remove the item from the list
                notifyItemRemoved(position); // Notify the adapter to remove the item from the UI
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Failed to delete video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView videoTitle;
        Button btnView, btnDownload;
        ImageView btnDelete; // Change from Button to ImageView to match your layout

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoTitle = itemView.findViewById(R.id.videoTitle);
            btnView = itemView.findViewById(R.id.btnView);
            btnDownload = itemView.findViewById(R.id.btnDownload);
            btnDelete = itemView.findViewById(R.id.btnDeleteVideo); // Delete button
        }
    }
}
