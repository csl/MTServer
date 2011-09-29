package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.MT.GPSStruct;
import com.MT.MTServer;

public class jdbcmysql {
  private Connection con = null; //Database objects
  //連接object
  private Statement stat = null;
  //執行,傳入之sql為完整字串
  private ResultSet rs = null;
  //結果集
  private PreparedStatement pst = null;
  //執行,傳入之sql為預儲之字申,需要傳入變數之位置
  //先利用?來做標示
  
  private MTServer mts;
 
  private String dropdbSQL = "DROP TABLE GPSREC ";
 
  private String createdbSQL = "CREATE TABLE GPSREC IF NOT EXISTS (" +
    "    id  INTEGER primary key" +
    "  , name VARCHAR(20) " +
    "  , gpsdata VARCHAR(200) " +
    "  , stime    VARCHAR(20) " +
    "  , dtime    VARCHAR(20) " +
    "  , who  	  VARCHAR(20))";
 
  private String insertdbSQL = "insert into GPSREC(id,name,gpsdata,stime,dtime,who) " +
  "select ifNULL(max(id),0)+1,?,?,?,?,? FROM GPSREC";

  private String updatedbSQL = "update GPSREC set name='?',gpsdata='?',stime='?',dtime='?' WHERE id = '?'";

  private String deletedbSQL = "delete FROM GPSREC WHERE id = '?'";
  
  private String selectSQL = "select * from GPSREC ";
 
  public jdbcmysql(MTServer mt)
  {
	mts = mt;
	  
    try {
      Class.forName("com.mysql.jdbc.Driver");
      //註冊driver
      con = DriverManager.getConnection(
      "jdbc:mysql://localhost/db?useUnicode=true&characterEncoding=Big5",
      "root","123456");
      //取得connection

//jdbc:mysql://localhost/test?useUnicode=true&characterEncoding=Big5
//localhost是主機名,test是database名
//useUnicode=true&characterEncoding=Big5使用的編碼
     
    }
    catch(ClassNotFoundException e)
    {
      System.out.println("DriverClassNotFound :"+e.toString());
    }//有可能會產生sqlexception
    catch(SQLException x) {
      System.out.println("Exception :"+x.toString());
    }
   
  }
  //建立table的方式
  //可以看看Statement的使用方式
  public void createTable()
  {
    try
    {
      stat = con.createStatement();
      stat.executeUpdate(createdbSQL);
    }
    catch(SQLException e)
    {
      System.out.println("CreateDB Exception :" + e.toString());
    }
    finally
    {
      Close();
    }
  }
  //新增資料
  //可以看看PrepareStatement的使用方式
  public void insertTable( String name, String gps, String stime, String dtime)
  {
    try
    {
      pst = con.prepareStatement(insertdbSQL);
     
      pst.setString(1, name);
      pst.setString(2, gps);
      pst.setString(3, stime);
      pst.setString(4, dtime);
      pst.setString(5, "user");
      pst.executeUpdate();
    }
    catch(SQLException e)
    {
      System.out.println("InsertDB Exception :" + e.toString());
    }
    finally
    {
      Close();
    }
  }
  
  public void updateTable(String id, String name, String gps, String stime, String dtime)
  {
    try
    {
      pst = con.prepareStatement(updatedbSQL);
     
      pst.setString(1, name);
      pst.setString(2, gps);
      pst.setString(3, stime);
      pst.setString(4, dtime);
      pst.setString(5, "user");
      pst.setString(6, id);
      pst.executeUpdate();
    }
    catch(SQLException e)
    {
      System.out.println("InsertDB Exception :" + e.toString());
    }
    finally
    {
      Close();
    }
  }

  public void deleteidTable(String id)
  {
    try
    {
      pst = con.prepareStatement(deletedbSQL);
     
      pst.setString(1, id);
      pst.executeUpdate();
    }
    catch(SQLException e)
    {
      System.out.println("InsertDB Exception :" + e.toString());
    }
    finally
    {
      Close();
    }
  }

  
  //刪除Table,
  //跟建立table很像
  public void dropTable()
  {
    try
    {
      stat = con.createStatement();
      stat.executeUpdate(dropdbSQL);
    }
    catch(SQLException e)
    {
      System.out.println("DropDB Exception :" + e.toString());
    }
    finally
    {
      Close();
    }
  }
  
  //查詢資料
  //回傳GPS
  public String SelectTableTime(String stime, String dtime)
  {
	  String start, end;
	  
    try
    {
      stat = con.createStatement();
      rs = stat.executeQuery(selectSQL);
      //System.out.println("ID\t\tName\t\tPASSWORD");
      while(rs.next())
      {
    	start = rs.getString("stime");
    	end = rs.getString("dtime");
    	
    	if (Integer.valueOf(stime) <= Integer.valueOf(start) 
    			&& Integer.valueOf(dtime) <= Integer.valueOf(end))
    	{
    	    Close();
    		return  rs.getString("gps");
    	}
    	
        //System.out.println(rs.getInt("id")+"\t\t"+
       //rs.getString("name")+"\t\t"+rs.getString("passwd"));
      }
    }
    catch(SQLException e)
    {
      System.out.println("DropDB Exception :" + e.toString());
    }
    finally
    {
      Close();
    }
    
    return "nogps";
  }
  
  //查詢資料
  //回傳GPS
  public void SelectTable()
  {
	mts.gpslist.clear();
	
    try
    {
      stat = con.createStatement();
      rs = stat.executeQuery(selectSQL);
      //System.out.println("ID\t\tName\t\tPASSWORD");
      while(rs.next())
      {
        GPSStruct data = new GPSStruct();
    	  
        data.id = rs.getString("id");
        data.name = rs.getString("name");
        data.gps = rs.getString("gps");
        data.stime = rs.getString("stime");
        data.dtime = rs.getString("dtime");
        data.who = rs.getString("who");
        
        mts.gpslist.add(data);
        //System.out.println(rs.getInt("id")+"\t\t"+
       //rs.getString("name")+"\t\t"+rs.getString("passwd"));
      }
    }
    catch(SQLException e)
    {
      System.out.println("DropDB Exception :" + e.toString());
    }
    finally
    {
      Close();
    }
  }
  
  
  private void Close()
  {
    try
    {
      if(rs!=null)
      {
        rs.close();
        rs = null;
      }
      if(stat!=null)
      {
        stat.close();
        stat = null;
      }
      if(pst!=null)
      {
        pst.close();
        pst = null;
      }
    }
    catch(SQLException e)
    {
      System.out.println("Close Exception :" + e.toString());
    }
  }
}