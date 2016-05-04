package mfs.node;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import mfs.Utility;
import mfs.filesystem.Filesystem;
import mfs.filesystem.FilesystemImpl;
import mfs.network.Client;
import mfs.network.Message;
import mfs.network.MessageContract;
import mfs.service.ServiceAccessor;

public class MobileNodeImpl implements MobileNode {
    private static final String LOG_TAG = MobileNodeImpl.class.getSimpleName();

    private String id;
    private String name;
    private String address;
    private boolean isConnected;
    private Filesystem backingFilesystem;

    public MobileNodeImpl(String id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setAddress(String address) {
        this.address = address;
    }


    @Override
    public boolean isConnected() {
        return isConnected;
    }

    public void  setConnected(boolean isConnected) {
        this.isConnected =  isConnected;
    }

    @Override
    public boolean connect() {
        // request filesystem metadata from the node
        Message requestMessage = new Message(ServiceAccessor.getMyId(),
                MessageContract.Type.MSG_GET_FS_METADATA,
                "GET FILESYSTEM METADATA");
        Client.Response<String> response = Client.getInstance().executeRequestString(
                Utility.getIpFromAddress(getAddress()),
                Utility.getPortFromAddress(getAddress()),
                Utility.convertMessageToString(requestMessage));
        if(response == null) {
            return false;
        }
        Message responseMessage = Utility.convertStringToMessage(response.getResult());
        response.close();
        if (responseMessage.getType() != MessageContract.Type.MSG_GET_FS_METADATA_SUCCESS) {
            return false;
        }
        JSONObject metadata;
        // parse the response
        try {
            JSONObject jsonResponse = new JSONObject(responseMessage.getBody());
            metadata = jsonResponse.getJSONObject(MessageContract.Field.FIELD_FS_METADATA);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Unable to parse MSG_GET_FS_METADATA response.", e);
            return false;
        }

        if (backingFilesystem != null) {
            backingFilesystem.setFilesystemMetadata(metadata);
        } else {
            backingFilesystem = new FilesystemImpl(metadata, this);
        }
        setConnected(true);
        return true;
    }

    @Override
    public Filesystem getBackingFilesystem() {
        if(isConnected()) {
            return backingFilesystem;
        }
        else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MobileNode && ((MobileNode)o).getId().equals(this.getId());
    }
}
