package database.jdbc.service;

import database.jdbc.domain.Member;
import database.jdbc.repository.MemberRepositoryV1;
import database.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

    private final MemberRepositoryV2 memberRepository;

    private final DataSource dataSource;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        Connection connection = dataSource.getConnection();

        try {
            connection.setAutoCommit(false); // 트랜잭션 시작

            bizLogic(connection, fromId, toId, money);
            connection.commit(); //성공시 커밋
        } catch (Exception e) {
            connection.rollback();
            throw new IllegalStateException(e);
        } finally {
            release(connection);
        }

    }

    private void bizLogic(Connection connection, String fromId, String toId , int money) throws SQLException{
        Member fromMember = memberRepository.findById(connection, fromId);
        Member toMember = memberRepository.findById(connection, toId);

        memberRepository.update(connection, fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(connection, toId, toMember.getMoney() + money);
    }

    public void validation(Member toMember) {

        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

    private void release(Connection connection) {
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (Exception e) {
                log.info("error", e);
            }
        }
    }
}
