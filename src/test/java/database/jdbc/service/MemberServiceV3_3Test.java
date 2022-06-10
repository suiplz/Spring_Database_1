package database.jdbc.service;

import database.jdbc.domain.Member;
import database.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.SQLException;

import static database.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * 트랜잭션 - @Transactional AOP
 */
@Slf4j
@SpringBootTest
class MemberServiceV3_3Test {

    public static final String Member_A = "mebmerA";
    public static final String Member_B = "mebmerB";
    public static final String Member_EX = "ex";

    @Autowired
    private MemberRepositoryV3 memberRepository;

    @Autowired
    private MemberServiceV3_3 memberService;

    @Test
    void AopCheck() {
        log.info("memberService class={}", memberService.getClass());
        log.info("memberRepository class={}", memberRepository.getClass());
        assertThat(AopUtils.isAopProxy(memberService)).isTrue();
        assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
    }


    @TestConfiguration
    static class TestConfig {

        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        }

        @Bean
        PlatformTransactionManager transactionManager() {
            return new DataSourceTransactionManager(dataSource());
        }

        @Bean
        MemberRepositoryV3 memberRepositoryV3() {
            return new MemberRepositoryV3(dataSource());
        }

        @Bean
        MemberServiceV3_3 memberServiceV3_3() {
            return new MemberServiceV3_3(memberRepositoryV3());
        }
    }

    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(Member_A);
        memberRepository.delete(Member_B);
        memberRepository.delete(Member_EX);
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {


        Member memberA = new Member(Member_A, 10000);
        Member memberB = new Member(Member_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        log.info("START TX");
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
        log.info("END TX");

        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);

    }

    @Test
    @DisplayName("이체중 예외 발생")
    void accountTransferEx() throws SQLException {


        Member memberA = new Member(Member_A, 10000);
        Member memberEx = new Member(Member_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEx);


        assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberEx.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberB.getMoney()).isEqualTo(10000);

    }
}