package com.ville.ultimatechatclient;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.MenuItem;
import android.widget.ToggleButton;
import java.util.Random;

/*
    The main controller Activity of the application.
    @author Ville Lohkovuori
    10/2017
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // assuming they're named identically to their ids, I figured it's clearer to do this
    // once at the beginning than to keep assigning the Views in every method separately
    private DrawerLayout drawerLayout;
    private GridView userGrid;
    private LinearLayout convoLayout;
    private TextView helpText;
    private FrameLayout helpFrame;
    private ScrollView helpScroll;
    private EditText inputField;
    private Button sendButton;
    private Button createChanButton;
    private Button deleteChanButton;
    private Spinner channelSpinner;
    private TextView channelText;
    private ToggleButton toggleButton;
    private ScrollView convoScroll;
    private EditText promptField;
    private Button okButton;
    private RelativeLayout promptLayout;

    private ChatReader reader;

    private boolean makingUser = true; // boolean to alter the behaviour of the promptField. If it is false, then you're creating a channel when the field appears
    private boolean unregistered = true; // making a new user and renaming a user use the same operation, so unregistered != makingUser, necessitating two booleans
    private boolean actuallyClickedOnSpinner = false; // needed as a kludge for correct Spinner operation in the channel menu (otherwise item #1 is automatically selected)

    // =============================================================================================
    // NATIVE ANDROID TOP-ORDER METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        reader = new ChatReader(this);
        Thread readThread = new Thread(reader);
        readThread.start(); // for 'technical reasons', the reader then creates the writer...

        // assigns all the billion and one Views that were declared above.
        // used purely to spare space by putting that stuff at the bottom of the file
        assignViews();

        setSupportActionBar((Toolbar)findViewById(R.id.topToolbar)); // set the custom toolbar as the AppBar
        helpText.setText(Utils.helpBlurb()); // load the help text
        postAdminMsg(Utils.startWelcomeMsg()); // post welcome msg in 3 parts
        postAdminMsg(Utils.middleWelcomeMsg());
        postAdminMsg(Utils.endWelcomeMsg());

        initListenersEtc(); // all the necessary listeners and other such thingies that are needed for various views to work properly
    } // end onCreate()

    @Override
    protected void onDestroy() {
        reader.getWriter().writeDataToServer(":quit\n"); // ensures that the user is deregistered upon disconnect
        super.onDestroy();
    }

    // =============================================================================================
    // MAIN CLIENT LOGIC (some Android methods and some custom ones)

    // creates a new modified TextView for each new message and updates the 'main screen' ScrollView with it.
    public void updateScreen(final ChatMessage msg) {
        runOnUiThread(new Runnable() { // ensures that the call 'happens' on the UIThread, not on the calling thread
            public void run() {

                String untrimmed = msg.getUserName();
                String userName = untrimmed.substring(0, untrimmed.length()-1); // removes the ':' at the end of the name

                String text = msg.toString();

                // this could be moved into a method, but it seems better to show what happens right here
                Integer bgColor;
                if (msg.isOwn()) { // own user
                    bgColor = R.color.colorCUser; // own user has a permanent color across all app starts
                    text = text.replaceFirst(":", "(you):"); // add an identifying tag to your own msgs
                }
                else if (Utils.userColors.containsKey(userName)) { // other, old user

                    bgColor = Utils.userColors.get(userName);
                }
                else { // new user (just joined)
                    if(Utils.freeColors.size() < 1) {
                        Utils.freeColors = Utils.loadColorList(); // if all the colors are in use, begin using duplicate ones
                    }

                    int index = new Random(System.currentTimeMillis()).nextInt(Utils.freeColors.size()); // 0-11 (less as colors get removed)
                    bgColor = Utils.freeColors.remove(index);
                    Utils.userColors.put(userName, bgColor); // mark that the color's in use, so that future msgs will also use it
                }

                // in order to make the color work, it must be converted to a format that AS understands upon compilation (I guess that is the word?)
                ChatBubble bubble = new ChatBubble(getApplicationContext(), text, getResources().getColor(bgColor, null), 20.0f);
                convoLayout.addView(bubble);
                bubble.setParams(msg.isOwn(), 40, 450, 600); // adjust the size and placement of the bubble

                scrollDown(convoScroll); // scroll the ScrollView all the way down

                // upon logoff, change username and quit events of all users except your own, add their reserved
                // colors back to getFreeColors() and remove the colors from userColors
                if (Utils.spliceInput(msg.toString()).length > 4 && !msg.isOwn()) {

                    String action = Utils.spliceInput(msg.toString())[4]; // a HIGHLY brittle operation..! unacceptable in a real app

                    if (action.equals("quit") || action.equals("logged") || action.equals("renamed")) {

                        Utils.freeColors.add(Utils.userColors.get(userName));
                        Utils.userColors.remove(userName);
                    }
                } // end outer if-statement
            }
        }); // end runOnUiThread()
    } // end updateScreen()

    // deals with any String from the server that comes with the command tag as its first item.
    // the named cases are the server's responses to successful commands (failed ones will be printed as 'default' toasts)
    public void updateUI(final String cmd, final String[] cmdBody) { // cmdBody = whatever comes after the command tag and the command word itself ('CMD_TAG + help', etc)

        runOnUiThread(new Runnable() { // ensures that the call 'happens' on the UIThread, not on the calling thread
            public void run() {

                switch(cmd) {

                    case "users":

                        // populates the GridView (in the side drawer) with the contents of the cmdBody (a list of users)
                        UserListAdapter adapter = new UserListAdapter(getApplicationContext(), cmdBody);
                        userGrid.setAdapter(adapter);
                        break;

                    case "channels": // called (and returned) when opening the left side drawer

                        // obtain the correct channel order
                        String[] channelArray = Utils.formatToChannelArray(cmdBody);

                        // populate the spinner from the modified array
                        CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(getApplicationContext(), channelArray);
                        channelSpinner.setAdapter(spinnerAdapter);
                        break;

                    case "quit":

                        Utils.toast(Utils.arrToStr(cmdBody), getApplicationContext());
                        reader.terminate();
                        reader.getWriter().terminate();
                        System.exit(0); // works, but apparently is a big no-no in Android development. I'm not sure how to 'close' the client without this though
                        break;

                    case "channel":

                        // useless now due to the visual UI
                        break;

                    case "globalusers":

                        channelSpinner.setVisibility(View.INVISIBLE);
                        deleteChanButton.setVisibility(View.INVISIBLE);
                        createChanButton.setVisibility(View.INVISIBLE);
                        channelText.setText(R.string.globalUsers);

                        // populate the GridView (in the side drawer) with the contents of the cmdBody (a global list of users)
                        UserListAdapter adapter2 = new UserListAdapter(getApplicationContext(), cmdBody);
                        userGrid.setAdapter(adapter2);
                        break;

                    case "logoff":

                        unregistered = true;
                        toggleButton.setVisibility(View.INVISIBLE);
                        createChanButton.setVisibility(View.INVISIBLE);
                        deleteChanButton.setVisibility(View.INVISIBLE);
                        Utils.toast(Utils.arrToStr(cmdBody), getApplicationContext());
                        convoLayout.removeAllViews();
                        invalidateOptionsMenu(); // redraw the options menu, since you must register again
                        break;

                    case "tochan":

                        reader.getWriter().writeDataToServer(":users\n"); // update the userList on each channel pick (as the drawer remains open)
                        Utils.toast(Utils.arrToStr(cmdBody), getApplicationContext());
                        convoLayout.removeAllViews();
                        break;

                    case "makechan": // it makes the new channel and also switches to it

                        Utils.toast(Utils.arrToStr(cmdBody), getApplicationContext());
                        convoLayout.removeAllViews();
                        break;

                    case "delchan":

                        Utils.toast(Utils.arrToStr(cmdBody), getApplicationContext());
                        convoLayout.removeAllViews(); // should only be done if you were present on the channel... Too little time left to change it
                        break;

                    case "user":

                        if (cmdBody[0].equals("New")) { // a 'bit' flimsy, but ehh, it works

                            unregistered = false;
                            invalidateOptionsMenu(); // redraw the options menu since its items need to be altered

                            toggleButton.setVisibility(View.VISIBLE);
                            createChanButton.setVisibility(View.VISIBLE);
                            deleteChanButton.setVisibility(View.VISIBLE);
                        } // end if-statement
                        Utils.toast(Utils.arrToStr(cmdBody), getApplicationContext());
                        break;

                    default:

                        Utils.toast(Utils.arrToStr(cmdBody), getApplicationContext()); // a failure msg gets printed
                        break;
                } // end switch-statement
            } // end run()
        }); // end runOnUiThread()
    } // end updateUI

    // for determining button behaviours
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.sendButton:

                if (!inputField.getText().toString().isEmpty()) {

                    String data = inputField.getText().toString()+"\n";
                    reader.getWriter().writeDataToServer(data);
                    inputField.getText().clear();
                }
                break;

            case R.id.okButton: // of the promptField

                if (!promptField.getText().toString().isEmpty()) { // not entering a name and clicking 'ok' will close the window without registering

                    if (makingUser) {
                        reader.getWriter().writeDataToServer(":user " + promptField.getText().toString() + "\n");
                    }
                    else { // == making new channel; I'm reusing the same input field for this
                        reader.getWriter().writeDataToServer(":makechan " + promptField.getText().toString() + "\n");
                    }
                    promptField.getText().clear();
                } // end empty-check

                okButton.setVisibility(View.INVISIBLE);
                promptField.setVisibility(View.INVISIBLE);
                promptLayout.setVisibility(View.INVISIBLE);
                break;

            case R.id.createChanButton:

                makingUser = false; // not making a user == making a channel

                drawerLayout.closeDrawer(Gravity.START); // close the drawer so it won't block the promptField

                promptLayout.setVisibility(View.VISIBLE);
                okButton.setVisibility(View.VISIBLE);
                promptField.setVisibility(View.VISIBLE);
                promptField.setHint(R.string.makeChanHint);
                promptField.requestFocus();
                break;

            case R.id.deleteChanButton:

                reader.getWriter().writeDataToServer(":delchan " + channelSpinner.getItemAtPosition(0).toString() + "\n");
                drawerLayout.closeDrawer(Gravity.START); // close the drawer to let it update upon re-open
                break;
        } // end switch-statement
    } // end onClick()

    // creates the upper-right hand corner menu.
    // this gets called again after calling invalidateOptionsMenu(),
    // ensuring that the menu always contains the correct items
    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        MenuItem register = menu.findItem(R.id.action_register);
        MenuItem rename = menu.findItem(R.id.action_rename);
        MenuItem logoff = menu.findItem(R.id.action_logoff);

        if (unregistered) {

            register.setVisible(true);
            rename.setVisible(false);
            logoff.setVisible(false);
        }
        else {
            register.setVisible(false);
            rename.setVisible(true);
            logoff.setVisible(true);
        }
        return true;
    } // end onCreateOptionsMenu()

    // used for the menu items of the right-hand corner menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_register:

                makingUser = true;
                promptLayout.setVisibility(View.VISIBLE);
                promptField.setVisibility(View.VISIBLE);
                okButton.setVisibility(View.VISIBLE);
                promptField.setHint(R.string.RegisterHint);
                promptField.requestFocus();
                return true;

            case R.id.action_rename:

                makingUser = true;
                promptLayout.setVisibility(View.VISIBLE);
                promptField.setVisibility(View.VISIBLE);
                okButton.setVisibility(View.VISIBLE);
                promptField.setHint(R.string.RegisterHint);
                promptField.requestFocus();
                return true;

            case R.id.action_logoff:

                reader.getWriter().writeDataToServer(":logoff\n");
                return true;

            case R.id.action_help:

                helpFrame.bringToFront();
                helpText.bringToFront();
                helpScroll.bringToFront();
                helpScroll.setVisibility(View.VISIBLE);
                helpFrame.setVisibility(View.VISIBLE);
                helpText.setVisibility(View.VISIBLE);
                inputField.setVisibility(View.INVISIBLE); // get these out of the way of the help blurb
                sendButton.setVisibility(View.INVISIBLE);
                return true;

            case R.id.action_quit:

                reader.getWriter().writeDataToServer(":quit\n");
                return true;

            default:

                // if we got here, the user's action was not recognized.
                // invoke the superclass to handle it (n practice, should never happen)
                return super.onOptionsItemSelected(item);
        } // end switch-statement
    } // end onOptionsItemSelected()

    // =============================================================================================
    // HELPER METHODS (that it makes no sense to put into Utils, due to their nature of dealing with Views and Activities)

    // used for posting the initial welcome msg (as it needs to be posted on the screen and not as a Toast)
    private void postAdminMsg(final String msg) {

        Integer bgColor = getResources().getColor(R.color.colorCAdmin, null);
        ChatBubble bubble = new ChatBubble(getApplicationContext(), msg, bgColor, 10.0f);
        convoLayout.addView(bubble);
        bubble.setParams(false, 10, 800, 800);

        scrollDown(convoScroll);
    } // end postAdminMsg()

    private void scrollDown(final ScrollView scroll) {

        // makes the scrollView automatically scroll down with each new msg
        scroll.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                scroll.post(new Runnable() {

                    public void run() {

                        scroll.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    } // end scrollDown()

    // just to have these at the bottom... called in onCreate()
    private void initListenersEtc() {

        // a drawer needs this to work properly (or some custom thingy that I had no hope of implementing).
        // I'm not sure if the two Strings are needed, but I created them just in case
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_closed) {

            // Called when a drawer has settled in a completely closed state
            @Override
            public void onDrawerClosed(View view) {

                super.onDrawerClosed(view);
                actuallyClickedOnSpinner = false;
            }

            // Called when a drawer has settled in a completely open state.
            // NOTE: using this causes a small delay in updating the drawer contents,
            // but I couldn't get the other options to work properly
            @Override
            public void onDrawerOpened(View drawerView) {

                super.onDrawerOpened(drawerView);

                // retrieve the lists of channels and users from the server;
                // updateUI() then populates the drawer with them
                reader.getWriter().writeDataToServer(":channels\n");
                reader.getWriter().writeDataToServer(":users\n");

                if (unregistered) {

                    channelSpinner.setEnabled(false);
                }
                else {
                    channelSpinner.setEnabled(true);
                }
            } // end onDrawerOpened()
        }; // end toggle setup
        drawerLayout.addDrawerListener(toggle);

        // a listener for the button that toggles between a global and a channel-oriented view
        // in the left side drawer
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    reader.getWriter().writeDataToServer(":globalusers\n");
                }
                else {
                    reader.getWriter().writeDataToServer(":users\n");
                    reader.getWriter().writeDataToServer(":channels\n");
                    channelSpinner.setVisibility(View.VISIBLE);
                    deleteChanButton.setVisibility(View.VISIBLE);
                    createChanButton.setVisibility(View.VISIBLE);
                    channelText.setText(R.string.channelText);
                    actuallyClickedOnSpinner = false; // for some reason, the spinner keeps choosing the first item without this...
                }
            }
        }); // end toggleButton listener setup

        // listen for touches on the top container and hide the help text-box upon touch (if it is open)
        helpScroll.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent e) {

                helpScroll.setVisibility(View.INVISIBLE);
                helpFrame.setVisibility(View.INVISIBLE);
                helpText.setVisibility(View.INVISIBLE);
                inputField.setVisibility(View.VISIBLE);
                sendButton.setVisibility(View.VISIBLE);
                return true;
            }
        }); // end helpScroll listener setup

        // the channel spinner needs a listener to actually switch between the channels
        channelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if (actuallyClickedOnSpinner) { // needed to deal with the fact that the first spinner item is automatically chosen when the drawer is opened... this 'eats' the first, superfluous 'click' event
                    String selectedChannel = parent.getItemAtPosition(position).toString();
                    reader.getWriter().writeDataToServer(":tochan " + selectedChannel + "\n"); // switches to the selected channel on the server-side

                    actuallyClickedOnSpinner = false;
                }
                else {
                    actuallyClickedOnSpinner = true;
                }
            } // end onItemSelected()

            public void onNothingSelected(AdapterView<?> parent) { // must be implemented or there's an error
            }
        }); // end channelSpinner listener setup
    } // end initListenersEtc()

    private void assignViews() {

        // ... who designed this logic, again? was it the same guy who designed Java itself?
        userGrid = (GridView)findViewById(R.id.userGrid);
        convoLayout = (LinearLayout)findViewById(R.id.convoLayout);
        helpText = (TextView)findViewById(R.id.helpText);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawerLayout);
        helpFrame = (FrameLayout)findViewById(R.id.helpFrame);
        helpScroll = (ScrollView)findViewById(R.id.helpScroll);
        inputField = (EditText)findViewById(R.id.inputField);
        sendButton = (Button)findViewById(R.id.sendButton);
        createChanButton = (Button)findViewById(R.id.createChanButton);
        deleteChanButton = (Button)findViewById(R.id.deleteChanButton);
        channelSpinner = (Spinner)findViewById(R.id.channelSpinner);
        channelText = (TextView)findViewById(R.id.channelText);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        convoScroll = (ScrollView)findViewById(R.id.convoScroll);
        promptField = (EditText)findViewById(R.id.promptField);
        okButton = (Button)findViewById(R.id.okButton);
        promptLayout = (RelativeLayout)findViewById(R.id.promptLayout);
    } // end assignViews()
} // end class