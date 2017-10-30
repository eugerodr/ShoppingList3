package upc.eseiaat.pma.prova_21;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {

    private ListView list;
    private Button btn_add;
    private EditText edit_item;

    private ArrayList<ShoppingItem> item_list;
    private ArrayAdapter<ShoppingItem> adapter;

    private static final String FILENAME= "shopping_list.txt";
    private static final int MAX_BYTES=8000;

    private void writeItemList(){
        try {
            FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            for (int i=0; i<item_list.size(); i++){
                ShoppingItem it = item_list.get(i);
                String line = String.format("%s;%b\n", it.getText(), it.isChecked());
                fos.write(line.getBytes());
            }
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e("eugerodr", "writeItemList: FileNotFoundException");
            Toast.makeText(this, R.string.cannot_write, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("eugerodr", "writeItemList: IOException");
            Toast.makeText(this, R.string.cannot_write, Toast.LENGTH_SHORT).show();
        }
    }

    private void readItemList() {
        item_list = new ArrayList<>();
        try {
            FileInputStream fis = openFileInput(FILENAME);
            byte[] buffer = new byte[MAX_BYTES];
            int nread = fis.read(buffer);
            String content = new String(buffer, 0, nread);
            String[] lines = content.split("\n");
            for (String line : lines) {
                String[] parts = line.split(";");
                item_list.add(new ShoppingItem(parts[0], parts[1].equals("true")));
            }
            fis.close();

        } catch (FileNotFoundException e) {
            Log.i("eugerodr", "readItemList: FileNotFoundException");
        } catch (IOException e) {
            Log.e("eugerodr", "readItemList: IOException");
            Toast.makeText(this, R.string.cannot_read, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        writeItemList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = (ListView) findViewById(R.id.list);
        btn_add = (Button) findViewById(R.id.btn_add);
        edit_item = (EditText) findViewById(R.id.editBox);

        readItemList();

        adapter = new ShoppingListAdapter(this,R.layout.shopping_item, item_list);

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
            }
        });

        edit_item.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                addItem();
                return true;
            }
        });

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
                maybeRemoveItem(pos);
                return true;
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                item_list.get(pos).toggleChecked();
                adapter.notifyDataSetChanged();
            }
        });

        list.setAdapter(adapter);
    }

    private void maybeRemoveItem(final int pos) {
        String msg = getResources().getString(R.string.message_2);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm);
        builder.setMessage(String.format(msg, item_list.get(pos).getText()));
        builder.setPositiveButton(R.string.erase, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                item_list.remove(pos);
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    private void addItem() {
        String item = edit_item.getText().toString();
        if (!item.isEmpty()) {
            item_list.add(new ShoppingItem(item));
            adapter.notifyDataSetChanged();
            edit_item.setText("");
        }

        list.smoothScrollToPosition(item_list.size()-1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_checked:
                clearChecked();
                return true;
            case R.id.clear_all:
                clearAll();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearChecked() {
        int i=0;
        while (i < item_list.size()) {
            if (item_list.get(i).isChecked()) {
                item_list.remove(i);
            }
            else {
                i++;
            }
        }
        adapter.notifyDataSetChanged();
    }


    private void clearAll() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.confirm);
        builder.setMessage(R.string.message_clear);
        builder.setPositiveButton(R.string.erase, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                item_list.clear();
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(R.string.cancel,null);
        builder.create().show();

        }

    }


