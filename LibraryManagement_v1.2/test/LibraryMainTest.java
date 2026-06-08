import javax.swing.*;

public class LibraryMainTest {
    public static void main(String[] args) {
        // IntelliJ 콘솔창 대신 GUI 팝업창을 띄워 마스킹 테스트
        // JPasswordField를 사용하면 입력하는 글자가 ● 형태로 자동 가림 처리됩니다.
        JPasswordField passwordField = new JPasswordField();

        int action = JOptionPane.showConfirmDialog(
                null,
                passwordField,
                "테스트할 비밀번호를 입력하세요 (마스킹 검증)",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (action == JOptionPane.OK_OPTION) {
            // 입력받은 char 배열을 문자열로 변환
            String password = new String(passwordField.getPassword());

            System.out.println("\n---------------------------------------------------");
            System.out.println("[검증 결과] 입력하신 비밀번호는 다음과 같습니다: " + password);
            System.out.println("-> 팝업창에서 ● 표시로 가려졌다면 마스킹 로직 검증 완료입니다.");
            System.out.println("---------------------------------------------------");
        }
    }
}