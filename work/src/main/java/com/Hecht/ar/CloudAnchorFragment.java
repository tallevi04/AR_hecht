package com.Hecht.ar;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.Point.OrientationMode;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.Hecht.ar.helpers.CameraPermissionHelper;
import com.Hecht.ar.helpers.CloudAnchorManager;
import com.Hecht.ar.helpers.FirebaseManager;
import com.Hecht.ar.helpers.ResolveDialogFragment;
import com.Hecht.ar.helpers.SnackbarHelper;
import com.Hecht.ar.helpers.StorageManager;
import com.Hecht.ar.helpers.TapHelper;
import com.Hecht.ar.helpers.TrackingStateHelper;
import com.Hecht.ar.rendering.BackgroundRenderer;
import com.Hecht.ar.rendering.ObjectRenderer;
import com.Hecht.ar.rendering.ObjectRenderer.BlendMode;
import com.Hecht.ar.rendering.PlaneRenderer;
import com.Hecht.ar.rendering.PointCloudRenderer;
import com.Hecht.ar.helpers.DisplayRotationHelper;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Main Fragment for the Cloud Anchors.
 * This is where the AR Session and the Cloud Anchors are managed.
 */
public class CloudAnchorFragment extends Fragment implements GLSurfaceView.Renderer {
    private static final String TAG = CloudAnchorFragment.class.getSimpleName();
    private GLSurfaceView surfaceView;
    private boolean installRequested;
    private Button resolveButton;
    private Session session;
    private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
    private final CloudAnchorManager cloudAnchorManager = new CloudAnchorManager();
    private DisplayRotationHelper displayRotationHelper;
    private TrackingStateHelper trackingStateHelper;
    private TapHelper tapHelper;
    private final StorageManager storageManager = new StorageManager();
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
    public String cloudAnchorId = "";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        tapHelper = new TapHelper(context);
        trackingStateHelper = new TrackingStateHelper(requireActivity());
        firebaseManager = new FirebaseManager(context);
    }
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate from the Layout XML file.
        View rootView = inflater.inflate(R.layout.cloud_anchor_fragment, container, false);
        GLSurfaceView surfaceView = rootView.findViewById(R.id.surfaceView);
        // Detect Surface and view
        this.surfaceView = surfaceView;
        displayRotationHelper = new DisplayRotationHelper(requireContext());
        surfaceView.setOnTouchListener(tapHelper);
        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        surfaceView.setWillNotDraw(false);
        Button clearButton = rootView.findViewById(R.id.clear_button);
        clearButton.setOnClickListener(v -> onClearButtonPressed());
        resolveButton = rootView.findViewById(R.id.resolve_button);
        resolveButton.setOnClickListener(v -> onResolveButtonPressed());
        return rootView;
    }

    /**
     * check for arcore google service enabled device
     * And other necessary permissions
     */
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

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(requireActivity())) {
                    CameraPermissionHelper.requestCameraPermission(requireActivity());
                    return;
                }
                //To create a hosted anchor on a user tap instead of a regular one. To do that, you will need to configure the ARCore Session to enable Cloud Anchors.
                // Create the session.
                session = new Session(requireActivity());

                // Configure the session.
                Config config = new Config(session);
                config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);
                session.configure(config);

            } catch (UnavailableArcoreNotInstalledException
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

        // Check for the camera if available or not
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
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();
        }
    }

    /**
     * Prompt Camera permission on screen
     */
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

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
        try {
            // Create the texture and pass it to ARCore session to be filled during update().
            backgroundRenderer.createOnGlThread(getContext());
            planeRenderer.createOnGlThread(getContext(), "models/trigrid.png");
            pointCloudRenderer.createOnGlThread(getContext());

            virtualObject.createOnGlThread(getContext(), "models/info_icon.obj", "models/andy.png");
            virtualObject.setMaterialProperties(0.0f, 2.0f, 0.5f, 6.0f);

            virtualObjectShadow
                    .createOnGlThread(getContext(), "models/andy_shadow.obj", "models/andy_shadow.png");
            virtualObjectShadow.setBlendMode(BlendMode.Shadow);
            virtualObjectShadow.setMaterialProperties(1.0f, 0.0f, 0.0f, 1.0f);

        } catch (IOException e) {
            Log.e(TAG, "Failed to read an asset file", e);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // It will Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (session == null) {
            return;
        }
        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session);

        try {
            session.setCameraTextureName(backgroundRenderer.getTextureId());

            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            Frame frame = session.update();
            // Add this line right below:
            cloudAnchorManager.onUpdate();
            Camera camera = frame.getCamera();

            // Handle one tap per frame.
            handleTap(frame, camera);

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

            // If we didn't detect any plane, show searchingPlane message.
            if (!hasTrackingPlane()) {
                messageSnackbarHelper.showMessage(getActivity(), SEARCHING_PLANE_MESSAGE);
            }

            // Visualize planes.
            planeRenderer.drawPlanes(
                    session.getAllTrackables(Plane.class), camera.getDisplayOrientedPose(), projmtx);

            if (currentAnchor != null && currentAnchor.getTrackingState() == TrackingState.TRACKING) {
                currentAnchor.getPose().toMatrix(anchorMatrix, 0);
                // Update and draw the model and its shadow.
                virtualObject.updateModelMatrix(anchorMatrix, 0.005f);
                virtualObjectShadow.updateModelMatrix(anchorMatrix, 0.005f);
                virtualObject.draw(viewmtx, projmtx, colorCorrectionRgba, andyColor);
                virtualObjectShadow.draw(viewmtx, projmtx, colorCorrectionRgba, andyColor);
            }
        } catch (Throwable t) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t);
        }
    }

    // Handle only one tap per frame.
    private void handleTap(Frame frame, Camera camera) {
        if (currentAnchor != null) {
            // It will do nothing if there was already an anchor and simply Return.
            return;
        }
        MotionEvent tap = tapHelper.poll();
        if (tap != null && camera.getTrackingState() == TrackingState.TRACKING) {
            for (HitResult hit : frame.hitTest(tap)) {
                // Check if any plane was hit.
                // And if that tap was hit inside the plane polygon
                Trackable trackable = hit.getTrackable();
                // This will Creates an anchor if a plane or a surface oriented point was hit.
                if ((trackable instanceof Plane
                        && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())
                        && (PlaneRenderer.calculateDistanceToPlane(hit.getHitPose(), camera.getPose()) > 0))
                        || (trackable instanceof Point
                        && ((Point) trackable).getOrientationMode()
                        == OrientationMode.ESTIMATED_SURFACE_NORMAL)) {
                    // Adding an Anchor will tell ARCore that it should track this position in space.
                    // Then this anchor will be created on the Plane to place the 3D model,Tn the correct position relative both to the world and to the plane.
                    currentAnchor = hit.createAnchor();
                    getActivity().runOnUiThread(() -> resolveButton.setEnabled(false));
                    messageSnackbarHelper.showMessage(getActivity(), "Now hosting anchor...");
                    cloudAnchorManager.hostCloudAnchor(session, currentAnchor, /* ttl= */ 300, this::onHostedAnchorAvailable);
                    break;
                }
            }
        }
    }

    //Checks if we detected at least one plane.
    private boolean hasTrackingPlane() {
        for (Plane plane : session.getAllTrackables(Plane.class)) {
            if (plane.getTrackingState() == TrackingState.TRACKING) {
                return true;
            }
        }
        return false;
    }

    private synchronized void onClearButtonPressed() {
        // This will Clear the anchor from the scene.
        // If the button is pressed.
        cloudAnchorManager.clearListeners();
        resolveButton.setEnabled(true);
        currentAnchor = null;
    }
    // THis method will check that a cloud anchor ID is present in Firebase or not.
    // If exist or not it will pop a message
    private synchronized void onHostedAnchorAvailable(Anchor anchor) {
        Anchor.CloudAnchorState cloudState = anchor.getCloudAnchorState();
        // Cross check the ID if hosted or not
        if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
            cloudAnchorId = anchor.getCloudAnchorId();
            Log.i("tallevi: cloudAnchorId", cloudAnchorId);
            firebaseManager.nextShortCode(shortCode -> {
                if (shortCode != null) {
                    firebaseManager.storeUsingShortCode(shortCode, cloudAnchorId);
                    messageSnackbarHelper.showMessage(getActivity(), "Cloud Anchor Hosted. Short code: " + shortCode);
                    openDialog(cloudAnchorId);
                } else {
                    // Firebase could not provide a short code.
                    messageSnackbarHelper.showMessage(getActivity(), "Cloud Anchor Hosted, but could not "
                            + "get a short code from Firebase.");
                }
            });
            currentAnchor = anchor;
        } else {
            messageSnackbarHelper.showMessage(getActivity(), "Error while hosting: " + cloudState.toString());
        }
    }

    // Clicking the Resolve button will open a dialog box that prompts the user for a short code.
    // The short code is used to retrieve the Cloud Anchor ID from StorageManager and resolve the anchor to be hosted.
    private synchronized void onResolveButtonPressed() {
        ResolveDialogFragment dialog = ResolveDialogFragment.createWithOkListener(
                this::onShortCodeEntered);
        dialog.show(getActivity().getSupportFragmentManager(), "Resolve");
    }
    // Dialog box that will ask for short code
    // See DetailsFragment.java file
    public void openDialog(String cloudAnchorId) {
        Dialog dialog = new Dialog(cloudAnchorId);
        dialog.show(getActivity().getSupportFragmentManager(), "Dialog");
    }
    // When the short code is entered it will check and prompt a message on screen.
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

    /**
     * Resolving cloud anchor with relative short code
     * Toast message will appear on the bottom
     */
    private synchronized void onResolvedAnchorAvailable(Anchor anchor, int shortCode) {
        Anchor.CloudAnchorState cloudState = anchor.getCloudAnchorState();

        messageSnackbarHelper.showMessage(
                getActivity(),
                cloudAnchorId);
        if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
            messageSnackbarHelper.showMessage(getActivity(), "Cloud Anchor Resolved. Short code: " + shortCode);
            currentAnchor = anchor;
        } else {
            messageSnackbarHelper.showMessage(
                    getActivity(),
                    "Error while resolving anchor with short code " + shortCode + ". Error: "
                            + cloudState.toString());
            resolveButton.setEnabled(true);
        }
    }
}
