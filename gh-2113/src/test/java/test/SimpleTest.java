package test;

import static org.junit.Assert.*;

import java.io.Reader;
import java.sql.Connection;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("test/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }
    // prepare in-memory database
    try (SqlSession session = sqlSessionFactory.openSession();
        Connection conn = session.getConnection();
        Reader reader = Resources.getResourceAsReader("test/CreateDB.sql")) {
      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.runScript(reader);
    }
  }

  @Test
  public void fromIssueDescription() {
    try {
      SqlSession sqlSession = sqlSessionFactory.openSession(false);
      try {
        Mapper mapper = sqlSession.getMapper(Mapper.class);
        User user = new User();
        user.setId(2);
        user.setName("User2");
        mapper.insertUser(user);
        sqlSession.commit();
      } catch (Throwable e) {
        // This catch clause is not necessary
        // Session will be rolled back if commit is not called.
        sqlSession.rollback();
        // MyBatis throws PersistenceException which is a RuntimeException.
        throw new RuntimeException(e);
      } finally {
        sqlSession.close();
      }
    } catch (RuntimeException e) {
      e.printStackTrace();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertNull(mapper.getUser(2));
    }
  }

  @Test
  public void recommendedCode() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession(false)) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = new User();
      user.setId(2);
      user.setName("User2");
      mapper.insertUser(user);
      sqlSession.commit();
    } catch (PersistenceException e) {
      e.printStackTrace();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertNull(mapper.getUser(2));
    }
  }

}
