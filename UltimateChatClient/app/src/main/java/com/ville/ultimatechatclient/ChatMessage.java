package com.ville.ultimatechatclient;

/*
    A class for reconstructing a ChatMessage object on the client-side.
    There are enough fields present that it's justified, imo.
    @author Ville Lohkovuori
    10/2017
 */

public class ChatMessage {

    private String msgBody;
    private String userName;
    private String timeStamp;
    private boolean isOwn;

    // I wanted to have a 'regular' constructor as well, but it turns out I never used this
    public ChatMessage(String timeStamp, String isOwn, String userName, String msgBody) {

        this.timeStamp = timeStamp;
        this.isOwn = Boolean.parseBoolean(isOwn);
        this.userName = userName;
        this.msgBody = msgBody;
    }

    // this depends on the form of the msg String coming from the server staying consistent...
    // a very brittle approach, but to write the checks to prevent any mishaps would be beyond tedious
    public ChatMessage(String[] splices) {

        this.userName = splices[2];
        this.isOwn = Boolean.parseBoolean(splices[1]);
        this.timeStamp = splices[0];

        StringBuilder sb = new StringBuilder();

        for (int i = 3; i < splices.length; i++) {
            sb.append(splices[i]);
            sb.append(" ");
        }
        this.msgBody = sb.toString();
    } // end constructor

    public String getUserName() {
        return this.userName;
    }

    public boolean isOwn() {
        return this.isOwn;
    }

    // even though these are never used, they exist for consistency
    public String getMsgBody() {
        return this.msgBody;
    }

    public String getTimeStamp() {
        return this.timeStamp;
    }

    // used as the standard format of chat bubble messages.
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append(this.userName);
        sb.append("\n"); // so that the username appears in the upper left corner
        sb.append(this.msgBody);
        sb.append("\n"); // timeStamp on the last line
        sb.append(this.timeStamp);

        return sb.toString();
    }
} // end class