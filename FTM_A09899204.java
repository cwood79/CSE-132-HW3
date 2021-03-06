// Christopher Wood A09899204
import java.sql.*;

public class FTM_A09899204 {

	// This code performs a semi-naive transitive closure on a graph representing transactions between bank accounts which are owned by different customers

	public static void main(String args[]) throws SQLException, ClassNotFoundException 
	{
		// open connection to database
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:" + args[0]);

		Statement stmt = conn.createStatement();
		ResultSet rs;

		stmt.executeUpdate("DROP TABLE IF EXISTS influence");

		stmt.executeUpdate("CREATE TABLE influence ( [from] VARCHAR(100) NOT NULL, [to] VARCHAR(100) NOT NULL, FOREIGN KEY ([from]) REFERENCES customer(name) ON DELETE CASCADE, FOREIGN KEY([to]) REFERENCES customer(name) ON DELETE CASCADE )");

		stmt.executeUpdate("CREATE TABLE delta ( [from] VARCHAR(100) NOT NULL, [to] VARCHAR(100) NOT NULL, FOREIGN KEY ([from]) REFERENCES customer(name) ON DELETE CASCADE, FOREIGN KEY([to]) REFERENCES customer(name) ON DELETE CASCADE )");

		stmt.executeUpdate("CREATE TABLE old ( [from] VARCHAR(100) NOT NULL, [to] VARCHAR(100) NOT NULL, FOREIGN KEY ([from]) REFERENCES customer(name) ON DELETE CASCADE, FOREIGN KEY([to]) REFERENCES customer(name) ON DELETE CASCADE )");

		// create G table
		stmt.executeUpdate("CREATE TABLE funds ( [from] VARCHAR(100) NOT NULL, [to] VARCHAR(100) NOT NULL, FOREIGN KEY ([from]) REFERENCES customer(name) ON DELETE CASCADE, FOREIGN KEY([to]) REFERENCES customer(name) ON DELETE CASCADE )");
		String fund = "INSERT INTO funds([from], [to]) " +
						"SELECT c1.name AS [from], c2.name AS [to] " +
						"FROM transfer t, customer c1, customer c2, " + 
						"account a1, account a2, depositor d1, depositor d2 " +
						"WHERE c1.name = d1.cname AND a1.no = d1.ano AND " +
						"c2.name = d2.cname AND a2.no = d2.ano AND t.src = a1.no AND t.tgt = a2.no";

		stmt.executeUpdate(fund);

		// T = G
		stmt.executeUpdate("INSERT INTO influence SELECT * FROM funds");

		// Delta = G
		stmt.executeUpdate("INSERT INTO delta SELECT * FROM funds");

		rs = stmt.executeQuery("SELECT COUNT(*) FROM delta");

		int i = 0;
		// main loop 
		while(rs.getInt(1) != 0)
		//while(i < 2)
		{
		//	i++;
			System.out.println("Updating!");
			System.out.println("Delta size is " + rs.getInt(1));

			stmt.executeUpdate("DELETE FROM old");
			// save old T
			stmt.executeUpdate("INSERT INTO old SELECT DISTINCT * FROM influence");

			stmt.executeUpdate("DELETE FROM influence");

			// insert later part of union into table, no duplicates
			// do update here
			String update = "INSERT INTO influence " + 
							"SELECT DISTINCT * FROM old " +
							"UNION " +
							"SELECT DISTINCT x.[from], y.[to] " +
							"FROM funds x, delta y " +
							"WHERE x.[to] = y.[from]";

			stmt.executeUpdate(update);

			// delta = t - old
			stmt.executeUpdate("DROP TABLE delta");

			stmt.executeUpdate("CREATE TABLE delta ( [from] VARCHAR(100) NOT NULL, [to] VARCHAR(100) NOT NULL, FOREIGN KEY ([from]) REFERENCES customer(name) ON DELETE CASCADE, FOREIGN KEY([to]) REFERENCES customer(name) ON DELETE CASCADE )");

			stmt.executeUpdate("INSERT INTO delta " +
								"SELECT DISTINCT * FROM influence " +
								 "EXCEPT " +
								 "SELECT DISTINCT * FROM old");

			rs = stmt.executeQuery("SELECT COUNT(*) FROM delta");

		}

		// filter tuples
		stmt.executeUpdate("DELETE FROM influence WHERE [from] = [to]");

		// clean up
		stmt.executeUpdate("DROP TABLE delta");
		stmt.executeUpdate("DROP TABLE funds");
		stmt.executeUpdate("DROP TABLE old");

		rs.close();
		stmt.close();
		conn.close();

		System.out.println("Done!");

	}


}