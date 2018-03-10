package sample.databasesampleandroid;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class CocktailMemoActivity extends AppCompatActivity {

    // 選択されたカクテルの主キーIDを表すフィールド
    int _cocktailsId = -1;

    // 選択されたカクテル名を表すフィールド
    String _cocktailName = "";

    // カクテル名を表示するフィールド
    TextView _tvCocktailName;

    // 保存ボタンフィールド
    Button _btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cocktail_memo);

        // カクテル名を表示するTextViewを取得
        _tvCocktailName = findViewById(R.id.tvCocktailName);
        // 保存ほぼ端を取得
        _btnSave = findViewById(R.id.btnSave);
        // カクテルリスト用ListView(lvCocktail)を取得
        ListView lvCocktail = findViewById(R.id.lvCocktail);
        // lvCocktailにリスナを登録
        lvCocktail.setOnItemClickListener(new ListItemClickListenter());
    }

    // 保存ボタン顔されたときの処理メソッド
    public void onSaveButtonClick(View view) {
        // 感想欄を取得
        EditText etNote = findViewById(R.id.etNote);
        String note = etNote.getText().toString();

        // データベースヘルパーオブジェクトを取得
        DatabaseHelper helper = new DatabaseHelper(CocktailMemoActivity.this);
        // データベースヘルパーオブジェクトからデータベース接続オブジェクトを取得
        SQLiteDatabase db = helper.getWritableDatabase();

        try {
            // まずりすとで選択されたカクテルのメモデータを削除、その後インサートを行う
            // 削除用SQL文字列を用意
            String sqlDelete = "DELETE FROM cocktailmemo WHERE _id = ?";
            // SQL文字列を元にプリアドステートメントを取得
            SQLiteStatement stmt = db.compileStatement(sqlDelete);
            // 変数のバインド
            stmt.bindLong(1, _cocktailsId);
            // 削除SQLの実行
            stmt.executeUpdateDelete();

            // インサート用SQL文字列の用意
            String sqlInsert = "INSERT INTO cocktailmemo (_id, name, note) VALUES (?, ?, ?)";
            // SQL文字列を元にプリアドステートメントを取得
            stmt = db.compileStatement(sqlInsert);
            // 変数のバインド
            stmt.bindLong(1, _cocktailsId);
            stmt.bindString(2, _cocktailName);
            stmt.bindString(3, note);
            // インサートSQLの実行
            stmt.executeInsert();

        }
        finally {
            // データベース接続オブジェクトの解放
            db.close();
        }

        // カクテル名を未選択に変更
        _tvCocktailName.setText(getString(R.string.tv_name));
        // 感想欄の入力値を消去
        etNote.setText("");
        // 保存ボタンをタップできないように変更
        _btnSave.setEnabled(false);

    }

    // リストがタップされたときの処理が記述されたメンバクラス
    private class ListItemClickListenter implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // タップされた行番号フィールドの主キーIDを代入
            _cocktailsId = position;
            // タップされた行のデータを取得、これがカクテル名となるのでフィールドに代入
            _cocktailName = (String) parent.getItemAtPosition(position);
            // カクテル名を表示するTextViewに表示カクテル名を設定
            _tvCocktailName.setText(_cocktailName);
            // 保存ボタンをタップできるように設定
            _btnSave.setEnabled(true);

            // データベースヘルパーオブジェクトを作成
            DatabaseHelper helper = new DatabaseHelper(CocktailMemoActivity.this);
            // データベースヘルパーオブジェクトからデータベース接続オブジェクトを取得
            SQLiteDatabase db = helper.getWritableDatabase();
            try {
                // 主キーによる検索SQL文字列の用意
                String sql = "SELECT * FROM cocktailmemo WHERE _id = " + _cocktailsId;
                // SQLの実行
                Cursor cursor = db.rawQuery(sql, null);
                // データベースから取得した値を格納する変数の用意。データがなかったときのための初期値も用意
                String note = "";
                // SQL実行の戻り値であるカーソルオブジェクトをループさせてデータベース内のデータを取得
                while(cursor.moveToNext()) {
                    // カラムのインデックス値を取得
                    int idxNote = cursor.getColumnIndex("note");
                    // カラムのインデックス値を元に実際のデータを取得する
                    note = cursor.getString(idxNote);
                }
                // 感想のEditTextの書く画面部品を取得しデータベースの値を反映
                EditText etNote = findViewById(R.id.etNote);
                etNote.setText(note);
            }
            finally {
                // データベース接続オブジェクトの解放
                db.close();
            }
        }

    }
}
