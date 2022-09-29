package com.Hecht.ar;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.Hecht.ar.R;
import com.Hecht.ar.helpers.CameraPermissionHelper;
import com.Hecht.ar.helpers.CloudAnchorManager;
import com.Hecht.ar.helpers.DisplayRotationHelper;
import com.Hecht.ar.helpers.FirebaseManager;
import com.Hecht.ar.helpers.ResolveDialogFragment;
import com.Hecht.ar.helpers.SnackbarHelper;
import com.Hecht.ar.helpers.TapHelper;
import com.Hecht.ar.helpers.TrackingStateHelper;
import com.Hecht.ar.rendering.BackgroundRenderer;
import com.Hecht.ar.rendering.ObjectRenderer;
import com.Hecht.ar.rendering.PlaneRenderer;
import com.Hecht.ar.rendering.PointCloudRenderer;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * A simple {Fragment} subclass.
 * User Fragment it is triggered when you enter as a user in the application
 */
public class UserFragment extends Fragment implements GLSurfaceView.Renderer {

    private static final String TAG = CloudAnchorFragment.class.getSimpleName();

    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    private GLSurfaceView surfaceView;

    private boolean installRequested;

    private Button resolveButton;
    private TextView obj_name, obj_url;
    private LinearLayout details;

    private Session session;
    private int counter = 0;
    private String name = "";
    private String url = "";
    private String desc = "";

    private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
    // Add this line right below.
    private final CloudAnchorManager cloudAnchorManager = new CloudAnchorManager();

    private DisplayRotationHelper displayRotationHelper;
    private TrackingStateHelper trackingStateHelper;
    private TapHelper tapHelper;

    FirebaseDatabase rootNode;
    DatabaseReference reference;

    private FirebaseManager firebaseManager;

    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
    private final ObjectRenderer virtualObject = new ObjectRenderer();
    private final ObjectRenderer virtualObjectShadow = new ObjectRenderer();
    private final PlaneRenderer planeRenderer = new PlaneRenderer();
    private final PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();

    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private final float[] anchorMatrix = new float[16];
    private static final String SEARCHING_PLANE_MESSAGE = "Searching for surfaces...";
    private final float[] andyColor = {139.0f, 195.0f, 74.0f, 255.0f};

    @Nullable
    private Anchor currentAnchor = null;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        tapHelper = new TapHelper(context);
        trackingStateHelper = new TrackingStateHelper(requireActivity());
        firebaseManager = new FirebaseManager(context);
    }

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public UserFragment() {
        // constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using the parameters
     */
    public static UserFragment newInstance(String param1, String param2) {
        UserFragment fragment = new UserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_user, container, false);
        GLSurfaceView surfaceView = rootView.findViewById(R.id.surfaceView);
        obj_name = rootView.findViewById(R.id.tv_name);
        obj_url = rootView.findViewById(R.id.tv_url);
        details = rootView.findViewById(R.id.llDetails);
        this.surfaceView = surfaceView;
        displayRotationHelper = new DisplayRotationHelper(requireContext());
        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        surfaceView.setWillNotDraw(false);
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Do not add anchor if there was already an anchor
                if (currentAnchor == null) {
                    return;
                } else if (counter == 0) {
                    counter++;
                    showBottomSheetDialog(name, url, desc);
                } else if (counter == 1) {
                    counter = 0;
                }
            }
        });

        Button clearButton = rootView.findViewById(R.id.clear_button);
        clearButton.setOnClickListener(v -> onClearButtonPressed());
        resolveButton = rootView.findViewById(R.id.resolve_button);
        resolveButton.setOnClickListener(v -> onResolveButtonPressed());

        return rootView;
    }

    // check for any configuration issues related to camera and Arcore functionality
    // If available the application will continue its execution process
    @Override
    public void onResume() {
        super.onResume();
        if (session == null) {
            Exception exception = null;
            String message = null;
            try {
                switch (ArCoreApk.getInstance().requestInstall(requireActivity(), !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                // Required Camera permissions to operate.
                if (!CameraPermissionHelper.hasCameraPermission(requireActivity())) {
                    CameraPermissionHelper.requestCameraPermission(requireActivity());
                    return;
                }

                // Create the session.
                session = new Session(requireActivity());

                // Configure the session.
                Config config = new Config(session);
                config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);
                session.configure(config);

            }
            //Arcore Availability check
            catch (UnavailableArcoreNotInstalledException
                    | UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (UnavailableDeviceNotCompatibleException e) {
                message = "This device does not support AR";
                exception = e;
            } catch (Exception e) {
                message = "Failed to create AR session";
                exception = e;
            }

            if (message != null) {
                messageSnackbarHelper.showError(requireActivity(), message);

                Log.e(TAG, "Exception creating session", exception);
                return;
            }
        }

        // will not resume till the camera is available
        try {
            session.resume();
        } catch (CameraNotAvailableException e) {
            messageSnackbarHelper
                    .showError(requireActivity(), "Camera not available. Try restarting the app.");
            session = null;
            return;
        }
        surfaceView.onResume();
        displayRotationHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            /**
             * GLSurfaceView is paused first so that it does not try to query the session.
             * If Session is paused before GLSurfaceView,
             */
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();
        }
    }

    // Alert for Camera permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        if (!CameraPermissionHelper.hasCameraPermission(requireActivity())) {
            Toast.makeText(requireActivity(), "Camera permission is needed to run this application",
                    Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(requireActivity())) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(requireActivity());
            }
            requireActivity().finish();
        }
    }

    // Rendering objects. This involves reading shaders also.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        try {
            // Create the texture and pass it to ARCore session to be filled during update() call.
            // Take care of the naming convention used in Firebase.
            backgroundRenderer.createOnGlThread(getContext());
            planeRenderer.createOnGlThread(getContext(), "models/trigrid.png");
            pointCloudRenderer.createOnGlThread(getContext());

            virtualObject.createOnGlThread(getContext(), "models/info_icon.obj", "models/andy.png");
            virtualObject.setMaterialProperties(0.0f, 2.0f, 0.5f, 6.0f);

            virtualObjectShadow.createOnGlThread(getContext(), "models/andy_shadow.obj", "models/andy_shadow.png");
            virtualObjectShadow.setBlendMode(ObjectRenderer.BlendMode.Shadow);
            virtualObjectShadow.setMaterialProperties(1.0f, 0.0f, 0.0f, 1.0f);

        } catch (IOException e) {
            Log.e(TAG, "Failed to read an asset file", e);
        }
    }

    //User can adjust the height and width
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (session == null) {
            return;
        }
        // Notify ARCore session that the view size changed so that the perspective matrix
        // And the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session);

        try {
            session.setCameraTextureName(backgroundRenderer.getTextureId());

            // Obtain the current frame from ARSession and update the frame
            Frame frame = session.update();
            cloudAnchorManager.onUpdate();
            Camera camera = frame.getCamera();

            // If frame is ready, render camera preview image to the GL surface.
            backgroundRenderer.draw(frame);

            // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
            trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

            // If not tracking, don't draw 3D objects, show tracking failure reason instead.
            if (camera.getTrackingState() == TrackingState.PAUSED) {
                messageSnackbarHelper.showMessage(
                        getActivity(), TrackingStateHelper.getTrackingFailureReasonString(camera));
                return;
            }

            // Get projection matrix.
            float[] projmtx = new float[16];
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

            // Get camera matrix and draw.
            float[] viewmtx = new float[16];
            camera.getViewMatrix(viewmtx, 0);

            // Compute lighting from average intensity of the image.
            // The first three components are color scaling factors.
            // The last one is the average pixel intensity in gamma space.
            final float[] colorCorrectionRgba = new float[4];
            frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);

            // Visualize tracked points.
            // Use try-with-resources to automatically release the point cloud.
            try (PointCloud pointCloud = frame.acquirePointCloud()) {
                pointCloudRenderer.update(pointCloud);
                pointCloudRenderer.draw(viewmtx, projmtx);
            }

            // No tracking error at this point. If we didn't detect any plane
            // Show searchingPlane message.
            if (!hasTrackingPlane()) {
                messageSnackbarHelper.showMessage(getActivity(), SEARCHING_PLANE_MESSAGE);
            }
            if (currentAnchor != null && currentAnchor.getTrackingState() == TrackingState.TRACKING) {
                currentAnchor.getPose().toMatrix(anchorMatrix, 0);

                // Update and draw the model and its shadow.
                virtualObject.updateModelMatrix(anchorMatrix, 0.005f);
                virtualObjectShadow.updateModelMatrix(anchorMatrix, 0.005f);

                // Draw the object relative to its view matrix,projection matrix,color
                virtualObject.draw(viewmtx, projmtx, colorCorrectionRgba, andyColor);
                virtualObjectShadow.draw(viewmtx, projmtx, colorCorrectionRgba, andyColor);
            }
        } catch (Throwable t) {
            // This This will avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t);
        }
    }


    // Handle only one tap per frame, as taps are usually low frequency compared to frame rate.
    private void handleTap(Frame frame, Camera camera) {
        MotionEvent tap = tapHelper.poll();
        if (tap != null && camera.getTrackingState() == TrackingState.TRACKING) {
            for (HitResult hit : frame.hitTest(tap)) {
                // Check if any plane was hit, and if it was hit inside the plane polygon
                Trackable trackable = hit.getTrackable();
                // Creates an anchor if a plane or an oriented point was hit.

            }
        }
    }

    // when in state of tracking
    private boolean hasTrackingPlane() {
        for (Plane plane : session.getAllTrackables(Plane.class)) {
            if (plane.getTrackingState() == TrackingState.TRACKING) {
                return true;
            }
        }
        return false;
    }

    private synchronized void onClearButtonPressed() {
        // Clear the anchor from the scene.
        // The next line is the new addition.
        cloudAnchorManager.clearListeners();
        // The next line is the new addition.
        resolveButton.setEnabled(true);

        currentAnchor = null;
    }

    // Resolve Button
    private synchronized void onResolveButtonPressed() {
        ResolveDialogFragment dialog = ResolveDialogFragment.createWithOkListener(
                this::onShortCodeEntered);
        dialog.show(getActivity().getSupportFragmentManager(), "Resolve");
    }

    // Logic for checking Short code entered or not
    private synchronized void onShortCodeEntered(int shortCode) {
        firebaseManager.getCloudAnchorId(shortCode, cloudAnchorId -> {
            if (cloudAnchorId == null || cloudAnchorId.isEmpty()) {
                messageSnackbarHelper.showMessage(
                        getActivity(),
                        "A Cloud Anchor ID for the short code " + shortCode + " was not found.");
                return;
            }
            resolveButton.setEnabled(false);
            cloudAnchorManager.resolveCloudAnchor(
                    session,
                    cloudAnchorId,
                    anchor -> onResolvedAnchorAvailable(anchor, shortCode));
        });
    }

    // IF the given user short code is resolved
    private synchronized void onResolvedAnchorAvailable(Anchor anchor, int shortCode) {
        Anchor.CloudAnchorState cloudState = anchor.getCloudAnchorState();

        if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
            messageSnackbarHelper.showMessage(getActivity(), "Cloud Anchor Resolved. Short code: " + shortCode);
            currentAnchor = anchor;

            // Get the instance from firebase using relative path and name/label
            rootNode = FirebaseDatabase.getInstance();
            reference = rootNode.getReference("user").child("object_labeled");

            // Event listener for 3D model Attributes
            // if the short code of the model was not found it will throw a error message
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild(currentAnchor.getCloudAnchorId())) {
                        name = String.valueOf(snapshot.child(currentAnchor.getCloudAnchorId()).child("object_name").getValue());
                        url = String.valueOf(snapshot.child(currentAnchor.getCloudAnchorId()).child("object_url").getValue());
                        desc = String.valueOf(snapshot.child(currentAnchor.getCloudAnchorId()).child("object_desc").getValue());
                        obj_name.setText(name);
                        obj_url.setText(url);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    messageSnackbarHelper.showMessage(getActivity(), error.toString());
                }
            });

        } else {
            messageSnackbarHelper.showMessage(
                    getActivity(),
                    "Error while resolving anchor with short code " + shortCode + ". Error: "
                            + cloudState.toString());
            resolveButton.setEnabled(true);
        }
    }

    // Implementation of bottom_sheet_dialog
    private void showBottomSheetDialog(String name, String url, String desc) {

        Dialog dialog = new Dialog(getActivity());
        dialog.setCancelable(true);
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        // Refer to bottom_sheet_dialog layout
        View view = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);
        dialog.setContentView(view);
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvDetail = view.findViewById(R.id.tvdetail);
        TextView cancelDialog = view.findViewById(R.id.tvCancelDialog);
        TextView moreDialog = view.findViewById(R.id.tvMoreDialog);
        tvName.setText(name);
        tvDetail.setText(desc);
        dialog.show();
        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        // Here user can hot more and it will open a url in a browser window
        moreDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
               if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    Toast.makeText(getContext(), "Url format provided is not correct", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Uri uri = Uri.parse(url);// missing 'http://' will cause crashed
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });
    }
}
