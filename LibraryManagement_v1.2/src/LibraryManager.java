import java.util.*;
import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class LibraryManager {
    private Map<Integer, Book> bookMap;
    private List<User> userList;
    private User currentUser;
    private LibraryRepository repository;
    private int bookCount = 0;

    /**
     * LibraryManager 생성자입니다.
     * @param repository 데이터를 저장하고 불러올 리포지토리 객체
     */
    public LibraryManager(LibraryRepository repository) {
        this.repository = repository;
    }

    /**
     * 시스템을 초기화합니다.
     * <p>리포지토리로부터 도서 데이터를 로드하고, 도서 ID 카운트를 현재 최대값으로 동기화합니다.</p>
     * * @see LibraryRepository#loadBooks()
     * @see <a href="https://github.com/sumannam/Java/issues/23">Issue #23: 프로그램 실행 시 데이터 로드</a>
     */
    public void initialize() {
        this.bookMap = repository.loadBooks();
        // ID 카운트 동기화
        for (Integer id : bookMap.keySet()) {
            if (id > bookCount) bookCount = id;
        }
    }

    /**
     * 사용자 로그인을 수행하고 인증 상태를 기록합니다.
     * <p>성공 시 {@code currentUser}에 사용자 정보를 저장합니다.</p>
     *
     * @param id 사용자 아이디
     * @param pw 사용자 비밀번호
     * @return 로그인 성공 여부 (성공 시 true)
     * @see LibraryRepository#loadUser(String, String)
     */
    public boolean login(String id, String pw) {
        // 기존에 List<String>으로 받던 부분을 User로 변경
//        this.userList = repository.loadLogin(id, pw);
        User user = repository.loadUser(id, pw);

        if (user != null) {
            this.currentUser = user; // 로그인 성공 시 현재 사용자 저장
            return true;
        }
        return false;
    }

    /** @return 현재 로그인 중인 {@link User} 객체 */
    public User getCurrentUser() {
        return currentUser;
    }

//    public void setCurretUser(String user) {
//        this.currentUser = user;
//    }

    public void addBook(String title, String author) {
        bookCount++;
        bookMap.put(bookCount, new Book(bookCount, title, author, true, "null"));
        System.out.println("-----------------------------------------------------------");
        System.out.printf("[결과] 등록이 완료되었습니다. (도서 ID: %d)\n", bookCount);
    }

    /**
     * 도서 정보를 수정합니다.
     * @param id     수정할 도서 ID
     * @param title  새 제목
     * @param author 새 저자
     * @return 수정 성공 여부
     */
    public boolean editBook(int id, String title, String author) {
        if (!bookMap.containsKey(id)) return false;
        Book book = bookMap.get(id);
        // 제목이나 저자가 비어있지 않을 때만 수정 (기존 로직 유지)
        return true;
    }

    /**
     * 도서를 시스템에서 삭제합니다.
     * @param id 삭제할 도서 ID
     * @return 삭제 성공 여부
     * * @see <a href="https://github.com/kkh-03/LibraryManagement/issues/1">Issue #1: 책 삭제 시 DB에서 해당 책 삭제 안됨</a>
     */
    public boolean deleteBook(int id) {
        repository.deleteBook(id);
        return bookMap.remove(id) != null;
    }

    /**
     * 도서 대출 처리를 수행합니다.
     * <p>도서가 대출 가능한 상태일 경우, 상태를 변경하고 현재 로그인 사용자를 대출자로 등록합니다.</p>
     *
     * @param id 대출할 도서 ID
     * @return 대출 성공 여부
     */
    public boolean borrowBook(int id) {
        if (!bookMap.containsKey(id))
            return false;

        Book book = bookMap.get(id);
        if (book.isAvailable()) {
            book.setAvailable(false);
            book.setBorrowerId(currentUser.getUserId());
            return true;
        }
        return false;
    }

    /**
     * 도서 반납 처리를 수행합니다.
     * <p>도서가 대출 중인 상태일 경우, 상태를 대출 가능으로 변경하고 대출자 정보를 초기화합니다.</p>
     *
     * @param id 반납할 도서 ID
     * @return 반납 성공 여부
     */
    public boolean returnBook(int id) {
        if (!bookMap.containsKey(id))
            return false;

        Book book = bookMap.get(id);
        if (!book.isAvailable()) {
            book.setAvailable(true);
            book.setBorrowerId("null");
            return true;
        }
        return false;
    }

    /**
     * 제목 키워드를 사용하여 도서를 검색합니다.
     * @param keyword 검색어
     * @return 검색된 도서 객체들의 리스트
     */
    public List<Book> searchBook(String keyword) {
        List<Book> found = new ArrayList<>();
        for (Book book : bookMap.values()) {
            if (book.getTitle().contains(keyword)) found.add(book);
        }
        return found;
    }

    /** @return 등록된 모든 도서 정보의 Collection */
    public Collection<Book> getAllBooks() {
        return bookMap.values();
    }

    public int getBookCount() {
        return bookCount;
    }

    /**
     * 현재 메모리의 도서 변경 내역을 리포지토리를 통해 저장합니다.
     * @see LibraryRepository#saveBooks(Map)
     * @see <a href="https://github.com/sumannam/Java/issues/42">Issue #42: 테이블 추가/수정/삭제 방식 수정</a>
     */
    public void saveChanges() {
        repository.saveBooks(bookMap);
    }

    /** @return 도서 ID와 객체가 맵핑된 전체 Map 객체 */
    public Map<Integer, Book> getBookMap() {
        return bookMap;
    }

    public void checkServerStatus(String ip) {
        try {
            // 입력값의 앞뒤 불필요한 공백 제거
            String trimmedIp = ip.trim();
            System.out.println("\n[시스템] 서버 상태 점검 시도 (안전한 표준 API 사용): " + trimmedIp);

            // 🛡️ 자바 표준 API를 통해 IP 또는 도메인 주소 객체 생성
            // 악성 문자열이 들어와도 명령어 분리자(&&, ;, |)가 실행되지 않고, 단순 주소 형식 오류로 안전하게 예외 처리됨
            InetAddress address = InetAddress.getByName(trimmedIp);

            // 3000ms(3초) 동안 ICMP Ping(또는 TCP 7번 포트)을 보내 도달 가능 여부 확인
            boolean isReachable = address.isReachable(3000);

            System.out.println("-----------------------------------------------------------");
            if (isReachable) {
                System.out.println("[결과] Reply from " + trimmedIp + ": 응답이 존재합니다.");
                System.out.println("[결과] 서버 네트워크가 원활하게 연결되어 있습니다.");
            } else {
                System.out.println("[결과] Request timed out: " + trimmedIp + " 서버에 연결할 수 없습니다.");
                System.out.println("[결과] 방화벽 정책을 확인하거나 IP 주소가 올바른지 확인하세요.");
            }
            System.out.println("-----------------------------------------------------------");

        } catch (java.net.UnknownHostException e) {
            System.out.println("[오류] 유효하지 않거나 호스트를 찾을 수 없는 IP 주소입니다: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[오류] 진단 중 예외 발생: " + e.getMessage());
        }
    }
}