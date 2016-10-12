package fr.protogen.masterdata.dal;



public class DBUtils {
	
	//public static String url="jdbc:postgresql://ns389914.ovh.net:5432/vitonjobdb";
	//public static String url="jdbc:postgresql://ns389914.ovh.net:9990/vitonjobdb";
	//public static String url="jdbc:postgresql://vps259989.ovh.net:5432/vitonjobdb";
	//public static String url="jdbc:postgresql://vitonjob.cjyvilyeiwcj.us-west-2.rds.amazonaws.com:5432/vitonjobdb";
	//public static String url="jdbc:postgresql://vitonjobdb.cjyvilyeiwcj.us-west-2.rds.amazonaws.com:5432/vitonjobdb";
	/*public static String url="jdbc:postgresql://vojproduction.cjyvilyeiwcj.us-west-2.rds.amazonaws.com:5432/vitonjobdb";
	public static final String driver="org.postgresql.Driver";
	public static final String username="jakj";
	public static final String password="ENUmaELI5H";*/
	public static final String server = System.getProperty("RDS_HOSTNAME");
	public static final String db = System.getProperty("RDS_DB_NAME");
	public static final String username=System.getProperty("RDS_USERNAME");
	public static final String password=System.getProperty("RDS_PASSWORD");
	public static final String url="jdbc:postgresql://"+server+":5432/"+db;
	
}
