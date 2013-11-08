package com.beyondar.example.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.beyondar.example.R;

public class ImageDialog extends DialogFragment {

	private Bitmap mImage;

	public void setImage(Bitmap image) {
		mImage = image;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		View view = inflater.inflate(R.layout.custom_image_dialog, null);
		ImageView image = (ImageView) view.findViewById(R.id.screensthoImage);
		image.setImageBitmap(mImage);
		builder.setView(view)
		// Add action buttons
				.setPositiveButton("Close", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// TODO: Recycle the bitmap!
						ImageDialog.this.getDialog().cancel();
					}
				});

		return builder.create();
	}

}
