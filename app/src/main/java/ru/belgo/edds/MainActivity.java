package ru.belgo.edds;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ListView listNews;
    ProgressBar loader;
    ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

    static final String news_id = "news_id";
    static final String news_title = "news_title";
    static final String news_desc = "news_desc";
    static final String news_url = "news_url";
    static final String news_date = "news_date";
    static final String news_preview = "news_preview";

    static final String server_ip = "192.168.1.82";
    static final String port = ":3012";

    static final String nodejs_path = "http://"+server_ip+port;
    static final String urlnews = nodejs_path+"/news/";

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    class DownloadNews extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        protected String doInBackground(String... args) {
            String xml = "";
            xml = Function.excuteGet(urlnews);
            return  xml;
        }

        @Override
        protected void onPostExecute(String xml) {
            if(xml.length()>0) // Just checking if not empty
            {
                try
                {
                    JSONArray jsonArray = new JSONArray(xml);
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(news_preview, jsonObject.optString(news_preview).toString());
                        map.put(news_title, jsonObject.optString(news_title).toString());
                        map.put(news_desc, jsonObject.optString(news_desc).toString());
                        map.put(news_url, nodejs_path+"/news/"+jsonObject.optString(news_id)+"/news_txt".toString());
                        map.put(news_date, jsonObject.optString(news_date).toString());
                        dataList.add(map);
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), R.string.json_error, Toast.LENGTH_SHORT).show();
                    return;
                }

                ListNewsAdapter adapter = new ListNewsAdapter(MainActivity.this, dataList);
                listNews.setAdapter(adapter);

                listNews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id)
                    {
                        Intent i = new Intent(MainActivity.this, DetailsActivity.class);
                        i.putExtra("url",dataList.get(position).get(news_url));
                        startActivity(i);
                    }
                });

            }else{
                Toast.makeText(getApplicationContext(), R.string.news_found, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void updateUI(@Nullable GoogleSignInAccount account) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        Menu menu = navigationView.getMenu();
        MenuItem sign_in_button = menu.findItem(R.id.nav_singInButton);
        MenuItem sign_out_button = menu.findItem(R.id.nav_disconnect_button);
        MenuItem incident_addbutton = menu.findItem(R.id.nav_incident_add);

        if (account != null) {
            if (incident_addbutton !=null)
                incident_addbutton.setVisible(true);

            String personName = account.getDisplayName();

            if (personName != null)
            {
                Toast.makeText(getApplicationContext(), R.string.successful_authorization, Toast.LENGTH_SHORT).show();

                TextView temptextView = (TextView) headerView.findViewById(R.id.header_name);
                temptextView.setVisibility(View.VISIBLE);
                temptextView.setText(personName);
            }
            Uri personPhoto = account.getPhotoUrl();
            ImageView tempImageView = findViewById(R.id.header_imageView);
            if ( personPhoto != null && tempImageView != null )
            {
                Picasso.get()
                        .load(personPhoto)
                        .resize(300, 300)
                        .into(tempImageView);
            }
            else
            {
                if (tempImageView !=null)
                    tempImageView.setVisibility(View.GONE);
            }
            String personEmail = account.getEmail();
            TextView temptextView = (TextView) findViewById(R.id.header_email);

            if ( personEmail != null && temptextView !=null )
            {
                tempImageView.setVisibility(View.VISIBLE);
                temptextView.setText(personEmail);
            }
            if ( sign_in_button != null && sign_out_button !=null )
            {
                sign_in_button.setVisible(false);
                sign_out_button.setVisible(true);
            }
            FloatingActionButton floating_Action_Button= findViewById(R.id.floatingactionbutton_actions_camera);
            if (floating_Action_Button !=null)
                floating_Action_Button.setVisibility(View.VISIBLE);

        }
        else {
            //Скрываем интерфейс

            TextView temptextView = (TextView) findViewById(R.id.header_name);
            TextView temptextView2 = (TextView) findViewById(R.id.header_email);
            ImageView tempImageView = findViewById(R.id.header_imageView);

            if (temptextView !=null && temptextView2 !=null && tempImageView != null)
            {
                temptextView.setText(getResources().getString(R.string.unautorized_header_name));
                temptextView2.setText(getResources().getString(R.string.unautorized_header_email));
                //temptextView.setVisibility(View.GONE);
                //temptextView2.setVisibility(View.GONE);
                tempImageView.setVisibility(View.GONE);
            }
            if ( sign_in_button != null && sign_out_button !=null )
            {
                sign_in_button.setVisible(true);
                sign_out_button.setVisible(false);
            }
            if (incident_addbutton !=null)
                incident_addbutton.setVisible(false);

            FloatingActionButton floating_Action_Button= findViewById(R.id.floatingactionbutton_actions_camera);
            if (floating_Action_Button !=null)
                floating_Action_Button.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // [START on_start_sign_in]
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
        // [END on_start_sign_in]
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //floatingactionbutton actions
        findViewById(R.id.floatingactionbutton_actions_phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dial = "tel:" + "112";
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(dial)));
            }
        });

        findViewById(R.id.floatingactionbutton_actions_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + "112"));

                //TODO придумать тут текст
                smsIntent.putExtra("sms_body","Надо подумать что сюда писать");
                startActivity(smsIntent);
            }
        });
        findViewById(R.id.floatingactionbutton_actions_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, AddIncActivity.class);
                startActivity(i);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listNews = (ListView) findViewById(R.id.listNews);
        loader = (ProgressBar) findViewById(R.id.loader_main);
        listNews.setEmptyView(loader);

        if(Function.isNetworkAvailable(getApplicationContext()))
        {
            DownloadNews newsTask = new DownloadNews();
            newsTask.execute();
        }else{
            Toast.makeText(getApplicationContext(), R.string.no_connection, Toast.LENGTH_LONG).show();
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            //Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // [START signIn]
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    // [START signOut]
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        updateUI(null);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        updateUI(null);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END revokeAccess]



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_incident_add) {
            Intent i = new Intent(MainActivity.this, AddIncActivity.class);
            startActivity(i);
        }
        else if (id == R.id.nav_settings) {

        }
        else if (id == R.id.nav_singInButton) {
            signIn();
        }
        else
            //Вообще тут кнопка деавторизации двух видов просто отключение и отключение с удалением данных
            // https://developers.google.com/identity/sign-in/android/disconnect
            // Кнопку с неполным откдюченим решил убрать
            /*if (id == R.id.singOutButton) {
            signOut();
        }
        else*/ if (id == R.id.nav_disconnect_button) {
            revokeAccess();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
