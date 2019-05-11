package ru.belgo.edds;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.net.Uri;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Intent i = new Intent(MainActivity.this, AddIncActivity.class);
            startActivity(i);
        }
         else if (id == R.id.nav_manage) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
