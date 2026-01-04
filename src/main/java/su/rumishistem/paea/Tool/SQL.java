package su.rumishistem.paea.Tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SQL {
	private static final String SQL_PATH = "/etc/paea/main.db";
	private static final String SQL_URL = "jdbc:sqlite:" + SQL_PATH;

	private static Connection connection;

	public static void init() throws SQLException, ClassNotFoundException, IOException {
		//ファイル初期化
		Path db_path = Path.of(SQL_PATH);
		Path dir_path = db_path.getParent();
		if (dir_path != null && !Files.exists(dir_path)) {
			Files.createDirectories(dir_path);
		}
		if (!Files.exists(db_path)) {
			Files.createFile(db_path);
		}

		//SQL初期化
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection(SQL_URL);
		connection.setAutoCommit(false);
		connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		if (connection == null) throw new RuntimeException("SQL error");

		//外部キー制約を有効化
		Statement stmt = connection.createStatement();
		stmt.execute("PRAGMA foreign_keys = ON;");
		stmt.close();

		//テーブルチェック
		PreparedStatement pstmt = connection.prepareStatement("SELECT 1 FROM `sqlite_master` WHERE `type` = 'table' AND `name` = 'ACCOUNT' LIMIT 1;");
		boolean account_table_exists;
		ResultSet result = pstmt.executeQuery();
		account_table_exists = result.next();
		result.close();
		pstmt.close();

		if (!account_table_exists) {
			Statement at_stmt = connection.createStatement();
			at_stmt.execute("""
				CREATE TABLE `ACCOUNT` (
					ID VARCHAR(256),
					TOKEN TEXT,
					HOST TEXT,
					SOFTWARE_NAME VARCHAR(64),
					SOFTWARE_VERSION VARCHAR(256)
				);
			""");
			at_stmt.close();
		}
	}

	public static Connection get_connection() throws SQLException {
		connection.commit();

		return connection;
	}

	//↓RJLのSQL.javaから持ってきた(要らない部分は削ってる)
	private static void stmt_set_param(PreparedStatement stmt, Object[] param_list) throws SQLException {
		for (int I = 0; I < param_list.length; I++) {
			Object Param = param_list[I];
			//型に寄って動作をかえる
			if(Param instanceof String){
				//Stringなら
				stmt.setString(I + 1, (String)Param);
			} else if(Param instanceof Integer){
				//Intなら
				stmt.setInt(I + 1, (int)Param);
			} else if (Param instanceof Long) {
				stmt.setLong(I + 1, (long)Param);
			} else if (Param instanceof  Boolean) {
				stmt.setBoolean(I + 1, (boolean)Param);
			} else if (Param == null) {
				stmt.setNull(I + 1, Types.NULL);
			} else {
				throw new Error(Param.getClass().getSimpleName() + "という型は非対応です");
			}
		}
	}

	public static List<HashMap<String, Object>> run(String script, Object[] param_list) throws SQLException {
		Connection connection = get_connection();
		PreparedStatement stmt = connection.prepareStatement(script);
		if (param_list != null) {
			stmt_set_param(stmt, param_list);
		}

		ResultSet result = stmt.executeQuery();
		ResultSetMetaData meta = result.getMetaData();
		int colum_count = meta.getColumnCount();

		List<HashMap<String, Object>> return_data = new ArrayList<HashMap<String,Object>>();
		while (result.next()) {
			HashMap<String, Object> row_data = new HashMap<String, Object>();
			for (int i = 1; i <= colum_count; i++) {
				String colum_name = meta.getColumnLabel(i);
				Object value = result.getObject(i);
				row_data.put(colum_name, value);
			}
			return_data.add(row_data);
		}
		return return_data;
	}

	public static void up_run(String script, Object[] param_list) throws SQLException {
		Connection connection = get_connection();
		PreparedStatement stmt = connection.prepareStatement(script);
		if (param_list != null) {
			stmt_set_param(stmt, param_list);
		}

		stmt.executeUpdate();
		connection.commit();
	}
}
