import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LibraryManagerOSTest {

    private LibraryManager libraryManager;

    @BeforeEach
    void setUp() {
        // 실제 DB에 영향을 주지 않기 위해 익명 클래스로 Mock 리포지토리 주입
        LibraryRepository mockRepo = new LibraryRepository() {
            @Override
            public Map<Integer, Book> loadBooks() {
                return new HashMap<>(); // 빈 맵 반환
            }
        };

        libraryManager = new LibraryManager(mockRepo);
        libraryManager.initialize();
    }

    @Test
    @DisplayName("OS Command Injection 방어 테스트: 악성 메타문자 주입 시 명령어가 실행되지 않아야 한다.")
    void testCheckServerStatusDefendsInjection() {
        // 콘솔 출력(System.out)을 캡처하기 위한 스트림 설정
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        // 악성 명령어가 병렬 실행되도록 유도하는 입력값
        String maliciousIp = "127.0.0.1 && dir";

        try {
            // 공격 값으로 서버 진단 실행
            libraryManager.checkServerStatus(maliciousIp);

            String consoleOutput = outputStream.toString();

            // 검증: 패치된 코드의 정규식 필터링에 의해 오류 메시지가 출력되었는지 확인
            // 또는 ProcessBuilder가 전체를 하나의 문자열 주소로 처리하여 ping 에러가 났는지 확인
            assertTrue(consoleOutput.contains("[오류]") || consoleOutput.contains("유효하지 않은"),
                    "악성 IP 입력 포맷이 정상적으로 차단되거나 안전하게 처리되어야 합니다.");

        } finally {
            // 원래 콘솔 출력 스트림으로 복구
            System.setOut(originalOut);
        }
    }
}