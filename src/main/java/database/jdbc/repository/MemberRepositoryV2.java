package database.jdbc.repository;

import database.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;


/**
 * JDBC - ConnectionParam
 */
@Slf4j
public class MemberRepositoryV2 {

    private final DataSource dataSource;

    public MemberRepositoryV2(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, member.getMemberId());
            preparedStatement.setInt(2, member.getMoney());
            preparedStatement.executeUpdate();
            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(connection, preparedStatement, null);
        }

    }

    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, memberId);
            rs = preparedStatement.executeQuery();

            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId=" + memberId);

            }
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(connection, preparedStatement, rs);
        }
    }
    public Member findById(Connection connection, String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        PreparedStatement preparedStatement = null;
        ResultSet rs = null;

        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, memberId);
            rs = preparedStatement.executeQuery();

            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId=" + memberId);

            }
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            JdbcUtils.closeResultSet(rs);
//            JdbcUtils.closeConnection(connection);
            JdbcUtils.closeStatement(preparedStatement);
        }
    }

    private Connection getConnection() throws SQLException{
        Connection connection = dataSource.getConnection();
        log.info("get connection={}, class={}", connection, connection.getClass());
        return connection;
    }

    public void update(String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, money);
            preparedStatement.setString(2, memberId);
            int resultSize = preparedStatement.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(connection, preparedStatement, null);
        }
    }

    public void update(Connection connection, String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";

        PreparedStatement preparedStatement = null;
        ResultSet rs = null;

        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, money);
            preparedStatement.setString(2, memberId);
            int resultSize = preparedStatement.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            JdbcUtils.closeResultSet(rs);
//            JdbcUtils.closeConnection(connection);
            JdbcUtils.closeStatement(preparedStatement);
        }
    }

    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id = ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, memberId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(connection, preparedStatement, null);
        }
    }

    private void close(Connection connection, Statement statement, ResultSet rs) {

        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeConnection(connection);
        JdbcUtils.closeStatement(statement);
    }
}