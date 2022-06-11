package database.jdbc.repository;

import database.jdbc.domain.Member;

public interface MemberRepositoryEx {
    Member save(Member member);
    Member findById(String memberId);
    void update(String memberId, int money);
    void delete(String memberId);
}
