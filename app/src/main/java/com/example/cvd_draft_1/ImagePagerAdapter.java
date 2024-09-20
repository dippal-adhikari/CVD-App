package com.example.cvd_draft_1;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ViewHolder> {

    private int[] images; // Array of image resources
    private String[] stepTitles; // Array of step titles like "Step 1", "Step 2"
    private String[] stepDescriptions; // Array of step descriptions

    public ImagePagerAdapter(int[] images, String[] stepTitles, String[] stepDescriptions) {
        this.images = images;
        this.stepTitles = stepTitles;
        this.stepDescriptions = stepDescriptions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("ImagePagerAdapter", "Binding description for position: " + position + ", description: " + stepDescriptions[position]);

        // Set image for the current step
        holder.imageView.setImageResource(images[position]);

        // Set step title (e.g., "Step 1")
        holder.stepText.setText(stepTitles[position]);

        // Set step description
        holder.stepDescription.setText(stepDescriptions[position]);
    }



    @Override
    public int getItemCount() {
        return images.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView stepText;
        TextView stepDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            stepText = itemView.findViewById(R.id.stepText);
            stepDescription = itemView.findViewById(R.id.stepDescription);
        }
    }
}