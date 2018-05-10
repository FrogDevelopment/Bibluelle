package fr.frogdevelopment.bibluelle.manage;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import fr.frogdevelopment.bibluelle.GlideApp;
import fr.frogdevelopment.bibluelle.R;
import fr.frogdevelopment.bibluelle.data.DatabaseCreator;
import fr.frogdevelopment.bibluelle.data.dao.BookDao;
import fr.frogdevelopment.bibluelle.data.entities.Book;
import fr.frogdevelopment.bibluelle.data.entities.JsonExclude;

public class ManageFragment extends Fragment implements View.OnClickListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManageFragment.class);

    private static final int REQUEST_CODE_SIGN_IN = 0;
    private static final String FILE_NAME = "bibluelle.save";
    private static final String SYNC_VERSION = "sync_version";
    private static final CustomPropertyKey VERSION_PROPERTY_KEY = new CustomPropertyKey("version", CustomPropertyKey.PUBLIC);

    private GoogleSignInClient mGoogleSignInClient;
    private DriveResourceClient mDriveResourceClient;
    private SignInButton mSignInButton;
    private ImageView mPhoto;
    private TextView mName;
    private TextView mEmail;
    private View mConnectedView;
    private SharedPreferences preferences;

    private final Gson mGson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return f.getAnnotation(JsonExclude.class) != null;
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            })
            .create();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mConnectedView = view.findViewById(R.id.card_profile);

        mSignInButton = view.findViewById(R.id.sign_in_button);
        mSignInButton.setSize(SignInButton.SIZE_WIDE);
        mSignInButton.setOnClickListener(this);

        mPhoto = view.findViewById(R.id.profile_photo);
        mName = view.findViewById(R.id.profile_name);
        mEmail = view.findViewById(R.id.profile_email);

        view.findViewById(R.id.sign_out).setOnClickListener(this);
        view.findViewById(R.id.disconnect).setOnClickListener(this);
        view.findViewById(R.id.save).setOnClickListener(this);
        view.findViewById(R.id.synchronize).setOnClickListener(this);
    }

    private SweetAlertDialog mSweetAlertDialog;

    @Override
    public void onStart() {
        super.onStart();

        LOGGER.info("Start sign in");

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER)
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), signInOptions);

        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(requireContext());
        updateUI(lastSignedInAccount);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {

                mSweetAlertDialog.dismissWithAnimation();
                mSweetAlertDialog = null;

                GoogleSignInAccount account = task.getResult(ApiException.class);
                updateUI(account);

            } catch (ApiException e) {
                LOGGER.error("signInResult -> failed code =" + e.getStatusCode(), e);
                updateUI(null);

                showError(R.string.profile_msg_error_signing, e.getStatusCode());
            }
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.sign_in_button:
                showLoading(R.string.profile_msg_loading_sign_in);
                startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
                break;

            case R.id.sign_out:
                showLoading(R.string.profile_msg_loading_sign_out);
                mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
                    mSweetAlertDialog.dismissWithAnimation();
                    mSweetAlertDialog = null;

                    updateUI(null);
                });
                break;

            case R.id.disconnect:
                showConfirm(R.string.profile_msg_confirm_disconnect, R.string.global_continue, sweetAlertDialog -> revokeAccess());

                break;

            case R.id.save:
                showConfirm(R.string.profile_msg_confirm_save, R.string.profile_action_save, sweetAlertDialog -> saveData());
                break;

            case R.id.synchronize:
                showConfirm(R.string.profile_msg_confirm_sync, R.string.profile_action_sync, sweetAlertDialog -> syncData());
                break;
        }
    }

    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
            mSignInButton.setVisibility(View.INVISIBLE);
            mConnectedView.setVisibility(View.VISIBLE);

            mDriveResourceClient = Drive.getDriveResourceClient(requireContext(), account);

            GlideApp.with(this)
                    .asBitmap()
                    .dontAnimate()
                    .load(account.getPhotoUrl())
                    .into(mPhoto);

            mName.setText(account.getDisplayName());
            mEmail.setText(account.getEmail());

            preferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        } else {
            mSignInButton.setVisibility(View.VISIBLE);
            mConnectedView.setVisibility(View.INVISIBLE);
        }
    }

    private void revokeAccess() {
        ThreadPoolExecutor executor = getExecutor();

        searchFile(executor)
                .continueWithTask(executor, task -> {
                    if (task.isSuccessful()) {
                        LOGGER.info("revokeAccess -> delete file");
                        return mDriveResourceClient.delete(task.getResult().getDriveId().asDriveResource());

                    } else {
                        LOGGER.info("revokeAccess -> no file to delete");
                        return Tasks.forResult(null);
                    }
                })
                .continueWithTask(executor, task -> {
                    LOGGER.info("revokeAccess -> sign out");
                    return mGoogleSignInClient.revokeAccess();
                })
                .addOnSuccessListener(driveFile -> {

                    SharedPreferences.Editor edit = preferences.edit();
                    edit.putInt(SYNC_VERSION, 0);
                    edit.apply();

                    mSweetAlertDialog.dismissWithAnimation();
                    mSweetAlertDialog = null;

                    updateUI(null);
                })
                .addOnFailureListener(e -> {
                    LOGGER.error("revokeAccess -> an error occurred", e);
                    showError(R.string.profile_msg_error_revoke_access, ExceptionUtils.getMessage(e));
                });
    }

    private void saveData() {
        showLoading(R.string.profile_msg_loading_uploading);

        LOGGER.info("Save data -> fetch data");

        LiveData<List<Book>> allIsbn = DatabaseCreator.getInstance().getBookDao().loadAllBooks();
        allIsbn.observe(this, data -> {
            allIsbn.removeObservers(ManageFragment.this);

            if (data != null && !data.isEmpty()) {
                ThreadPoolExecutor executor = getExecutor();
                searchFile(executor)
                        .continueWithTask(task -> {
                            if (task.isCanceled()) { // no file to sync
                                // save data
                                saveData(data);
                            } else {
                                Metadata metadata = task.getResult();

                                if (isDriveNewerVersion(metadata)) {
                                    mSweetAlertDialog.changeAlertType(SweetAlertDialog.WARNING_TYPE);
                                    mSweetAlertDialog
                                            .setTitleText(getString(R.string.title_warning))
                                            .setContentText(getString(R.string.profile_msg_warning_new_version))
                                            .showContentText(true)
                                            .setConfirmText(getString(R.string.profile_action_update))
                                            .setConfirmClickListener(sweetAlertDialog -> {
                                                // update data
                                                downloadData(executor, metadata.getDriveId().asDriveFile());
                                            })
                                            .setCancelText("Override")
                                            .setCancelClickListener(sweetAlertDialog -> {
                                                // save data
                                                saveData(data);
                                            });
                                } else {
                                    // save data
                                    saveData(data);
                                }
                            }

                            return Tasks.forResult(null);
                        });
            } else {
                showError(R.string.profile_msg_error_no_data_to_save);
            }
        });
    }

    private void saveData(List<Book> data) {
        int nextVersion = preferences.getInt(SYNC_VERSION, 0) + 1;

        LOGGER.info("Save data -> access AppFolder & Drive contents");
        final Task<DriveFolder> appFolderTask = mDriveResourceClient.getAppFolder();
        final Task<DriveContents> driveContentsTask = mDriveResourceClient.createContents();

        Tasks.whenAll(appFolderTask, driveContentsTask)
                .continueWithTask(getExecutor(), task -> {
                    DriveFolder appFolder = appFolderTask.getResult();
                    DriveContents contents = driveContentsTask.getResult();

                    LOGGER.info("Save data -> open contents outputStream");
                    try (OutputStream outputStream = contents.getOutputStream();
                         Writer writer = new OutputStreamWriter(outputStream)) {

                        LOGGER.info("Save data -> transform files to byte arrays");
                        for (Book book : data) {
                            try {
                                book.thumbnailByte = IOUtils.toByteArray(requireContext().getFileStreamPath(book.getThumbnailFile()));
                                book.coverByte = IOUtils.toByteArray(requireContext().getFileStreamPath(book.getCoverFile()));
                            } catch (IOException e) {
                                return Tasks.forException(e);
                            }
                        }

                        LOGGER.info("Save data -> transform to json");
                        String json = mGson.toJson(data);

                        LOGGER.info("Save data -> write data");
                        writer.write(json);
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(FILE_NAME)
                            .setDescription("Bibluelle saved books")
                            .setMimeType("text/plain")
                            .setStarred(true)
                            .setLastViewedByMeDate(new Date())
                            .setCustomProperty(VERSION_PROPERTY_KEY, String.valueOf(nextVersion))
                            .build();

                    LOGGER.info("Save data -> create file");
                    return mDriveResourceClient.createFile(appFolder, changeSet, contents);
                })
                .addOnSuccessListener(driveFile -> {
                    LOGGER.info("Save data -> File created");
                    showSuccess(R.string.profile_msg_success_data_saved);

                    LOGGER.info("Save data -> update version");
                    SharedPreferences.Editor edit = preferences.edit();
                    edit.putInt(SYNC_VERSION, nextVersion);
                    edit.apply();
                })
                .addOnFailureListener(e -> {
                    LOGGER.error("Save data -> Unable to create file", e);
                    showError(R.string.profile_msg_error_save_data, ExceptionUtils.getMessage(e));
                });
    }

    private void syncData() {
        ThreadPoolExecutor executor = getExecutor();

        searchFile(executor)
                .continueWithTask(task -> {
                    if (task.isCanceled()) { // no file to sync
                        mSweetAlertDialog.changeAlertType(SweetAlertDialog.WARNING_TYPE);
                        mSweetAlertDialog
                                .setTitleText(getString(R.string.title_warning))
                                .setContentText(getString(R.string.profile_msg_error_no_data_to_sync))
                                .setConfirmText(getString(android.R.string.ok))
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    mSweetAlertDialog = null;
                                })
                                .showContentText(true)
                                .showCancelButton(false);
                    } else {
                        Metadata metadata = task.getResult();

                        if (isDriveNewerVersion(metadata)) {
                            mSweetAlertDialog.changeAlertType(SweetAlertDialog.WARNING_TYPE);
                            mSweetAlertDialog
                                    .setTitleText(getString(R.string.title_warning))
                                    .setContentText(getString(R.string.profile_msg_warning_new_version))
                                    .showContentText(true)
                                    .setConfirmText(getString(R.string.profile_action_update))
                                    .setConfirmClickListener(sweetAlertDialog -> {
                                        // continue
                                        downloadData(executor, metadata.getDriveId().asDriveFile());
                                    })
                                    .setCancelText(getString(android.R.string.cancel))
                                    .setCancelClickListener(sweetAlertDialog -> {
                                        // cancel
                                        sweetAlertDialog.dismissWithAnimation();
                                        mSweetAlertDialog = null;
                                    });
                        } else {
                            showSuccess(R.string.profile_msg_success_no_new_version);
                        }
                    }

                    return Tasks.forResult(null);
                })
                .addOnFailureListener(e -> {
                    LOGGER.error("Sync data -> Unable to retrieved data", e);
                    showError(R.string.profile_msg_error_sync_data, ExceptionUtils.getMessage(e));
                });
    }

    private void downloadData(ThreadPoolExecutor executor, DriveFile driveFile) {
        showLoading(R.string.profile_msg_loading_downloading);
        LOGGER.info("Sync data -> Retrieving file contents");
        mDriveResourceClient.openFile(driveFile, DriveFile.MODE_READ_ONLY)
                .continueWithTask(executor, task -> {
                    LOGGER.info("Sync data -> Opening contents inputStream");

                    DriveContents contents = task.getResult();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(contents.getInputStream()))) {

                        LOGGER.info("Sync data -> Reading file contents");
                        StringBuilder builder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }

                        LOGGER.info("Sync data -> Transform from JSON");
                        Book[] books = mGson.fromJson(builder.toString(), Book[].class);

                        LOGGER.info("Sync data -> save into database");
                        for (Book book : books) {
                            try {
                                saveFile(book.coverByte, book.getCoverFile());
                                saveFile(book.thumbnailByte, book.getThumbnailFile());
                            } catch (IOException e) {
                                return Tasks.forException(e);
                            }

                            BookDao.insert(book);
                        }
                    }

                    return mDriveResourceClient.discardContents(contents);
                })
                .addOnSuccessListener(aVoid -> {
                    LOGGER.info("Sync data -> Data retrieved");
                    showSuccess(R.string.profile_msg_success_new_version_sync);
                })
                .addOnFailureListener(e -> {
                    LOGGER.error("Sync data -> Unable to retrieved data", e);
                    showError(R.string.profile_msg_error_sync_data, ExceptionUtils.getMessage(e));
                });
    }

    private Task<Metadata> searchFile(ThreadPoolExecutor executor) {
        showLoading(R.string.profile_msg_loading_connecting);

        LOGGER.info("Search file -> access App Folder");
        return mDriveResourceClient.getAppFolder()
                .continueWithTask(executor, task -> {
                    DriveFolder appFolderResult = task.getResult();
                    Query query = new Query.Builder()
                            .addFilter(Filters.eq(SearchableField.TITLE, FILE_NAME))
                            .build();

                    return mDriveResourceClient.queryChildren(appFolderResult, query);
                })
                .continueWithTask(executor, task -> {
                    if (task.isSuccessful()) {
                        MetadataBuffer metadataBuffer = task.getResult();

                        if (metadataBuffer.getCount() > 0) {
                            for (Metadata metadata : metadataBuffer) {
                                if (FILE_NAME.equals(metadata.getTitle())) {
                                    return Tasks.forResult(metadata);
                                }
                            }
                        }
                    }

                    return Tasks.forCanceled();
                });
    }

    public boolean isDriveNewerVersion(Metadata metadata) {
        Map<CustomPropertyKey, String> customProperties = metadata.getCustomProperties();
        String version = customProperties.getOrDefault(VERSION_PROPERTY_KEY, "0");

        Integer driveVersion = Integer.valueOf(version);

        int sync_version = preferences.getInt(SYNC_VERSION, 0);

	    return driveVersion > sync_version;
    }

    /**
     * Create a new ThreadPoolExecutor with 2 threads for each processor on the device and a 60 second keep-alive time.
     * cf https://developers.google.com/android/guides/tasks
     **/
    @NonNull
    private ThreadPoolExecutor getExecutor() {
        int numCores = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(numCores * 2, numCores * 2, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    private void showLoading(int messageId) {
        if (mSweetAlertDialog == null) {
            mSweetAlertDialog = new SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE);
        } else {
            mSweetAlertDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
        }
        mSweetAlertDialog.setTitleText(getString(messageId));
        mSweetAlertDialog.setCancelable(false);
        mSweetAlertDialog.showCancelButton(false);
        mSweetAlertDialog.showContentText(false);

        if (!mSweetAlertDialog.isShowing()) {
            mSweetAlertDialog.show();
        }
    }

    private void showError(int messageId, Object... formatArgs) {
        if (mSweetAlertDialog == null) {
            mSweetAlertDialog = new SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE);
        } else {
            mSweetAlertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
        }

        mSweetAlertDialog.setTitleText(getString(R.string.title_error));
        mSweetAlertDialog.setContentText(getString(messageId, formatArgs));
        mSweetAlertDialog.setConfirmText(getString(android.R.string.ok));
        mSweetAlertDialog.setConfirmClickListener(sweetAlertDialog -> {
            sweetAlertDialog.dismissWithAnimation();
            mSweetAlertDialog = null;
        });

        if (!mSweetAlertDialog.isShowing()) {
            mSweetAlertDialog.show();
        }
    }

    private void showSuccess(int messageId) {
        if (mSweetAlertDialog == null) {
            mSweetAlertDialog = new SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE);
        } else {
            mSweetAlertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
        }
        mSweetAlertDialog.setTitleText(getString(R.string.title_success));
        mSweetAlertDialog.setContentText(getString(messageId));
        mSweetAlertDialog.setConfirmText(getString(android.R.string.ok));
        mSweetAlertDialog.showContentText(true);
        mSweetAlertDialog.setConfirmClickListener(sweetAlertDialog -> {
            sweetAlertDialog.dismissWithAnimation();
            mSweetAlertDialog = null;
        });

        if (!mSweetAlertDialog.isShowing()) {
            mSweetAlertDialog.show();
        }
    }

    private void showConfirm(int messageId, int confirmId, SweetAlertDialog.OnSweetClickListener listener) {
        if (mSweetAlertDialog == null) {
            mSweetAlertDialog = new SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE);
        } else {
            mSweetAlertDialog.changeAlertType(SweetAlertDialog.WARNING_TYPE);
        }

        mSweetAlertDialog.setTitleText(getString(R.string.title_warning));
        mSweetAlertDialog.setContentText(getString(messageId));
        mSweetAlertDialog.setCancelText(getString(android.R.string.cancel));
        mSweetAlertDialog.setCancelClickListener(sweetAlertDialog -> {
            sweetAlertDialog.dismissWithAnimation();
            mSweetAlertDialog = null;
        });
        mSweetAlertDialog.setConfirmText(getString(confirmId));
        mSweetAlertDialog.setConfirmClickListener(listener);

        if (!mSweetAlertDialog.isShowing()) {
            mSweetAlertDialog.show();
        }
    }

    private void saveFile(byte[] data, String fileName) throws IOException {
        try (OutputStream fOut = requireContext().openFileOutput(fileName, Context.MODE_PRIVATE)) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        }
    }

}
