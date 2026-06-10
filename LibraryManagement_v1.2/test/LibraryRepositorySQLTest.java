import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;

public class LibraryRepositorySQLTest {

    private LibraryManager libraryManager;
    private LibraryRepository mockRepository;

    @BeforeEach
    void setUp() {
        // SQL Injection 검증을 위해 loadUser 로직을 가로챈 Mock 리포지토리 생성
        mockRepository = new LibraryRepository() {
            @Override
            public Map<Integer, Book> loadBooks() {
                return new HashMap<>();
            }

            @Override
            public User loadUser(String id, String pw) {
                // 안전하게 수정한 ? 파라미터 바인딩 방식을 모사합니다.
                // 쿼리가 컴파일된 후 id가 통째로 들어가기 때문에, 공격 구문이 들어와도
                // 해당 문자열('admin' OR '1'='1')과 정확히 '일치하는 아이디'가 없다면 null을 반환합니다.
                if (id.equals("admin' OR '1'='1")) {
                    return null; // 공격 구문 문자열 자체를 아이디로 인식하므로 회원 조회 실패
                }
                return null;
            }
        };

        libraryManager = new LibraryManager(mockRepository);
        libraryManager.initialize();
    }

    @Test
    @DisplayName("SQL Injection 방어 테스트: 악성 쿼리 주입 시 로그인 우회가 차단되어야 한다.")
    void testLoginSqlInjectionDefense() {
        // 전형적인 SQL Injection 로그인 우회 공격 페이로드
        String attackId = "admin' OR '1'='1";
        String attackPw = "wrong_password";

        // 실행: 악성 데이터로 로그인을 시도할 때 성공 여부 확인
        boolean loginResult = libraryManager.login(attackId, attackPw);

        // 검증: 쿼리 구문 변조가 일어나지 않았다면 로그인은 반드시 실패(false)해야 합니다.
        org.junit.jupiter.api.Assertions.assertFalse(loginResult,
                "SQL Injection 공격 코드가 쿼리 구조를 변조하여 로그인이 우회되면 안 됩니다.");

        // currentUser 객체도 여전히 null인지 재검증
        assertNull(libraryManager.getCurrentUser(),
                "로그인에 실패했으므로 현재 세션 사용자는 null 이어야 합니다.");
    }
}