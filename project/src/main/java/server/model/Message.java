package server.model;

import com.google.gson.Gson;

import java.util.List;

/**
 * Represents a message sent from the client to the server.
 *
 * <br>
 * Messages are JSON specified in the following format: <br>
 * <strong>method</strong> - The method to call on the server (e.g. insert) <br>
 * <strong>id</strong> - The id of the list to get (optional) <br>
 * <strong>list</strong> - The list to insert (optional) <br>
 */
public class Message {
    private String method;
    private String listId;
    private ShoppingList list;
    private List<ShoppingList> lists;
    private String address;
    private String quorumId;
    private String redirectId;

    // status update
    private String statusNodeAddress;
    private Boolean statusValue;

    public Message() {}

    public Message(String message) {
        new Gson().fromJson(message, Message.class);
    }

    public String getMethod() {
        return method;
    }

    /**
     * Get the id of the list to get. <br>
     * Note that this is different from getList().getId() because the message may not contain a list.
     * @return
     */
    public String getListId() {
        return listId;
    }

    public ShoppingList getList() {
        return list;
    }

    public String getQuorumId() {
        return quorumId;
    }

    public String getRedirectId() {
        return redirectId;
    }

    public String getAddress() {
        return address;
    }

    public String getStatusNodeAddress() {
        return statusNodeAddress;
    }

    public Boolean getStatusValue() {
        return statusValue;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setList(ShoppingList list) {
        this.list = list;
    }

    public void setQuorumId(String quorumId) {
        this.quorumId = quorumId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setStatusNodeAddress(String statusNodeAddress) {
        this.statusNodeAddress = statusNodeAddress;
    }

    public void setStatusValue(Boolean statusValue) {
        this.statusValue = statusValue;
    }

    public void setRedirectId(String redirectId) {
        this.redirectId = redirectId;
    }

    public List<ShoppingList> getLists() {
        return lists;
    }

    public void setLists(List<ShoppingList> lists) {
        this.lists = lists;
    }
}