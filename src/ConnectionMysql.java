import java.sql.*;
public class ConnectionMysql {
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    static final String DB_URL = "jdbc:mysql://localhost:3306/mydata";
    static final String USER = "root";
    static final String PASS = "12345678";
    
    public static Connection connection() {
    	Connection conn = null;
    	try {
			// 注册 JDBC 驱动
			Class.forName("com.mysql.jdbc.Driver");
   
			// 打开链接
			conn = DriverManager.getConnection(DB_URL,USER,PASS);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
    }
    
    public static void insertData(String[] dataStr) {
    	Connection conn = connection();
    	PreparedStatement stmt = null;
        try{
            String sql;
            sql = "insert into air_data (wifiid,shebeibiaozhi,shebeileixing,temperature,humidity) values(?,?,?,?,?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, dataStr[0]);
            stmt.setString(2, dataStr[1]);
            stmt.setString(3, dataStr[2]);
            stmt.setString(4, dataStr[3]);
            stmt.setString(5, dataStr[4]);
            //执行插入语句
            stmt.execute();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
    }
    
    public static void insertDEVData(String data) {
    	Connection conn = connection();
        Statement stmt = null;
        try{
            stmt = conn.createStatement();
            String sql;
            
            sql = "insert into devdata (data) values('"+data+"')";
            //执行插入语句
            stmt.executeUpdate(sql);
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
    }
    
    public static void selectData() {
    	Connection conn = connection();
        Statement stmt = null;
        try{
            stmt = conn.createStatement();
            String sql;
            
            sql = "select * from air_data";
            //执行查询语句
            ResultSet rs = stmt.executeQuery(sql);
            // 展开结果集数据库
            System.out.println("*********data的所有数据**********");
            while(rs.next()){
                // 通过字段检索
                int id  = rs.getInt("id");
                String wifiid = rs.getString("wifiid");
                String shebeibiaozhi = rs.getString("shebeibiaozhi");
                String shebeileixing = rs.getString("shebeileixing");
                String temperature = rs.getString("temperature");
                String humidity = rs.getString("humidity");
                // 输出数据
                System.out.print("ID: " + id);
                System.out.print("  无线id: " + wifiid);
                System.out.print("  设备标志: " + shebeibiaozhi);
                System.out.print("  设备类型: " + shebeileixing);
                System.out.print("  温度: " + temperature);
                System.out.print("  湿度: " + humidity);
                System.out.print("\n");
            }
            System.out.println("*************************");
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
    }
    
    public static void selectDEVData() {
    	Connection conn = connection();
        Statement stmt = null;
        try{
            stmt = conn.createStatement();
            String sql;
            
            sql = "select * from devdata";
            //执行查询语句
            ResultSet rs = stmt.executeQuery(sql);
            // 展开结果集数据库
            System.out.println("*********devdata的所有数据**********");
            while(rs.next()){
                // 通过字段检索
                int id  = rs.getInt("id");
                String data = rs.getString("data");
                // 输出数据
                System.out.print("ID: " + id);
                System.out.print("  data: " + data);
                System.out.print("\n");
            }
            System.out.println("*************************");
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
    }

}
