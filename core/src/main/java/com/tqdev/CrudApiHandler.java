java
package com.tqdev;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Properties;
// Add import statement
import java.util.*;

public class CrudApiHandler extends AbstractHandler 
{
  protected HikariDataSource dataSource;
  
  public static void main(String[] args) throws Exception
  {
    // jetty config
    Properties properties = new Properties();
    properties.load(CrudApiHandler.class.getClassLoader().getResourceAsStream("jetty.properties"));
    HttpConfiguration config = new HttpConfiguration();
    config.setSendServerVersion( false );
    HttpConnectionFactory factory = new HttpConnectionFactory( config );
    Server server = new Server();
    ServerConnector connector = new ServerConnector(server,factory);
    server.setConnectors( new Connector[] { connector } );
    connector.setHost(properties.getProperty("host"));
    connector.setPort(Integer.parseInt(properties.getProperty("port")));
    server.addConnector(connector);
    server.setHandler(new CrudApiHandler());
    server.start();
    server.join();
  }

  public CrudApiHandler() throws IOException
  {
    this.dataSource = this.getDataSource();
  }

  protected HikariDataSource getDataSource()
  {
    HikariDataSource dataSource;
    try {
      Properties properties = new Properties();
      properties.load(CrudApiHandler.class.getClassLoader().getResourceAsStream("hikari.properties"));
      dataSource = new HikariDataSource(new HikariConfig(properties));
    } catch (Exception e) {
      System.out.println(e);
      dataSource = null;
    }
    return dataSource;
  }

  protected Connection getConnection()
  {
    Connection link;
    try {
      link = this.dataSource.getConnection();
    } catch (SQLException e) {
      System.out.println(e);
      link = null;
    }
    return link;
  }

  public void handle(String target,Request baseReq,HttpServletRequest req,HttpServletResponse resp) 
        throws IOException, ServletException
  {
    Gson gson = new Gson();
    // get the HTTP method, path and body of the request
    String method = req.getMethod();
    String[] request = req.getPathInfo().replaceAll("/$|^/","").split("/");
    @SuppressWarnings("unchecked")
    Map<String, Object> input = gson.fromJson(req.getReader(), Map.class);
    // connect to the mysql database
    Connection link = this.getConnection();
    // retrieve the table and key from the path
    String table = request[0].replaceAll("[^a-zA-Z0-9_]+","");
    int key = (request.length>1?Integer.parseInt(request[1]):-1);
    // escape the columns and values from the input object
    String[] columns = input==null?(new String[0]):(String[])input.keySet().toArray();
    for (int i=0;i<columns.length;i++) {
      columns[i] = columns[i].replaceAll("[^a-zA-Z0-9_]+","");
    }
    // build the SET part of the SQL command
    String set = "";
    for (int i=0;i<columns.length;i++) {
      set+=(i>0?",":"")+"`"+columns[i]+"`=?";
    }
    // create SQL based on HTTP method
    String sql="";
    if (method.equals("GET")) {
        sql = "select * from `"+table+"`"+(key>0?" WHERE id="+key:"");
    } else if (method.equals("PUT")) {
        sql = "update `"+table+"` set "+set+" where id="+key;
    } else if (method.equals("POST")) {
        sql = "insert into `"+table+"` set "+set;
    } else if (method.equals("DELETE")) {
        sql = "delete `"+table+"` where id="+key;
    }
    PreparedStatement statement=null;
    try {
      // execute SQL statement
      statement = link.prepareStatement(sql);
      for (int i=0;i<columns.length;i++) {
        statement.setObject(i, input.get(columns[i]));
      }
      // print results, insert id or affected row count
      resp.setContentType("application/json; charset=utf-8");
      resp.setStatus(HttpServletResponse.SC_OK);
      PrintWriter w = resp.getWriter();
      if (method.equals("GET")) {
        ResultSet result = statement.executeQuery();
        ResultSetMetaData meta = result.getMetaData();
        int colCount = meta.getColumnCount();
        if (key<0) w.print('[');
        int row=0;
        while (result.next()) {
          if (row>0) w.print(',');
          w.print('{');
          for (int col=1;col<=colCount;col++) {
            if (col>1) w.print(',');
            w.print(gson.toJson(meta.getColumnName(col)));
            w.print(':');
            w.print(gson.toJson(result.getObject(col)));
          }
          w.print('}');
          row++;
        }
        if (key<0) w.print(']');
      } else if (method.equals("POST")) {
        statement.executeUpdate(sql);
        ResultSet result = statement.getGeneratedKeys();
        result.next();
        w.print(gson.toJson(result.getObject(1)));
      } else {
        w.print(gson.toJson(statement.executeUpdate(sql)));
      }
    }
    catch (SQLException e) {
      System.out.println("SQLException:"+e);
    }
    finally {
      // close mysql connection
      if (statement != null) try { statement.close(); } catch (SQLException ignore) {}
      if (link != null) try { link.close(); } catch (SQLException ignore) {}
    }
    baseReq.setHandled(true);
  }
}